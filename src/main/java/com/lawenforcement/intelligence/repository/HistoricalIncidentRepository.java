package com.lawenforcement.intelligence.repository;

import com.lawenforcement.intelligence.model.HistoricalIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoricalIncidentRepository extends JpaRepository<HistoricalIncident, Long> {
    Optional<HistoricalIncident> findByCaseId(String caseId);
}
