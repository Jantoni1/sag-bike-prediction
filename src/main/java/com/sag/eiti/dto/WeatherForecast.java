package com.sag.eiti.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WeatherForecast {

    //list of forecasts for every 3 hours of the next 5 days
    @JsonProperty("list")
    List<WeatherDetails> threeHourForecastList;


}
