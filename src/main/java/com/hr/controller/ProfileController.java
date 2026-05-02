package com.hr.controller;
// GRASP Pattern: Controller — handles UC-05 View Employee Profile
// Participants: Employee (Information Expert), AccessControlService (Pure Fabrication),
//              AuditLogService (Pure Fabrication)

import com.hr.model.Employee;
import com.hr.service.AccessControlService;
import com.hr.service.AuditLogService;
import com.hr.service.EmployeeService;

import java.sql.SQLException;
import java.util.List;

public class ProfileController {

    private final EmployeeService employeeService;
    private final AccessControlService accessControlService; // Pure Fabrication
    private final AuditLogService auditLogService;           // Pure Fabrication

    public ProfileController() throws SQLException {
        this.employeeService      = new EmployeeService();
        this.accessControlService = new AccessControlService(); // no SQLException — plain constructor
        this.auditLogService      = new AuditLogService();
    }

    /**
     * UC-05: Retrieve an employee's profile with access-control filtering.
     * Logs the read access in the audit log and returns the Employee object.
     *
     * @param employeeID    ID of the employee whose profile is being viewed
     * @param userRole      role of the requesting user (e.g. ADMIN, HR, EMPLOYEE)
     * @param viewingUserId ID of the user making the request
     * @return Employee object (caller should apply field filtering via {@link #getPermittedFields})
     * @throws SQLException on database error
     */
    public Employee viewEmployeeProfile(int employeeID, String userRole, int viewingUserId)
            throws SQLException {
        Employee emp = employeeService.getEmployee(employeeID);
        List<String> permittedFields = accessControlService.getPermittedFields(userRole, employeeID);
        auditLogService.logReadAccess(viewingUserId, employeeID, String.join(",", permittedFields));
        return emp;
    }

    /**
     * Returns the list of field names visible to the given role for the specified employee.
     */
    public List<String> getPermittedFields(String userRole, int employeeID) {
        return accessControlService.getPermittedFields(userRole, employeeID);
    }

    /**
     * Checks whether a user role is allowed to access a specific resource.
     */
    public boolean canAccess(String userRole, String resource) {
        return accessControlService.canAccess(userRole, resource);
    }
}
