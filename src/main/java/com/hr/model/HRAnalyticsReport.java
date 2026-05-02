package com.hr.model;

// OOP Principle: Inheritance — HRAnalyticsReport extends the abstract Report base class.
// Polymorphism — overrides getReportType(), getSummary(), getFormat() enabling
// callers to handle both report types through the common Report supertype.

import java.time.LocalDateTime;

public class HRAnalyticsReport extends Report {

    private int    creatorId;
    private String reportPeriod;
    private String summaryMetrics;

    public HRAnalyticsReport() {}

    public HRAnalyticsReport(int id, int creatorId, String reportPeriod,
                             LocalDateTime generatedAt, String summaryMetrics) {
        super(id, generatedAt, creatorId);
        this.creatorId      = creatorId;
        this.reportPeriod   = reportPeriod;
        this.summaryMetrics = summaryMetrics;
    }

    // ── Abstract method overrides (Polymorphism) ─────────────────────────────

    @Override
    public String getReportType() { return "HR_ANALYTICS"; }

    @Override
    public String getSummary() {
        return "HR Analytics report for period: " + reportPeriod
                + " | Metrics: " + summaryMetrics;
    }

    @Override
    public String getFormat() { return "EXCEL"; }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
        this.generatedBy = creatorId; // keep superclass field in sync
    }

    public String getReportPeriod() { return reportPeriod; }
    public void setReportPeriod(String reportPeriod) { this.reportPeriod = reportPeriod; }

    public String getSummaryMetrics() { return summaryMetrics; }
    public void setSummaryMetrics(String summaryMetrics) { this.summaryMetrics = summaryMetrics; }
}
