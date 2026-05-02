package com.hr.controller;
// GRASP Pattern: Controller — handles UC-15 Generate HR Analytics Report.
// Participants: HRAnalyticsReport (Creator, via Factory Method),
//              AnalyticsAggregator (Pure Fabrication), DataVisualization (Pure Fabrication + Strategy),
//              ReportArchive (Pure Fabrication, NFR-2 enforcement)
//
// Design Patterns used:
// - Factory Method: AnalyticsReportCreator.createReport() builds the HRAnalyticsReport instance.
// - Strategy: DataVisualization selects chart type at runtime (bar, pie, line).
// - NFR-2 (Data Retention): ReportArchive enforces 5-year retention policy.

import com.hr.dao.HRAnalyticsReportDAO;
import com.hr.model.HRAnalyticsReport;
import com.hr.model.Report;
import com.hr.service.AnalyticsAggregator;
import com.hr.service.AnalyticsReportCreator;
import com.hr.service.DataVisualization;
import com.hr.service.ReportArchive;
import com.hr.service.ReportCreator;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class HRAnalyticsController {

    private final AnalyticsAggregator  analyticsAggregator;
    private final DataVisualization    dataVisualization;
    private final ReportArchive        reportArchive;    // NFR-2: 5-year retention
    private final HRAnalyticsReportDAO reportDAO;
    private final ReportCreator        reportCreator;    // Factory Method

    public HRAnalyticsController() throws SQLException {
        this.analyticsAggregator = new AnalyticsAggregator();
        this.dataVisualization   = new DataVisualization();
        this.reportArchive       = new ReportArchive();
        this.reportDAO           = new HRAnalyticsReportDAO();
        this.reportCreator       = new AnalyticsReportCreator(); // Factory Method
    }

    /**
     * UC-15: Generate an HR analytics report for the requested KPI metrics.
     *
     * Workflow (matches SD-15):
     * 1. Validate metric availability via AnalyticsAggregator.
     * 2. Aggregate KPI data.
     * 3. Create HRAnalyticsReport via Factory Method (AnalyticsReportCreator).
     * 4. Generate charts via DataVisualization (Strategy pattern).
     * 5. Register with ReportArchive (NFR: 5-year retention).
     * 6. Persist to database.
     *
     * @param headcount  include headcount KPI
     * @param attrition  include attrition KPI
     * @param leave      include leave utilisation KPI
     * @param attendance include attendance KPI
     * @param period     reporting period (e.g. "2026-Q1")
     * @param userId     ID of the requesting admin
     * @return formatted report string for display
     * @throws IllegalArgumentException if requested metrics are unavailable
     * @throws SQLException             on database error
     */
    public String generateAnalyticsReport(boolean headcount, boolean attrition,
                                          boolean leave, boolean attendance,
                                          String period, int userId) throws SQLException {
        // Step 1–2: Aggregate KPI data
        Map<String, Object> kpiData = analyticsAggregator.aggregateKPI(
                headcount, attrition, leave, attendance);

        // Step 3: Factory Method creates the report
        Report baseReport = reportCreator.generate(period, userId);
        HRAnalyticsReport report = (HRAnalyticsReport) baseReport;
        report.setReportPeriod(period);
        report.setGeneratedAt(LocalDateTime.now());
        report.setCreatorId(userId);
        report.setSummaryMetrics(kpiData.toString());

        // Step 4: Generate chart visualisations
        String chartOutput = dataVisualization.generateCharts(kpiData);

        // Step 5: Register for 5-year retention (NFR-2)
        reportArchive.archiveReport(report.getId(), userId, period);

        // Step 6: Persist
        reportDAO.insert(report);

        // Build human-readable output
        StringBuilder sb = new StringBuilder();
        sb.append("=== HR Analytics Report ===\n");
        sb.append("Period  : ").append(period).append("\n\n");
        kpiData.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
        sb.append("\nVisualization:\n").append(chartOutput);

        System.out.println("[ANALYTICS] UC-15 complete: " + report.getSummary());
        return sb.toString();
    }

    /** Validates that all requested metrics are available in the database. */
    public List<String> validateMetricsAvailability(List<String> metrics) throws SQLException {
        return analyticsAggregator.validateMetricsAvailability(metrics);
    }
}
