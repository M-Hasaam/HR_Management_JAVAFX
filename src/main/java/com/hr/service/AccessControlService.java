package com.hr.service;

// GRASP Pattern: Pure Fabrication — implements Role-Based Access Control (RBAC).
// No domain concept maps naturally to "field-level permission checking", so this
// class exists purely to keep access-control logic in one place, satisfying both
// high cohesion and low coupling principles.

import java.util.Arrays;
import java.util.List;

public class AccessControlService {

    /**
     * Returns the list of data fields the given role is permitted to access
     * for the specified employee.
     *
     * <ul>
     *   <li>ADMIN   — unrestricted ("all")</li>
     *   <li>HR      — personal info, employment, salary, leave, attendance, performance</li>
     *   <li>EMPLOYEE— own personal info, leave balance, attendance (salary/performance masked)</li>
     *   <li>default — personal info only</li>
     * </ul>
     *
     * @param userRole   Role of the requesting user
     * @param employeeId ID of the employee whose record is being accessed (reserved for
     *                   future row-level security checks, e.g. "own record only")
     * @return List of permitted field-group names
     */
    public List<String> getPermittedFields(String userRole, int employeeId) {
        return switch (userRole) {
            case "ADMIN" -> Arrays.asList("all");
            case "HR"    -> Arrays.asList(
                    "personalInfo", "employment", "salary",
                    "leaveBalance", "attendance", "performance");
            case "EMPLOYEE" -> Arrays.asList(
                    "personalInfo", "leaveBalance", "attendance");
            // salary and performance details are masked for EMPLOYEE role
            default -> Arrays.asList("personalInfo");
        };
    }

    /**
     * Returns true if the given role is allowed to access the specified field group.
     *
     * @param userRole Role of the requesting user
     * @param field    Field-group name to check
     * @return true if access is permitted
     */
    public boolean canAccess(String userRole, String field) {
        List<String> permitted = getPermittedFields(userRole, 0);
        return permitted.contains("all") || permitted.contains(field);
    }
}
