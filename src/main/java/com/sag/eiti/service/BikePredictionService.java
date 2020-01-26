package com.sag.eiti.service;

import com.sag.eiti.entity.PredictedTripsPerHour;
import com.sag.eiti.entity.TripsPerHourHistorical;
import com.sag.eiti.repository.PredictedTripsRepository;
import com.sag.eiti.repository.TripsPerHourHistoricalRepository;
import lombok.Value;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class BikePredictionService {

    private BergenCityBikesService bergenCityBikesService;

    private DarkSkyWeatherService darkSkyWeatherService;

    private TripsPerHourHistoricalRepository tripsPerHourHistoricalRepository;

    private PredictedTripsRepository predictedTripsRepository;

    private final static Integer NUMBER_OF_SAMPLES = 8;

    public BikePredictionService(BergenCityBikesService bergenCityBikesService, DarkSkyWeatherService darkSkyWeatherService, TripsPerHourHistoricalRepository tripsPerHourHistoricalRepository, PredictedTripsRepository predictedTripsRepository) {
        this.bergenCityBikesService = bergenCityBikesService;
        this.darkSkyWeatherService = darkSkyWeatherService;
        this.tripsPerHourHistoricalRepository = tripsPerHourHistoricalRepository;
        this.predictedTripsRepository = predictedTripsRepository;
    }

    public List<PredictedTripsPerHour> getData() {
        return getPrediction(new BergenCityBikesService.RequestedTimeSpan(OffsetDateTime.now().minusHours(12), OffsetDateTime.now().minusHours(2)));
    }

    private List<OffsetDateTime> getRequestedTimeInOneHourSpans(List<OffsetDateTime> requestedHours) {
        return IntStream.rangeClosed(1, NUMBER_OF_SAMPLES)
                .boxed()
                .flatMap(numberOfWeeksBack -> requestedHours.stream().map(requestedHour -> requestedHour.minusWeeks(numberOfWeeksBack)))
                .collect(Collectors.toList());
    }

    public List<PredictedTripsPerHour> getPrediction(BergenCityBikesService.RequestedTimeSpan predictionTimeSpan) {

        var predictedHours = predictionTimeSpan.toHours();

        List<PredictedTripsPerHour> predictedTripsPerHours = getPredictedTripsPerHourFromDatabase(predictedHours);

        var missingPredictedHours = predictedHours.stream()
                .filter(requestedHour -> predictedTripsPerHours.stream()
                        .noneMatch(trip -> trip.getPredictedHourStart().equals(requestedHour.toInstant())))
                .collect(Collectors.toList());

        var bikeRentTimeSpans = missingPredictedHours.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::getRequestedTimeInOneHourSpans));

        var historicalTripsData = tripsPerHourHistoricalRepository.findAllByTripsHourStartIn(bikeRentTimeSpans.stream()
                .map(OffsetDateTime::toInstant)
                .collect(Collectors.toList()));

        var missingHistoricalHours = bikeRentTimeSpans.stream()
                .filter(requestedHour -> historicalTripsData.stream()
                        .noneMatch(tripData -> tripData.getTripsHourStart().equals(requestedHour.toInstant())))
                .collect(Collectors.toList());

        var newHistoricalTripData = missingHistoricalHours.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::getMissingTripData));

        tripsPerHourHistoricalRepository.saveAll(newHistoricalTripData);

        var newTripData = Stream.concat(historicalTripsData.stream(), newHistoricalTripData.stream())
                .map(tripData -> new BikeData(tripData.getTemperature(), tripData.getNumberOfRides(), tripData.getTripsHourStart().atOffset(ZoneOffset.UTC)))
                .collect(Collectors.groupingBy(bikeData -> bikeData.getRequestedHour().getHour()));

        var weatherForecast = darkSkyWeatherService.getWeatherForecast(missingPredictedHours);

        var newTrips = missingPredictedHours.stream()
                .map(missingPredictedHour -> new HistoricalBikeRides(missingPredictedHour, newTripData.get(missingPredictedHour.getHour())))
                .map(bikeData -> {
                    var predictedTemperature = weatherForecast.get(bikeData.getTime());

                    return new PredictedTripsPerHour(null, bikeData.getTime().toInstant(),
                            getPredictedValue(bikeData, predictedTemperature).intValue(), predictedTemperature, OffsetDateTime.now().toInstant());
                }).collect(Collectors.toList());

        predictedTripsRepository.saveAll(newTrips);

        return Stream.concat(predictedTripsPerHours.stream(), newTrips.stream()).sorted(Comparator.comparing(PredictedTripsPerHour::getPredictedHourStart)).collect(Collectors.toList());
    }

    private List<PredictedTripsPerHour> getPredictedTripsPerHourFromDatabase(List<OffsetDateTime> dates) {
        return predictedTripsRepository.getPredictedTripsPerHourByPredictedHourStartIn(dates.stream().map(OffsetDateTime::toInstant).collect(Collectors.toList()));
    }

    private Double getPredictedValue(BikePredictionService.HistoricalBikeRides bikeRides, double temperature) {
        SimpleRegression regression = new SimpleRegression();

        removeOutliers(bikeRides).forEach(pastDayData -> regression.addData(pastDayData.getTemperature(), pastDayData.getNumberOfRides()));

        return regression.predict(temperature);
    }

    public List<BikeData> getHistoricalRidesData(BergenCityBikesService.RequestedTimeSpan requestedTimeSpan) {
        var predictedHours = requestedTimeSpan.toHours();

        var predictedTripsPerHours = tripsPerHourHistoricalRepository.findAllByTripsHourStartIn(predictedHours.stream()
                .map(OffsetDateTime::toInstant)
                .collect(Collectors.toList()));

        var missingPredictedHours = predictedHours.stream()
                .filter(requestedHour -> predictedTripsPerHours.stream()
                        .noneMatch(trip -> trip.getTripsHourStart().equals(requestedHour.toInstant())))
                .collect(Collectors.toList());

        var newHistoricalTripData = missingPredictedHours.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::getMissingTripData));

        tripsPerHourHistoricalRepository.saveAll(newHistoricalTripData);

        return Stream.concat(predictedTripsPerHours.stream(), newHistoricalTripData.stream())
                .sorted(Comparator.comparingInt(tripHourData -> tripHourData.getTripsHourStart().atOffset(ZoneOffset.UTC).getHour()))
                .map(tripData -> new BikeData(tripData.getTemperature(), tripData.getNumberOfRides(), tripData.getTripsHourStart().atOffset(ZoneOffset.UTC)))
                .collect(Collectors.toList());
    }

    public static List<BikeData> removeOutliers(HistoricalBikeRides historicalBikeRides) {
        var ridesCount = historicalBikeRides.getBikeData().stream()
                .map(BikeData::getNumberOfRides)
                .collect(Collectors.toList());

        if (ridesCount.size() == 0)
            return new ArrayList<>();

        double average = ridesCount.stream()
                .mapToDouble(a -> a)
                .average().orElse(0.0);

        double standardDeviation = Math.sqrt(ridesCount.stream().mapToDouble(number -> Math.pow(number - average, 2))
                .average().orElse(0.0));

        return historicalBikeRides.getBikeData().stream()
                .filter(bikeData -> Math.abs(bikeData.getNumberOfRides() - average) < 2 * standardDeviation)
                .collect(Collectors.toList());
    }

    @Value
    public static class HistoricalBikeRides {
        private OffsetDateTime time;
        private List<BikeData> bikeData;
    }

    private List<TripsPerHourHistorical> getMissingTripData(List<OffsetDateTime> requestedHours) {
        var tripData = bergenCityBikesService.getPreviousTrips(requestedHours);
        return darkSkyWeatherService.getHistoricalWeatherForDay(tripData);
    }

    @Value
    public static class BikeData {
        private Double temperature;
        private Integer numberOfRides;
        private OffsetDateTime requestedHour;

        public BikeData(Double temperature, Integer numberOfRides, OffsetDateTime requestedHour) {
            this.temperature = temperature;
            this.numberOfRides = numberOfRides;
            this.requestedHour = requestedHour.withOffsetSameInstant(ZoneOffset.UTC);
        }
    }

}
