package com.sag.eiti.actors;

import akka.actor.AbstractActor;
import com.sag.eiti.config.interfaces.Actor;
import com.sag.eiti.dto.BikeAvailability;
import org.springframework.web.client.RestTemplate;

@Actor
public class BikeManager extends AbstractActor {

    private static final String BIKE_DATA_URL = "https://nextbike.net/maps/nextbike-live.xml?city=376";

    @Override
    public Receive createReceive() {
        return null;
    }


}
