package com.hr.controller;
// GRASP Pattern: Controller — handles UC-13 Monitor Employee Probation Status.
// Participants: ProbationRecord (Information Expert), Employee (Information Expert),
//              NotificationService (Pure Fabrication), EmployeeService (Information Expert),
//              TaskScheduler (Pure Fabrication)
//
// Business Rules enforced here:
// 1. CONFIRMED → employment_type=PERMANENT, probation_end_date cleared, record CLOSED.
// 2. EXTENDED  → requires new end date (Ext 3a); total duration must not exceed 12 months (Ext 3b).
// 3. TERMINATED→ requires documented reason, employee status updated to TERMINATED.
// 4. Ext 2a: warns if no interim performance evaluation exists for the employee.
// 5. Ext 4b: simulated payroll update — graceful failure with retry queue.

import com.hr.dao.PerformanceEvaluationDAO;
import com.hr.dao.ProbationRecordDAO;
import com.hr.model.Employee;
import com.hr.model.ProbationRecord;
import com.hr.service.EmployeeService;
import com.hr.service.NotificationService;
import com.hr.service.TaskScheduler;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

public class ProbationMonitorController {

    private static final Set<String> VALID_DECISIONS = Set.of("CONFIRMED", "EXTENDED", "TERMINATED");
    private static final int MAX_PROBATION_MONTHS = 12;

    private final ProbationRecordDAO       probationDAO;
    private final PerformanceEvaluationDAO evalDAO;
    private final EmployeeService          employeeService;
    private final NotificationService      notificationService;
    private final TaskScheduler            taskScheduler;

    public ProbationMonitorController() throws SQLException {
        this.probationDAO        = new ProbationRecordDAO();
        this.evalDAO             = new PerformanceEvaluationDAO();
        this.employeeService     = new EmployeeService();
        this.notificationService = new NotificationService();
        this.taskScheduler       = new TaskScheduler();
    }

    /** UC-13 Step 1: All active probation records for the dashboard. */
    public List<ProbationRecord> getActiveRecords() throws SQLException {
        return probationDAO.getActive();
    }

    /** Ext 1a: Recently closed records shown when dashboard has no active cases. */
    public List<ProbationRecord> getRecentlyClosed() throws SQLException {
        return probationDAO.getRecentlyClosed(5);
    }

    /** UC-13 Step 2: Full probation record for a specific employee. */
    public ProbationRecord getProbationRecord(int employeeId) throws SQLException {
        return probationDAO.getByEmployee(employeeId);
    }

    /**
     * Ext 2a: Returns true if a submitted performance evaluation exists for this employee.
     * HR should complete an interim evaluation before making a probation decision.
     */
    public boolean hasInterimEvaluation(int employeeId) throws SQLException {
        return !evalDAO.getByEmployee(employeeId).isEmpty();
    }

    /**
     * UC-13 Step 4: Record a probation decision.
     *
     * @param probationId  probation record to update
     * @param decision     CONFIRMED | EXTENDED | TERMINATED
     * @param decisionDate date decision is made
     * @param notes        HR notes
     * @param newEndDate   required when decision=EXTENDED (Ext 3a); null otherwise
     * @param reason       required when decision=TERMINATED; null otherwise
     * @throws IllegalArgumentException if decision is invalid or required fields are missing
     * @throws IllegalStateException    if Ext 3b max duration would be exceeded
     * @throws SQLException             on database error
     */
    public void recordDecision(int probationId, String decision, LocalDate decisionDate,
                               String notes, LocalDate newEndDate,
                               String reason) throws SQLException {
        if (!VALID_DECISIONS.contains(decision))
            throw new IllegalArgumentException("Decision must be one of: " + VALID_DECISIONS);

        ProbationRecord rec = probationDAO.getAll().stream()
                .filter(r -> r.getId() == probationId)
                .findFirst().orElse(null);
        if (rec == null)
            throw new IllegalArgumentException("Probation record not found (id=" + probationId + ").");

        switch (decision) {
            case "EXTENDED" -> {
                // Ext 3a: new end date is mandatory
                if (newEndDate == null)
                    throw new IllegalArgumentException(
                            "Extension requires a new probation end date.");
                if (!newEndDate.isAfter(rec.getEndDate()))
                    throw new IllegalArgumentException(
                            "New end date must be after the current end date (" + rec.getEndDate() + ").");

                // Ext 3b: total duration must not exceed MAX_PROBATION_MONTHS
                long totalDays = ChronoUnit.DAYS.between(rec.getStartDate(), newEndDate);
                long maxDays   = MAX_PROBATION_MONTHS * 30L;
                if (totalDays > maxDays)
                    throw new IllegalStateException(
                            "Policy limit exceeded: total probation duration would be "
                            + (totalDays / 30) + " months. Maximum allowed is "
                            + MAX_PROBATION_MONTHS + " months.\n"
                            + "Escalate to Admin for override authorization.");

                probationDAO.extend(probationId, newEndDate, notes);
                notificationService.notifyProbationDecision(rec.getEmployeeId(), decision,
                        "Probation extended to " + newEndDate + ". " + (notes != null ? notes : ""));
                taskScheduler.scheduleReminders(probationId, newEndDate);
            }
            case "CONFIRMED" -> {
                probationDAO.recordDecision(probationId, decision, decisionDate, notes, null);

                // Postcondition: update employment type to PERMANENT and clear probation_end_date
                Employee emp = employeeService.getEmployee(rec.getEmployeeId());
                if (emp != null) {
                    emp.setEmploymentType("PERMANENT");
                    emp.setProbationEndDate(null);
                    employeeService.updateEmployee(emp);
                }

                notificationService.notifyProbationDecision(rec.getEmployeeId(), decision,
                        "Congratulations — your probation has been completed successfully. "
                        + "You are now a permanent employee.");

                // Ext 4b: simulate payroll module update — graceful failure
                boolean payrollUpdated = simulatePayrollUpdate(rec.getEmployeeId());
                if (!payrollUpdated)
                    System.out.println("[PAYROLL] Employment type update queued for retry — "
                            + "Payroll team alerted to verify employee " + rec.getEmployeeId()
                            + " status manually.");
            }
            case "TERMINATED" -> {
                if (reason == null || reason.isBlank())
                    throw new IllegalArgumentException(
                            "Termination requires a documented reason.");

                probationDAO.recordDecision(probationId, decision, decisionDate, notes, reason);

                Employee emp = employeeService.getEmployee(rec.getEmployeeId());
                if (emp != null) {
                    emp.setStatus("TERMINATED");
                    employeeService.updateEmployee(emp);
                }

                notificationService.notifyProbationDecision(rec.getEmployeeId(), decision,
                        "Your probation period has ended in termination. Reason: " + reason);
            }
        }

        System.out.println("[PROBATION] Decision=" + decision
                + " | ProbationID=" + probationId + " | Date=" + decisionDate);
    }

    /** Returns all probation records. */
    public List<ProbationRecord> getAllRecords() throws SQLException {
        return probationDAO.getAll();
    }

    public void scheduleDeadlineAlerts(int probationId, LocalDate probationEndDate) {
        taskScheduler.scheduleReminders(probationId, probationEndDate);
    }

    /** Ext 4b: Simulates sending employment type update to the Payroll module. */
    private boolean simulatePayrollUpdate(int employeeId) {
        System.out.println("[PAYROLL] Employment type update sent for employee " + employeeId
                + " → PERMANENT");
        return true;
    }
}
