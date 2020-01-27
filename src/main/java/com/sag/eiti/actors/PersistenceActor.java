package com.sag.eiti.actors;

import akka.actor.AbstractActor;
import com.sag.eiti.config.interfaces.Actor;
import com.sag.eiti.entity.PredictedTripsPerHour;
import com.sag.eiti.entity.TripsPerHourHistorical;
import com.sag.eiti.repository.PredictedTripsRepository;
import com.sag.eiti.repository.TripsPerHourHistoricalRepository;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Actor
public class PersistenceActor extends AbstractActor {

    private TripsPerHourHistoricalRepository tripsPerHourHistoricalRepository;

    private PredictedTripsRepository predictedTripsRepository;

    public PersistenceActor(TripsPerHourHistoricalRepository tripsPerHourHistoricalRepository, PredictedTripsRepository predictedTripsRepository) {
        this.tripsPerHourHistoricalRepository = tripsPerHourHistoricalRepository;
        this.predictedTripsRepository = predictedTripsRepository;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BikeRidesPredictionRequest.class, this::findExistingPredictions)
                .match(HistoricalBikeRidesRequest.class, this::findExistingHistoricalRideMeasures)
                .match(HistoricalBikeRidesSaveRequest.class, this::saveHistoricalRides)
                .match(PredictedBikeRidesSaveRequest.class, this::savePredictedRides)
                .build();
    }

    private void savePredictedRides(PredictedBikeRidesSaveRequest predictedBikeRidesSaveRequest) {
        predictedTripsRepository.saveAll(predictedBikeRidesSaveRequest.tripsToBeSaved);
    }

    private void saveHistoricalRides(HistoricalBikeRidesSaveRequest historicalBikeRidesSaveRequest) {
        tripsPerHourHistoricalRepository.saveAll(historicalBikeRidesSaveRequest.tripsToBeSaved);
    }

    private void findExistingPredictions(BikeRidesPredictionRequest bikeRidesPredictionRequest) {
        var existingPredictions = predictedTripsRepository.getPredictedTripsPerHourByPredictedHourStartIn(bikeRidesPredictionRequest.getPredictionDates()
                .stream().map(OffsetDateTime::toInstant).collect(Collectors.toList()));

        var missingPredictedHours = bikeRidesPredictionRequest.getPredictionDates().stream()
                .filter(requestedHour -> existingPredictions.stream()
                        .noneMatch(trip -> trip.getPredictedHourStart().equals(requestedHour.toInstant())))
                .collect(Collectors.toList());

        var response = new BikeRidesPredictionResponse(bikeRidesPredictionRequest.id, existingPredictions, missingPredictedHours);
        getSender().tell(response, getSelf());

    }

    private void findExistingHistoricalRideMeasures(HistoricalBikeRidesRequest historicalBikeRidesRequest) {
        List<OffsetDateTime> missingPredictedHours = historicalBikeRidesRequest.requestedHours;

        var historicalTripsData = tripsPerHourHistoricalRepository.findAllByTripsHourStartIn(missingPredictedHours.stream()
                .map(OffsetDateTime::toInstant)
                .collect(Collectors.toList()));

        var missingHistoricalHours = missingPredictedHours.stream()
                .filter(requestedHour -> historicalTripsData.stream()
                        .noneMatch(tripData -> tripData.getTripsHourStart().equals(requestedHour.toInstant())))
                .collect(Collectors.toList());

        var response = new HistoricalBikeRidesResponse(historicalBikeRidesRequest.getId(), historicalTripsData, missingHistoricalHours);

        getSender().tell(response, getSelf());
    }


    @Value
    public static class BikeRidesPredictionRequest {
        UUID id;
        List<OffsetDateTime> predictionDates;
    }

    @Value
    public static class BikeRidesPredictionResponse {
        UUID id;
        List<PredictedTripsPerHour> existingTrips;
        List<OffsetDateTime> missingTripsDates;
    }

    @Value
    public static class HistoricalBikeRidesRequest {
        UUID id;
        List<OffsetDateTime> requestedHours;
    }

    @Value
    public static class HistoricalBikeRidesResponse {
        UUID id;
        List<TripsPerHourHistorical> tripsPerHourHistoricalList;
        List<OffsetDateTime> missingRidesData;
    }

    @Value
    public static class HistoricalBikeRidesSaveRequest {
        List<TripsPerHourHistorical> tripsToBeSaved;
    }

    @Value
    public static class PredictedBikeRidesSaveRequest {
        List<PredictedTripsPerHour> tripsToBeSaved;
    }
}
