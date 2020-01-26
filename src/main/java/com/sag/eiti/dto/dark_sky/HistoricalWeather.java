
package com.sag.eiti.dto.dark_sky;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "latitude",
    "longitude",
    "timezone",
    "hourly",
    "daily",
    "flags",
    "offset"
})
@Data
public class HistoricalWeather {

    @JsonProperty("timezone")
    private String timezone;
    @JsonProperty("hourly")
    private Hourly hourly;
    @JsonProperty("offset")
    private Integer offset;

}
