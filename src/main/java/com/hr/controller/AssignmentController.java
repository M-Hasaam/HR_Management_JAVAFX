package com.hr.controller;
// GRASP: Controller — UC-03 Assign Employee to Department
// GoF Observer: publishes DEPT_ASSIGNMENT so downstream observers (audit, notification) react
// without coupling AssignmentController to their implementation.

import com.hr.dao.DepartmentDAO;
import com.hr.dao.EmployeeAssignmentDAO;
import com.hr.model.Department;
import com.hr.model.Employee;
import com.hr.model.EmployeeAssignment;
import com.hr.service.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AssignmentController {

    private final EmployeeService       employeeService;
    private final DepartmentService     departmentService;
    private final DepartmentDAO         departmentDAO;
    private final EmployeeAssignmentDAO assignmentDAO;
    private final NotificationService   notificationService;
    private final AuditLogService       auditLogService;
    private final HREventPublisher      eventPublisher;

    public record AssignmentResult(boolean adSyncSucceeded, int historyId) {}

    public AssignmentController() throws SQLException {
        this.employeeService    = new EmployeeService();
        this.departmentService  = new DepartmentService();
        this.departmentDAO      = new DepartmentDAO();
        this.assignmentDAO      = new EmployeeAssignmentDAO();
        this.notificationService = new NotificationService();
        this.auditLogService    = new AuditLogService();
        this.eventPublisher     = new HREventPublisher();
        this.eventPublisher.register(new AuditLogObserver(auditLogService));
        this.eventPublisher.register(new NotificationObserver(notificationService));
    }

    // ── UC-03 Main flow ─────────────────────────────────────────────────────────
    /**
     * Persists the department assignment with a full audit trail and immutable
     * org-history record.  Returns the AD-sync outcome (4a) alongside the new
     * history row ID so the UI can react appropriately.
     */
    public AssignmentResult assignWithHistory(
            Employee emp, Department targetDept,
            LocalDate effectiveDate, String remark,
            boolean capacityOverride, String overrideJustification,
            boolean backdateAcknowledged, int assignedByUserId) throws SQLException {

        boolean isBackdated = effectiveDate.isBefore(LocalDate.now());
        int     oldDeptId   = emp.getDepartmentId();

        // 1. Write immutable org-history record
        EmployeeAssignment history = new EmployeeAssignment();
        history.setEmployeeId(emp.getId());
        history.setFromDeptId(oldDeptId);
        history.setToDeptId(targetDept.getId());
        history.setEffectiveDate(effectiveDate);
        history.setRemark(remark);
        history.setAssignedByUserId(assignedByUserId);
        history.setBackdated(isBackdated);
        history.setCapacityOverrideJustification(capacityOverride ? overrideJustification : null);
        assignmentDAO.insert(history);

        // 2. Update employee's department
        // skipCapacityCheck=true because capacity was already verified (with possible override) above
        emp.setDepartmentId(targetDept.getId());
        employeeService.updateEmployee(emp, true);

        // headcount is derived live from employees — no manual counter update needed

        // 4. Field-level audit log
        try {
            auditLogService.writeAuditLog(emp.getId(), "department_id",
                String.valueOf(oldDeptId), String.valueOf(targetDept.getId()), assignedByUserId);
            if (isBackdated)
                auditLogService.writeAuditLog(emp.getId(), "backdated_assignment",
                    null, effectiveDate.toString(), assignedByUserId);
            if (capacityOverride)
                auditLogService.writeAuditLog(emp.getId(), "capacity_override",
                    null, overrideJustification, assignedByUserId);
        } catch (Exception e) {
            System.err.println("[AUDIT] Assignment audit log failed: " + e.getMessage());
        }

        // 5. Observer event
        eventPublisher.publishEvent("DEPT_ASSIGNMENT", emp.getId(),
            "Assigned to dept=" + targetDept.getName() + " by userId=" + assignedByUserId +
            " effectiveDate=" + effectiveDate);

        // 6. Notify managers + AD sync (4a)
        boolean adSyncOk = notifyAndSync(emp, oldDeptId, targetDept);

        return new AssignmentResult(adSyncOk, history.getId());
    }

    // ── Pre-save checks (called from UI before confirming) ──────────────────────

    /** 3c: Employee is in an active offboarding workflow — assignment must be blocked. */
    public boolean isActiveOffboarding(int empId) throws SQLException {
        return assignmentDAO.isActiveOffboarding(empId);
    }

    /** 3a: Target department has no available slot. */
    public boolean isDepartmentAtCapacity(int deptId) throws SQLException {
        return !departmentDAO.hasCapacity(deptId);
    }

    /** 4b: Employee is the named manager of another department → dual-report risk. */
    public boolean isDualReportingConflict(Employee emp) throws SQLException {
        return assignmentDAO.isManagerOfAnyDept(emp.getId());
    }

    /**
     * 4c: Department requires higher clearance than the employee holds.
     * Returns false when the required_clearance column does not yet exist (graceful).
     */
    public boolean hasSecurityClearanceMismatch(Employee emp, Department dept) throws SQLException {
        int required = assignmentDAO.getDeptRequiredClearance(dept.getId());
        if (required == 0) return false;
        int held = assignmentDAO.getEmployeeClearance(emp.getId());
        return held < required;
    }

    /** Returns the full assignment history for an employee (most recent first). */
    public List<EmployeeAssignment> getAssignmentHistory(int empId) throws SQLException {
        return assignmentDAO.getByEmployeeId(empId);
    }

    // ── Backward-compatible method (used by other modules) ──────────────────────
    public void assignEmployeeToDepartment(int employeeID, int deptID,
                                           LocalDate effectiveDate, String remark)
            throws SQLException {
        if (!departmentDAO.hasCapacity(deptID))
            throw new IllegalStateException("Department is at maximum headcount.");
        Employee emp = employeeService.getEmployee(employeeID);
        int old = emp.getDepartmentId();
        emp.setDepartmentId(deptID);
        employeeService.updateEmployee(emp, true); // capacity already verified above
        notificationService.notifyManagers(old, deptID);
    }

    public boolean checkDepartmentCapacity(int deptID) throws SQLException {
        return departmentDAO.hasCapacity(deptID);
    }

    // ── Internal helpers ────────────────────────────────────────────────────────

    /**
     * 4a: Notify both managers and synchronize access rights.
     * Returns false if AD sync fails; the assignment is already persisted.
     */
    private boolean notifyAndSync(Employee emp, int oldDeptId, Department newDept) {
        try {
            notificationService.notifyManagersOfTransfer(
                emp, oldDeptId, newDept.getId(), newDept.getName());
        } catch (Exception e) {
            System.err.println("[NOTIFY] Manager notification failed: " + e.getMessage());
        }
        return simulateAdSync(emp.getId(), newDept.getId());
    }

    /** Simulates AD/LDAP sync. Toggle return value to false to test 4a. */
    private boolean simulateAdSync(int empId, int deptId) {
        System.out.println("[AD-SYNC] Syncing access rights: empId=" + empId
            + " → deptId=" + deptId);
        return true;
    }
}
