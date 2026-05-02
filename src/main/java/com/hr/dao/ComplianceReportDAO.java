package com.hr.dao;

import com.hr.model.ComplianceReport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplianceReportDAO {
    private final Connection conn;

    public ComplianceReportDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public void insert(ComplianceReport report) throws SQLException {
        String sql = """
                INSERT INTO compliance_reports
                  (report_type, generated_at, generated_by, parameters, format, status, archive_path)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, report.getReportType());
            ps.setTimestamp(2, report.getGeneratedAt() != null
                    ? Timestamp.valueOf(report.getGeneratedAt()) : null);
            ps.setInt(3, report.getGeneratedBy());
            ps.setString(4, report.getParameters());
            ps.setString(5, report.getFormat());
            ps.setString(6, report.getStatus());
            ps.setString(7, report.getArchivePath());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) report.setId(keys.getInt(1));
            }
        }
    }

    public List<ComplianceReport> getAll() throws SQLException {
        List<ComplianceReport> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM compliance_reports ORDER BY generated_at DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private ComplianceReport map(ResultSet rs) throws SQLException {
        ComplianceReport report = new ComplianceReport();
        report.setId(rs.getInt("id"));
        report.setReportType(rs.getString("report_type"));
        Timestamp ga = rs.getTimestamp("generated_at");
        report.setGeneratedAt(ga != null ? ga.toLocalDateTime() : null);
        report.setGeneratedBy(rs.getInt("generated_by"));
        report.setParameters(rs.getString("parameters"));
        report.setFormat(rs.getString("format"));
        report.setStatus(rs.getString("status"));
        report.setArchivePath(rs.getString("archive_path"));
        return report;
    }
}
