package com.lawenforcement.alert.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "investigation_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long complaintId;

    @Column(length = 50)
    private String complaintNumber;

    @Column(nullable = false, length = 50)
    private String alertLevel; // CRITICAL_RED, WARNING_AMBER, INFO_BLUE

    @Column(nullable = false, columnDefinition = "TEXT")
    private String alertMessage;

    @Column(nullable = false)
    private boolean acknowledged;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
