package com.sag.eiti.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class RidesDataWithTemperature {
    OffsetDateTime dateTime;
    Double temperature;
    Long numberOfRides;
}
