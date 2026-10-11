package com.lawenforcement.alert.repository;

import com.lawenforcement.alert.model.InvestigationAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestigationAlertRepository extends JpaRepository<InvestigationAlert, Long> {
    List<InvestigationAlert> findAllByOrderByCreatedAtDesc();
    List<InvestigationAlert> findByAcknowledgedFalseOrderByCreatedAtDesc();
    List<InvestigationAlert> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);
    long countByAcknowledgedFalse();
}
