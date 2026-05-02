package com.hr.service;

// GRASP Pattern: Pure Fabrication — isolates employee persistence from domain logic.
// This class does not represent a real-world concept; it exists purely to handle
// database persistence for Employee entities without polluting the domain model.

import com.hr.dao.EmployeeDAO;
import com.hr.model.Employee;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EmployeeRepository {

    private final EmployeeDAO dao;

    public EmployeeRepository() throws SQLException {
        this.dao = new EmployeeDAO();
    }

    /**
     * Checks for duplicate national ID or email before insertion.
     * Throws IllegalArgumentException with a field-specific message if a duplicate is found.
     */
    public void validateDuplicate(String nationalId, String email) throws SQLException {
        if (dao.existsByNationalId(nationalId))
            throw new IllegalArgumentException(
                "National ID is already registered to another employee.");
        if (dao.existsByEmail(email))
            throw new IllegalArgumentException(
                "Email address is already registered to another employee.");
    }

    public boolean existsByNationalId(String nationalId) throws SQLException {
        return dao.existsByNationalId(nationalId);
    }

    public boolean existsByEmail(String email) throws SQLException {
        return dao.existsByEmail(email);
    }

    /**
     * Persists a new Employee and returns the system-generated employee code.
     */
    public String create(Employee employee) throws SQLException {
        dao.insert(employee);
        return "EMP-" + LocalDate.now().getYear() + "-" + String.format("%04d", employee.getId());
    }

    /**
     * Updates an existing Employee record.
     */
    public void save(Employee employee) throws SQLException {
        dao.update(employee);
    }

    /**
     * Retrieves a single Employee by primary key.
     */
    public Employee findById(int id) throws SQLException {
        return dao.getById(id);
    }

    /**
     * Retrieves all Employee records.
     */
    public List<Employee> findAll() throws SQLException {
        return dao.getAll();
    }
}
