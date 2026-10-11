package com.lawenforcement.evidence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "digital_evidence")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long complaintId;

    @Column(nullable = false, length = 50)
    private String evidenceType; // TRANSACTION_UTR, PHISHING_URL, SUSPECT_PHONE, CRYPTO_WALLET, MALICIOUS_APK_HASH

    @Column(nullable = false, length = 500)
    private String referenceValue;

    @Column(nullable = false, length = 100)
    private String digitalHash; // Simulated SHA-256 integrity hash for Sec 65B Indian Evidence Act

    @Column(nullable = false, length = 100)
    private String chainOfCustodyOfficer;

    @Column(nullable = false)
    private LocalDateTime loggedAt;

    @PrePersist
    protected void onCreate() {
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now();
        }
    }
}
