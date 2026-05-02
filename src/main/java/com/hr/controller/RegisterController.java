package com.hr.controller;
// GRASP Pattern: Controller — handles UC-01 Register New Employee workflow

import com.hr.dao.DepartmentDAO;
import com.hr.dao.UserAccountDAO;
import com.hr.model.Department;
import com.hr.model.Employee;
import com.hr.model.UserAccount;
import com.hr.service.AuthService;
import com.hr.service.EmployeeRepository;
import com.hr.service.NotificationService;

import java.sql.SQLException;
import java.time.LocalDate;

public class RegisterController {

    private final EmployeeRepository  employeeRepository;
    private final NotificationService notificationService;
    private final DepartmentDAO       departmentDAO;
    private final UserAccountDAO      userAccountDAO;

    public record RegistrationResult(
        String  employeeCode,
        boolean emailQueued,
        String  username,
        String  tempPassword
    ) {}

    public RegisterController() throws SQLException {
        this.employeeRepository  = new EmployeeRepository();
        this.notificationService = new NotificationService();
        this.departmentDAO       = new DepartmentDAO();
        this.userAccountDAO      = new UserAccountDAO();
    }

    // ── Original method — preserved for backward compatibility ──────────────
    public String registerNewEmployee(Employee employeeData) throws SQLException {
        employeeRepository.validateDuplicate(employeeData.getNationalId(), employeeData.getEmail());
        if (isDepartmentAtCapacity(employeeData.getDepartmentId()))
            throw new IllegalStateException("Department is at maximum headcount.");
        String employeeID = employeeRepository.create(employeeData);
        notificationService.sendWelcomeEmail(employeeData);
        return employeeID;
    }

    // ── Real-time individual duplicate checks (called from UI on focus-lost) ─

    public boolean checkNationalIdDuplicate(String nationalId) throws SQLException {
        if (nationalId == null || nationalId.isBlank()) return false;
        return employeeRepository.existsByNationalId(nationalId);
    }

    public boolean checkEmailDuplicate(String email) throws SQLException {
        if (email == null || email.isBlank()) return false;
        return employeeRepository.existsByEmail(email);
    }

    // ── Department capacity check ───────────────────────────────────────────

    public boolean isDepartmentAtCapacity(int deptId) throws SQLException {
        Department dept = departmentDAO.getById(deptId);
        if (dept == null || dept.getMaxHeadcount() <= 0) return false; // no limit set
        return dept.getCurrentHeadcount() >= dept.getMaxHeadcount();
    }

    // ── Full UC-01 registration with user provisioning ──────────────────────

    public RegistrationResult registerWithProvisioning(
            Employee emp, boolean capacityOverride, String overrideJustification)
            throws SQLException {

        // Step 2a/2b: distinct duplicate checks
        employeeRepository.validateDuplicate(emp.getNationalId(), emp.getEmail());

        // Step 3a: capacity guard (skipped when admin has explicitly overridden)
        if (!capacityOverride && isDepartmentAtCapacity(emp.getDepartmentId())) {
            throw new IllegalStateException(
                "Department is at maximum headcount. Admin override is required.");
        }

        // Persist employee record — repository generates the EMP-YYYY-NNNN code
        String employeeCode = employeeRepository.create(emp);

        // Log capacity override for audit trail
        if (capacityOverride && overrideJustification != null && !overrideJustification.isBlank()) {
            System.out.println("[AUDIT] Capacity override — dept=" + emp.getDepartmentId()
                + ", employee=" + employeeCode + ", justification: " + overrideJustification);
        }

        // Provision user account
        String username    = generateUniqueUsername(emp);
        String tempPwd     = "Emp@" + LocalDate.now().getYear() + "!";
        UserAccount account = new UserAccount();
        account.setEmployeeId(emp.getId());
        account.setUsername(username);
        account.setPasswordHash(AuthService.hashPassword(tempPwd));
        account.setRole("Employee");
        account.setAccountStatus("ACTIVE");
        userAccountDAO.insert(account);

        // Step 5a: send welcome email; if SMTP is unavailable it returns false (queued)
        boolean sent = notificationService.sendWelcomeEmailWithFallback(emp, username, tempPwd);

        return new RegistrationResult(employeeCode, !sent, username, tempPwd);
    }

    // ── Kept for EmployeeController backward compatibility ──────────────────

    public boolean validateDuplicate(String nationalID, String email) throws SQLException {
        try {
            employeeRepository.validateDuplicate(nationalID, email);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private String generateUniqueUsername(Employee emp) throws SQLException {
        String base = (emp.getFirstName() + "." + emp.getLastName())
            .toLowerCase()
            .replaceAll("[^a-z0-9.]", "");
        String candidate = base;
        int suffix = 1;
        while (userAccountDAO.getByUsername(candidate) != null) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}
