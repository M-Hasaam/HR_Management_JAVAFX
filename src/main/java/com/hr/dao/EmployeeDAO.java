package com.hr.dao;

import com.hr.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    private final Connection conn;

    public EmployeeDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public List<Employee> getAll() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = """
                SELECT e.*, d.name AS dept_name
                FROM employees e
                LEFT JOIN departments d ON e.department_id = d.id
                ORDER BY e.id
                """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Employee getById(int id) throws SQLException {
        String sql = """
                SELECT e.*, d.name AS dept_name
                FROM employees e
                LEFT JOIN departments d ON e.department_id = d.id
                WHERE e.id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void insert(Employee e) throws SQLException {
        String sql = """
                INSERT INTO employees
                  (first_name, last_name, email, phone, national_id, date_of_birth, gender,
                   department_id, designation_id, position, employment_type,
                   basic_salary, hire_date, probation_end_date, status, address)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getFirstName());
            ps.setString(2, e.getLastName());
            ps.setString(3, e.getEmail());
            ps.setString(4, e.getPhone());
            ps.setString(5, e.getNationalId());
            ps.setDate(6, e.getDateOfBirth() != null ? Date.valueOf(e.getDateOfBirth()) : null);
            ps.setString(7, e.getGender());
            ps.setInt(8, e.getDepartmentId());
            if (e.getDesignationId() > 0) ps.setInt(9, e.getDesignationId());
            else ps.setNull(9, Types.INTEGER);
            ps.setString(10, e.getPosition());
            ps.setString(11, e.getEmploymentType());
            ps.setBigDecimal(12, e.getBasicSalary());
            ps.setDate(13, e.getHireDate() != null ? Date.valueOf(e.getHireDate()) : null);
            ps.setDate(14, e.getProbationEndDate() != null ? Date.valueOf(e.getProbationEndDate()) : null);
            ps.setString(15, e.getStatus());
            ps.setString(16, e.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.setId(keys.getInt(1));
            }
        }
    }

    public void update(Employee e) throws SQLException {
        String sql = """
                UPDATE employees
                SET first_name=?, last_name=?, email=?, phone=?,
                    national_id=?, date_of_birth=?, gender=?,
                    department_id=?, designation_id=?, position=?, employment_type=?,
                    basic_salary=?, hire_date=?, probation_end_date=?, status=?,
                    address=?
                WHERE id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getFirstName());
            ps.setString(2, e.getLastName());
            ps.setString(3, e.getEmail());
            ps.setString(4, e.getPhone());
            ps.setString(5, e.getNationalId());
            ps.setDate(6, e.getDateOfBirth() != null ? Date.valueOf(e.getDateOfBirth()) : null);
            ps.setString(7, e.getGender());
            ps.setInt(8, e.getDepartmentId());
            if (e.getDesignationId() > 0) ps.setInt(9, e.getDesignationId());
            else ps.setNull(9, Types.INTEGER);
            ps.setString(10, e.getPosition());
            ps.setString(11, e.getEmploymentType());
            ps.setBigDecimal(12, e.getBasicSalary());
            ps.setDate(13, e.getHireDate() != null ? Date.valueOf(e.getHireDate()) : null);
            ps.setDate(14, e.getProbationEndDate() != null ? Date.valueOf(e.getProbationEndDate()) : null);
            ps.setString(15, e.getStatus());
            ps.setString(16, e.getAddress());
            ps.setInt(17, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM employees WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existsByNationalId(String nationalId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE national_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nationalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE email=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Employee map(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("id"));
        e.setFirstName(rs.getString("first_name"));
        e.setLastName(rs.getString("last_name"));
        e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone"));
        e.setNationalId(rs.getString("national_id"));
        Date dob = rs.getDate("date_of_birth");
        e.setDateOfBirth(dob != null ? dob.toLocalDate() : null);
        e.setGender(rs.getString("gender"));
        e.setDepartmentId(rs.getInt("department_id"));
        e.setDepartmentName(rs.getString("dept_name"));
        e.setDesignationId(rs.getInt("designation_id"));
        e.setPosition(rs.getString("position"));
        e.setEmploymentType(rs.getString("employment_type"));
        e.setBasicSalary(rs.getBigDecimal("basic_salary"));
        Date hd = rs.getDate("hire_date");
        e.setHireDate(hd != null ? hd.toLocalDate() : null);
        Date ped = rs.getDate("probation_end_date");
        e.setProbationEndDate(ped != null ? ped.toLocalDate() : null);
        e.setStatus(rs.getString("status"));
        e.setAddress(rs.getString("address"));
        return e;
    }
}
