package com.hr.controller;
// GRASP: Controller — UC-02 Update Employee Record
// GoF Observer: UpdateController publishes EMPLOYEE_UPDATED; AuditLogObserver +
//   NotificationObserver react without direct coupling.

import com.hr.model.Employee;
import com.hr.service.*;

import java.sql.SQLException;
import java.util.Objects;

public class UpdateController {

    private final EmployeeService  employeeService;
    private final PayrollNotifier  payrollNotifier;
    private final HREventPublisher eventPublisher;
    private final AuditLogService  auditLogService;

    public UpdateController() throws SQLException {
        this.employeeService = new EmployeeService();
        this.payrollNotifier = new PayrollNotifier();
        this.auditLogService = new AuditLogService();
        this.eventPublisher  = new HREventPublisher();
        this.eventPublisher.register(new AuditLogObserver(auditLogService));
        this.eventPublisher.register(new NotificationObserver(new NotificationService()));
    }

    // ── Backward-compatible method (used by old EmployeeController dialog) ───
    public void updateEmployeeRecord(int employeeID, Employee updated, String reason)
            throws SQLException {
        Employee existing = employeeService.getEmployee(employeeID);
        employeeService.updateEmployee(updated);
        String payload = "Updated: " + existing + " → " + updated + " | Reason: " + reason;
        eventPublisher.publishEvent("EMPLOYEE_UPDATED", employeeID, payload);
        payrollNotifier.notifyPayroll(employeeID, updated.getPosition(),
            updated.getBasicSalary() != null ? updated.getBasicSalary().doubleValue() : 0.0);
    }

    // ── UC-02 Full: field-level audit trail + payroll fallback ───────────────
    /**
     * Saves the updated record, writes one audit entry per changed field,
     * and notifies payroll if salary or position changed.
     *
     * @return true if payroll was notified, false if notification was queued (4c)
     */
    public boolean updateWithAuditTrail(Employee existing, Employee updated,
                                        String reason, int updaterId) throws SQLException {
        // Per-field diff → individual audit_log rows
        diffAndLog(existing, updated, updaterId);
        // Log the human reason separately
        auditLogService.writeAuditLog(existing.getId(), "update_reason", null, reason, updaterId);

        // Persist
        employeeService.updateEmployee(updated);

        // Publish general Observer event
        eventPublisher.publishEvent("EMPLOYEE_UPDATED", existing.getId(),
            "Reason: " + reason + " | By userId=" + updaterId);

        // Payroll notification when salary or position changes (4c handled in UI)
        boolean salaryChanged   = !Objects.equals(existing.getBasicSalary(), updated.getBasicSalary());
        boolean positionChanged = !Objects.equals(existing.getPosition(),    updated.getPosition());
        if (salaryChanged || positionChanged) {
            return notifyPayrollSafe(updated);
        }
        return true;
    }

    // ── Fetch single employee (used for form pre-population & conflict check) ─
    public Employee getEmployee(int id) throws SQLException {
        return employeeService.getEmployee(id);
    }

    // ── 4a: Optimistic concurrency — compare snapshot vs current DB state ────
    public boolean hasBeenModified(Employee snapshot, Employee current) {
        return !Objects.equals(snapshot.getFirstName(),   current.getFirstName())
            || !Objects.equals(snapshot.getLastName(),    current.getLastName())
            || !Objects.equals(snapshot.getEmail(),       current.getEmail())
            || !Objects.equals(snapshot.getPosition(),    current.getPosition())
            || !Objects.equals(snapshot.getBasicSalary(), current.getBasicSalary())
            || snapshot.getDepartmentId() != current.getDepartmentId();
    }

    // ── 3b: Log restricted-field access attempt ──────────────────────────────
    public void logRestrictedFieldAccess(int empId, String field, int userId) {
        try {
            auditLogService.writeAuditLog(empId, field + "_ACCESS_DENIED",
                null, "Attempted by userId=" + userId, userId);
        } catch (SQLException e) {
            System.err.println("[AUDIT] Restricted access log failed: " + e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean notifyPayrollSafe(Employee emp) {
        try {
            payrollNotifier.notifyPayroll(emp.getId(), emp.getPosition(),
                emp.getBasicSalary() != null ? emp.getBasicSalary().doubleValue() : 0.0);
            return true;
        } catch (Exception e) {
            System.err.println("[PAYROLL] Notification failed, queued for retry: " + e.getMessage());
            return false;
        }
    }

    private void diffAndLog(Employee old, Employee upd, int uid) throws SQLException {
        log(old.getId(), "first_name",      old.getFirstName(),       upd.getFirstName(),       uid);
        log(old.getId(), "last_name",       old.getLastName(),        upd.getLastName(),        uid);
        log(old.getId(), "email",           old.getEmail(),           upd.getEmail(),           uid);
        log(old.getId(), "phone",           old.getPhone(),           upd.getPhone(),           uid);
        log(old.getId(), "gender",          old.getGender(),          upd.getGender(),          uid);
        log(old.getId(), "address",         old.getAddress(),         upd.getAddress(),         uid);
        log(old.getId(), "position",        old.getPosition(),        upd.getPosition(),        uid);
        log(old.getId(), "employment_type", old.getEmploymentType(),  upd.getEmploymentType(),  uid);
        log(old.getId(), "status",          old.getStatus(),          upd.getStatus(),          uid);
        log(old.getId(), "department_id",
            String.valueOf(old.getDepartmentId()), String.valueOf(upd.getDepartmentId()), uid);
        log(old.getId(), "basic_salary",
            old.getBasicSalary() != null ? old.getBasicSalary().toPlainString() : null,
            upd.getBasicSalary() != null ? upd.getBasicSalary().toPlainString() : null, uid);
        log(old.getId(), "date_of_birth",
            old.getDateOfBirth() != null ? old.getDateOfBirth().toString() : null,
            upd.getDateOfBirth() != null ? upd.getDateOfBirth().toString() : null, uid);
    }

    private void log(int empId, String field, String oldVal, String newVal, int uid)
            throws SQLException {
        if (!Objects.equals(oldVal, newVal))
            auditLogService.writeAuditLog(empId, field, oldVal, newVal, uid);
    }
}
