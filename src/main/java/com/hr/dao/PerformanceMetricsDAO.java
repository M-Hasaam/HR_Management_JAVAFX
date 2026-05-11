package com.hr.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PerformanceMetricsDAO {
    private final Connection conn;

    public PerformanceMetricsDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public void saveOperation(String operationName, long elapsedMs, boolean slaBreached,
            int totalOperations, long totalTimeMs,
            double averageResponseTimeMs) throws SQLException {
        String sql = """
                INSERT INTO performance_metrics
                  (operation_name, elapsed_ms, sla_breached, total_operations,
                   total_time_ms, average_response_time_ms, recorded_at)
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, operationName);
            ps.setLong(2, elapsedMs);
            ps.setBoolean(3, slaBreached);
            ps.setInt(4, totalOperations);
            ps.setLong(5, totalTimeMs);
            ps.setDouble(6, averageResponseTimeMs);
            ps.executeUpdate();
        }
    }
}