package com.lawenforcement.evidence.repository;

import com.lawenforcement.evidence.model.DigitalEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DigitalEvidenceRepository extends JpaRepository<DigitalEvidence, Long> {
    List<DigitalEvidence> findByComplaintIdOrderByLoggedAtDesc(Long complaintId);
    long countByComplaintId(Long complaintId);
}
