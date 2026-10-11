package com.lawenforcement.caseapp.controller;

import com.lawenforcement.alert.model.InvestigationAlert;
import com.lawenforcement.alert.repository.InvestigationAlertRepository;
import com.lawenforcement.caseapp.model.Complaint;
import com.lawenforcement.caseapp.repository.ComplaintRepository;
import com.lawenforcement.evidence.model.DigitalEvidence;
import com.lawenforcement.evidence.repository.DigitalEvidenceRepository;
import com.lawenforcement.fraud.model.FraudTransaction;
import com.lawenforcement.fraud.repository.FraudTransactionRepository;
import com.lawenforcement.intelligence.dto.AnalysisRequest;
import com.lawenforcement.intelligence.dto.AnalysisResponse;
import com.lawenforcement.intelligence.dto.CorrelatedCaseDTO;
import com.lawenforcement.intelligence.dto.CorrelationRequest;
import com.lawenforcement.intelligence.model.HistoricalIncident;
import com.lawenforcement.intelligence.service.CyberIntelligenceService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class DashboardController {

    private final ComplaintRepository complaintRepository;
    private final CyberIntelligenceService intelligenceService;
    private final DigitalEvidenceRepository evidenceRepository;
    private final FraudTransactionRepository transactionRepository;
    private final InvestigationAlertRepository alertRepository;

    public DashboardController(ComplaintRepository complaintRepository,
                               CyberIntelligenceService intelligenceService,
                               DigitalEvidenceRepository evidenceRepository,
                               FraudTransactionRepository transactionRepository,
                               InvestigationAlertRepository alertRepository) {
        this.complaintRepository = complaintRepository;
        this.intelligenceService = intelligenceService;
        this.evidenceRepository = evidenceRepository;
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
    }

    @PostConstruct
    public void seedInitialCases() {
        if (complaintRepository.count() > 0) {
            return;
        }

        // Case 1
        Complaint c1 = Complaint.builder()
                .complaintNumber("CC-2024-1001")
                .complainantName("Ramesh Chandra")
                .contactPhone("9876543210")
                .incidentDescription("Caller claimed my electricity bill was unpaid and power would be cut at 9:30 PM. Instructed to install QuickSupport and transfer ₹1,25,000 via UPI to refund-desk@ybl.")
                .lossAmount(125000.0)
                .suspectIdentifier("refund-desk@ybl")
                .status("UNDER_INVESTIGATION")
                .assignedOfficer("Insp. R. Sharma (Cyber Cell)")
                .aiCategory("ONLINE_FINANCIAL_FRAUD")
                .aiPriority("HIGH")
                .aiConfidenceScore(0.92)
                .aiKeyFactors("Multiple IoC tokens: [upi, transfer, refund]; loss amount ₹1,25,000; suspect identifier refund-desk@ybl matches syndicate.")
                .registeredAt(LocalDateTime.now().minusHours(24))
                .build();
        c1 = complaintRepository.save(c1);

        evidenceRepository.save(DigitalEvidence.builder()
                .complaintId(c1.getId())
                .evidenceType("TRANSACTION_UTR")
                .referenceValue("UPI/412309871234/REFUND")
                .digitalHash("a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f8091a2b3c4d5e6f708192")
                .chainOfCustodyOfficer("Insp. R. Sharma")
                .loggedAt(LocalDateTime.now().minusHours(23))
                .build());

        evidenceRepository.save(DigitalEvidence.builder()
                .complaintId(c1.getId())
                .evidenceType("SUSPECT_PHONE")
                .referenceValue("+91-9876543210")
                .digitalHash("f1e2d3c4b5a60987f1e2d3c4b5a60987f1e2d3c4b5a60987f1e2d3c4b5a60987")
                .chainOfCustodyOfficer("Insp. R. Sharma")
                .loggedAt(LocalDateTime.now().minusHours(23))
                .build());

        transactionRepository.save(FraudTransaction.builder()
                .complaintId(c1.getId())
                .transactionReference("UPI-ICICI-992102")
                .amount(125000.0)
                .senderAccount("AC-SBI-9876501")
                .beneficiaryAccount("refund-desk@ybl")
                .beneficiaryBankOrVpa("Yes Bank VPA")
                .transactionStatus("FREEZE_REQUESTED")
                .velocityTag("HIGH_VELOCITY_MULE_ACCOUNT")
                .transactionTime(LocalDateTime.now().minusHours(24))
                .build());

        // Case 2
        Complaint c2 = Complaint.builder()
                .complaintNumber("CC-2024-1002")
                .complainantName("Priya Sundaram")
                .contactPhone("9811223344")
                .incidentDescription("Received SMS claiming SBI NetBanking was blocked. Clicked link http://secure-sbi-portal.xyz, entered password and OTP. ₹72,000 debited.")
                .lossAmount(72000.0)
                .suspectIdentifier("http://secure-sbi-portal.xyz")
                .status("NEW")
                .assignedOfficer("Unassigned")
                .aiCategory("PHISHING_ATTACK")
                .aiPriority("CRITICAL")
                .aiConfidenceScore(0.95)
                .aiKeyFactors("High-risk credential harvesting: [link, url, password, apk, kyc]; fraudulent cloned SBI portal domain.")
                .registeredAt(LocalDateTime.now().minusHours(12))
                .build();
        c2 = complaintRepository.save(c2);

        evidenceRepository.save(DigitalEvidence.builder()
                .complaintId(c2.getId())
                .evidenceType("PHISHING_URL")
                .referenceValue("http://secure-sbi-portal.xyz")
                .digitalHash("99887766554433221100aabbccddeeff99887766554433221100aabbccddeeff")
                .chainOfCustodyOfficer("SI K. Verma")
                .loggedAt(LocalDateTime.now().minusHours(11))
                .build());

        transactionRepository.save(FraudTransaction.builder()
                .complaintId(c2.getId())
                .transactionReference("IMPS-HDFC-882910")
                .amount(72000.0)
                .senderAccount("AC-HDFC-445566")
                .beneficiaryAccount("AC-AXIS-990011")
                .beneficiaryBankOrVpa("Axis Bank Cyberabad")
                .transactionStatus("FLAGGED")
                .velocityTag("STANDARD")
                .transactionTime(LocalDateTime.now().minusHours(12))
                .build());

        alertRepository.save(InvestigationAlert.builder()
                .complaintId(c2.getId())
                .complaintNumber(c2.getComplaintNumber())
                .alertLevel("CRITICAL_RED")
                .alertMessage("Active Phishing Domain Live: http://secure-sbi-portal.xyz actively targeting SBI NetBanking users.")
                .acknowledged(false)
                .createdAt(LocalDateTime.now().minusHours(12))
                .build());

        // Case 3
        Complaint c3 = Complaint.builder()
                .complaintNumber("CC-2024-1003")
                .complainantName("Rohit Patel")
                .contactPhone("9123456780")
                .incidentDescription("Joined telegram task group promising crypto investment returns. Transferred initial amount to fastcrypto@paytm.")
                .lossAmount(1850000.0)
                .suspectIdentifier("fastcrypto@paytm")
                .status("NEW")
                .assignedOfficer("Unassigned")
                .aiCategory("ONLINE_FINANCIAL_FRAUD")
                .aiPriority("CRITICAL")
                .aiConfidenceScore(0.94)
                .aiKeyFactors("Assigned CRITICAL priority: Loss exceeds ₹5,00,000 threshold; Classified as ONLINE_FINANCIAL_FRAUD based on 3 IoC tokens: [crypto, investment, transfer].")
                .registeredAt(LocalDateTime.now().minusHours(4))
                .build();
        c3 = complaintRepository.save(c3);

        transactionRepository.save(FraudTransaction.builder()
                .complaintId(c3.getId())
                .transactionReference("RTGS-HDFC-102938")
                .amount(1850000.0)
                .senderAccount("AC-KOTAK-10293")
                .beneficiaryAccount("fastcrypto@paytm")
                .beneficiaryBankOrVpa("Paytm Bank VPA")
                .transactionStatus("FLAGGED")
                .velocityTag("HIGH_VELOCITY_MULE_ACCOUNT")
                .transactionTime(LocalDateTime.now().minusHours(4))
                .build());

        alertRepository.save(InvestigationAlert.builder()
                .complaintId(c3.getId())
                .complaintNumber(c3.getComplaintNumber())
                .alertLevel("CRITICAL_RED")
                .alertMessage("High-Value Financial Exfiltration (₹18,50,000) - Unassigned Case in NEW status!")
                .acknowledged(false)
                .createdAt(LocalDateTime.now().minusHours(4))
                .build());

        alertRepository.save(InvestigationAlert.builder()
                .complaintId(c3.getId())
                .complaintNumber(c3.getComplaintNumber())
                .alertLevel("WARNING_AMBER")
                .alertMessage("Recurring Fraud Syndicate Entity: fastcrypto@paytm matched active multi-state investment syndicate.")
                .acknowledged(false)
                .createdAt(LocalDateTime.now().minusHours(4))
                .build());
    }

    /**
     * Unified Single-Page Command Center (GET / and GET /dashboard)
     */
    @GetMapping({"/", "/dashboard"})
    public String viewUnifiedDashboard(@RequestParam(value = "search", required = false) String search,
                                       @RequestParam(value = "priority", required = false) String priority,
                                       @RequestParam(value = "status", required = false) String status,
                                       @RequestParam(value = "testNarrative", required = false) String testNarrative,
                                       @RequestParam(value = "testLoss", required = false) Double testLoss,
                                       @RequestParam(value = "testSuspect", required = false) String testSuspect,
                                       @RequestParam(value = "queryIoC", required = false) String queryIoC,
                                       @RequestParam(value = "tab", required = false) String tab,
                                       Model model) {

        // KPI Counts
        long totalCases = complaintRepository.count();
        Double totalLoss = complaintRepository.sumTotalLoss();
        long criticalCases = complaintRepository.countByAiPriority("CRITICAL");
        long underInvestigation = complaintRepository.countByStatus("UNDER_INVESTIGATION");
        long muleAccountsCount = transactionRepository.countMuleAccounts();
        long unacknowledgedAlerts = alertRepository.countByAcknowledgedFalse();
        long pendingFreezesCount = transactionRepository.countByTransactionStatus("FREEZE_REQUESTED") + transactionRepository.countByTransactionStatus("FLAGGED");

        // Active Alerts Feed
        List<InvestigationAlert> activeAlerts = alertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc();

        // Filtered Complaints Ledger
        List<Complaint> complaints = complaintRepository.searchComplaints(search, priority, status);

        // Transactions Watchlist
        List<FraudTransaction> recentTransactions = transactionRepository.findAllByOrderByTransactionTimeDesc();

        // Pre-seeded Historical Cases
        List<HistoricalIncident> historicalCases = intelligenceService.getAllHistoricalCases();
        model.addAttribute("historicalCases", historicalCases);

        // AI Sandbox Simulation
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

        // Reverse IoC Lookup
        if (queryIoC != null && !queryIoC.isBlank()) {
            CorrelationRequest corrReq = CorrelationRequest.builder()
                    .suspectIdentifier(queryIoC)
                    .incidentDescription(testNarrative != null ? testNarrative : "")
                    .build();
            List<CorrelatedCaseDTO> correlations = intelligenceService.correlate(corrReq);
            model.addAttribute("correlations", correlations);
            model.addAttribute("queryIoC", queryIoC);
        }

        // Active Tab Selection
        String currentTab = "cases";
        if (tab != null && !tab.isBlank()) {
            currentTab = tab;
        } else if ((testNarrative != null && !testNarrative.isBlank()) || (queryIoC != null && !queryIoC.isBlank())) {
            currentTab = "intel";
        }

        // Model attributes
        model.addAttribute("totalCases", totalCases);
        model.addAttribute("totalLoss", totalLoss != null ? totalLoss : 0.0);
        model.addAttribute("criticalCases", criticalCases);
        model.addAttribute("underInvestigation", underInvestigation);
        model.addAttribute("muleAccountsCount", muleAccountsCount);
        model.addAttribute("unacknowledgedAlerts", unacknowledgedAlerts);
        model.addAttribute("pendingFreezesCount", pendingFreezesCount);
        model.addAttribute("activeAlerts", activeAlerts);
        model.addAttribute("complaints", complaints);
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("selectedPriority", priority != null ? priority : "");
        model.addAttribute("selectedStatus", status != null ? status : "");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("activeTab", currentTab);
        model.addAttribute("newComplaint", new Complaint());

        return "dashboard";
    }

    @GetMapping("/complaints")
    public String complaintsRedirect() {
        return "redirect:/dashboard?tab=cases";
    }

    @GetMapping("/intelligence")
    public String intelligenceRedirect() {
        return "redirect:/dashboard?tab=intel";
    }
}
