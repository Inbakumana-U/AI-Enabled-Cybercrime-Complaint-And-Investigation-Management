package com.lawenforcement.caseapp.controller;

import com.lawenforcement.caseapp.model.Complaint;
import com.lawenforcement.caseapp.repository.ComplaintRepository;
import com.lawenforcement.intelligence.dto.AnalysisRequest;
import com.lawenforcement.intelligence.dto.AnalysisResponse;
import com.lawenforcement.intelligence.dto.CorrelatedCaseDTO;
import com.lawenforcement.intelligence.dto.CorrelationRequest;
import com.lawenforcement.intelligence.service.CyberIntelligenceService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Controller
public class CaseWebController {

    private final ComplaintRepository complaintRepository;
    private final CyberIntelligenceService intelligenceService;

    public CaseWebController(ComplaintRepository complaintRepository,
                             CyberIntelligenceService intelligenceService) {
        this.complaintRepository = complaintRepository;
        this.intelligenceService = intelligenceService;
    }

    @PostConstruct
    public void seedInitialCases() {
        if (complaintRepository.count() > 0) {
            return;
        }

        Complaint c1 = Complaint.builder()
                .complaintNumber("CC-2024-1001")
                .complainantName("Vikramaditya Sharma")
                .contactPhone("9811223344")
                .incidentDescription("Victim received fraudulent call regarding electricity bill refund. Guided to install AnyDesk and transfer funds via UPI to refund-desk@ybl.")
                .lossAmount(125000.0)
                .suspectIdentifier("refund-desk@ybl")
                .status("UNDER_INVESTIGATION")
                .assignedOfficer("Inspector Rajesh Kumar")
                .aiCategory("ONLINE_FINANCIAL_FRAUD")
                .aiPriority("HIGH")
                .aiConfidenceScore(0.85)
                .aiKeyFactors("Assigned HIGH priority: Loss exceeds ₹50,000 threshold; Classified as ONLINE_FINANCIAL_FRAUD based on 3 IoC tokens: [refund, upi, transfer].")
                .registeredAt(LocalDateTime.now().minusDays(2))
                .build();
        complaintRepository.save(c1);

        Complaint c2 = Complaint.builder()
                .complaintNumber("CC-2024-1002")
                .complainantName("Meenakshi Sundaram")
                .contactPhone("9744332211")
                .incidentDescription("SMS alert stating PAN card deactivated. Clicked phishing link to cloned SBI netbanking portal url. Entered login password and OTP.")
                .lossAmount(650000.0)
                .suspectIdentifier("http://secure-sbi-portal.xyz")
                .status("ASSIGNED")
                .assignedOfficer("Officer Ananya Verma")
                .aiCategory("PHISHING_ATTACK")
                .aiPriority("CRITICAL")
                .aiConfidenceScore(0.94)
                .aiKeyFactors("Assigned CRITICAL priority: Loss exceeds ₹5,00,000 threshold; Classified as PHISHING_ATTACK based on 4 IoC tokens: [link, url, login, password].")
                .registeredAt(LocalDateTime.now().minusDays(1))
                .build();
        complaintRepository.save(c2);

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
        complaintRepository.save(c3);
    }

    /**
     * Default Route: Redirects to Case Desk (/complaints)
     */
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/complaints";
    }

    /**
     * Workspace 1: Case & Investigation Desk Landing Page
     */
    @GetMapping("/complaints")
    public String listComplaints(@RequestParam(value = "search", required = false) String search,
                                 @RequestParam(value = "priority", required = false) String priority,
                                 @RequestParam(value = "status", required = false) String status,
                                 Model model) {
        List<Complaint> complaints = complaintRepository.searchComplaints(search, priority, status);

        long totalCases = complaintRepository.count();
        Double totalLoss = complaintRepository.sumTotalLoss();
        long criticalCases = complaintRepository.countByAiPriority("CRITICAL");
        long underInvestigation = complaintRepository.countByStatus("UNDER_INVESTIGATION");

        model.addAttribute("complaints", complaints);
        model.addAttribute("totalCases", totalCases);
        model.addAttribute("totalLoss", totalLoss);
        model.addAttribute("criticalCases", criticalCases);
        model.addAttribute("underInvestigation", underInvestigation);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("selectedPriority", priority != null ? priority : "");
        model.addAttribute("selectedStatus", status != null ? status : "");
        model.addAttribute("activeWorkspace", "cases");

        return "complaint-list";
    }

    /**
     * Intake Form
     */
    @GetMapping("/complaints/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("complaint", new Complaint());
        model.addAttribute("activeWorkspace", "cases");
        return "complaint-register";
    }

    /**
     * Intake Submission -> Executes AI Intelligence Analysis -> Persists -> Redirects
     */
    @PostMapping("/complaints/register")
    public String registerComplaint(@ModelAttribute Complaint complaint, RedirectAttributes redirectAttributes) {
        complaint.setComplaintNumber(generateComplaintNumber());
        complaint.setStatus("NEW");
        complaint.setRegisteredAt(LocalDateTime.now());

        // Modular service call directly to AI Intelligence engine
        AnalysisRequest req = AnalysisRequest.builder()
                .incidentDescription(complaint.getIncidentDescription())
                .lossAmount(complaint.getLossAmount())
                .suspectIdentifier(complaint.getSuspectIdentifier())
                .build();

        AnalysisResponse analysis = intelligenceService.analyze(req);

        complaint.setAiCategory(analysis.getCategory());
        complaint.setAiPriority(analysis.getPriorityLevel());
        complaint.setAiConfidenceScore(analysis.getConfidenceScore());
        complaint.setAiKeyFactors(analysis.getKeyFactors());

        Complaint saved = complaintRepository.save(complaint);

        redirectAttributes.addFlashAttribute("successMessage",
                "Complaint filed successfully [" + saved.getComplaintNumber() + "] with AI Priority: " + saved.getAiPriority());

        return "redirect:/complaints/" + saved.getId();
    }

    /**
     * Case Dossier: Features 2, 3, and 4
     */
    @GetMapping("/complaints/{id}")
    public String viewComplaintDetail(@PathVariable("id") Long id, Model model) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid complaint Id: " + id));

        // Pattern Correlation Query directly against Intelligence engine
        CorrelationRequest corrReq = CorrelationRequest.builder()
                .suspectIdentifier(complaint.getSuspectIdentifier())
                .incidentDescription(complaint.getIncidentDescription())
                .build();
        List<CorrelatedCaseDTO> correlatedCases = intelligenceService.correlate(corrReq);

        model.addAttribute("complaint", complaint);
        model.addAttribute("correlatedCases", correlatedCases);
        model.addAttribute("activeWorkspace", "cases");

        return "complaint-detail";
    }

    /**
     * Case Status & Officer Assignment Workflow
     */
    @PostMapping("/complaints/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") String status,
                               @RequestParam(value = "assignedOfficer", required = false) String assignedOfficer,
                               RedirectAttributes redirectAttributes) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid complaint Id: " + id));

        complaint.setStatus(status);
        if (assignedOfficer != null && !assignedOfficer.isBlank()) {
            complaint.setAssignedOfficer(assignedOfficer.trim());
        }
        complaintRepository.save(complaint);

        redirectAttributes.addFlashAttribute("successMessage", "Case investigation status & officer assignment updated.");
        return "redirect:/complaints/" + id;
    }

    private synchronized String generateComplaintNumber() {
        int randomCode = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "CC-2024-" + randomCode;
    }
}
