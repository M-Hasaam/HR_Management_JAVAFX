package com.hr.controller;
// GRASP Pattern: Controller — handles UC-09 Request Attendance Correction.
// Participants: AttendanceCorrectionRequest (Creator/Information Expert),
//              AuditLogService (Pure Fabrication, Observer subscriber)
//
// Business Rules enforced here:
// 1. Corrected value must differ from the original value.
// 2. Justification must not be blank.
// 3. Every correction attempt is logged in the audit trail (compliance requirement).

import com.hr.dao.AttendanceCorrectionDAO;
import com.hr.dao.AttendanceRecordDAO;
import com.hr.model.AttendanceCorrectionRequest;
import com.hr.model.AttendanceRecord;
import com.hr.service.AuditLogService;

import java.sql.SQLException;

public class AttendanceCorrectionController {

    private final AttendanceCorrectionDAO correctionDAO;
    private final AttendanceRecordDAO     attendanceDAO;
    private final AuditLogService         auditLogService; // Pure Fabrication

    public AttendanceCorrectionController() throws SQLException {
        this.correctionDAO  = new AttendanceCorrectionDAO();
        this.attendanceDAO  = new AttendanceRecordDAO();
        this.auditLogService = new AuditLogService();
    }

    /**
     * UC-09: Submit an attendance correction request.
     * Validates the request, persists it, and logs the attempt in the audit trail.
     *
     * @param attendanceId ID of the attendance record to correct
     * @param field        the field being corrected (e.g. "check_in_time")
     * @param originalVal  current (incorrect) value
     * @param correctedVal new (correct) value
     * @param reason       justification provided by the employee
     * @throws IllegalArgumentException if corrected value is blank or same as original
     * @throws SQLException             on database error
     */
    public void submitCorrectionRequest(int attendanceId, String field,
                                        String originalVal, String correctedVal,
                                        String reason) throws SQLException {
        if (correctedVal == null || correctedVal.isBlank()) {
            throw new IllegalArgumentException("Corrected value must not be blank.");
        }
        if (correctedVal.equals(originalVal)) {
            throw new IllegalArgumentException(
                    "Corrected value must differ from the original value.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Justification must not be blank.");
        }

        AttendanceCorrectionRequest req = new AttendanceCorrectionRequest();
        req.setAttendanceId(attendanceId);
        req.setOriginalValue(originalVal);
        req.setCorrectedValue(correctedVal);
        req.setJustification(reason);
        req.setStatus("PENDING");

        AttendanceRecord ar = attendanceDAO.getById(attendanceId);
        if (ar != null) req.setEmployeeId(ar.getEmployeeId());

        correctionDAO.create(req);
        auditLogService.recordCorrectionAttempt(req.getEmployeeId(), attendanceId,
                originalVal, correctedVal);

        System.out.println("[CORRECTION] Request submitted: AttendanceID=" + attendanceId
                + " | Field: " + field + " | " + originalVal + " → " + correctedVal);
    }
}
