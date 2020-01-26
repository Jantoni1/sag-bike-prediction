package com.sag.eiti.actors;

import akka.actor.AbstractActor;
import com.sag.eiti.config.interfaces.Actor;

@Actor
public class TestActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::print)
                .build();
    }

    private void print(String data) {
        System.out.println(String.format("%s %x", data, hashCode()));
    }

}
