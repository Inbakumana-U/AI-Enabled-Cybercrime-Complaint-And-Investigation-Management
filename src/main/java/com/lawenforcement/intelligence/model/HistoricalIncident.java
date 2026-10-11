package com.lawenforcement.intelligence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "historical_incidents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String caseId;

    @Column(length = 100)
    private String suspectIdentifier; // Phone, UPI, or Account

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 20)
    private String priorityLevel;

    @Column(nullable = false)
    private Double lossAmount;

    @Column(columnDefinition = "TEXT")
    private String scamNarrative;
}