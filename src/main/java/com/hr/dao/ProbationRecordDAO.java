package com.hr.dao;

import com.hr.model.ProbationRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProbationRecordDAO {
    private final Connection conn;

    public ProbationRecordDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public void insert(ProbationRecord rec) throws SQLException {
        String sql = """
                INSERT INTO probation_records
                  (employee_id, start_date, end_date, extensions, decision, decision_date,
                   reason, status, decision_made_by, notes)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rec.getEmployeeId());
            ps.setDate(2, rec.getStartDate() != null ? Date.valueOf(rec.getStartDate()) : null);
            ps.setDate(3, rec.getEndDate() != null ? Date.valueOf(rec.getEndDate()) : null);
            ps.setInt(4, rec.getExtensions());
            ps.setString(5, rec.getDecision());
            ps.setDate(6, rec.getDecisionDate() != null ? Date.valueOf(rec.getDecisionDate()) : null);
            ps.setString(7, rec.getReason());
            ps.setString(8, rec.getStatus());
            ps.setInt(9, rec.getDecisionMadeBy());
            ps.setString(10, rec.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) rec.setId(keys.getInt(1));
            }
        }
    }

    /** Step 1: Returns only ACTIVE probation records for the dashboard. */
    public List<ProbationRecord> getActive() throws SQLException {
        List<ProbationRecord> list = new ArrayList<>();
        String sql = """
                SELECT pr.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM probation_records pr
                JOIN employees e ON pr.employee_id = e.id
                WHERE pr.status = 'ACTIVE'
                ORDER BY pr.end_date ASC
                """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Ext 1a: Returns the N most recently closed probation records. */
    public List<ProbationRecord> getRecentlyClosed(int limit) throws SQLException {
        List<ProbationRecord> list = new ArrayList<>();
        String sql = """
                SELECT pr.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM probation_records pr
                JOIN employees e ON pr.employee_id = e.id
                WHERE pr.status = 'CLOSED'
                ORDER BY pr.decision_date DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Ext 3a: Extends probation — updates end_date, increments extensions count, keeps ACTIVE. */
    public void extend(int probationId, LocalDate newEndDate, String notes) throws SQLException {
        String sql = """
                UPDATE probation_records
                SET end_date=?, extensions=extensions+1, decision='EXTENDED',
                    decision_date=?, notes=?
                WHERE id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(newEndDate));
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setString(3, notes);
            ps.setInt(4, probationId);
            ps.executeUpdate();
        }
    }

    public List<ProbationRecord> getAll() throws SQLException {
        List<ProbationRecord> list = new ArrayList<>();
        String sql = """
                SELECT pr.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM probation_records pr
                JOIN employees e ON pr.employee_id = e.id
                ORDER BY pr.id DESC
                """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public ProbationRecord getByEmployee(int employeeId) throws SQLException {
        String sql = """
                SELECT pr.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM probation_records pr
                JOIN employees e ON pr.employee_id = e.id
                WHERE pr.employee_id=?
                ORDER BY pr.id DESC
                LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /** CONFIRMED or TERMINATED: records decision, sets status=CLOSED, stores reason. */
    public void recordDecision(int probationId, String decision,
                               LocalDate decisionDate, String notes,
                               String reason) throws SQLException {
        String sql = """
                UPDATE probation_records
                SET decision=?, decision_date=?, notes=?, reason=?, status='CLOSED'
                WHERE id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, decision);
            ps.setDate(2, decisionDate != null ? Date.valueOf(decisionDate) : null);
            ps.setString(3, notes);
            ps.setString(4, reason);
            ps.setInt(5, probationId);
            ps.executeUpdate();
        }
    }

    private ProbationRecord map(ResultSet rs) throws SQLException {
        ProbationRecord rec = new ProbationRecord();
        rec.setId(rs.getInt("id"));
        rec.setEmployeeId(rs.getInt("employee_id"));
        rec.setEmployeeName(rs.getString("emp_name"));
        Date sd = rs.getDate("start_date");
        rec.setStartDate(sd != null ? sd.toLocalDate() : null);
        Date ed = rs.getDate("end_date");
        rec.setEndDate(ed != null ? ed.toLocalDate() : null);
        rec.setExtensions(rs.getInt("extensions"));
        rec.setDecision(rs.getString("decision"));
        Date dd = rs.getDate("decision_date");
        rec.setDecisionDate(dd != null ? dd.toLocalDate() : null);
        rec.setReason(rs.getString("reason"));
        rec.setStatus(rs.getString("status"));
        rec.setDecisionMadeBy(rs.getInt("decision_made_by"));
        rec.setNotes(rs.getString("notes"));
        return rec;
    }
}
