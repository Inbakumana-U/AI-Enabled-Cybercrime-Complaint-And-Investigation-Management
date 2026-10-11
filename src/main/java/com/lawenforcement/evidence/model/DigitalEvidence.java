package com.lawenforcement.evidence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lawenforcement.caseapp.model.Complaint;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "digital_evidence")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id", insertable = false, updatable = false)
    private Long complaintId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id")
    @JsonIgnore
    private Complaint complaint;

    // File Metadata fields
    @Column(length = 255)
    private String fileName;

    @Column(length = 150)
    private String fileType; // MIME type

    private Long fileSize; // in bytes

    @Column(length = 64)
    private String sha256Hash; // Cryptographic SHA-256 over raw file bytes

    @Column(name = "file_data", columnDefinition = "bytea")
    @org.hibernate.annotations.JdbcType(org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType.class)
    private byte[] fileData; // Stored directly in PostgreSQL bytea

    // Existing evidentiary metadata & backwards-compatibility fields
    @Column(length = 50)
    private String evidenceType; // EVIDENTIARY_DOCUMENT, TRANSACTION_UTR, PHISHING_URL, SUSPECT_PHONE, CRYPTO_WALLET, MALICIOUS_APK_HASH

    @Column(length = 500)
    private String referenceValue;

    @Column(length = 100)
    private String digitalHash; // Section 65B hash

    @Column(length = 100)
    private String chainOfCustodyOfficer;

    @Column(nullable = false)
    private LocalDateTime loggedAt;

    @PrePersist
    protected void onCreate() {
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now();
        }
        if (digitalHash == null && sha256Hash != null) {
            digitalHash = sha256Hash;
        } else if (sha256Hash == null && digitalHash != null) {
            sha256Hash = digitalHash;
        }
    }

    public boolean isBinaryFile() {
        return fileData != null && fileData.length > 0;
    }

    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "0 B";
        }
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }
}
