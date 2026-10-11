package com.lawenforcement.intelligence.config;

import com.lawenforcement.intelligence.model.HistoricalIncident;
import com.lawenforcement.intelligence.repository.HistoricalIncidentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HistoricalDataSeeder {

    @Bean
    CommandLineRunner seedDatabase(HistoricalIncidentRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(HistoricalIncident.builder()
                        .caseId("CASE-2026-REFUND-01")
                        .suspectIdentifier("refund-desk@ybl")
                        .category("ONLINE_FINANCIAL_FRAUD")
                        .priorityLevel("CRITICAL")
                        .lossAmount(650000.0)
                        .scamNarrative("Fake electricity disconnection alert followed by remote APK installation and unauthorized transfer")
                        .build());

                repo.save(HistoricalIncident.builder()
                        .caseId("CASE-2026-KYC-02")
                        .suspectIdentifier("support-pay@icici")
                        .category("PHISHING_ATTACK")
                        .priorityLevel("HIGH")
                        .lossAmount(120000.0)
                        .scamNarrative("Identity verification alert leading to cloned netbanking login portal")
                        .build());

                repo.save(HistoricalIncident.builder()
                        .caseId("CASE-2026-TEL-03")
                        .suspectIdentifier("9876543210")
                        .category("ONLINE_FINANCIAL_FRAUD")
                        .priorityLevel("CRITICAL")
                        .lossAmount(950000.0)
                        .scamNarrative("Telegram prepaid task scam demanding crypto deposits with guaranteed 300% returns")
                        .build());
            }
        };
    }
}