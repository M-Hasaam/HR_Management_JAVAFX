package com.hr.dao;

import com.hr.model.EmployeeAssignment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeAssignmentDAO {

    private final Connection conn;

    public EmployeeAssignmentDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /** Inserts an immutable org-history record. */
    public void insert(EmployeeAssignment a) throws SQLException {
        String sql = """
            INSERT INTO employee_org_history
              (employee_id, from_dept_id, to_dept_id, effective_date, remark,
               assigned_by_user_id, is_backdated, capacity_override_justification)
            VALUES (?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getEmployeeId());
            if (a.getFromDeptId() > 0) ps.setInt(2, a.getFromDeptId());
            else                        ps.setNull(2, Types.INTEGER);
            ps.setInt(3, a.getToDeptId());
            ps.setDate(4, Date.valueOf(a.getEffectiveDate()));
            ps.setString(5, a.getRemark());
            if (a.getAssignedByUserId() > 0) ps.setInt(6, a.getAssignedByUserId());
            else                              ps.setNull(6, Types.INTEGER);
            ps.setBoolean(7, a.isBackdated());
            ps.setString(8, a.getCapacityOverrideJustification());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) a.setId(keys.getInt(1));
            }
        }
    }

    /** Returns all history records for one employee, most recent first. */
    public List<EmployeeAssignment> getByEmployeeId(int empId) throws SQLException {
        String sql = """
            SELECT h.*,
                   fd.name                             AS from_dept_name,
                   td.name                             AS to_dept_name,
                   CONCAT(e.first_name,' ',e.last_name) AS emp_name
            FROM employee_org_history h
            LEFT JOIN departments fd ON h.from_dept_id = fd.id
            JOIN  departments td     ON h.to_dept_id   = td.id
            JOIN  employees   e      ON h.employee_id  = e.id
            WHERE h.employee_id = ?
            ORDER BY h.created_at DESC
            """;
        List<EmployeeAssignment> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** 3c: True when the employee's status is 'OFFBOARDING'. */
    public boolean isActiveOffboarding(int empId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT status FROM employees WHERE id = ?")) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && "OFFBOARDING".equalsIgnoreCase(rs.getString("status"));
            }
        }
    }

    /** 4b: True when the employee is the named manager of any department. */
    public boolean isManagerOfAnyDept(int empId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM departments WHERE manager_id = ?")) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * 4c: Returns the required security clearance level for a department.
     * Returns 0 (no restriction) if the column does not yet exist.
     */
    public int getDeptRequiredClearance(int deptId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT required_clearance FROM departments WHERE id = ?")) {
            ps.setInt(1, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("required_clearance") : 0;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Unknown column")) return 0;
            throw e;
        }
    }

    /**
     * 4c: Returns the security clearance level held by an employee.
     * Returns 0 (none) if the column does not yet exist.
     */
    public int getEmployeeClearance(int empId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT security_clearance FROM employees WHERE id = ?")) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("security_clearance") : 0;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Unknown column")) return 0;
            throw e;
        }
    }

    private EmployeeAssignment map(ResultSet rs) throws SQLException {
        EmployeeAssignment a = new EmployeeAssignment();
        a.setId(rs.getInt("id"));
        a.setEmployeeId(rs.getInt("employee_id"));
        a.setEmployeeName(rs.getString("emp_name"));
        a.setFromDeptId(rs.getInt("from_dept_id"));
        a.setFromDeptName(rs.getString("from_dept_name"));
        a.setToDeptId(rs.getInt("to_dept_id"));
        a.setToDeptName(rs.getString("to_dept_name"));
        Date d = rs.getDate("effective_date");
        if (d != null) a.setEffectiveDate(d.toLocalDate());
        a.setRemark(rs.getString("remark"));
        a.setAssignedByUserId(rs.getInt("assigned_by_user_id"));
        a.setBackdated(rs.getBoolean("is_backdated"));
        a.setCapacityOverrideJustification(rs.getString("capacity_override_justification"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        return a;
    }
}
