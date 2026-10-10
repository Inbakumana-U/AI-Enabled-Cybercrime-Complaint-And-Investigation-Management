package com.lawenforcement.caseapp.repository;

import com.lawenforcement.caseapp.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    List<Complaint> findAllByOrderByRegisteredAtDesc();

    List<Complaint> findTop5ByOrderByRegisteredAtDesc();

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.complaintNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.complainantName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.contactPhone) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:priority IS NULL OR :priority = '' OR c.aiPriority = :priority) AND " +
           "(:status IS NULL OR :status = '' OR c.status = :status) " +
           "ORDER BY c.registeredAt DESC")
    List<Complaint> searchComplaints(@Param("search") String search,
                                     @Param("priority") String priority,
                                     @Param("status") String status);

    long countByAiPriority(String aiPriority);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(c.lossAmount), 0.0) FROM Complaint c")
    Double sumTotalLoss();
}
