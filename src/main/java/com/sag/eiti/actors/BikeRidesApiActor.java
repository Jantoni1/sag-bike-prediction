package com.sag.eiti.actors;

import akka.actor.AbstractActor;
import com.sag.eiti.config.interfaces.Actor;
import com.sag.eiti.dto.TimeSpanRidesDetails;
import com.sag.eiti.dto.bergen_city_bike.BergenCityBikesRide;
import lombok.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Actor
public class BikeRidesApiActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(HistoricalTripsRequest.class, this::getPreviousTrips)
                .build();
    }

    @Value
    public static class HistoricalTripsRequest {
        UUID id;
        OffsetDateTime month;
        List<OffsetDateTime> requestedHours;
    }

    @Value
    public static class HistoricalTripsResponse {
        UUID id;
        OffsetDateTime month;
        List<TimeSpanRidesDetails> timeSpanRidesDetails;
    }

    private final static String ARCHIVE_DATA_URL = "https://data.urbansharing.com/bergenbysykkel.no/trips/v1/%04d/%02d.json";

    public void getPreviousTrips(HistoricalTripsRequest request) {
        var data = getPreviousTimeSpansDetails(request.getMonth(), request.getRequestedHours());

        getSender().tell(new HistoricalTripsResponse(request.id, request.getMonth(), data), getSelf());
        getContext().stop(getSelf());
    }

    private List<TimeSpanRidesDetails> getPreviousTimeSpansDetails(OffsetDateTime monthStartDate, List<OffsetDateTime> requestedHours) {
        ZoneOffset zoneOffset = monthStartDate.getOffset();
        Set<OffsetDateTime> hours = new HashSet<>(requestedHours);
        Map<OffsetDateTime, Long> ridesHourly = getMonthRides(monthStartDate).stream()
                .filter(ride -> hours.contains(ride.getRideStart().withOffsetSameInstant(zoneOffset).truncatedTo(ChronoUnit.HOURS)))
                .collect(Collectors.groupingBy(ride -> ride.getRideStart().withOffsetSameInstant(zoneOffset).truncatedTo(ChronoUnit.HOURS), Collectors.counting()));

        return requestedHours.stream()
                .map(requestedHour -> new TimeSpanRidesDetails(requestedHour, ridesHourly.getOrDefault(requestedHour, 0L)))
                .collect(Collectors.toList());
    }

    private ArrayList<BergenCityBikesRide> getMonthRides(OffsetDateTime date) {
        ParameterizedTypeReference<ArrayList<BergenCityBikesRide>> bergenCityBikesClass = new ParameterizedTypeReference<>() {
        };
        return new RestTemplate().exchange(getApiUrl(date.getYear(), date.getMonth()), HttpMethod.GET, null, bergenCityBikesClass).getBody();
    }

    private String getApiUrl(int year, Month month) {
        return String.format(ARCHIVE_DATA_URL, year, month.getValue());
    }

}
