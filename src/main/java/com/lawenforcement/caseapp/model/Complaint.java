package com.lawenforcement.caseapp.model;

import com.lawenforcement.alert.model.InvestigationAlert;
import com.lawenforcement.evidence.model.DigitalEvidence;
import com.lawenforcement.fraud.model.FraudTransaction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "complaints")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String complaintNumber;

    @Column(nullable = false, length = 150)
    private String complainantName;

    @Column(nullable = false, length = 30)
    private String contactPhone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String incidentDescription;

    @Column(nullable = false)
    private Double lossAmount;

    @Column(length = 150)
    private String suspectIdentifier; // UPI ID, Phone, or Bank Account

    @Column(nullable = false, length = 30)
    private String status; // NEW, ASSIGNED, UNDER_INVESTIGATION, ESCALATED, RESOLVED

    @Column(length = 100)
    private String assignedOfficer;

    @Column(length = 100)
    private String aiCategory;

    @Column(length = 30)
    private String aiPriority;

    private Double aiConfidenceScore;

    @Column(columnDefinition = "TEXT")
    private String aiKeyFactors;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "complaintId")
    @Builder.Default
    private List<DigitalEvidence> evidenceList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "complaintId")
    @Builder.Default
    private List<FraudTransaction> transactionList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "complaintId")
    @Builder.Default
    private List<InvestigationAlert> alertList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "complaintId")
    @Builder.Default
    private List<InvestigationLog> auditLogs = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "NEW";
        }
    }
}
