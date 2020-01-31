package com.sag.eiti.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.util.Timeout;
import com.sag.eiti.dto.RidesDataWithTemperature;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static akka.pattern.Patterns.ask;

public class PredictionModelActor extends AbstractActor {

    private Map<TimePeriod, SimpleRegression> weekDayRegressionModels;

    private final static Integer MAX_HOURS = 24;

    public PredictionModelActor() {
        weekDayRegressionModels = Arrays.stream(DayOfWeek.values())
                .flatMap(dayOfWeek -> IntStream.range(0, MAX_HOURS).mapToObj(hour -> new TimePeriod(dayOfWeek, hour)))
                .collect(Collectors.toMap(Function.identity(), day -> new SimpleRegression()));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LearningDataMessage.class, this::updateRegressionModels)
                .match(PredictionRequest.class, this::estimateBikeUsage)
                .build();
    }

    private void estimateBikeUsage(PredictionRequest predictionRequest) {
        ActorRef sender = getSender();
        ActorRef temperatureCastActor = getContext().getSystem().actorOf(Props.create(TemperatureCastActor.class));
        var request = new TemperatureCastActor.TemperatureForecastRequest(UUID.randomUUID(), predictionRequest.getRequestedHours());
        var timeout = new Timeout(Duration.create(10, "seconds"));
        var temperatureFuture = ask(temperatureCastActor, request, 10000);
        try {
            var result = (TemperatureCastActor.TemperatureForecastResponse) Await.result(temperatureFuture, timeout.duration());
            var estimatedUsage = predictionRequest.requestedHours.stream().map(hour -> {
                var temperature = result.getHourlyTemperatures().get(hour);
                var key = new TimePeriod(hour);
                return Double.valueOf(weekDayRegressionModels.get(key).predict(temperature)).intValue();
            }).collect(Collectors.toList());
            sender.tell(new PredictionResponse(estimatedUsage), getSelf());

        } catch (Exception ignored) {}
    }

    private void updateRegressionModels(LearningDataMessage message) {
        System.out.println("UPDATING PREDICTION MODEL");
        message.monthlyTripsDataList.forEach(tripData -> {
            var key = new TimePeriod(tripData.getDateTime().getDayOfWeek(), tripData.getDateTime().getHour());
            weekDayRegressionModels.get(key).addData(tripData.getTemperature(), tripData.getNumberOfRides());
        });
    }

    @Value
    public static class LearningDataMessage {
        List<RidesDataWithTemperature> monthlyTripsDataList;
    }

    @Value
    public static class PredictionRequest {
        List<OffsetDateTime> requestedHours;
    }

    @Value
    public static class PredictionResponse {
        List<Integer> estimatedNumberOfRides;
    }

    @Value
    @AllArgsConstructor
    private static class TimePeriod {

        public TimePeriod(OffsetDateTime offsetDateTime) {
            this.dayOfWeek = offsetDateTime.getDayOfWeek();
            this.hour = offsetDateTime.getHour();
        }

        DayOfWeek dayOfWeek;
        Integer hour;
    }
}
