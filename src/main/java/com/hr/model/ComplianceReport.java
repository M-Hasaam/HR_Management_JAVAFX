package com.hr.model;

// OOP Principle: Inheritance — ComplianceReport extends the abstract Report base class.
// Polymorphism — overrides getReportType(), getSummary(), getFormat() so callers
// can handle ComplianceReport and HRAnalyticsReport uniformly via the Report type.

import java.time.LocalDateTime;

public class ComplianceReport extends Report {

    private String reportType;
    private String parameters;
    private String format;
    private String status;
    private String archivePath;

    public ComplianceReport() {}

    public ComplianceReport(int id, String reportType, LocalDateTime generatedAt, int generatedBy,
                            String parameters, String format, String status, String archivePath) {
        super(id, generatedAt, generatedBy);
        this.reportType  = reportType;
        this.parameters  = parameters;
        this.format      = format;
        this.status      = status;
        this.archivePath = archivePath;
    }

    // ── Abstract method overrides (Polymorphism) ─────────────────────────────

    @Override
    public String getReportType() { return reportType != null ? reportType : "COMPLIANCE"; }

    @Override
    public String getSummary() {
        return "Compliance report [" + reportType + "] for period: " + parameters
                + " | Status: " + status;
    }

    @Override
    public String getFormat() { return format != null ? format : "PDF"; }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public void setFormat(String format) { this.format = format; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getArchivePath() { return archivePath; }
    public void setArchivePath(String archivePath) { this.archivePath = archivePath; }
}
