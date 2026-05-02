package com.hr.dao;

import com.hr.model.AttendanceCorrectionRequest;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceCorrectionDAO {
    private final Connection conn;

    public AttendanceCorrectionDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Inserts a new attendance correction request and returns the generated id as String.
     */
    public String create(AttendanceCorrectionRequest req) throws SQLException {
        String sql = """
                INSERT INTO attendance_corrections
                  (attendance_id, employee_id, original_value, corrected_value, justification, status)
                VALUES (?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, req.getAttendanceId());
            ps.setInt(2, req.getEmployeeId());
            ps.setString(3, req.getOriginalValue());
            ps.setString(4, req.getCorrectedValue());
            ps.setString(5, req.getJustification());
            ps.setString(6, req.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return String.valueOf(keys.getInt(1));
            }
        }
        return null;
    }

    public List<AttendanceCorrectionRequest> getAll() throws SQLException {
        List<AttendanceCorrectionRequest> list = new ArrayList<>();
        String sql = """
                SELECT ac.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM attendance_corrections ac
                JOIN employees e ON ac.employee_id = e.id
                ORDER BY ac.id DESC
                """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<AttendanceCorrectionRequest> getByEmployee(int employeeId) throws SQLException {
        List<AttendanceCorrectionRequest> list = new ArrayList<>();
        String sql = """
                SELECT ac.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM attendance_corrections ac
                JOIN employees e ON ac.employee_id = e.id
                WHERE ac.employee_id=?
                ORDER BY ac.id DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void updateStatus(int correctionId, String status, LocalDate reviewDate) throws SQLException {
        String sql = "UPDATE attendance_corrections SET status=?, review_date=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setDate(2, reviewDate != null ? Date.valueOf(reviewDate) : null);
            ps.setInt(3, correctionId);
            ps.executeUpdate();
        }
    }

    private AttendanceCorrectionRequest map(ResultSet rs) throws SQLException {
        AttendanceCorrectionRequest req = new AttendanceCorrectionRequest();
        req.setId(rs.getInt("id"));
        req.setAttendanceId(rs.getInt("attendance_id"));
        req.setEmployeeId(rs.getInt("employee_id"));
        req.setEmployeeName(rs.getString("emp_name"));
        req.setOriginalValue(rs.getString("original_value"));
        req.setCorrectedValue(rs.getString("corrected_value"));
        req.setJustification(rs.getString("justification"));
        req.setStatus(rs.getString("status"));
        Date rd = rs.getDate("review_date");
        req.setReviewDate(rd != null ? rd.toLocalDate() : null);
        return req;
    }
}
