package com.hr.dao;

import com.hr.model.PerformanceEvaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerformanceEvaluationDAO {
    private final Connection conn;

    public PerformanceEvaluationDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public void insert(PerformanceEvaluation eval) throws SQLException {
        String sql = """
                INSERT INTO performance_evaluations
                  (employee_id, evaluator_id, cycle_id, evaluation_date,
                   aggregate_score, remarks, status)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, eval.getEmployeeId());
            if (eval.getEvaluatorId() > 0) ps.setInt(2, eval.getEvaluatorId());
            else ps.setNull(2, Types.INTEGER);
            if (eval.getCycleId() > 0) ps.setInt(3, eval.getCycleId());
            else ps.setNull(3, Types.INTEGER);
            ps.setDate(4, eval.getEvaluationDate() != null ? Date.valueOf(eval.getEvaluationDate()) : null);
            ps.setDouble(5, eval.getAggregateScore());
            ps.setString(6, eval.getRemarks());
            ps.setString(7, eval.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) eval.setId(keys.getInt(1));
            }
        }
    }

    public List<PerformanceEvaluation> getAll() throws SQLException {
        List<PerformanceEvaluation> list = new ArrayList<>();
        String sql = """
                SELECT pe.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM performance_evaluations pe
                JOIN employees e ON pe.employee_id = e.id
                ORDER BY pe.evaluation_date DESC
                """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<PerformanceEvaluation> getByEmployee(int employeeId) throws SQLException {
        List<PerformanceEvaluation> list = new ArrayList<>();
        String sql = """
                SELECT pe.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM performance_evaluations pe
                JOIN employees e ON pe.employee_id = e.id
                WHERE pe.employee_id=?
                ORDER BY pe.evaluation_date DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<PerformanceEvaluation> getByCycle(int cycleId) throws SQLException {
        List<PerformanceEvaluation> list = new ArrayList<>();
        String sql = """
                SELECT pe.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM performance_evaluations pe
                JOIN employees e ON pe.employee_id = e.id
                WHERE pe.cycle_id=?
                ORDER BY pe.evaluation_date DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cycleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Special Req: returns true if a SUBMITTED evaluation already exists for this employee+cycle. */
    public boolean existsForEmployeeCycle(int employeeId, int cycleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM performance_evaluations " +
                     "WHERE employee_id=? AND cycle_id=? AND status='SUBMITTED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, cycleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Ext 4a: returns average aggregate score for all SUBMITTED evaluations in a cycle.
     *  Returns 0 if no prior submissions exist (outlier check is skipped). */
    public double getAverageByCycle(int cycleId) throws SQLException {
        String sql = "SELECT AVG(aggregate_score) FROM performance_evaluations " +
                     "WHERE cycle_id=? AND status='SUBMITTED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cycleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    return rs.wasNull() ? 0 : avg;
                }
            }
        }
        return 0;
    }

    private PerformanceEvaluation map(ResultSet rs) throws SQLException {
        PerformanceEvaluation eval = new PerformanceEvaluation();
        eval.setId(rs.getInt("id"));
        eval.setEmployeeId(rs.getInt("employee_id"));
        eval.setEmployeeName(rs.getString("emp_name"));
        eval.setEvaluatorId(rs.getInt("evaluator_id"));
        eval.setCycleId(rs.getInt("cycle_id"));
        Date ed = rs.getDate("evaluation_date");
        eval.setEvaluationDate(ed != null ? ed.toLocalDate() : null);
        eval.setAggregateScore(rs.getDouble("aggregate_score"));
        eval.setRemarks(rs.getString("remarks"));
        eval.setStatus(rs.getString("status"));
        return eval;
    }
}
