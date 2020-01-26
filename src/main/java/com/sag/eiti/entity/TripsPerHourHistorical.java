package com.sag.eiti.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.OffsetDateTime;

@Entity(name="historical_trip_data")
@Getter
@Setter
@SequenceGenerator(name="trips_historical_seq", allocationSize=1)
@AllArgsConstructor
@NoArgsConstructor
public class TripsPerHourHistorical {

    @Column(name="id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trips_historical_seq")
    private Integer id;

    @Column(name="measured_hour_start")
    private Instant tripsHourStart;

    @Column(name="number_of_rides")
    private Integer numberOfRides;

    @Column(name="temperature")
    private Double temperature;

}
