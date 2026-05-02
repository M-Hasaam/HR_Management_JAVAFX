package com.hr.dao;

import com.hr.model.HRAnalyticsReport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HRAnalyticsReportDAO {
    private final Connection conn;

    public HRAnalyticsReportDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public void insert(HRAnalyticsReport report) throws SQLException {
        String sql = """
                INSERT INTO hr_analytics_reports
                  (creator_id, report_period, generated_at, summary_metrics)
                VALUES (?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (report.getCreatorId() > 0) ps.setInt(1, report.getCreatorId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, report.getReportPeriod());
            ps.setTimestamp(3, report.getGeneratedAt() != null
                    ? Timestamp.valueOf(report.getGeneratedAt()) : null);
            ps.setString(4, report.getSummaryMetrics());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) report.setId(keys.getInt(1));
            }
        }
    }

    public List<HRAnalyticsReport> getAll() throws SQLException {
        List<HRAnalyticsReport> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM hr_analytics_reports ORDER BY generated_at DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private HRAnalyticsReport map(ResultSet rs) throws SQLException {
        HRAnalyticsReport report = new HRAnalyticsReport();
        report.setId(rs.getInt("id"));
        report.setCreatorId(rs.getInt("creator_id"));
        report.setReportPeriod(rs.getString("report_period"));
        Timestamp ga = rs.getTimestamp("generated_at");
        report.setGeneratedAt(ga != null ? ga.toLocalDateTime() : null);
        report.setSummaryMetrics(rs.getString("summary_metrics"));
        return report;
    }
}
