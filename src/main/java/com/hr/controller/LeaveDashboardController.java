package com.hr.controller;
// GRASP Pattern: Controller — handles UC-10 View Leave Balance and Attendance Summary.
// Participants: LeaveBalance (Information Expert), AttendanceRecord (Information Expert),
//              ReportGenerator (Pure Fabrication, uses Strategy pattern)
//
// Justification: Dashboard data assembly (combining leave balance + attendance KPIs)
// is a business-logic concern, not a UI rendering concern.  Moving it here enforces
// the 3-tier separation and makes this logic independently testable.

import com.hr.dao.AttendanceRecordDAO;
import com.hr.model.AttendanceRecord;
import com.hr.service.LeaveRequestService;
import com.hr.service.ReportGenerator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LeaveDashboardController {

    private final LeaveRequestService leaveService;
    private final AttendanceRecordDAO attendanceDAO;
    private final ReportGenerator     reportGenerator; // Pure Fabrication + Strategy

    public LeaveDashboardController() throws SQLException {
        this.leaveService    = new LeaveRequestService();
        this.attendanceDAO   = new AttendanceRecordDAO();
        this.reportGenerator = new ReportGenerator();
    }

    /**
     * UC-10: Retrieve the leave balance for a given employee.
     *
     * @param employeeId ID of the employee
     * @return int[3] — {annualBalance, sickBalance, personalBalance}
     * @throws SQLException on database error
     */
    public int[] getLeaveBalance(int employeeId) throws SQLException {
        return leaveService.getLeaveBalance(employeeId);
    }

    /**
     * UC-10: Retrieve attendance records for an employee within an optional date range.
     * If dateRange is null or incomplete, returns all records.
     *
     * @param employeeId ID of the employee
     * @param dateRange  optional two-element array [startDate, endDate]
     * @return list of AttendanceRecord objects
     * @throws SQLException on database error
     */
    public List<AttendanceRecord> getAttendanceSummary(int employeeId, LocalDate[] dateRange)
            throws SQLException {
        if (dateRange != null && dateRange.length >= 2
                && dateRange[0] != null && dateRange[1] != null) {
            return attendanceDAO.getByEmployeeAndDateRange(employeeId, dateRange[0], dateRange[1]);
        }
        return attendanceDAO.getByEmployee(employeeId);
    }

    /**
     * UC-10: Generate a combined leave + attendance report in the requested format.
     * Uses the Strategy pattern via ReportGenerator (PDF / Excel / CSV).
     *
     * @param employeeId ID of the employee
     * @param format     output format ("PDF", "EXCEL", "CSV")
     * @return formatted report string
     * @throws SQLException on database error
     */
    public String generateDashboardReport(int employeeId, String format) throws SQLException {
        int[] balance = leaveService.getLeaveBalance(employeeId);
        List<AttendanceRecord> records = attendanceDAO.getByEmployee(employeeId);
        return reportGenerator.generateReport(balance, records, format);
    }
}
