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
     * Standalone Complaint Intake Form redirect
     */
    @InitBinder
    public void initBinder(org.springframework.web.bind.WebDataBinder binder) {
        binder.setDisallowedFields("evidenceFiles");
    }

    /**
     * Standalone Complaint Intake Form redirect
     */
    @GetMapping("/complaints/register")
    public String showRegistrationForm() {
        return "redirect:/intake";
    }

    /**
     * Form Submission: Intake Pipeline with Multi-Part File Upload
     */
    @PostMapping(value = "/complaints/register", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public String registerComplaint(@ModelAttribute Complaint complaint,
                                    @RequestParam(value = "evidenceFiles", required = false) org.springframework.web.multipart.MultipartFile[] files,
                                    RedirectAttributes redirectAttributes) {
        Complaint saved = workflowService.processNewComplaint(complaint);

        int uploadedCount = 0;
        if (files != null) {
            for (org.springframework.web.multipart.MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        workflowService.attachEvidenceFile(
                                saved,
                                file.getOriginalFilename(),
                                file.getContentType(),
                                file.getBytes(),
                                "Complaint Intake Desk"
                        );
                        uploadedCount++;
                    } catch (java.io.IOException e) {
                        // Log error and continue
                        System.err.println("Error processing uploaded evidence file: " + e.getMessage());
                    }
                }
            }
        }

        String msg = "Complaint filed successfully [" + saved.getComplaintNumber() + "] with AI Priority: " + saved.getAiPriority();
        if (uploadedCount > 0) {
            msg += " (" + uploadedCount + " digital evidence file" + (uploadedCount > 1 ? "s" : "") + " secured with Section 65B SHA-256 integrity hashes).";
        }
        redirectAttributes.addFlashAttribute("successMessage", msg);
        return "redirect:/complaints/" + saved.getId();
    }

    /**
     * File Download / Retrieval REST Endpoint
     */
    @GetMapping("/evidence/{evidenceId}/download")
    public org.springframework.http.ResponseEntity<byte[]> downloadEvidenceFile(@PathVariable("evidenceId") Long evidenceId) {
        DigitalEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence item not found: " + evidenceId));

        if (evidence.getFileData() == null || evidence.getFileData().length == 0) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }

        String fileName = evidence.getFileName() != null ? evidence.getFileName() : "evidence-" + evidence.getId() + ".bin";
        String contentType = evidence.getFileType() != null ? evidence.getFileType() : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                .header(org.springframework.http.HttpHeaders.CONTENT_LENGTH, String.valueOf(evidence.getFileData().length))
                .body(evidence.getFileData());
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
        return "redirect:/ledger";
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
        return "redirect:/ledger";
    }
}
