package com.hr.service;

// GRASP Pattern: Pure Fabrication — cross-system notification to the payroll module.
// GoF Pattern: Adapter — communicates with ExternalPayrollSystem via
//   PayrollSystemAdapter, translating HR-domain calls into legacy API calls.
//
// Justification for Adapter here: the external payroll engine uses employee
// reference strings and method names incompatible with HR domain model.
// Routing through the adapter means this class (and all callers) remain
// insulated from any future change to the external payroll vendor's API.

public class PayrollNotifier {

    // The adapter bridges HR calls → ExternalPayrollSystem API (GoF: Adapter pattern)
    private final PayrollIntegration payrollIntegration;

    public PayrollNotifier() {
        // Wire up: Adapter wraps the external system
        this.payrollIntegration = new PayrollSystemAdapter(new ExternalPayrollSystem());
    }

    /** Constructor for injecting a custom PayrollIntegration (e.g. for testing). */
    public PayrollNotifier(PayrollIntegration payrollIntegration) {
        this.payrollIntegration = payrollIntegration;
    }

    /**
     * Queues a payroll update when an employee's designation or salary changes.
     *
     * @param employeeId  ID of the affected employee
     * @param designation New or current designation title
     * @param salary      New or current basic salary
     * @return "notified"
     */
    public String notifyPayroll(int employeeId, String designation, double salary) {
        payrollIntegration.queueSalaryUpdate(employeeId, designation, salary);
        return "notified";
    }

    /**
     * Records confirmed attendance hours so payroll can calculate variable pay.
     *
     * @param attendanceId ID of the finalised attendance record
     * @param totalHours   Total hours worked in the period
     * @return "recorded"
     */
    public String notifyPayrollAttendance(int attendanceId, double totalHours) {
        payrollIntegration.recordHours(attendanceId, totalHours);
        return "recorded";
    }

    /**
     * Triggers the final-settlement process when an employee is terminated.
     *
     * @param employeeId ID of the departing employee
     * @return "settlementQueued"
     */
    public String triggerFinalSettlement(int employeeId) {
        payrollIntegration.initiateSettlement(employeeId);
        return "settlementQueued";
    }
}
