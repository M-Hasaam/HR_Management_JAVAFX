package com.hr.service;

import com.hr.dao.DepartmentDAO;
import com.hr.dao.EmployeeDAO;
import com.hr.model.Department;
import com.hr.model.Employee;

import java.sql.SQLException;
import java.util.List;

public class EmployeeService {
    private final EmployeeDAO    dao;
    private final DepartmentDAO  departmentDAO;

    public EmployeeService() throws SQLException {
        this.dao           = new EmployeeDAO();
        this.departmentDAO = new DepartmentDAO();
    }

    public List<Employee> getAllEmployees() throws SQLException {
        return dao.getAll();
    }

    public Employee getEmployee(int id) throws SQLException {
        return dao.getById(id);
    }

    public void addEmployee(Employee emp) throws SQLException {
        validate(emp);
        enforceCapacity(emp.getDepartmentId());
        emp.setStatus("ACTIVE");
        dao.insert(emp);
    }

    /** Standard update — enforces capacity when the department changes. */
    public void updateEmployee(Employee emp) throws SQLException {
        updateEmployee(emp, false);
    }

    /**
     * Update with optional capacity bypass.
     * Pass skipCapacityCheck=true only when the caller has already verified
     * capacity independently (e.g. UC-03 AssignmentController with admin override).
     */
    public void updateEmployee(Employee emp, boolean skipCapacityCheck) throws SQLException {
        validate(emp);
        if (!skipCapacityCheck) {
            Employee existing = dao.getById(emp.getId());
            // Only check capacity when the department is actually changing
            if (existing != null && existing.getDepartmentId() != emp.getDepartmentId()) {
                enforceCapacity(emp.getDepartmentId());
            }
        }
        dao.update(emp);
    }

    public void deleteEmployee(int id) throws SQLException {
        dao.delete(id);
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private void enforceCapacity(int deptId) throws SQLException {
        if (deptId <= 0) return;
        Department dept = departmentDAO.getById(deptId);
        if (dept == null || dept.getMaxHeadcount() <= 0) return; // 0 = no limit
        if (dept.getCurrentHeadcount() >= dept.getMaxHeadcount()) {
            throw new IllegalStateException(
                "Department \"" + dept.getName() + "\" is at maximum capacity ("
                + dept.getCurrentHeadcount() + " / " + dept.getMaxHeadcount()
                + "). Use the Assign Department module with admin override to proceed.");
        }
    }

    private void validate(Employee emp) {
        if (emp.getFirstName() == null || emp.getFirstName().isBlank())
            throw new IllegalArgumentException("First name is required.");
        if (emp.getLastName() == null || emp.getLastName().isBlank())
            throw new IllegalArgumentException("Last name is required.");
        if (emp.getEmail() == null || !emp.getEmail().contains("@"))
            throw new IllegalArgumentException("A valid email is required.");
        if (emp.getBasicSalary() != null && emp.getBasicSalary().doubleValue() < 0)
            throw new IllegalArgumentException("Salary must be a positive number.");
    }
}
