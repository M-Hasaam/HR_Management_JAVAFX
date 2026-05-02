package com.hr.model;

import java.time.LocalDate;

public class AttendanceCorrectionRequest {
    private int id;
    private int attendanceId;
    private int employeeId;
    private String employeeName;
    private String originalValue;
    private String correctedValue;
    private String justification;
    private String status;
    private LocalDate reviewDate;

    public AttendanceCorrectionRequest() {}

    public AttendanceCorrectionRequest(int id, int attendanceId, int employeeId, String employeeName,
                                       String originalValue, String correctedValue, String justification,
                                       String status, LocalDate reviewDate) {
        this.id = id;
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.originalValue = originalValue;
        this.correctedValue = correctedValue;
        this.justification = justification;
        this.status = status;
        this.reviewDate = reviewDate;
    }

    /**
     * Validates that the corrected value is non-null, non-blank, and different from the original.
     */
    public boolean validateLogicalConsistency() {
        return correctedValue != null && !correctedValue.isBlank() && !correctedValue.equals(originalValue);
    }

    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getOriginalValue() { return originalValue; }
    public void setOriginalValue(String originalValue) { this.originalValue = originalValue; }

    public String getCorrectedValue() { return correctedValue; }
    public void setCorrectedValue(String correctedValue) { this.correctedValue = correctedValue; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
}
