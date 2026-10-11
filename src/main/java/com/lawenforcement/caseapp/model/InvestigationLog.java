package com.lawenforcement.caseapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "investigation_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long complaintId;

    @Column(nullable = false, length = 100)
    private String officerName;

    @Column(nullable = false, length = 100)
    private String actionTaken;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
