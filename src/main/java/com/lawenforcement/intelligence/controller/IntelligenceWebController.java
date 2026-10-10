package com.lawenforcement.intelligence.controller;

import com.lawenforcement.intelligence.dto.AnalysisRequest;
import com.lawenforcement.intelligence.dto.AnalysisResponse;
import com.lawenforcement.intelligence.dto.CorrelatedCaseDTO;
import com.lawenforcement.intelligence.dto.CorrelationRequest;
import com.lawenforcement.intelligence.model.HistoricalIncident;
import com.lawenforcement.intelligence.service.CyberIntelligenceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/intelligence")
public class IntelligenceWebController {

    private final CyberIntelligenceService intelligenceService;

    public IntelligenceWebController(CyberIntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService;
    }

    /**
     * Workspace 2: AI Intelligence & Threat Telemetry Hub
     */
    @GetMapping
    public String viewIntelligenceHub(@RequestParam(value = "testNarrative", required = false) String testNarrative,
                                      @RequestParam(value = "testLoss", required = false) Double testLoss,
                                      @RequestParam(value = "testSuspect", required = false) String testSuspect,
                                      @RequestParam(value = "queryIoC", required = false) String queryIoC,
                                      Model model) {

        List<HistoricalIncident> historicalCases = intelligenceService.getAllHistoricalCases();
        model.addAttribute("historicalCases", historicalCases);

        // Interactive Sandbox Triage
        if (testNarrative != null && !testNarrative.isBlank()) {
            AnalysisRequest req = AnalysisRequest.builder()
                    .incidentDescription(testNarrative)
                    .lossAmount(testLoss != null ? testLoss : 0.0)
                    .suspectIdentifier(testSuspect)
                    .build();
            AnalysisResponse testAnalysis = intelligenceService.analyze(req);
            model.addAttribute("testAnalysis", testAnalysis);
            model.addAttribute("testNarrative", testNarrative);
            model.addAttribute("testLoss", testLoss);
            model.addAttribute("testSuspect", testSuspect);
        }

        // Live IoC Correlation Tester
        if (queryIoC != null && !queryIoC.isBlank()) {
            CorrelationRequest corrReq = CorrelationRequest.builder()
                    .suspectIdentifier(queryIoC)
                    .incidentDescription(testNarrative != null ? testNarrative : "")
                    .build();
            List<CorrelatedCaseDTO> correlations = intelligenceService.correlate(corrReq);
            model.addAttribute("correlations", correlations);
            model.addAttribute("queryIoC", queryIoC);
        }

        model.addAttribute("activeWorkspace", "intelligence");
        return "intelligence-hub";
    }
}
