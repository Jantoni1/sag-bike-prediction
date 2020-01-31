package com.sag.eiti.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.Value;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.LongStream;

public class PredictionModelDataProviderActor extends AbstractActor {

    private ActorRef predictionModelActor;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TrainModelRequest.class, this::getPrediction)
                .match(BikeRidesApiActor.FullMonthlyTripDataResponse.class, this::onMonthlyDataReceived)
                .build();
    }

    private void onMonthlyDataReceived(BikeRidesApiActor.FullMonthlyTripDataResponse response) {
        predictionModelActor.tell(new PredictionModelActor.LearningDataMessage(response.getMonthlyTripsDataList()), getSelf());
    }

    private void getPrediction(TrainModelRequest trainModelRequest) {
        this.predictionModelActor = trainModelRequest.getPredictionModelActor();

        LongStream.rangeClosed(0, ChronoUnit.MONTHS.between(trainModelRequest.getTrainingDateStart(), trainModelRequest.getTrainingDateEnd()))
                .mapToObj(offset -> trainModelRequest.getTrainingDateStart().plusMonths(offset))
                .forEach(this::requestMonthlyRidesData);
    }

    private void requestMonthlyRidesData(OffsetDateTime month) {
        var bikeRidesApiActor = getContext().getSystem().actorOf(Props.create(BikeRidesApiActor.class));
        bikeRidesApiActor.tell(new BikeRidesApiActor.FullMonthlyTripDataRequest(month), getSelf());
    }

    @Value
    public static class TrainModelRequest {
        ActorRef predictionModelActor;
        OffsetDateTime trainingDateStart;
        OffsetDateTime trainingDateEnd;
    }

}
