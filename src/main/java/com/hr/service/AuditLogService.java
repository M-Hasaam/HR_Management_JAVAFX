package com.hr.service;

// GRASP Pattern: Pure Fabrication (GoF: Observer-like pattern) — centralizes all audit logging.
// Rather than scattering log() calls across domain classes, this service acts as a single
// audit sink so every state change can be captured consistently and the storage mechanism
// can be swapped without touching business logic.

import com.hr.dao.AuditLogDAO;

import java.sql.SQLException;

public class AuditLogService {

    private final AuditLogDAO dao;

    public AuditLogService() throws SQLException {
        this.dao = new AuditLogDAO();
    }

    /**
     * Records a field-level change to an Employee record.
     *
     * @param employeeId  ID of the employee whose data changed
     * @param fieldName   Name of the changed field
     * @param oldVal      Previous value
     * @param newVal      New value
     * @param updaterId   ID of the user who performed the update
     * @return "logged"
     */
    public String writeAuditLog(int employeeId, String fieldName,
                                String oldVal, String newVal,
                                int updaterId) throws SQLException {
        dao.log(updaterId, "UPDATE", "Employee", employeeId, fieldName, oldVal, newVal);
        return "logged";
    }

    /**
     * Records that a user read sensitive fields of an Employee record.
     *
     * @param userId          ID of the user reading the data
     * @param employeeId      ID of the employee whose data was read
     * @param sensitiveFields Comma-separated list of sensitive field names accessed
     * @return "logged"
     */
    public String logReadAccess(int userId, int employeeId,
                                String sensitiveFields) throws SQLException {
        dao.log(userId, "READ", "Employee", employeeId, sensitiveFields, null, null);
        return "logged";
    }

    /**
     * Records an attendance-correction attempt (approved, rejected, or submitted).
     *
     * @param employeeId   ID of the employee raising the correction
     * @param attendanceId ID of the attendance record being corrected
     * @param action       Action label (e.g. "REQUEST_CORRECTION", "APPROVE_CORRECTION")
     * @param timestamp    ISO timestamp of when the action occurred
     * @return "logged"
     */
    public String recordCorrectionAttempt(int employeeId, int attendanceId,
                                          String action, String timestamp) throws SQLException {
        dao.log(employeeId, action, "AttendanceCorrection", attendanceId, null, null, timestamp);
        return "logged";
    }

    /**
     * Records the submission of a performance evaluation.
     *
     * @param evaluatorId ID of the evaluator submitting the form
     * @param employeeId  ID of the employee being evaluated
     * @param cycleId     ID of the evaluation cycle
     * @return "logged"
     */
    public String logEvaluationSubmission(int evaluatorId, int employeeId,
                                          int cycleId) throws SQLException {
        dao.log(evaluatorId, "SUBMIT_EVALUATION", "PerformanceEvaluation",
                employeeId, "cycleId", null, String.valueOf(cycleId));
        return "logged";
    }
}
