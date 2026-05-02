package com.hr.service;

import com.hr.dao.DepartmentDAO;
import com.hr.model.Department;

import java.sql.SQLException;
import java.util.List;

public class DepartmentService {
    private final DepartmentDAO dao;

    public DepartmentService() throws SQLException {
        this.dao = new DepartmentDAO();
    }

    public List<Department> getAllDepartments() throws SQLException {
        return dao.getAll();
    }

    public void addDepartment(Department dept) throws SQLException {
        validate(dept);
        dao.insert(dept);
    }

    public void updateDepartment(Department dept) throws SQLException {
        validate(dept);
        dao.update(dept);
    }

    public void deleteDepartment(int id) throws SQLException {
        if (dao.hasEmployees(id))
            throw new IllegalStateException("Cannot delete department — it still has assigned employees.");
        dao.delete(id);
    }

    private void validate(Department dept) {
        if (dept.getName() == null || dept.getName().isBlank())
            throw new IllegalArgumentException("Department name is required.");
    }
}
