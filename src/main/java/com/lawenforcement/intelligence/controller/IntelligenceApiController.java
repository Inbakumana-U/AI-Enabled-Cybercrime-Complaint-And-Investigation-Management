package com.lawenforcement.intelligence.controller;

import com.lawenforcement.intelligence.dto.AnalysisRequest;
import com.lawenforcement.intelligence.dto.AnalysisResponse;
import com.lawenforcement.intelligence.dto.CorrelatedCaseDTO;
import com.lawenforcement.intelligence.dto.CorrelationRequest;
import com.lawenforcement.intelligence.service.CyberIntelligenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intelligence")
public class IntelligenceApiController {

    private final CyberIntelligenceService intelligenceService;

    public IntelligenceApiController(CyberIntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeComplaint(@RequestBody AnalysisRequest request) {
        return ResponseEntity.ok(intelligenceService.analyze(request));
    }

    @PostMapping("/correlate")
    public ResponseEntity<List<CorrelatedCaseDTO>> correlateCase(@RequestBody CorrelationRequest request) {
        return ResponseEntity.ok(intelligenceService.correlate(request));
    }
}
