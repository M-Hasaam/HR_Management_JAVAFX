package com.hr.controller;
// GRASP Pattern: Controller — handles UC-08 Record Daily Attendance.
// Participants: AttendanceRecord (Creator/Information Expert),
//              PayrollNotifier (Pure Fabrication, uses Adapter internally)
//
// Justification: Separating UC-08 business logic from the JavaFX UI controller
// completes the 3-tier architecture: UI → SD Controller → Service/DAO → DB.
// The UI layer should never contain business rules (attendance status derivation,
// payroll notification); those live here.

import com.hr.dao.AttendanceRecordDAO;
import com.hr.model.AttendanceRecord;
import com.hr.service.PayrollNotifier;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;

public class AttendanceRecordController {

    private final AttendanceRecordDAO attendanceDAO;
    private final PayrollNotifier     payrollNotifier; // Pure Fabrication + Adapter

    public AttendanceRecordController() throws SQLException {
        this.attendanceDAO   = new AttendanceRecordDAO();
        this.payrollNotifier = new PayrollNotifier();
    }

    /**
     * UC-08: Record employee check-in.
     * Creates an attendance record and notifies payroll that a session has started.
     *
     * @param employeeId ID of the employee checking in
     * @param timestamp  check-in timestamp
     * @param method     recording method (BIOMETRIC, MANUAL, CARD, MOBILE)
     * @return generated attendance record ID string
     * @throws SQLException on database error
     */
    public String recordCheckIn(int employeeId, LocalDateTime timestamp, String method)
            throws SQLException {
        String recordId = attendanceDAO.create(employeeId, timestamp);
        payrollNotifier.notifyPayrollAttendance(employeeId, 0.0); // session started
        System.out.println("[ATTENDANCE] Check-in recorded: Employee " + employeeId
                + " | Method: " + method + " | Time: " + timestamp);
        return recordId;
    }

    /**
     * UC-08: Record employee check-out.
     * Computes total hours, derives attendance status, updates the record, and notifies payroll.
     *
     * Business Rules:
     * - hours >= 8.0 → PRESENT
     * - hours >= 4.0 → HALF_DAY
     * - hours < 4.0  → EARLY_DEPARTURE
     *
     * @param employeeId ID of the employee checking out
     * @param timestamp  check-out timestamp
     * @throws SQLException         on database error
     * @throws IllegalStateException if no check-in record exists for today
     */
    public void recordCheckOut(int employeeId, LocalDateTime timestamp)
            throws SQLException {
        AttendanceRecord today = attendanceDAO.getTodayRecord(employeeId);
        if (today == null) {
            throw new IllegalStateException(
                    "No check-in record found for today. Cannot check out.");
        }
        if (today.getCheckInTime() == null) {
            throw new IllegalStateException("Check-in time is missing for today's record.");
        }

        long minutes = Duration.between(today.getCheckInTime(), timestamp).toMinutes();
        double hours = minutes / 60.0;

        String status;
        if      (hours >= 8.0) status = "PRESENT";
        else if (hours >= 4.0) status = "HALF_DAY";
        else                   status = "EARLY_DEPARTURE";

        attendanceDAO.updateCheckOut(today.getId(), timestamp, hours, status);
        payrollNotifier.notifyPayrollAttendance(today.getId(), hours);

        System.out.println("[ATTENDANCE] Check-out recorded: Employee " + employeeId
                + " | Hours: " + String.format("%.2f", hours) + " | Status: " + status);
    }

    /** Retrieves today's attendance record for an employee, or null if not checked in. */
    public AttendanceRecord getTodayRecord(int employeeId) throws SQLException {
        return attendanceDAO.getTodayRecord(employeeId);
    }
}
