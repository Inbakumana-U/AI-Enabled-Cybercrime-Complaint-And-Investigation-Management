package com.lawenforcement.caseapp.service;

import com.lawenforcement.alert.model.InvestigationAlert;
import com.lawenforcement.alert.repository.InvestigationAlertRepository;
import com.lawenforcement.caseapp.model.Complaint;
import com.lawenforcement.caseapp.model.InvestigationLog;
import com.lawenforcement.caseapp.repository.ComplaintRepository;
import com.lawenforcement.caseapp.repository.InvestigationLogRepository;
import com.lawenforcement.evidence.model.DigitalEvidence;
import com.lawenforcement.evidence.repository.DigitalEvidenceRepository;
import com.lawenforcement.fraud.model.FraudTransaction;
import com.lawenforcement.fraud.repository.FraudTransactionRepository;
import com.lawenforcement.intelligence.dto.AnalysisRequest;
import com.lawenforcement.intelligence.dto.AnalysisResponse;
import com.lawenforcement.intelligence.dto.CorrelatedCaseDTO;
import com.lawenforcement.intelligence.service.CyberIntelligenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvestigationWorkflowService {

    private final ComplaintRepository complaintRepository;
    private final DigitalEvidenceRepository evidenceRepository;
    private final FraudTransactionRepository transactionRepository;
    private final InvestigationAlertRepository alertRepository;
    private final InvestigationLogRepository logRepository;
    private final CyberIntelligenceService intelligenceService;

    public InvestigationWorkflowService(ComplaintRepository complaintRepository,
                                        DigitalEvidenceRepository evidenceRepository,
                                        FraudTransactionRepository transactionRepository,
                                        InvestigationAlertRepository alertRepository,
                                        InvestigationLogRepository logRepository,
                                        CyberIntelligenceService intelligenceService) {
        this.complaintRepository = complaintRepository;
        this.evidenceRepository = evidenceRepository;
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
        this.logRepository = logRepository;
        this.intelligenceService = intelligenceService;
    }

    /**
     * Intake processing pipeline: AI Triage, Entity Persistence, Automated Alerts & Seed Evidence.
     */
    @Transactional
    public Complaint processNewComplaint(Complaint complaint) {
        complaint.setComplaintNumber(generateComplaintNumber());
        complaint.setStatus("NEW");
        complaint.setRegisteredAt(LocalDateTime.now());

        // Run AI Analysis
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

        // Alert Rule 1: High Financial Loss (> ₹5,00,000)
        if (saved.getLossAmount() != null && saved.getLossAmount() > 500000.0) {
            alertRepository.save(InvestigationAlert.builder()
                    .complaintId(saved.getId())
                    .complaintNumber(saved.getComplaintNumber())
                    .alertLevel("CRITICAL_RED")
                    .alertMessage("High-Value Financial Exfiltration (₹" + String.format("%,.2f", saved.getLossAmount()) + ") - Immediate 1930 Lien / Bank Freeze Required.")
                    .acknowledged(false)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        // Alert Rule 2: Suspect Identifier matches historical syndicates
        List<CorrelatedCaseDTO> correlations = intelligenceService.correlate(saved.getSuspectIdentifier(), saved.getIncidentDescription());
        if (!correlations.isEmpty()) {
            alertRepository.save(InvestigationAlert.builder()
                    .complaintId(saved.getId())
                    .complaintNumber(saved.getComplaintNumber())
                    .alertLevel("WARNING_AMBER")
                    .alertMessage("Recurring Fraud Syndicate Entity: Suspect identifier matched " + correlations.get(0).getHistoricalCaseId() + " (" + correlations.get(0).getSimilarityPercentage() + "% match).")
                    .acknowledged(false)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        // Alert Rule 3: Case registered in NEW status without investigator assignment
        alertRepository.save(InvestigationAlert.builder()
                .complaintId(saved.getId())
                .complaintNumber(saved.getComplaintNumber())
                .alertLevel("INFO_BLUE")
                .alertMessage("Case registered in NEW status awaiting investigating officer assignment.")
                .acknowledged(false)
                .createdAt(LocalDateTime.now())
                .build());

        // Automatically create initial digital evidence token if suspect handle exists
        if (saved.getSuspectIdentifier() != null && !saved.getSuspectIdentifier().isBlank()) {
            String evType = saved.getSuspectIdentifier().contains("@") ? "TRANSACTION_UTR" :
                    (saved.getSuspectIdentifier().startsWith("http") ? "PHISHING_URL" : "SUSPECT_PHONE");

            DigitalEvidence initialEv = DigitalEvidence.builder()
                    .complaint(saved)
                    .complaintId(saved.getId())
                    .evidenceType(evType)
                    .referenceValue(saved.getSuspectIdentifier())
                    .digitalHash(generateSha256(saved.getSuspectIdentifier()))
                    .sha256Hash(generateSha256(saved.getSuspectIdentifier()))
                    .chainOfCustodyOfficer("System Ingestion Engine")
                    .loggedAt(LocalDateTime.now())
                    .build();
            evidenceRepository.save(initialEv);

            // Create initial transaction entry
            String velocity = (saved.getLossAmount() != null && saved.getLossAmount() > 500000.0)
                    ? "HIGH_VELOCITY_MULE_ACCOUNT" : "STANDARD";

            transactionRepository.save(FraudTransaction.builder()
                    .complaintId(saved.getId())
                    .transactionReference("TXN-" + saved.getComplaintNumber() + "-01")
                    .amount(saved.getLossAmount() != null ? saved.getLossAmount() : 0.0)
                    .senderAccount("Victim Account (" + saved.getContactPhone() + ")")
                    .beneficiaryAccount(saved.getSuspectIdentifier())
                    .beneficiaryBankOrVpa(saved.getSuspectIdentifier())
                    .transactionStatus("FLAGGED")
                    .velocityTag(velocity)
                    .transactionTime(LocalDateTime.now())
                    .build());
        }

        // Initial Audit Log
        logRepository.save(InvestigationLog.builder()
                .complaintId(saved.getId())
                .officerName("AI Triage Dispatch")
                .actionTaken("Case Ingested & Evaluated")
                .notes("AI Classification: " + saved.getAiCategory() + " | Priority: " + saved.getAiPriority() + " | Factors: " + saved.getAiKeyFactors())
                .timestamp(LocalDateTime.now())
                .build());

        return saved;
    }

    /**
     * Appends digital evidence with Section 65B SHA-256 integrity hash and audit log.
     */
    @Transactional
    public DigitalEvidence addEvidence(Long complaintId, String evidenceType, String referenceValue, String officerName) {
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        String hash = generateSha256(referenceValue != null ? referenceValue : "");
        DigitalEvidence evidence = DigitalEvidence.builder()
                .complaint(complaint)
                .complaintId(complaintId)
                .evidenceType(evidenceType)
                .referenceValue(referenceValue)
                .digitalHash(hash)
                .sha256Hash(hash)
                .chainOfCustodyOfficer(officerName != null && !officerName.isBlank() ? officerName : "Investigating Officer")
                .loggedAt(LocalDateTime.now())
                .build();
        DigitalEvidence saved = evidenceRepository.save(evidence);

        logRepository.save(InvestigationLog.builder()
                .complaintId(complaintId)
                .officerName(evidence.getChainOfCustodyOfficer())
                .actionTaken("Digital Evidence Attached")
                .notes("Evidence Type: " + evidenceType + " | Ref: " + referenceValue + " | SHA-256: " + hash)
                .timestamp(LocalDateTime.now())
                .build());

        return saved;
    }

    /**
     * Records a new transaction and detects high-velocity mule account patterns.
     */
    @Transactional
    public FraudTransaction addTransaction(Long complaintId, String transactionReference, Double amount,
                                           String senderAccount, String beneficiaryAccount, String beneficiaryBankOrVpa) {
        String velocityTag = "STANDARD";
        if (amount != null && amount > 500000.0) {
            velocityTag = "HIGH_VELOCITY_MULE_ACCOUNT";
        }

        FraudTransaction txn = FraudTransaction.builder()
                .complaintId(complaintId)
                .transactionReference(transactionReference)
                .amount(amount != null ? amount : 0.0)
                .senderAccount(senderAccount)
                .beneficiaryAccount(beneficiaryAccount)
                .beneficiaryBankOrVpa(beneficiaryBankOrVpa)
                .transactionStatus("FLAGGED")
                .velocityTag(velocityTag)
                .transactionTime(LocalDateTime.now())
                .build();
        FraudTransaction saved = transactionRepository.save(txn);

        logRepository.save(InvestigationLog.builder()
                .complaintId(complaintId)
                .officerName("1930 Financial Desk")
                .actionTaken("Transaction Logged")
                .notes("Ref: " + transactionReference + " | Amount: ₹" + amount + " | Beneficiary: " + beneficiaryAccount)
                .timestamp(LocalDateTime.now())
                .build());

        return saved;
    }

    /**
     * Simulates 1930 Bank Freeze Notice.
     */
    @Transactional
    public FraudTransaction simulateBankFreeze(Long transactionId) {
        FraudTransaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid transaction ID: " + transactionId));

        txn.setTransactionStatus("FREEZE_REQUESTED");
        FraudTransaction updated = transactionRepository.save(txn);

        logRepository.save(InvestigationLog.builder()
                .complaintId(txn.getComplaintId())
                .officerName("1930 Nodal Liaison Officer")
                .actionTaken("1930 Bank Freeze Notice Dispatched")
                .notes("Issued statutory lien request for Txn Ref: " + txn.getTransactionReference()
                        + " against beneficiary " + txn.getBeneficiaryAccount() + " for ₹" + txn.getAmount())
                .timestamp(LocalDateTime.now())
                .build());

        return updated;
    }

    /**
     * Updates complaint lifecycle state and officer assignment with remarks log.
     */
    @Transactional
    public Complaint updateCaseStatus(Long complaintId, String status, String assignedOfficer, String remarks) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid complaint Id: " + complaintId));

        String oldStatus = complaint.getStatus();
        complaint.setStatus(status);
        if (assignedOfficer != null && !assignedOfficer.isBlank()) {
            complaint.setAssignedOfficer(assignedOfficer);
        }
        Complaint updated = complaintRepository.save(complaint);

        logRepository.save(InvestigationLog.builder()
                .complaintId(complaintId)
                .officerName(complaint.getAssignedOfficer() != null ? complaint.getAssignedOfficer() : "Investigating Officer")
                .actionTaken("Status Transition: " + oldStatus + " -> " + status)
                .notes(remarks != null && !remarks.isBlank() ? remarks : "Routine status update recorded.")
                .timestamp(LocalDateTime.now())
                .build());

        return updated;
    }

    public String generateSha256(String input) {
        if (input == null) return generateSha256(new byte[0]);
        return generateSha256(input.getBytes(StandardCharsets.UTF_8));
    }

    public String generateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(data != null ? data : new byte[0]);
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "HASH-" + (data != null ? Math.abs(java.util.Arrays.hashCode(data)) : 0);
        }
    }

    /**
     * Attaches an uploaded multi-part binary file directly to a complaint and stores in PostgreSQL.
     */
    @Transactional
    public DigitalEvidence attachEvidenceFile(Complaint complaint, String originalFilename, String contentType, byte[] fileBytes, String officerName) {
        String hash = generateSha256(fileBytes);
        DigitalEvidence evidence = DigitalEvidence.builder()
                .complaint(complaint)
                .complaintId(complaint.getId())
                .fileName(originalFilename != null ? originalFilename : "artifact.bin")
                .fileType(contentType != null ? contentType : "application/octet-stream")
                .fileSize(fileBytes != null ? (long) fileBytes.length : 0L)
                .fileData(fileBytes)
                .sha256Hash(hash)
                .digitalHash(hash)
                .evidenceType("EVIDENTIARY_DOCUMENT")
                .referenceValue(originalFilename != null ? originalFilename : "BINARY_ARTIFACT")
                .chainOfCustodyOfficer(officerName != null && !officerName.isBlank() ? officerName : "Complaint Registration Intake")
                .loggedAt(LocalDateTime.now())
                .build();

        complaint.addEvidenceFile(evidence);
        DigitalEvidence saved = evidenceRepository.save(evidence);

        logRepository.save(InvestigationLog.builder()
                .complaintId(complaint.getId())
                .officerName(evidence.getChainOfCustodyOfficer())
                .actionTaken("Binary Evidence File Uploaded")
                .notes("File: " + evidence.getFileName() + " (" + evidence.getFormattedFileSize() + ") | Type: " + evidence.getFileType() + " | SHA-256: " + hash)
                .timestamp(LocalDateTime.now())
                .build());

        return saved;
    }

    public List<InvestigationAlert> getUnacknowledgedAlerts() {
        return alertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc();
    }

    public CyberIntelligenceService getCyberIntelligenceService() {
        return intelligenceService;
    }

    private synchronized String generateComplaintNumber() {
        return "CC-2024-" + (1000 + (int)(Math.random() * 9000));
    }
}
