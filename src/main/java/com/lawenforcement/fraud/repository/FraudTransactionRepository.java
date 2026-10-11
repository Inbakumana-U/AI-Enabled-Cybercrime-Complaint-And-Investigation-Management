package com.lawenforcement.fraud.repository;

import com.lawenforcement.fraud.model.FraudTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudTransactionRepository extends JpaRepository<FraudTransaction, Long> {
    List<FraudTransaction> findByComplaintIdOrderByTransactionTimeDesc(Long complaintId);
    List<FraudTransaction> findAllByOrderByTransactionTimeDesc();
    
    long countByTransactionStatus(String transactionStatus);

    @Query("SELECT COUNT(DISTINCT f.beneficiaryAccount) FROM FraudTransaction f WHERE f.velocityTag = 'HIGH_VELOCITY_MULE_ACCOUNT'")
    long countMuleAccounts();
}
