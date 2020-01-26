package com.sag.eiti.repository;

import com.sag.eiti.entity.PredictedTripsPerHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PredictedTripsRepository extends JpaRepository<PredictedTripsPerHour, Integer> {

    List<PredictedTripsPerHour> getPredictedTripsPerHourByPredictedHourStartIn(List<Instant> predictedHours);
}
