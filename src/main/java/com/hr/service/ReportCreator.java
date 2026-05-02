package com.hr.service;

// GoF Design Pattern: Factory Method.
//
// Justification: The system produces two distinct report types (ComplianceReport,
// HRAnalyticsReport) that share a common lifecycle: create → populate → persist.
// The Factory Method pattern defines the creation step as an abstract method
// (createReport) that each concrete subclass overrides.  The shared algorithm
// (generate, which calls createReport then saves) lives here in the superclass.
// Adding a third report type requires only a new subclass — no changes here.
//
// This also reinforces the Inheritance + Polymorphism OOP requirement:
// ReportCreator is the abstract superclass; concrete factories are subclasses.

import com.hr.model.Report;

public abstract class ReportCreator {

    /**
     * Factory Method — subclasses override this to instantiate the correct Report subtype.
     *
     * @param period  reporting period string (e.g. "2026-Q1")
     * @param userId  ID of the user requesting the report
     * @return a freshly constructed, unpersisted Report subtype
     */
    public abstract Report createReport(String period, int userId);

    /**
     * Template for the report generation workflow.
     * Calls the factory method, then logs and returns the result.
     *
     * @param period reporting period
     * @param userId requesting user's ID
     * @return the generated Report object (ready to be persisted by the caller)
     */
    public Report generate(String period, int userId) {
        Report report = createReport(period, userId);
        System.out.println("[FACTORY] Report created: " + report);
        return report;
    }
}
