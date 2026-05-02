package com.hr.dao;

import com.hr.model.AttendanceRecord;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceRecordDAO {
    private final Connection conn;

    public AttendanceRecordDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Inserts a new attendance record with a check-in time and returns the generated id as String.
     */
    public String create(int employeeId, LocalDateTime checkInTime) throws SQLException {
        String sql = """
                INSERT INTO attendance_records
                  (employee_id, attendance_date, check_in_time, attendance_status, correction_flag)
                VALUES (?, ?, ?, 'PRESENT', FALSE)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(checkInTime.toLocalDate()));
            ps.setTimestamp(3, Timestamp.valueOf(checkInTime));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return String.valueOf(keys.getInt(1));
            }
        }
        return null;
    }

    /**
     * Updates the check-out time, total hours, and final status for an existing record.
     */
    public void updateCheckOut(int attendanceId, LocalDateTime checkOutTime,
                               double totalHours, String status) throws SQLException {
        String sql = """
                UPDATE attendance_records
                SET check_out_time=?, total_hours=?, attendance_status=?
                WHERE id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(checkOutTime));
            ps.setDouble(2, totalHours);
            ps.setString(3, status);
            ps.setInt(4, attendanceId);
            ps.executeUpdate();
        }
    }

    public List<AttendanceRecord> getByEmployee(int employeeId) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = """
                SELECT ar.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM attendance_records ar
                JOIN employees e ON ar.employee_id = e.id
                WHERE ar.employee_id=?
                ORDER BY ar.attendance_date DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<AttendanceRecord> getByEmployeeAndDateRange(int employeeId,
                                                             LocalDate start, LocalDate end) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = """
                SELECT ar.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM attendance_records ar
                JOIN employees e ON ar.employee_id = e.id
                WHERE ar.employee_id=? AND ar.attendance_date BETWEEN ? AND ?
                ORDER BY ar.attendance_date DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public AttendanceRecord getById(int id) throws SQLException {
        String sql = """
                SELECT ar.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM attendance_records ar
                JOIN employees e ON ar.employee_id = e.id
                WHERE ar.id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /** Returns today's attendance record for the given employee, or null if none exists. */
    public AttendanceRecord getTodayRecord(int employeeId) throws SQLException {
        String sql = """
                SELECT ar.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM attendance_records ar
                JOIN employees e ON ar.employee_id = e.id
                WHERE ar.attendance_date = CURRENT_DATE AND ar.employee_id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /**
     * Returns the total number of attendance records stored in the system.
     * Kept for compatibility with ComplianceDataAgg / AnalyticsAggregator.
     */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance_records";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLSyntaxErrorException e) {
            System.err.println("[AttendanceRecordDAO] Table attendance_records not found, returning 0.");
        }
        return 0;
    }

    public int countByPeriod(String period) throws SQLException {
        return countAll();
    }

    public double getAttendanceRate() throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance_records WHERE attendance_status <> 'ABSENT'";
        int present = 0;
        int total = countAll();
        if (total == 0) return 100.0;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) present = rs.getInt(1);
        } catch (SQLSyntaxErrorException e) {
            System.err.println("[AttendanceRecordDAO] Table attendance_records not found.");
        }
        return total > 0 ? (present * 100.0 / total) : 100.0;
    }

    private AttendanceRecord map(ResultSet rs) throws SQLException {
        AttendanceRecord ar = new AttendanceRecord();
        ar.setId(rs.getInt("id"));
        ar.setEmployeeId(rs.getInt("employee_id"));
        ar.setEmployeeName(rs.getString("emp_name"));
        Date ad = rs.getDate("attendance_date");
        ar.setAttendanceDate(ad != null ? ad.toLocalDate() : null);
        Timestamp ci = rs.getTimestamp("check_in_time");
        ar.setCheckInTime(ci != null ? ci.toLocalDateTime() : null);
        Timestamp co = rs.getTimestamp("check_out_time");
        ar.setCheckOutTime(co != null ? co.toLocalDateTime() : null);
        ar.setTotalHours(rs.getDouble("total_hours"));
        ar.setAttendanceStatus(rs.getString("attendance_status"));
        ar.setCorrectionFlag(rs.getBoolean("correction_flag"));
        return ar;
    }
}
