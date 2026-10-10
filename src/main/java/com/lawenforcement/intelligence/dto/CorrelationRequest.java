package com.lawenforcement.intelligence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationRequest {
    private String suspectIdentifier;
    private String incidentDescription;
}
