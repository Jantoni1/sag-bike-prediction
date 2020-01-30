package com.sag.eiti.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.util.Timeout;
import com.sag.eiti.config.interfaces.Actor;
import com.sag.eiti.dto.RidesDataWithTemperature;
import com.sag.eiti.dto.TimeSpanRidesDetails;
import com.sag.eiti.dto.bergen_city_bike.BergenCityBikesRide;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.impl.Promise;
import scala.util.Try;

import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static akka.pattern.Patterns.ask;

@Actor
public class BikeRidesApiActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(HistoricalTripsRequest.class, this::getPreviousTrips)
                .match(FullMonthlyTripDataRequest.class, this::getFullMonthlyTripDataInfo)
                .build();
    }

    private void getFullMonthlyTripDataInfo(FullMonthlyTripDataRequest fullMonthlyTripDataRequest) {
        var data = getMonthRides(fullMonthlyTripDataRequest.yearMonthDate).stream()
                .collect(Collectors.groupingBy(rideDetails -> rideDetails.getRideStart().truncatedTo(ChronoUnit.HOURS), Collectors.counting()));

        var temperatureCastActor = getContext().getSystem().actorOf(Props.create(TemperatureCastActor.class));
        var response = data.keySet().stream()
                .map(date -> date.truncatedTo(ChronoUnit.DAYS))
                .distinct()
                .flatMap(day -> getRidesDataPerDay(day, data, temperatureCastActor))
                .collect(Collectors.toList());

        getSender().tell(new FullMonthlyTripDataResponse(response), getSelf());
    }

    private Stream<RidesDataWithTemperature> getRidesDataPerDay(OffsetDateTime day, Map<OffsetDateTime, Long> hourlyRides, ActorRef temperatureCastActor) {
        var request = new TemperatureCastActor.HourlyHistoricalTemperatureForDayRequest(day);
        var timeout = new Timeout(Duration.create(10, "seconds"));
        var temperatureFuture = ask(temperatureCastActor, request, 10000);
        try {
            var result = (TemperatureCastActor.HourlyHistoricalTemperatureForDayResponse) Await.result(temperatureFuture, timeout.duration());
            return result.getHourlyData().stream().map(hourlyData -> new RidesDataWithTemperature(hourlyData.getOffsetDateTime(), hourlyData.getTemperature(), hourlyRides.getOrDefault(hourlyData.getOffsetDateTime(), 0L)));
        } catch (Exception ignored) {
            return Stream.empty();
        }
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

    @Value
    public static class FullMonthlyTripDataRequest {
        OffsetDateTime yearMonthDate;
    }

    @Value
    public static class FullMonthlyTripDataResponse {
        List<RidesDataWithTemperature> monthlyTripsDataList;
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

    private List<BergenCityBikesRide> getMonthRides(OffsetDateTime date) {
        ParameterizedTypeReference<ArrayList<BergenCityBikesRide>> bergenCityBikesClass = new ParameterizedTypeReference<>() {
        };
        return new RestTemplate().exchange(getApiUrl(date.getYear(), date.getMonth()), HttpMethod.GET, null, bergenCityBikesClass).getBody();
    }

    private String getApiUrl(int year, Month month) {
        return String.format(ARCHIVE_DATA_URL, year, month.getValue());
    }

}
