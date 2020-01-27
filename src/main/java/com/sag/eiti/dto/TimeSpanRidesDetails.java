package com.sag.eiti.dto;

import lombok.Value;

import java.time.OffsetDateTime;

@Value
public class TimeSpanRidesDetails {
    private OffsetDateTime requestedHour;
    private long ridesCount;
}
