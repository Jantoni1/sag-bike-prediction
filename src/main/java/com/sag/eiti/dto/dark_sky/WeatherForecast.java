package com.sag.eiti.dto.dark_sky;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "hourly",
})
@Data
public class WeatherForecast {
    @JsonProperty("hourly")
    private Hourly hourly;
}
