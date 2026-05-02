package com.hr.service;

// GoF Design Pattern: Adapter — converts ExternalPayrollSystem's API into
// the PayrollIntegration interface expected by HR business logic classes.
//
// Justification: The external payroll engine uses employee reference strings and
// legacy method names incompatible with the HR domain model.  This adapter
// translates HR-domain calls (employeeId, designation, salary) into legacy-system
// calls (empRef string, amountPKR, sessionId) without modifying either side.
// If the payroll vendor changes their API, only this class needs updating.
//
// This is a classic Object Adapter: the adaptee is held as a field (composition),
// not inherited, to avoid tight coupling to ExternalPayrollSystem's class hierarchy.

public class PayrollSystemAdapter implements PayrollIntegration {

    private final ExternalPayrollSystem externalSystem;

    public PayrollSystemAdapter(ExternalPayrollSystem externalSystem) {
        this.externalSystem = externalSystem;
    }

    @Override
    public void queueSalaryUpdate(int employeeId, String designation, double salary) {
        // Translate: HR int ID → legacy "EMP-{id}" reference string
        externalSystem.submitSalaryChange("EMP-" + employeeId, designation, salary);
    }

    @Override
    public void recordHours(int attendanceId, double hoursWorked) {
        // Translate: HR int attendanceId → legacy session ID string
        externalSystem.logAttendanceHours("ATT-" + attendanceId, hoursWorked);
    }

    @Override
    public void initiateSettlement(int employeeId) {
        // Translate: HR int ID → legacy reference string
        externalSystem.processFinalPayout("EMP-" + employeeId);
    }
}
