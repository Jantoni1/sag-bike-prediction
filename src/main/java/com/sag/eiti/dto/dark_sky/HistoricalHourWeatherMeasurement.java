
package com.sag.eiti.dto.dark_sky;

import java.time.Instant;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "time",
    "summary",
    "icon",
    "precipIntensity",
    "precipProbability",
    "precipType",
    "temperature",
    "apparentTemperature",
    "dewPoint",
    "humidity",
    "pressure",
    "windSpeed",
    "windGust",
    "windBearing",
    "cloudCover",
    "uvIndex",
    "visibility",
    "ozone"
})
@Data
public class HistoricalHourWeatherMeasurement {

    @JsonIgnore
    public OffsetDateTime offsetDateTime;
    @JsonProperty("time")
    public Long timeStamp;
    @JsonProperty("summary")
    public String summary;
    @JsonProperty("icon")
    public String icon;
    @JsonProperty("precipIntensity")
    public Double precipIntensity;
    @JsonProperty("precipProbability")
    public Double precipProbability;
    @JsonProperty("precipType")
    public String precipType;
    @JsonProperty("temperature")
    public Double temperature;
    @JsonProperty("apparentTemperature")
    public Double apparentTemperature;
    @JsonProperty("dewPoint")
    public Double dewPoint;
    @JsonProperty("humidity")
    public Double humidity;
    @JsonProperty("pressure")
    public Double pressure;
    @JsonProperty("windSpeed")
    public Double windSpeed;
    @JsonProperty("windGust")
    public Double windGust;
    @JsonProperty("windBearing")
    public Integer windBearing;
    @JsonProperty("cloudCover")
    public Double cloudCover;
    @JsonProperty("uvIndex")
    public Integer uvIndex;
    @JsonProperty("visibility")
    public Integer visibility;
    @JsonProperty("ozone")
    public Double ozone;

    public void setOffsetDateTime(ZoneOffset zoneOffset) {
        this.offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(timeStamp), zoneOffset);
    }

}
