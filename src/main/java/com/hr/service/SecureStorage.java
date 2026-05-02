package com.hr.service;

// GRASP Pattern: Pure Fabrication — enforces retention policy for compliance reports.
// Secure storage of reports (encryption, access control, retention periods) is a
// cross-cutting infrastructure concern that does not belong to any domain class.
// Centralising it here satisfies high cohesion and makes the policy easy to change.

import java.time.LocalDate;

public class SecureStorage {

    /**
     * Archives a compliance report under a retention policy.
     *
     * In production this method would write the report content to an encrypted file
     * store, attach metadata, and register the retention expiry date so the
     * report is automatically purged when the policy period ends.
     *
     * @param reportContent   The serialised report content to archive
     * @param retentionPolicy Policy identifier (e.g. "7-YEARS", "3-YEARS", "PERMANENT")
     * @return The storage path where the report was archived
     */
    public String archiveReport(String reportContent, String retentionPolicy) {
        String path = "reports/compliance/" + LocalDate.now() + "_report.txt";
        System.out.println("[SECURE STORAGE] Report archived at: " + path
                + " with policy: " + retentionPolicy);
        return path;
    }
}
