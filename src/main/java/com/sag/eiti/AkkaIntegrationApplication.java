package com.sag.eiti;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.sag.eiti.actors.TestActor;
import com.sag.eiti.config.SpringExtension;
import com.sag.eiti.config.SpringProps;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class AkkaIntegrationApplication {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ActorSystem system;

    public static void main(String[] args) {
        SpringApplication.run(AkkaIntegrationApplication.class, args);
    }

    @PostConstruct
    void init() {
        system = ActorSystem.create("actor-system", ConfigFactory.load());
        SpringExtension.getInstance().get(system).initialize(context);
    }

}
