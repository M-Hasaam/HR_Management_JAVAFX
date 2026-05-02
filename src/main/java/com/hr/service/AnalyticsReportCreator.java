package com.hr.service;

// GoF Design Pattern: Factory Method — concrete factory for HRAnalyticsReport.
//
// Justification: Analytics reports require different defaults (summaryMetrics
// initialised to the requested KPI list, no archivePath, Excel format for
// data analysis) compared to compliance reports.  This subclass encapsulates
// those construction details so callers work with the abstract ReportCreator type.

import com.hr.model.HRAnalyticsReport;
import com.hr.model.Report;

import java.time.LocalDateTime;

public class AnalyticsReportCreator extends ReportCreator {

    @Override
    public Report createReport(String period, int userId) {
        HRAnalyticsReport report = new HRAnalyticsReport();
        report.setGeneratedAt(LocalDateTime.now());
        report.setGeneratedBy(userId);
        report.setReportPeriod(period);
        report.setSummaryMetrics("headcount,attrition,leaveUtilization,performanceBenchmarks");
        return report;
    }
}
