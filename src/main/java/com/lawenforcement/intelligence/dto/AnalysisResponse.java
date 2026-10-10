package com.lawenforcement.intelligence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private String category;
    private String priorityLevel;
    private Double confidenceScore;
    private String keyFactors;
}
