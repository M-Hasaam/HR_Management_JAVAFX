package com.hr.service;

// GoF Design Pattern: Adapter — Adaptee (the incompatible external system).
//
// Justification: This class simulates the legacy/third-party payroll engine whose
// method names (submitSalaryChange, logAttendanceHours, processFinalPayout) differ
// from the HR system's target interface (PayrollIntegration).  The Adapter pattern
// wraps this class so HR code never calls these mismatched methods directly.

public class ExternalPayrollSystem {

    /** Legacy API: submit a salary change event with amount in PKR. */
    public void submitSalaryChange(String empRef, String role, double amountPKR) {
        System.out.println("[EXTERNAL PAYROLL] Salary change submitted: "
                + empRef + " | Role: " + role + " | Amount: PKR " + amountPKR);
    }

    /** Legacy API: log attendance hours against an attendance session ID. */
    public void logAttendanceHours(String sessionId, double hours) {
        System.out.println("[EXTERNAL PAYROLL] Attendance hours logged: "
                + "Session " + sessionId + " | Hours: " + hours);
    }

    /** Legacy API: trigger the final payout workflow for a given employee reference. */
    public void processFinalPayout(String empRef) {
        System.out.println("[EXTERNAL PAYROLL] Final payout initiated for: " + empRef);
    }
}
