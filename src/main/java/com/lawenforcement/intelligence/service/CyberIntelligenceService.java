package com.lawenforcement.intelligence.service;

import com.lawenforcement.intelligence.dto.AnalysisRequest;
import com.lawenforcement.intelligence.dto.AnalysisResponse;
import com.lawenforcement.intelligence.dto.CorrelatedCaseDTO;
import com.lawenforcement.intelligence.dto.CorrelationRequest;
import com.lawenforcement.intelligence.model.HistoricalIncident;
import com.lawenforcement.intelligence.repository.HistoricalIncidentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CyberIntelligenceService {

    private final HistoricalIncidentRepository incidentRepository;

    private static final Set<String> FINANCIAL_FRAUD_KEYWORDS = Set.of(
            "upi", "transfer", "crypto", "investment", "refund", "otp", "bank"
    );

    private static final Set<String> PHISHING_KEYWORDS = Set.of(
            "link", "url", "login", "password", "apk", "kyc", "update"
    );

    private static final Set<String> IDENTITY_THEFT_KEYWORDS = Set.of(
            "impersonation", "fake profile", "aadhaar", "pan", "blackmail"
    );

    public CyberIntelligenceService(HistoricalIncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @PostConstruct
    public void seedHistoricalDataset() {
        if (incidentRepository.count() > 0) {
            return;
        }

        List<HistoricalIncident> seeds = List.of(
                HistoricalIncident.builder()
                        .caseId("HC-2024-8801")
                        .suspectIdentifier("refund-desk@ybl")
                        .category("ONLINE_FINANCIAL_FRAUD")
                        .priorityLevel("HIGH")
                        .lossAmount(125000.0)
                        .scamNarrative("electricity bill scam refund kyc update remote app support upi transfer anydesk")
                        .build(),

                HistoricalIncident.builder()
                        .caseId("HC-2024-8802")
                        .suspectIdentifier("+91-9876543210")
                        .category("ONLINE_FINANCIAL_FRAUD")
                        .priorityLevel("CRITICAL")
                        .lossAmount(650000.0)
                        .scamNarrative("bank account transfer otp fraud electricity bill scam anydesk siphoning")
                        .build(),

                HistoricalIncident.builder()
                        .caseId("HC-2024-8803")
                        .suspectIdentifier("fastcrypto@paytm")
                        .category("ONLINE_FINANCIAL_FRAUD")
                        .priorityLevel("CRITICAL")
                        .lossAmount(1850000.0)
                        .scamNarrative("telegram crypto task fraud investment return scheme usdt vip account")
                        .build(),

                HistoricalIncident.builder()
                        .caseId("HC-2024-8804")
                        .suspectIdentifier("http://secure-sbi-portal.xyz")
                        .category("PHISHING_ATTACK")
                        .priorityLevel("CRITICAL")
                        .lossAmount(520000.0)
                        .scamNarrative("phishing link url login password harvesting sbi cloned portal kyc apk update")
                        .build(),

                HistoricalIncident.builder()
                        .caseId("HC-2024-8805")
                        .suspectIdentifier("impersonator_ig_support")
                        .category("IDENTITY_THEFT")
                        .priorityLevel("HIGH")
                        .lossAmount(75000.0)
                        .scamNarrative("fake profile impersonation blackmail social media morphing video call extortion")
                        .build(),

                HistoricalIncident.builder()
                        .caseId("HC-2024-8806")
                        .suspectIdentifier("+91-9123456780")
                        .category("ONLINE_FINANCIAL_FRAUD")
                        .priorityLevel("MEDIUM")
                        .lossAmount(25000.0)
                        .scamNarrative("task fraud work from home youtube video like investment prepaid recharge")
                        .build(),

                HistoricalIncident.builder()
                        .caseId("HC-2024-8807")
                        .suspectIdentifier("customs-clearance@airtel")
                        .category("ONLINE_FINANCIAL_FRAUD")
                        .priorityLevel("HIGH")
                        .lossAmount(320000.0)
                        .scamNarrative("customs courier interception digital arrest fake police call pmla transfer penalty")
                        .build(),

                HistoricalIncident.builder()
                        .caseId("HC-2024-8808")
                        .suspectIdentifier("+91-9345678901")
                        .category("ONLINE_FINANCIAL_FRAUD")
                        .priorityLevel("LOW")
                        .lossAmount(4500.0)
                        .scamNarrative("olx qr code receive money pin scan advance token fake officer marketplace")
                        .build()
        );

        incidentRepository.saveAll(seeds);
    }

    public List<HistoricalIncident> getAllHistoricalCases() {
        return incidentRepository.findAll();
    }

    /**
     * AI Analysis & Prioritization Engine
     */
    public AnalysisResponse analyze(AnalysisRequest request) {
        String description = request.getIncidentDescription() != null ? request.getIncidentDescription().toLowerCase() : "";
        Double loss = request.getLossAmount() != null ? request.getLossAmount() : 0.0;
        String suspect = request.getSuspectIdentifier() != null ? request.getSuspectIdentifier().toLowerCase() : "";

        String combined = description + " " + suspect;

        List<String> finMatches = findMatches(combined, FINANCIAL_FRAUD_KEYWORDS);
        List<String> phishMatches = findMatches(combined, PHISHING_KEYWORDS);
        List<String> idMatches = findMatches(combined, IDENTITY_THEFT_KEYWORDS);

        int finCount = finMatches.size();
        int phishCount = phishMatches.size();
        int idCount = idMatches.size();

        String category;
        List<String> matchedTokens;
        if (finCount >= phishCount && finCount >= idCount && finCount > 0) {
            category = "ONLINE_FINANCIAL_FRAUD";
            matchedTokens = finMatches;
        } else if (phishCount >= finCount && phishCount >= idCount && phishCount > 0) {
            category = "PHISHING_ATTACK";
            matchedTokens = phishMatches;
        } else if (idCount > 0) {
            category = "IDENTITY_THEFT";
            matchedTokens = idMatches;
        } else {
            category = "ONLINE_FINANCIAL_FRAUD";
            matchedTokens = List.of("financial");
        }

        int vectorCount = (finCount > 0 ? 1 : 0) + (phishCount > 0 ? 1 : 0) + (idCount > 0 ? 1 : 0);
        boolean containsHighSeverityTerms = description.contains("ransomware") || description.contains("anydesk") || 
                                           description.contains("cloned") || description.contains("apk") || phishCount >= 3;

        String priority;
        String priorityReason;
        if (loss > 500000.0 || containsHighSeverityTerms) {
            priority = "CRITICAL";
            priorityReason = loss > 500000.0 ? "Loss exceeds ₹5,00,000 threshold" : "Contains high-severity threat terms";
        } else if (loss > 50000.0 || vectorCount >= 2) {
            priority = "HIGH";
            priorityReason = loss > 50000.0 ? "Loss exceeds ₹50,000 threshold" : "Matches multiple threat vectors (" + vectorCount + " vectors)";
        } else if (loss > 5000.0) {
            priority = "MEDIUM";
            priorityReason = "Loss exceeds ₹5,000 threshold";
        } else {
            priority = "LOW";
            priorityReason = "Standard baseline exposure";
        }

        int totalHits = finCount + phishCount + idCount;
        double confidence = totalHits >= 3 ? 0.94 : (totalHits >= 1 ? 0.85 : 0.70);

        String keyFactors = String.format(
                "Assigned %s priority: %s; Classified as %s based on %d IoC tokens: [%s].",
                priority,
                priorityReason,
                category,
                matchedTokens.size(),
                String.join(", ", matchedTokens)
        );

        return AnalysisResponse.builder()
                .category(category)
                .priorityLevel(priority)
                .confidenceScore(confidence)
                .keyFactors(keyFactors)
                .build();
    }

    /**
     * Case Correlation & Pattern Identification Engine
     */
    public List<CorrelatedCaseDTO> correlate(String suspectId, String text) {
        return correlate(CorrelationRequest.builder()
                .suspectIdentifier(suspectId)
                .incidentDescription(text)
                .build());
    }

    public List<CorrelatedCaseDTO> correlate(CorrelationRequest request) {
        String activeSuspect = request.getSuspectIdentifier() != null ? request.getSuspectIdentifier().trim().toLowerCase() : "";
        String activeNarrative = request.getIncidentDescription() != null ? request.getIncidentDescription().toLowerCase() : "";

        List<HistoricalIncident> historicalCases = incidentRepository.findAll();
        List<CorrelatedCaseDTO> results = new ArrayList<>();

        Set<String> activeWords = extractKeywords(activeNarrative);

        for (HistoricalIncident hc : historicalCases) {
            String histSuspect = hc.getSuspectIdentifier() != null ? hc.getSuspectIdentifier().trim().toLowerCase() : "";

            // Linkage Rule 1: Exact Match on phone, UPI ID, or account number -> 95% Match
            if (!activeSuspect.isEmpty() && (histSuspect.equals(activeSuspect) || histSuspect.contains(activeSuspect) || activeSuspect.contains(histSuspect))) {
                results.add(CorrelatedCaseDTO.builder()
                        .historicalCaseId(hc.getCaseId())
                        .matchedIdentifier(hc.getSuspectIdentifier())
                        .similarityPercentage(95)
                        .explanation("Reused Suspect Digital Identifier (" + hc.getSuspectIdentifier() + "). Direct linkage to known syndicate.")
                        .build());
                continue;
            }

            // Linkage Rule 2: Modus Operandi Match on high token overlap in narrative -> 70% Match
            Set<String> histWords = extractKeywords(hc.getScamNarrative());
            long commonTokens = activeWords.stream().filter(histWords::contains).count();

            if (commonTokens >= 2) {
                results.add(CorrelatedCaseDTO.builder()
                        .historicalCaseId(hc.getCaseId())
                        .matchedIdentifier(hc.getSuspectIdentifier() != null ? hc.getSuspectIdentifier() : "N/A")
                        .similarityPercentage(70)
                        .explanation("Similar Scam Pattern / Modus Operandi (" + commonTokens + " overlapping threat tokens detected).")
                        .build());
            }
        }

        results.sort(Comparator.comparingInt(CorrelatedCaseDTO::getSimilarityPercentage).reversed());
        return results;
    }

    private List<String> findMatches(String text, Set<String> keywords) {
        List<String> list = new ArrayList<>();
        for (String kw : keywords) {
            if (text.contains(kw)) {
                list.add(kw);
            }
        }
        return list;
    }

    private Set<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(text.split("[^a-zA-Z0-9]+"))
                .filter(w -> w.length() > 3)
                .collect(Collectors.toSet());
    }
}
