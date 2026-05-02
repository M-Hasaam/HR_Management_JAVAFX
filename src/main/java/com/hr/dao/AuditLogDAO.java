package com.hr.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuditLogDAO {
    private final Connection conn;

    public AuditLogDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Writes one audit log record.
     *
     * @param userId     ID of the user performing the action
     * @param action     Action label (e.g. "UPDATE", "READ", "SUBMIT_EVALUATION")
     * @param entityType Name of the affected entity (e.g. "Employee", "AttendanceCorrection")
     * @param entityId   ID of the affected entity row
     * @param fieldName  Field / sub-key affected (nullable)
     * @param oldValue   Previous value (nullable)
     * @param newValue   New / current value (nullable)
     */
    public void log(int userId, String action, String entityType, int entityId,
                    String fieldName, String oldValue, String newValue) throws SQLException {
        String sql = """
                INSERT INTO audit_log
                  (user_id, action, entity_type, entity_id, field_name, old_value, new_value, timestamp)
                VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, entityType);
            ps.setInt(4, entityId);
            ps.setString(5, fieldName);
            ps.setString(6, oldValue);
            ps.setString(7, newValue);
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLFeatureNotSupportedException e) {
            // Table may not exist in development — log to console instead.
            System.err.println("[AUDIT LOG] user=" + userId + " action=" + action
                    + " entity=" + entityType + "#" + entityId
                    + " field=" + fieldName + " old=" + oldValue + " new=" + newValue);
        }
    }

    public List<Map<String, Object>> getAll() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY timestamp DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                list.add(row);
            }
        } catch (SQLSyntaxErrorException e) {
            System.err.println("[AuditLogDAO] Table audit_log not found, returning empty list.");
        }
        return list;
    }
}
