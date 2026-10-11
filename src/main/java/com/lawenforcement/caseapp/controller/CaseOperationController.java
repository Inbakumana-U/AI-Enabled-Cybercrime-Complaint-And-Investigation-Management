package com.lawenforcement.caseapp.controller;

import com.lawenforcement.alert.repository.InvestigationAlertRepository;
import com.lawenforcement.caseapp.model.Complaint;
import com.lawenforcement.caseapp.model.InvestigationLog;
import com.lawenforcement.caseapp.repository.ComplaintRepository;
import com.lawenforcement.caseapp.repository.InvestigationLogRepository;
import com.lawenforcement.caseapp.service.InvestigationWorkflowService;
import com.lawenforcement.evidence.model.DigitalEvidence;
import com.lawenforcement.evidence.repository.DigitalEvidenceRepository;
import com.lawenforcement.fraud.model.FraudTransaction;
import com.lawenforcement.fraud.repository.FraudTransactionRepository;
import com.lawenforcement.intelligence.dto.CorrelatedCaseDTO;
import com.lawenforcement.intelligence.service.CyberIntelligenceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CaseOperationController {

    private final ComplaintRepository complaintRepository;
    private final DigitalEvidenceRepository evidenceRepository;
    private final FraudTransactionRepository transactionRepository;
    private final InvestigationAlertRepository alertRepository;
    private final InvestigationLogRepository logRepository;
    private final CyberIntelligenceService intelligenceService;
    private final InvestigationWorkflowService workflowService;

    public CaseOperationController(ComplaintRepository complaintRepository,
                                   DigitalEvidenceRepository evidenceRepository,
                                   FraudTransactionRepository transactionRepository,
                                   InvestigationAlertRepository alertRepository,
                                   InvestigationLogRepository logRepository,
                                   CyberIntelligenceService intelligenceService,
                                   InvestigationWorkflowService workflowService) {
        this.complaintRepository = complaintRepository;
        this.evidenceRepository = evidenceRepository;
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
        this.logRepository = logRepository;
        this.intelligenceService = intelligenceService;
        this.workflowService = workflowService;
    }

    /**
     * Standalone Complaint Intake Form (or accessed directly)
     */
    @GetMapping("/complaints/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("complaint", new Complaint());
        model.addAttribute("activePage", "register");
        return "complaint-register";
    }

    /**
     * Form Submission: Intake Pipeline
     */
    @PostMapping("/complaints/register")
    public String registerComplaint(@ModelAttribute Complaint complaint, RedirectAttributes redirectAttributes) {
        Complaint saved = workflowService.processNewComplaint(complaint);
        redirectAttributes.addFlashAttribute("successMessage",
                "Complaint filed successfully [" + saved.getComplaintNumber() + "] with AI Priority: " + saved.getAiPriority());
        return "redirect:/complaints/" + saved.getId();
    }

    /**
     * Case Dossier: Full Investigation View
     */
    @GetMapping("/complaints/{id}")
    public String viewComplaintDetail(@PathVariable("id") Long id, Model model) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid complaint Id: " + id));

        List<DigitalEvidence> evidenceList = evidenceRepository.findByComplaintIdOrderByLoggedAtDesc(id);
        List<FraudTransaction> transactionList = transactionRepository.findByComplaintIdOrderByTransactionTimeDesc(id);
        List<InvestigationLog> logs = logRepository.findByComplaintIdOrderByTimestampDesc(id);
        List<CorrelatedCaseDTO> correlations = intelligenceService.correlate(complaint.getSuspectIdentifier(), complaint.getIncidentDescription());

        model.addAttribute("complaint", complaint);
        model.addAttribute("evidenceList", evidenceList);
        model.addAttribute("transactionList", transactionList);
        model.addAttribute("logs", logs);
        model.addAttribute("correlations", correlations);
        model.addAttribute("newEvidence", new DigitalEvidence());
        model.addAttribute("newTransaction", new FraudTransaction());

        return "complaint-detail";
    }

    /**
     * Append Digital Evidence (Section 65B Certified)
     */
    @PostMapping("/complaints/{id}/evidence")
    public String addDigitalEvidence(@PathVariable("id") Long id,
                                     @RequestParam("evidenceType") String evidenceType,
                                     @RequestParam("referenceValue") String referenceValue,
                                     @RequestParam(value = "officerName", required = false) String officerName,
                                     RedirectAttributes redirectAttributes) {
        DigitalEvidence ev = workflowService.addEvidence(id, evidenceType, referenceValue, officerName);
        redirectAttributes.addFlashAttribute("successMessage",
                "Digital Evidence recorded under Section 65B with SHA-256 integrity hash: " + ev.getDigitalHash().substring(0, 16) + "...");
        return "redirect:/complaints/" + id;
    }

    /**
     * Log New Fraud Transaction
     */
    @PostMapping("/complaints/{id}/transactions")
    public String addFraudTransaction(@PathVariable("id") Long id,
                                      @RequestParam("transactionReference") String transactionReference,
                                      @RequestParam("amount") Double amount,
                                      @RequestParam("senderAccount") String senderAccount,
                                      @RequestParam("beneficiaryAccount") String beneficiaryAccount,
                                      @RequestParam("beneficiaryBankOrVpa") String beneficiaryBankOrVpa,
                                      RedirectAttributes redirectAttributes) {
        workflowService.addTransaction(id, transactionReference, amount, senderAccount, beneficiaryAccount, beneficiaryBankOrVpa);
        redirectAttributes.addFlashAttribute("successMessage",
                "Transaction " + transactionReference + " (₹" + amount + ") successfully flagged for financial surveillance.");
        return "redirect:/complaints/" + id;
    }

    /**
     * Simulate 1930 Bank Freeze Notice Action
     */
    @PostMapping("/transactions/{txnId}/freeze")
    public String simulateBankFreeze(@PathVariable("txnId") Long txnId,
                                     @RequestParam(value = "complaintId", required = false) Long complaintId,
                                     RedirectAttributes redirectAttributes) {
        FraudTransaction updated = workflowService.simulateBankFreeze(txnId);
        redirectAttributes.addFlashAttribute("successMessage",
                "Statutory 1930 Bank Freeze Notice dispatched for Ref: " + updated.getTransactionReference() + " to beneficiary " + updated.getBeneficiaryAccount());

        if (complaintId != null) {
            return "redirect:/complaints/" + complaintId;
        }
        return "redirect:/dashboard?tab=cases";
    }

    /**
     * Update Case Status & Officer Assignment
     */
    @PostMapping("/complaints/{id}/status")
    public String updateCaseStatus(@PathVariable("id") Long id,
                                   @RequestParam("status") String status,
                                   @RequestParam(value = "assignedOfficer", required = false) String assignedOfficer,
                                   @RequestParam(value = "remarks", required = false) String remarks,
                                   RedirectAttributes redirectAttributes) {
        workflowService.updateCaseStatus(id, status, assignedOfficer, remarks);
        redirectAttributes.addFlashAttribute("successMessage", "Case status progressed to " + status + ".");
        return "redirect:/complaints/" + id;
    }

    /**
     * Acknowledge Investigation Alert
     */
    @PostMapping("/alerts/{alertId}/acknowledge")
    public String acknowledgeAlert(@PathVariable("alertId") Long alertId, RedirectAttributes redirectAttributes) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setAcknowledged(true);
            alertRepository.save(alert);
        });
        redirectAttributes.addFlashAttribute("successMessage", "Escalation alert acknowledged by Investigating Officer.");
        return "redirect:/dashboard";
    }
}
