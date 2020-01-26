package com.sag.eiti.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TimeZone;

@Data
public class WeatherDetails implements Serializable {

    private BigDecimal temperature;

    private LocalDateTime time;

    @JsonSetter("dt")
    private void setTime(Long timestamp) {
        this.time = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), TimeZone.getDefault().toZoneId());
    }

    @JsonProperty("main")
    public void setTemperature(Map<String, String> main) {
        this.temperature = new BigDecimal(main.get("temp"));
    }

}
