package com.sag.eiti.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import com.sag.eiti.actors.BikeDemandActor;
import com.sag.eiti.actors.PredictionModelActor;
import com.sag.eiti.actors.PredictionModelDataProviderActor;
import org.springframework.stereotype.Service;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static akka.pattern.Patterns.ask;

@Service
public class BikeDemandPredictionService {

    private ActorSystem actorSystem;

    private ActorRef predictionModelActor;

    public BikeDemandPredictionService(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        predictionModelActor = actorSystem.actorOf(Props.create(PredictionModelActor.class));

        trainPredictionModel();
    }

    private void trainPredictionModel() {
        ActorRef bikeDemandActor = actorSystem.actorOf(Props.create(PredictionModelDataProviderActor.class));
        OffsetDateTime dataEndDate = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).minusDays(2);
        OffsetDateTime dataStartDate = dataEndDate.minusMonths(12);

        var trainingRequest = new PredictionModelDataProviderActor.TrainModelRequest(predictionModelActor, dataStartDate, dataEndDate);
        bikeDemandActor.tell(trainingRequest, bikeDemandActor);
    }

    public List<Integer> getPrediction(Long requestedPeriodStartSeconds, Long requestedPeriodEndSeconds) {
        OffsetDateTime requestedPeriodStart = Instant.ofEpochSecond(requestedPeriodStartSeconds)
                .atOffset(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
        OffsetDateTime requestedPeriodEnd = Instant.ofEpochSecond(requestedPeriodEndSeconds)
                .atOffset(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);

        if(!validateRequestedHours(requestedPeriodStart, requestedPeriodEnd)) {
            return new ArrayList<>();
        }

        var requestedHours = LongStream.rangeClosed(0, ChronoUnit.HOURS.between(requestedPeriodStart, requestedPeriodEnd))
                .mapToObj(requestedPeriodStart::plusHours)
                .collect(Collectors.toList());

        var timeout = new Timeout(Duration.create(10, "seconds"));
        var predictedData = ask(predictionModelActor, new PredictionModelActor.PredictionRequest(requestedHours), timeout);
        try {
            var result = (PredictionModelActor.PredictionResponse) Await.result(predictedData, timeout.duration());
            return result.getEstimatedNumberOfRides();
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private boolean validateRequestedHours(OffsetDateTime requestedPeriodStart, OffsetDateTime requestedPeriodEnd) {
        OffsetDateTime now = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
        return (requestedPeriodStart.equals(now) || requestedPeriodStart.isAfter(now))
                && (requestedPeriodEnd.equals(requestedPeriodStart) || requestedPeriodEnd.isAfter(requestedPeriodStart))
                && requestedPeriodEnd.isBefore(now.plusHours(47));
    }

}
