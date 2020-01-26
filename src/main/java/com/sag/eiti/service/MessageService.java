package com.sag.eiti.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.sag.eiti.actors.WeatherCaster;
import com.sag.eiti.config.SpringProps;
import com.sag.eiti.dto.bike.BikeAvailability;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class MessageService {

    private final ActorSystem system;

    public MessageService(ActorSystem system) {
        this.system = system;
    }

    public void getTheJobDone() {
        WeatherCaster.WeatherForecastRequest weatherForecastRequest = new WeatherCaster.WeatherForecastRequest(LocalDateTime.now(), ActorRef.noSender());
        ActorRef testActor = system.actorOf(SpringProps.create(system, WeatherCaster.class));
        testActor.tell(weatherForecastRequest, ActorRef.noSender());
    }

    private static final String BIKE_DATA_URL = "https://nextbike.net/maps/nextbike-live.xml?city=376";
    public void getBikeData() {

        RestTemplate restTemplate = new RestTemplate();
        BikeAvailability result = restTemplate.getForObject(BIKE_DATA_URL, BikeAvailability.class);
//        BikeAvailability result = restTemplate.getForObject(BIKE_DATA_URL, BikeAvailability.class);

        result.getCountry().getCity().getPlace().stream()
                .map(placeType -> Integer.parseInt(placeType.getFreeRacks())).reduce(Integer::sum)
                .ifPresent(System.out::println);
        result.getCountry().getCity().getPlace().stream()
                .map(placeType -> placeType.getBikeRacks() != null ? Integer.parseInt(placeType.getBikeRacks()) : 0)
                .reduce(Integer::sum)
                .ifPresent(System.out::println);
    }

}
