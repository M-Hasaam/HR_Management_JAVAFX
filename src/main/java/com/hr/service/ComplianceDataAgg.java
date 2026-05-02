package com.hr.service;

// GRASP Pattern: Information Expert — owns and aggregates compliance data.
// This class holds references to the DAOs that contain the compliance-relevant
// information, making it the natural expert for computing compliance summaries
// without scattering aggregation logic across multiple controllers.

import com.hr.dao.AttendanceRecordDAO;
import com.hr.dao.LeaveRequestDAO;

import java.sql.SQLException;

public class ComplianceDataAgg {

    private final LeaveRequestDAO    leaveDAO;
    private final AttendanceRecordDAO attendanceDAO;

    public ComplianceDataAgg() throws SQLException {
        this.leaveDAO      = new LeaveRequestDAO();
        this.attendanceDAO = new AttendanceRecordDAO();
    }

    /**
     * Aggregates compliance data for the specified domain and period.
     *
     * @param domain  One of "LEAVE", "ATTENDANCE", or "ALL"
     * @param period  Period descriptor (e.g. "2025-Q1", "2025-04")
     * @return A human-readable compliance summary string
     * @throws SQLException if a database error occurs
     */
    public String aggregateComplianceData(String domain, String period) throws SQLException {
        int count = switch (domain.toUpperCase()) {
            case "LEAVE"      -> leaveDAO.getAll().size();
            case "ATTENDANCE" -> attendanceDAO.countByPeriod(period);
            case "ALL"        -> leaveDAO.getAll().size() + attendanceDAO.countByPeriod(period);
            default -> {
                System.err.println("[ComplianceDataAgg] Unknown domain: " + domain
                        + " — returning 0.");
                yield 0;
            }
        };
        return "Compliance data for " + domain + " period " + period + ": " + count + " records";
    }
}
