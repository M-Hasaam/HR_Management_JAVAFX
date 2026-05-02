package com.hr.dao;

import com.hr.model.EvaluationCycle;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EvaluationCycleDAO {
    private final Connection conn;

    public EvaluationCycleDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public void insert(EvaluationCycle cycle) throws SQLException {
        String sql = """
                INSERT INTO evaluation_cycles
                  (cycle_name, start_date, end_date, evaluation_type,
                   applicable_scope, status, reminder_schedule)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cycle.getCycleName());
            ps.setDate(2, cycle.getStartDate() != null ? Date.valueOf(cycle.getStartDate()) : null);
            ps.setDate(3, cycle.getEndDate() != null ? Date.valueOf(cycle.getEndDate()) : null);
            ps.setString(4, cycle.getEvaluationType());
            ps.setString(5, cycle.getApplicableScope());
            ps.setString(6, cycle.getStatus());
            ps.setString(7, cycle.getReminderSchedule());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) cycle.setId(keys.getInt(1));
            }
        }
    }

    public List<EvaluationCycle> getAll() throws SQLException {
        List<EvaluationCycle> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM evaluation_cycles ORDER BY start_date DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public EvaluationCycle getById(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM evaluation_cycles WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void updateStatus(int cycleId, String status) throws SQLException {
        String sql = "UPDATE evaluation_cycles SET status=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, cycleId);
            ps.executeUpdate();
        }
    }

    /** Ext 2a: Returns any DRAFT or ACTIVE cycles whose period overlaps [start, end). */
    public List<EvaluationCycle> getConflicting(LocalDate start, LocalDate end) throws SQLException {
        String sql = """
                SELECT * FROM evaluation_cycles
                WHERE status IN ('DRAFT','ACTIVE')
                  AND start_date < ? AND end_date > ?
                ORDER BY start_date
                """;
        List<EvaluationCycle> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(end));
            ps.setDate(2, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Ext 3b: Sets the grace period end date for an ACTIVE cycle. */
    public void setGracePeriod(int cycleId, LocalDate until) throws SQLException {
        String sql = "UPDATE evaluation_cycles SET grace_period_until=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, until != null ? Date.valueOf(until) : null);
            ps.setInt(2, cycleId);
            ps.executeUpdate();
        }
    }

    private EvaluationCycle map(ResultSet rs) throws SQLException {
        EvaluationCycle cycle = new EvaluationCycle();
        cycle.setId(rs.getInt("id"));
        cycle.setCycleName(rs.getString("cycle_name"));
        Date sd = rs.getDate("start_date");
        cycle.setStartDate(sd != null ? sd.toLocalDate() : null);
        Date ed = rs.getDate("end_date");
        cycle.setEndDate(ed != null ? ed.toLocalDate() : null);
        cycle.setEvaluationType(rs.getString("evaluation_type"));
        cycle.setApplicableScope(rs.getString("applicable_scope"));
        cycle.setStatus(rs.getString("status"));
        cycle.setReminderSchedule(rs.getString("reminder_schedule"));
        Date gp = rs.getDate("grace_period_until");
        cycle.setGracePeriodUntil(gp != null ? gp.toLocalDate() : null);
        return cycle;
    }
}
