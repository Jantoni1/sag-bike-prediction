package com.sag.eiti.controller;

import com.sag.eiti.service.BikeDemandPredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/")
public class BikeDemandPredictionController {

    private BikeDemandPredictionService bikeDemandPredictionService;

    public BikeDemandPredictionController(BikeDemandPredictionService bikeDemandPredictionService) {
        this.bikeDemandPredictionService = bikeDemandPredictionService;
    }

    @GetMapping("/getPrediction")
    public ResponseEntity<List<Integer>> getPrediction(@RequestParam("from") Long requestedPeriodStartMilliseconds, @RequestParam("to") Long requestedPeriodEndMilliseconds) {
        return ResponseEntity.of(Optional.of(bikeDemandPredictionService.getPrediction(requestedPeriodStartMilliseconds, requestedPeriodEndMilliseconds)));
    }

}
