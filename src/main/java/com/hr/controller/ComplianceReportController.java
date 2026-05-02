package com.hr.controller;
// GRASP Pattern: Controller — handles UC-14 Generate Compliance Report.
// Participants: ComplianceReport (Creator, via Factory Method),
//              ComplianceDataAgg (Pure Fabrication), ReportGenerator (Pure Fabrication + Strategy),
//              SecureStorage (Pure Fabrication), ReportArchive (Pure Fabrication, NFR enforcement)
//
// Design Patterns used:
// - Factory Method: ComplianceReportCreator.createReport() builds the ComplianceReport instance.
// - Strategy: ReportGenerator selects PDF/Excel/CSV strategy at runtime.
// - NFR-2 (Data Retention): ReportArchive enforces 5-year retention policy on generated reports.

import com.hr.dao.ComplianceReportDAO;
import com.hr.model.ComplianceReport;
import com.hr.model.Report;
import com.hr.service.ComplianceDataAgg;
import com.hr.service.ComplianceReportCreator;
import com.hr.service.ReportArchive;
import com.hr.service.ReportCreator;
import com.hr.service.ReportGenerator;
import com.hr.service.SecureStorage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ComplianceReportController {

    private final ComplianceDataAgg   complianceDataAgg;
    private final ReportGenerator     reportGenerator;  // Pure Fabrication + Strategy
    private final SecureStorage       secureStorage;    // Pure Fabrication
    private final ReportArchive       reportArchive;    // NFR-2: 5-year retention
    private final ComplianceReportDAO reportDAO;
    private final ReportCreator       reportCreator;    // Factory Method

    public ComplianceReportController() throws SQLException {
        this.complianceDataAgg = new ComplianceDataAgg();
        this.reportGenerator   = new ReportGenerator();
        this.secureStorage     = new SecureStorage();
        this.reportArchive     = new ReportArchive();
        this.reportDAO         = new ComplianceReportDAO();
        this.reportCreator     = new ComplianceReportCreator(); // Factory Method
    }

    /**
     * UC-14: Generate and archive a compliance report.
     *
     * Workflow (matches SD-14):
     * 1. Aggregate policy adherence data via ComplianceDataAgg.
     * 2. Create ComplianceReport via Factory Method (ComplianceReportCreator).
     * 3. Format via Strategy (ReportGenerator selects PDF/Excel/CSV strategy).
     * 4. Store securely via SecureStorage.
     * 5. Register with ReportArchive (NFR: 5-year retention).
     * 6. Persist to database.
     *
     * @param domain  compliance domain (LEAVE, ATTENDANCE, ALL)
     * @param period  reporting period (e.g. "2026-Q1")
     * @param format  output format (PDF, EXCEL, CSV)
     * @param userId  ID of the requesting admin
     * @return the persisted ComplianceReport object
     * @throws SQLException on database error
     */
    public ComplianceReport generateComplianceReport(String domain, String period,
                                                     String format, int userId)
            throws SQLException {
        // Step 1: Aggregate data
        String aggregatedData = complianceDataAgg.aggregateComplianceData(domain, period);

        // Step 2: Factory Method creates the report instance
        Report baseReport = reportCreator.generate(period, userId);
        ComplianceReport report = (ComplianceReport) baseReport;
        report.setReportType(domain + "_COMPLIANCE");
        report.setFormat(format);
        report.setParameters(period);
        report.setGeneratedAt(LocalDateTime.now());
        report.setGeneratedBy(userId);

        // Step 3: Format via Strategy
        String formattedContent = reportGenerator.generateAndFormat(aggregatedData, format);
        System.out.println("[COMPLIANCE REPORT] Formatted output: " + formattedContent);

        // Step 4: Secure archival
        String archivePath = secureStorage.archiveReport(formattedContent, format);
        report.setArchivePath(archivePath);
        report.setStatus("GENERATED");

        // Step 5: Register for 5-year retention (NFR-2)
        String retentionConfirmation = reportArchive.archiveReport(report.getId(), userId, period);
        System.out.println("[COMPLIANCE REPORT] " + retentionConfirmation);

        // Step 6: Persist
        reportDAO.insert(report);

        System.out.println("[COMPLIANCE REPORT] UC-14 complete: " + report.getSummary());
        return report;
    }

    /** Returns all previously generated compliance reports. */
    public List<ComplianceReport> getAllReports() throws SQLException {
        return reportDAO.getAll();
    }
}
