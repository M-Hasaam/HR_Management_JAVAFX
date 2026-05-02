package com.hr.dao;

import com.hr.model.OffboardingWorkflow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffboardingWorkflowDAO {
    private final Connection conn;

    public OffboardingWorkflowDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public void insert(OffboardingWorkflow wf) throws SQLException {
        String sql = """
                INSERT INTO offboarding_workflows
                  (employee_id, separation_type, hire_date, last_working_date, exit_reason,
                   status, checklist_items, final_settlement_status)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, wf.getEmployeeId());
            ps.setString(2, wf.getSeparationType());
            ps.setDate(3, wf.getHireDate() != null ? Date.valueOf(wf.getHireDate()) : null);
            ps.setDate(4, wf.getLastWorkingDate() != null ? Date.valueOf(wf.getLastWorkingDate()) : null);
            ps.setString(5, wf.getExitReason());
            ps.setString(6, wf.getStatus());
            ps.setString(7, wf.getChecklistItems());
            ps.setString(8, wf.getFinalSettlementStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) wf.setId(keys.getInt(1));
            }
        }
    }

    public List<OffboardingWorkflow> getAll() throws SQLException {
        List<OffboardingWorkflow> list = new ArrayList<>();
        String sql = """
                SELECT ow.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM offboarding_workflows ow
                JOIN employees e ON ow.employee_id = e.id
                ORDER BY ow.id DESC
                """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public OffboardingWorkflow getByEmployee(int employeeId) throws SQLException {
        String sql = """
                SELECT ow.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM offboarding_workflows ow
                JOIN employees e ON ow.employee_id = e.id
                WHERE ow.employee_id=?
                ORDER BY ow.id DESC
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

    public void updateStatus(int workflowId, String status) throws SQLException {
        String sql = "UPDATE offboarding_workflows SET status=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, workflowId);
            ps.executeUpdate();
        }
    }

    private OffboardingWorkflow map(ResultSet rs) throws SQLException {
        OffboardingWorkflow wf = new OffboardingWorkflow();
        wf.setId(rs.getInt("id"));
        wf.setEmployeeId(rs.getInt("employee_id"));
        wf.setEmployeeName(rs.getString("emp_name"));
        wf.setSeparationType(rs.getString("separation_type"));
        Date hd = rs.getDate("hire_date");
        wf.setHireDate(hd != null ? hd.toLocalDate() : null);
        Date lwd = rs.getDate("last_working_date");
        wf.setLastWorkingDate(lwd != null ? lwd.toLocalDate() : null);
        wf.setExitReason(rs.getString("exit_reason"));
        wf.setStatus(rs.getString("status"));
        wf.setChecklistItems(rs.getString("checklist_items"));
        wf.setFinalSettlementStatus(rs.getString("final_settlement_status"));
        return wf;
    }
}
