package com.lawenforcement.fraud.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long complaintId;

    @Column(nullable = false, length = 100)
    private String transactionReference; // UTR / IMPS / UPI Ref

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 100)
    private String senderAccount;

    @Column(nullable = false, length = 100)
    private String beneficiaryAccount;

    @Column(nullable = false, length = 150)
    private String beneficiaryBankOrVpa;

    @Column(nullable = false, length = 50)
    private String transactionStatus; // FLAGGED, FREEZE_REQUESTED, FROZEN, ESCALATED

    @Column(length = 50)
    private String velocityTag; // e.g. HIGH_VELOCITY_MULE_ACCOUNT

    @Column(nullable = false)
    private LocalDateTime transactionTime;

    @PrePersist
    protected void onCreate() {
        if (transactionTime == null) {
            transactionTime = LocalDateTime.now();
        }
        if (transactionStatus == null) {
            transactionStatus = "FLAGGED";
        }
    }
}
