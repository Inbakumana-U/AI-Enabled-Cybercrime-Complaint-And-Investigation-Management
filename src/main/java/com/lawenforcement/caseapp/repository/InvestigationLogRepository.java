package com.lawenforcement.caseapp.repository;

import com.lawenforcement.caseapp.model.InvestigationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestigationLogRepository extends JpaRepository<InvestigationLog, Long> {
    List<InvestigationLog> findByComplaintIdOrderByTimestampDesc(Long complaintId);
}
