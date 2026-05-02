package com.hr.model;

// OOP Principle: Inheritance + Abstraction + Polymorphism.
// Abstract base class for all report types.  ComplianceReport and HRAnalyticsReport
// extend this class and override its abstract methods, giving callers a single
// polymorphic type to work with regardless of the concrete report kind.
//
// Justification: Both report types share generated-at metadata and a common
// lifecycle (generate → format → archive).  Extracting shared state and
// behaviour here avoids duplication and enables Factory Method and Strategy
// patterns to operate on the supertype.

import java.time.LocalDateTime;

public abstract class Report {

    protected int           id;
    protected LocalDateTime generatedAt;
    protected int           generatedBy;

    public Report() {}

    public Report(int id, LocalDateTime generatedAt, int generatedBy) {
        this.id          = id;
        this.generatedAt = generatedAt;
        this.generatedBy = generatedBy;
    }

    // ── Shared accessors ─────────────────────────────────────────────────────

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public LocalDateTime getGeneratedAt()       { return generatedAt; }
    public void setGeneratedAt(LocalDateTime t) { this.generatedAt = t; }

    public int getGeneratedBy()                 { return generatedBy; }
    public void setGeneratedBy(int userId)      { this.generatedBy = userId; }

    // ── Abstract contract (Polymorphism) ─────────────────────────────────────

    /** Domain-specific report type label (e.g. "COMPLIANCE", "HR_ANALYTICS"). */
    public abstract String getReportType();

    /** One-line human-readable summary of this report's content. */
    public abstract String getSummary();

    /** Output format identifier used by ReportFormatStrategy (e.g. "PDF", "EXCEL"). */
    public abstract String getFormat();

    // ── Shared behaviour ─────────────────────────────────────────────────────

    @Override
    public String toString() {
        return getReportType() + " [id=" + id + ", generatedAt=" + generatedAt + "]";
    }
}
