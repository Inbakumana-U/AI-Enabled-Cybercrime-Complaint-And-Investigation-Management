package com.lawenforcement.caseapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String status; // NEW, ASSIGNED, UNDER_INVESTIGATION, RESOLVED

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
