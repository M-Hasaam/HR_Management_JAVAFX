package com.hr.service;

// Non-Functional Requirement (NFR): Data Retention — compliance reports must be
// archived for a minimum of 5 years before deletion is permitted.
//
// Requirement: "Compliance reports archived ≥ 5 years."
// Implementation: archiveReport() stores a report entry with a retention expiry
// calculated as generatedAt + 5 years.  canDelete() enforces the policy by
// rejecting deletion requests for reports that have not yet reached their
// retention expiry date.
//
// GRASP Pattern: Pure Fabrication — archival policy is not a domain entity;
// this service exists to enforce the data-retention NFR.

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ReportArchive {

    /** Minimum years a report must be retained before deletion (NFR). */
    private static final int MIN_RETENTION_YEARS = 5;

    /** In-memory registry: reportId → retention expiry date. */
    private final Map<Integer, LocalDate> retentionRegistry = new HashMap<>();

    /**
     * Archives a report and registers its 5-year retention expiry.
     *
     * @param reportId  unique identifier of the report
     * @param creatorId ID of the user who generated the report
     * @param timestamp human-readable timestamp label for logging
     * @return archive confirmation message
     */
    public String archiveReport(int reportId, int creatorId, String timestamp) {
        LocalDate retentionExpiry = LocalDate.now().plusYears(MIN_RETENTION_YEARS);
        retentionRegistry.put(reportId, retentionExpiry);

        System.out.println("[REPORT ARCHIVE] Report " + reportId
                + " archived by user " + creatorId
                + " at " + timestamp
                + " | Retention expiry: " + retentionExpiry
                + " (NFR: " + MIN_RETENTION_YEARS + "-year minimum)");
        return "archived — retained until " + retentionExpiry;
    }

    /**
     * Checks whether a report has passed its minimum retention period.
     * Returns false (and logs a policy violation) if the report is still within
     * the 5-year mandatory retention window.
     *
     * @param reportId the report to check
     * @return true if the report may be deleted; false if it is still protected
     */
    public boolean canDelete(int reportId) {
        LocalDate expiry = retentionRegistry.get(reportId);
        if (expiry == null) {
            // Unknown report — allow deletion (not tracked)
            return true;
        }
        if (LocalDate.now().isBefore(expiry)) {
            System.err.println("[REPORT ARCHIVE] POLICY VIOLATION: Attempted to delete report "
                    + reportId + " before retention expiry " + expiry
                    + " (NFR: " + MIN_RETENTION_YEARS + "-year minimum).");
            return false;
        }
        return true;
    }

    /**
     * Returns the retention expiry date for a report, or null if not registered.
     */
    public LocalDate getRetentionExpiry(int reportId) {
        return retentionRegistry.get(reportId);
    }
}
