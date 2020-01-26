package com.sag.eiti.dto.bergen_city_bike;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

//public class BergenCityBikesRide {
//    OffsetDateTime rideStart;
//    OffsetDateTime rideEnd;
//
//    int duration;
//
//
//
//}

@Data
@NoArgsConstructor
public class BergenCityBikesRide {

    @JsonProperty("started_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss[.SSSSSS][]XXX")
    OffsetDateTime rideStart;
    @JsonProperty("ended_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss[.SSSSSS][]XXX")
    OffsetDateTime rideEnd;
    @JsonProperty("duration")
    private Integer duration;
    @JsonProperty("start_station_id")
    private String startStationId;
    @JsonProperty("start_station_name")
    private String startStationName;
    @JsonProperty("start_station_description")
    private String startStationDescription;
    @JsonProperty("start_station_latitude")
    private Double startStationLatitude;
    @JsonProperty("start_station_longitude")
    private Double startStationLongitude;
    @JsonProperty("end_station_id")
    private String endStationId;
    @JsonProperty("end_station_name")
    private String endStationName;
    @JsonProperty("end_station_description")
    private String endStationDescription;
    @JsonProperty("end_station_latitude")
    private Double endStationLatitude;
    @JsonProperty("end_station_longitude")
    private Double endStationLongitude;


}