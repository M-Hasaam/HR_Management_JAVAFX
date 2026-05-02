package com.hr.dao;

import com.hr.model.LeaveRequest;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LeaveRequestDAO {
    private final Connection conn;

    public LeaveRequestDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public List<LeaveRequest> getAll() throws SQLException {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = """
                SELECT lr.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM leave_requests lr
                JOIN employees e ON lr.employee_id = e.id
                ORDER BY lr.applied_date DESC
                """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void insert(LeaveRequest lr) throws SQLException {
        String sql = """
                INSERT INTO leave_requests
                  (employee_id, leave_type, start_date, end_date, days_requested,
                   reason, status, applied_date, approved_by, approved_date, comments, document_path)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lr.getEmployeeId());
            ps.setString(2, lr.getLeaveType());
            ps.setDate(3, Date.valueOf(lr.getStartDate()));
            ps.setDate(4, Date.valueOf(lr.getEndDate()));
            ps.setInt(5, lr.getDaysRequested());
            ps.setString(6, lr.getReason());
            ps.setString(7, lr.getStatus());
            ps.setDate(8, Date.valueOf(lr.getAppliedDate()));
            ps.setString(9, lr.getApprovedBy());
            ps.setDate(10, lr.getApprovedDate() != null ? Date.valueOf(lr.getApprovedDate()) : null);
            ps.setString(11, lr.getComments());
            ps.setString(12, lr.getDocumentPath());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) lr.setId(keys.getInt(1));
            }
        }
    }

    public LeaveRequest getById(int id) throws SQLException {
        String sql = """
                SELECT lr.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM leave_requests lr
                JOIN employees e ON lr.employee_id = e.id
                WHERE lr.id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE leave_requests SET status=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void updateApproval(int id, String approvedBy, String comments) throws SQLException {
        String sql = "UPDATE leave_requests SET approved_by=?, approved_date=CURRENT_DATE, comments=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, approvedBy);
            ps.setString(2, comments);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    /** Attaches a document reference to a PENDING_DOCUMENT request and moves it to PENDING. */
    public void submitDocument(int id, String documentPath) throws SQLException {
        String sql = "UPDATE leave_requests SET document_path=?, status='PENDING' WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentPath);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM leave_requests WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── UC-06 Alternative Flow queries ───────────────────────────────────────

    /** 2c: Returns PENDING/APPROVED/PENDING_DOCUMENT requests that overlap [start, end] for the employee. */
    public List<LeaveRequest> getOverlappingRequests(int employeeId, LocalDate start, LocalDate end)
            throws SQLException {
        String sql = """
                SELECT lr.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM leave_requests lr
                JOIN employees e ON lr.employee_id = e.id
                WHERE lr.employee_id = ?
                  AND lr.status IN ('PENDING','APPROVED','PENDING_DOCUMENT')
                  AND lr.start_date <= ? AND lr.end_date >= ?
                ORDER BY lr.start_date
                """;
        List<LeaveRequest> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(end));
            ps.setDate(3, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** 2b: Returns public holidays (date → description) that fall within [start, end].
     *  Returns empty map gracefully if the public_holidays table does not yet exist. */
    public Map<LocalDate, String> getHolidayMapInRange(LocalDate start, LocalDate end)
            throws SQLException {
        Map<LocalDate, String> result = new LinkedHashMap<>();
        String sql = "SELECT holiday_date, description FROM public_holidays " +
                     "WHERE holiday_date BETWEEN ? AND ? ORDER BY holiday_date";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.put(rs.getDate("holiday_date").toLocalDate(), rs.getString("description"));
            }
        } catch (SQLException e) {
            // Table doesn't exist yet — treat as no holidays rather than crashing
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("doesn't exist")) {
                return result;
            }
            throw e;
        }
        return result;
    }

    /** 3c: Returns probation_end_date for the employee, or null if none / column missing. */
    public LocalDate getEmployeeProbationEndDate(int employeeId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT probation_end_date FROM employees WHERE id = ?")) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date d = rs.getDate("probation_end_date");
                    return d != null ? d.toLocalDate() : null;
                }
            }
        }
        return null;
    }

    // ── Balance methods ──────────────────────────────────────────────────────

    public int[] getBalance(int employeeId) throws SQLException {
        String sql = "SELECT annual_balance, sick_balance, personal_balance " +
                     "FROM leave_balances WHERE employee_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new int[]{rs.getInt(1), rs.getInt(2), rs.getInt(3)};
            }
        }
        return new int[]{0, 0, 0};
    }

    public void deductBalance(int employeeId, String leaveType, int days) throws SQLException {
        String column = switch (leaveType) {
            case "ANNUAL"   -> "annual_balance";
            case "SICK"     -> "sick_balance";
            case "PERSONAL" -> "personal_balance";
            default -> throw new SQLException("Unknown leave type: " + leaveType);
        };
        String sql = "UPDATE leave_balances SET " + column + " = " + column + " - ? WHERE employee_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            ps.setInt(2, employeeId);
            ps.executeUpdate();
        }
    }

    public void restoreBalance(int employeeId, String leaveType, int days) throws SQLException {
        String column = switch (leaveType) {
            case "ANNUAL"   -> "annual_balance";
            case "SICK"     -> "sick_balance";
            case "PERSONAL" -> "personal_balance";
            default -> throw new SQLException("Unknown leave type: " + leaveType);
        };
        String sql = "UPDATE leave_balances SET " + column + " = " + column + " + ? WHERE employee_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            ps.setInt(2, employeeId);
            ps.executeUpdate();
        }
    }

    private LeaveRequest map(ResultSet rs) throws SQLException {
        LeaveRequest lr = new LeaveRequest();
        lr.setId(rs.getInt("id"));
        lr.setEmployeeId(rs.getInt("employee_id"));
        lr.setEmployeeName(rs.getString("emp_name"));
        lr.setLeaveType(rs.getString("leave_type"));
        lr.setStartDate(rs.getDate("start_date").toLocalDate());
        lr.setEndDate(rs.getDate("end_date").toLocalDate());
        lr.setDaysRequested(rs.getInt("days_requested"));
        lr.setReason(rs.getString("reason"));
        lr.setStatus(rs.getString("status"));
        lr.setAppliedDate(rs.getDate("applied_date").toLocalDate());
        lr.setApprovedBy(rs.getString("approved_by"));
        Date appDate = rs.getDate("approved_date");
        lr.setApprovedDate(appDate != null ? appDate.toLocalDate() : null);
        lr.setComments(rs.getString("comments"));
        try { lr.setDocumentPath(rs.getString("document_path")); } catch (SQLException ignored) {}
        return lr;
    }
}
