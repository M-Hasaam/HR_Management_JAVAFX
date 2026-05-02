package com.hr.service;

// GoF Design Pattern: Adapter — Target Interface.
//
// Justification: The HR system needs to integrate with an external payroll engine
// whose API (ExternalPayrollSystem) uses different method signatures and terminology.
// Defining this target interface lets all HR code depend on a stable, HR-domain
// vocabulary.  The PayrollSystemAdapter bridges the gap, so if the external system
// is replaced, only the adapter changes — not the HR business logic.

public interface PayrollIntegration {

    /**
     * Queues a payroll update when an employee's salary or designation changes.
     *
     * @param employeeId  affected employee's ID
     * @param designation new or current designation title
     * @param salary      new or current basic salary
     */
    void queueSalaryUpdate(int employeeId, String designation, double salary);

    /**
     * Records attendance hours so the payroll engine can compute variable pay.
     *
     * @param attendanceId attendance record ID
     * @param hoursWorked  total hours in the period
     */
    void recordHours(int attendanceId, double hoursWorked);

    /**
     * Initiates the final settlement process for a departing employee.
     *
     * @param employeeId departing employee's ID
     */
    void initiateSettlement(int employeeId);
}
