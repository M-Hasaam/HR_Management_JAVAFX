package com.hr.service;

// GoF Design Pattern: Factory Method — concrete factory for ComplianceReport.
//
// Justification: Compliance reports require specific default values (format = PDF,
// status = GENERATED, archivePath with retention prefix) that differ from
// analytics reports.  Encapsulating these defaults here means the ReportController
// never needs to know which concrete Report subtype to build — it delegates to
// this creator.

import com.hr.model.ComplianceReport;
import com.hr.model.Report;

import java.time.LocalDateTime;

public class ComplianceReportCreator extends ReportCreator {

    @Override
    public Report createReport(String period, int userId) {
        ComplianceReport report = new ComplianceReport();
        report.setGeneratedAt(LocalDateTime.now());
        report.setGeneratedBy(userId);
        report.setReportType("COMPLIANCE");
        report.setParameters(period);
        report.setFormat("PDF");
        report.setStatus("GENERATED");
        report.setArchivePath("/archive/compliance/" + period + "_" + System.currentTimeMillis());
        return report;
    }
}
