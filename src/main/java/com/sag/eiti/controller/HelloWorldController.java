package com.sag.eiti.controller;

import com.sag.eiti.entity.PredictedTripsPerHour;
import com.sag.eiti.service.BergenCityBikesService;
import com.sag.eiti.service.BikePredictionService;
import com.sag.eiti.service.MessageService;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class HelloWorldController {

    private MessageService messageService;

    private BikePredictionService bikePredictionService;

    public HelloWorldController(MessageService messageService, BikePredictionService bikePredictionService) {
        this.messageService = messageService;
        this.bikePredictionService = bikePredictionService;
    }

//    @RequestMapping("/")
//    public String index() {

//        // TODO: 21.01.2020 save predicted data to database
//
//    }

}
