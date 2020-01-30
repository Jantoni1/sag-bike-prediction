package com.sag.eiti.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.sag.eiti.config.SpringProps;
import com.sag.eiti.config.interfaces.Actor;
import com.sag.eiti.entity.PredictedTripsPerHour;
import com.sag.eiti.entity.TripsPerHourHistorical;
import com.sag.eiti.vaadin_views.MainView;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Series;
import lombok.Data;
import lombok.Value;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Actor
public class BikeDemandActor extends AbstractActor {

    private ActorRef persistenceActor;

    private Map<UUID, PredictionRequestData> predictionRequests;

    private Map<UUID, HistoricalRidesRequestData> historicalRequests;

    private final static Integer NUMBER_OF_SAMPLES = 8;

    public BikeDemandActor() {
        this.persistenceActor = getContext().getSystem().actorOf(SpringProps.create(getContext().getSystem(), PersistenceActor.class));
        this.predictionRequests = new HashMap<>();
        this.historicalRequests = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BikePredictionRequest.class, this::getPrediction)
                .match(BikeHistoricalRidesRequest.class, this::getHistoricalRidesData)
                .match(PersistenceActor.BikeRidesPredictionResponse.class, this::onPredictedTripsPerHourReceived)
                .match(PersistenceActor.HistoricalBikeRidesResponse.class, this::onPersistedHistoricalTripsReceived)
                .match(BikeRidesApiActor.HistoricalTripsResponse.class, this::onMissingHistoricalTripsReceived)
                .match(TemperatureCastActor.HistoricalTemperatureResponse.class, this::onTemperatureEnrichedHistoricalRidesDataReceived)
                .match(TemperatureCastActor.TemperatureForecastResponse.class, this::onForecastReceived)
                .build();
    }

    private void onForecastReceived(TemperatureCastActor.TemperatureForecastResponse response) {
        var data = predictionRequests.get(response.getId());

        var newTripData = Stream.concat(data.getPersistedTrips().stream(), data.getMissingTrips().stream())
                .map(tripData -> new BikeHistoricalRidesResponse(tripData.getTemperature(), tripData.getNumberOfRides(), tripData.getTripsHourStart().atOffset(ZoneOffset.UTC)))
                .collect(Collectors.groupingBy(bikeHistoricalRidesResponse -> bikeHistoricalRidesResponse.getRequestedHour().getHour()));

        var weatherForecast = response.getHourlyTemperatures();

        var newTrips = data.getMissingHours().stream()
                .map(missingPredictedHour -> new HistoricalBikeRidesRequest(missingPredictedHour, newTripData.get(missingPredictedHour.getHour())))
                .map(bikeData -> {
                    var predictedTemperature = weatherForecast.get(bikeData.getTime());

                    return new PredictedTripsPerHour(null, bikeData.getTime().toInstant(),
                            getPredictedValue(bikeData, predictedTemperature).intValue(), predictedTemperature, OffsetDateTime.now().toInstant());
                }).collect(Collectors.toList());

        var saveRequest = new PersistenceActor.PredictedBikeRidesSaveRequest(newTrips);
        persistenceActor.tell(saveRequest, getSelf());

        var predictionList = Stream.concat(data.getPersistedPredictedTrips().stream(), newTrips.stream())
                .sorted(Comparator.comparing(PredictedTripsPerHour::getPredictedHourStart))
                .map(PredictedTripsPerHour::getPredictedRides)
                .collect(Collectors.toList());

        sendDataToMainView(data.getPredictionRequest().getMainView(), data.getPredictionRequest().getSeries(), predictionList);
    }

    private void sendDataToMainView(MainView mainView, ListSeries series, List<Integer> tripData) {
        mainView.getUI().ifPresent(ui -> ui.access(() -> {
            tripData.stream().map(value -> Math.max(value, 0)).forEach(series::addData);
        }));
    }

    private void onTemperatureEnrichedHistoricalRidesDataReceived(TemperatureCastActor.HistoricalTemperatureResponse response) {
        if (predictionRequests.containsKey(response.getId())) {
            var processingData = predictionRequests.get(response.getId());

            var dataSaveRequest = new PersistenceActor.HistoricalBikeRidesSaveRequest(response.getTripsPerHourHistorical());
            persistenceActor.tell(dataSaveRequest, getSelf());

            processingData.getMissingTrips().addAll(response.getTripsPerHourHistorical());
            processingData.setMissingRequiredHistoricalTripMonths(processingData.getMissingRequiredHistoricalTripMonths() - 1);

            if (processingData.getMissingRequiredHistoricalTripMonths() == 0) {
                ActorRef predictionBikeRidesManager = getContext().getSystem().actorOf(Props.create(TemperatureCastActor.class));
                var request = new TemperatureCastActor.TemperatureForecastRequest(response.getId(), processingData.getMissingHours());
                predictionBikeRidesManager.tell(request, getSelf());
            }
        } else {
            var processingData = historicalRequests.get(response.getId());

            var dataSaveRequest = new PersistenceActor.HistoricalBikeRidesSaveRequest(response.getTripsPerHourHistorical());
            persistenceActor.tell(dataSaveRequest, getSelf());

            processingData.getMissingTrips().addAll(response.getTripsPerHourHistorical());
            processingData.setMissingRequiredHistoricalTripMonths(processingData.getMissingRequiredHistoricalTripMonths() - 1);

            if (processingData.getMissingRequiredHistoricalTripMonths() == 0) {
                var data = Stream.concat(processingData.getExistingHistoricalTrips().stream(), processingData.getMissingTrips().stream())
                        .sorted(Comparator.comparingInt(tripHourData -> tripHourData.getTripsHourStart().atOffset(ZoneOffset.UTC).getHour()))
                        .map(TripsPerHourHistorical::getNumberOfRides)
                        .collect(Collectors.toList());
                sendDataToMainView(processingData.getBikeHistoricalRidesRequest().getMainView(), processingData.getBikeHistoricalRidesRequest().getSeries(), data);
            }
        }
    }

    private void onMissingHistoricalTripsReceived(BikeRidesApiActor.HistoricalTripsResponse response) {
        ActorRef predictionBikeRidesManager = getContext().getSystem().actorOf(Props.create(TemperatureCastActor.class));
        var request = new TemperatureCastActor.HistoricalTemperatureRequest(response.getId(), response.getTimeSpanRidesDetails());
        predictionBikeRidesManager.tell(request, getSelf());
    }

    private void onPersistedHistoricalTripsReceived(PersistenceActor.HistoricalBikeRidesResponse response) {
        if (predictionRequests.containsKey(response.getId())) {
            predictionRequests.get(response.getId()).setPersistedTrips(response.getTripsPerHourHistoricalList());
            if(response.getMissingRidesData().isEmpty()) {
                ActorRef predictionBikeRidesManager = getContext().getSystem().actorOf(Props.create(TemperatureCastActor.class));
                var request = new TemperatureCastActor.TemperatureForecastRequest(response.getId(), predictionRequests.get(response.getId()).getMissingHours());
                predictionBikeRidesManager.tell(request, getSelf());
            } else {
                getMissingTripData(response.getId(), response.getMissingRidesData());
            }
        } else {
            onHistoricalRidesDataReceived(response);
        }
    }

    private void getMissingTripData(UUID id, List<OffsetDateTime> requestedHours) {
        Map<OffsetDateTime, List<OffsetDateTime>> requestedDatesGroupedByMonth =
                requestedHours.stream()
                        .collect(Collectors.groupingBy(requestedHour -> requestedHour.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)));

        if (predictionRequests.containsKey(id)) {
            predictionRequests.get(id).setMissingRequiredHistoricalTripMonths(requestedDatesGroupedByMonth.entrySet().size());
        } else {
            historicalRequests.get(id).setMissingRequiredHistoricalTripMonths(requestedDatesGroupedByMonth.entrySet().size());
        }

        requestedDatesGroupedByMonth.forEach((month, hours) -> {
            ActorRef bikeRidesActor = getContext().getSystem().actorOf(Props.create(BikeRidesApiActor.class));
            var request = new BikeRidesApiActor.HistoricalTripsRequest(id, month, hours);
            bikeRidesActor.tell(request, getSelf());
        });
    }


    private void onPredictedTripsPerHourReceived(PersistenceActor.BikeRidesPredictionResponse predictedHoursResponse) {
        var data = predictionRequests.get(predictedHoursResponse.getId());
        data.setMissingHours(predictedHoursResponse.getMissingTripsDates());
        if (predictedHoursResponse.getMissingTripsDates().isEmpty()) {
            var sortedData = predictedHoursResponse.getExistingTrips().stream()
                    .sorted(Comparator.comparing(PredictedTripsPerHour::getPredictedHourStart))
                    .map(PredictedTripsPerHour::getPredictedRides)
                    .collect(Collectors.toList());
            sendDataToMainView(data.getPredictionRequest().getMainView(), data.getPredictionRequest().getSeries(), sortedData);
        } else {
            var requestedHours = getRequestedTimeInOneHourSpans(predictedHoursResponse.getMissingTripsDates());
            var request = new PersistenceActor.HistoricalBikeRidesRequest(predictedHoursResponse.getId(), requestedHours);
            persistenceActor.tell(request, getSelf());
        }
    }

    private List<OffsetDateTime> getRequestedTimeInOneHourSpans(List<OffsetDateTime> requestedHours) {
        return IntStream.rangeClosed(1, NUMBER_OF_SAMPLES)
                .boxed()
                .flatMap(numberOfWeeksBack -> requestedHours.stream().map(requestedHour -> requestedHour.minusWeeks(numberOfWeeksBack)))
                .collect(Collectors.toList());
    }

    private void getPrediction(BikePredictionRequest predictionRequest) {
        PredictionRequestData predictionRequestData = new PredictionRequestData();
        predictionRequestData.setPredictionRequest(predictionRequest);

        var id = UUID.randomUUID();

        predictionRequests.put(id, predictionRequestData);

        persistenceActor.tell(new PersistenceActor.BikeRidesPredictionRequest(id, predictionRequest.toHours()), getSelf());
    }

    private Double getPredictedValue(HistoricalBikeRidesRequest bikeRides, double temperature) {
        SimpleRegression regression = new SimpleRegression();

        removeOutliers(bikeRides).forEach(pastDayData -> regression.addData(pastDayData.getTemperature(), pastDayData.getNumberOfRides()));

        return regression.predict(temperature);
    }

    public void onHistoricalRidesDataReceived(PersistenceActor.HistoricalBikeRidesResponse response) {
        var requestData = historicalRequests.get(response.getId());
        requestData.setExistingHistoricalTrips(response.getTripsPerHourHistoricalList());
        requestData.setMissingHistoricalTripsDates(response.getMissingRidesData());
        if (requestData.getMissingHistoricalTripsDates().isEmpty()) {
            var sortedData = response.getTripsPerHourHistoricalList().stream()
                    .sorted(Comparator.comparing(TripsPerHourHistorical::getTripsHourStart))
                    .map(TripsPerHourHistorical::getNumberOfRides)
                    .collect(Collectors.toList());

            sendDataToMainView(requestData.getBikeHistoricalRidesRequest().getMainView(), requestData.getBikeHistoricalRidesRequest().getSeries(), sortedData);
        } else {
            getMissingTripData(response.getId(), requestData.getMissingHistoricalTripsDates());
        }
    }

    public void getHistoricalRidesData(BikeHistoricalRidesRequest request) {
        var historicalRequestData = new HistoricalRidesRequestData();
        historicalRequestData.setBikeHistoricalRidesRequest(request);

        var id = UUID.randomUUID();

        historicalRequests.put(id, historicalRequestData);

        persistenceActor.tell(new PersistenceActor.HistoricalBikeRidesRequest(id, request.toHours()), getSelf());
    }

    public static List<BikeHistoricalRidesResponse> removeOutliers(HistoricalBikeRidesRequest historicalBikeRides) {
        var ridesCount = historicalBikeRides.getBikeHistoricalRidesData().stream()
                .map(BikeHistoricalRidesResponse::getNumberOfRides)
                .collect(Collectors.toList());

        if (ridesCount.size() == 0)
            return new ArrayList<>();

        double average = ridesCount.stream()
                .mapToDouble(a -> a)
                .average().orElse(0.0);

        double standardDeviation = Math.sqrt(ridesCount.stream().mapToDouble(number -> Math.pow(number - average, 2))
                .average().orElse(0.0));

        return historicalBikeRides.getBikeHistoricalRidesData().stream()
                .filter(bikeHistoricalRidesResponse -> Math.abs(bikeHistoricalRidesResponse.getNumberOfRides() - average) < 2 * standardDeviation)
                .collect(Collectors.toList());
    }

    @Value
    public static class BikeHistoricalRidesRequest {
        private OffsetDateTime timeSpanStart;
        private OffsetDateTime timeSpanEnd;
        private ListSeries series;
        private MainView mainView;

        public BikeHistoricalRidesRequest(OffsetDateTime timeSpanStart, OffsetDateTime timeSpanEnd, MainView mainView, ListSeries series) {
            this.timeSpanStart = timeSpanStart.truncatedTo(ChronoUnit.HOURS);
            this.timeSpanEnd = timeSpanEnd.truncatedTo(ChronoUnit.HOURS);
            this.series = series;
            this.mainView = mainView;
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

    @Value
    public static class BikePredictionRequest {
        private OffsetDateTime timeSpanStart;
        private OffsetDateTime timeSpanEnd;
        private MainView mainView;
        private ListSeries series;

        public BikePredictionRequest(OffsetDateTime timeSpanStart, OffsetDateTime timeSpanEnd, MainView mainView, ListSeries series) {
            this.timeSpanStart = timeSpanStart.truncatedTo(ChronoUnit.HOURS);
            this.timeSpanEnd = timeSpanEnd.truncatedTo(ChronoUnit.HOURS);
            this.mainView = mainView;
            this.series = series;
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

    @Value
    public static class HistoricalBikeRidesRequest {
        private OffsetDateTime time;
        private List<BikeHistoricalRidesResponse> bikeHistoricalRidesData;
    }


    @Value
    public static class BikeHistoricalRidesResponse {
        private Double temperature;
        private Integer numberOfRides;
        private OffsetDateTime requestedHour;

        public BikeHistoricalRidesResponse(Double temperature, Integer numberOfRides, OffsetDateTime requestedHour) {
            this.temperature = temperature;
            this.numberOfRides = numberOfRides;
            this.requestedHour = requestedHour.withOffsetSameInstant(ZoneOffset.UTC);
        }
    }

    @Data
    private static class PredictionRequestData {
        private UUID id;
        private BikePredictionRequest predictionRequest;

        private List<PredictedTripsPerHour> persistedPredictedTrips = new ArrayList<>();
        private List<OffsetDateTime> missingHours = new ArrayList<>();

        private int missingRequiredHistoricalTripMonths;
        private List<TripsPerHourHistorical> persistedTrips = new ArrayList<>();
        private List<TripsPerHourHistorical> missingTrips = new ArrayList<>();

    }

    @Data
    private static class HistoricalRidesRequestData {
        BikeHistoricalRidesRequest bikeHistoricalRidesRequest;

        List<TripsPerHourHistorical> existingHistoricalTrips = new ArrayList<>();
        List<OffsetDateTime> missingHistoricalTripsDates = new ArrayList<>();

        private List<TripsPerHourHistorical> missingTrips = new ArrayList<>();

        private int missingRequiredHistoricalTripMonths;
    }


}
