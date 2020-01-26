package com.sag.eiti.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity(name="predicted_trips")
@SequenceGenerator(name="predicted_trips_seq", allocationSize = 1)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PredictedTripsPerHour {

    @Column(name="id")
    @Id
    @GeneratedValue
    private Integer id;

    @Column(name="predicted_hour_start")
    private Instant predictedHourStart;

    @Column(name="predicted_rides")
    private Integer predictedRides;

    @Column(name="temperature")
    private Double temperature;

    @Column(name="insertDate")
    private Instant insertDate;

}
