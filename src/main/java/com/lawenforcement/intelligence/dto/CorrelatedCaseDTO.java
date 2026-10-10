package com.lawenforcement.intelligence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelatedCaseDTO {
    private String historicalCaseId;
    private String matchedIdentifier;
    private int similarityPercentage;
    private String explanation;
}
