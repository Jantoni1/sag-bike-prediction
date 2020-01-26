package com.sag.eiti.repository;

import com.sag.eiti.entity.TripsPerHourHistorical;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

public interface TripsPerHourHistoricalRepository extends JpaRepository<TripsPerHourHistorical, Integer> {

    List<TripsPerHourHistorical> findAllByTripsHourStartIn(List<Instant> tripsHours);

}
