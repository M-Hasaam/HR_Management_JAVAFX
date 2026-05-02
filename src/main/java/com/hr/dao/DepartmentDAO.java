package com.hr.dao;

import com.hr.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {
    private final Connection conn;

    public DepartmentDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public List<Department> getAll() throws SQLException {
        List<Department> list = new ArrayList<>();
        // current_headcount is always derived from the employees table — never from the stored column
        String sql = """
            SELECT d.id, d.name, d.description, d.manager_id, d.parent_dept_id,
                   d.max_headcount,
                   (SELECT COUNT(*) FROM employees e WHERE e.department_id = d.id) AS current_headcount
            FROM departments d
            ORDER BY d.name
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Department getById(int id) throws SQLException {
        String sql = """
            SELECT d.id, d.name, d.description, d.manager_id, d.parent_dept_id,
                   d.max_headcount,
                   (SELECT COUNT(*) FROM employees e WHERE e.department_id = d.id) AS current_headcount
            FROM departments d
            WHERE d.id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void insert(Department d) throws SQLException {
        // current_headcount is omitted — it defaults to 0 in DB and is always recomputed on SELECT
        String sql = """
                INSERT INTO departments
                  (name, description, manager_id, parent_dept_id, max_headcount)
                VALUES (?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getDescription());
            if (d.getManagerId() > 0) ps.setInt(3, d.getManagerId());
            else ps.setNull(3, Types.INTEGER);
            if (d.getParentDeptId() > 0) ps.setInt(4, d.getParentDeptId());
            else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, d.getMaxHeadcount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) d.setId(keys.getInt(1));
            }
        }
    }

    public void update(Department d) throws SQLException {
        // current_headcount is NOT updated here — it is always computed from employees
        String sql = """
                UPDATE departments
                SET name=?, description=?, manager_id=?, parent_dept_id=?, max_headcount=?
                WHERE id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getDescription());
            if (d.getManagerId() > 0) ps.setInt(3, d.getManagerId());
            else ps.setNull(3, Types.INTEGER);
            if (d.getParentDeptId() > 0) ps.setInt(4, d.getParentDeptId());
            else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, d.getMaxHeadcount());
            ps.setInt(6, d.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM departments WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean hasEmployees(int departmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE department_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean hasCapacity(int deptId) throws SQLException {
        // Uses live employee count — immune to stale counter values
        String sql = """
            SELECT d.max_headcount,
                   (SELECT COUNT(*) FROM employees e WHERE e.department_id = d.id) AS actual_count
            FROM departments d WHERE d.id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int max    = rs.getInt("max_headcount");
                    if (max <= 0) return true; // 0 means no limit set → always has capacity
                    int actual = rs.getInt("actual_count");
                    return actual < max;
                }
            }
        }
        return true; // dept not found → don't block
    }

    /** No-op: current_headcount is now computed from employees; no manual counter needed. */
    public void updateHeadcount(int deptId, int delta) {
        // intentionally empty — headcount is derived live from the employees table
    }

    private Department map(ResultSet rs) throws SQLException {
        Department d = new Department();
        d.setId(rs.getInt("id"));
        d.setName(rs.getString("name"));
        d.setDescription(rs.getString("description"));
        d.setManagerId(rs.getInt("manager_id"));
        d.setParentDeptId(rs.getInt("parent_dept_id"));
        d.setMaxHeadcount(rs.getInt("max_headcount"));
        d.setCurrentHeadcount(rs.getInt("current_headcount"));
        return d;
    }
}
