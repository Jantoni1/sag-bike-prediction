package com.sag.eiti.service;

import com.sag.eiti.dto.bergen_city_bike.BergenCityBikesRide;
import lombok.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Month;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class BergenCityBikesService {

    private final static String ARCHIVE_DATA_URL = "https://data.urbansharing.com/bergenbysykkel.no/trips/v1/%04d/%02d.json";

    public List<TimeSpanRidesDetails> getPreviousTrips(List<OffsetDateTime> requestedHours) {
        return requestedHours.stream()
                .collect(Collectors.groupingBy(requestedHour -> requestedHour.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)))
                .entrySet()
                .stream()
                .flatMap(entry -> getPreviousTimeSpansDetails(entry.getKey(), entry.getValue()).stream())
                .collect(Collectors.toList());
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

    @Value
    static class TimeSpanRidesDetails {
        private OffsetDateTime requestedHour;
        private long ridesCount;
    }

    private String getApiUrl(int year, Month month) {
        return String.format(ARCHIVE_DATA_URL, year, month.getValue());
    }

    @Value
    public static class RequestedTimeSpan {
        private OffsetDateTime timeSpanStart;
        private OffsetDateTime timeSpanEnd;

        public RequestedTimeSpan(OffsetDateTime timeSpanStart, OffsetDateTime timeSpanEnd) {
            this.timeSpanStart = timeSpanStart.truncatedTo(ChronoUnit.HOURS);
            this.timeSpanEnd = timeSpanEnd.truncatedTo(ChronoUnit.HOURS);
        }

        public RequestedTimeSpan minusWeeks(int numberOfWeeks) {
            return new RequestedTimeSpan(timeSpanStart.minusWeeks(numberOfWeeks), timeSpanEnd.minusWeeks(numberOfWeeks));
        }

        public List<OffsetDateTime> toHours() {
            return IntStream.rangeClosed(0, getTimeSpanInHours())
                    .mapToObj(timeSpanStart::plusHours)
                    .collect(Collectors.toList());
        }

        private Integer getTimeSpanInHours() {
            return (int) ChronoUnit.HOURS.between(timeSpanStart, timeSpanEnd);
        }
    }

}
