package com.hr.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceRecord {
    private int id;
    private int employeeId;
    private String employeeName;
    private LocalDate attendanceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private double totalHours;
    private String attendanceStatus;
    private boolean correctionFlag;

    public AttendanceRecord() {}

    public AttendanceRecord(int id, int employeeId, String employeeName, LocalDate attendanceDate,
                            LocalDateTime checkInTime, LocalDateTime checkOutTime, double totalHours,
                            String attendanceStatus, boolean correctionFlag) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.attendanceDate = attendanceDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.totalHours = totalHours;
        this.attendanceStatus = attendanceStatus;
        this.correctionFlag = correctionFlag;
    }

    // Business methods

    /**
     * Computes work hours from check-in to check-out.
     * Returns 0.0 if either time is null.
     */
    public double computeWorkHours() {
        if (checkInTime != null && checkOutTime != null) {
            return Duration.between(checkInTime, checkOutTime).toMinutes() / 60.0;
        }
        return 0.0;
    }

    /**
     * Returns true if the attendance date is within the 7-day correction window.
     */
    public boolean validateCorrectionWindow() {
        return LocalDate.now().isBefore(attendanceDate.plusDays(8));
    }

    /**
     * Flags attendance status based on check-in/check-out times.
     * After 9:10 AM = LATE; check-out before 5 PM = EARLY_DEPARTURE; otherwise PRESENT.
     */
    public String flagAttendanceStatus() {
        if (checkInTime != null) {
            int hour = checkInTime.getHour();
            int minute = checkInTime.getMinute();
            if (hour > 9 || (hour == 9 && minute > 10)) {
                return "LATE";
            }
        }
        if (checkOutTime != null) {
            if (checkOutTime.getHour() < 17) {
                return "EARLY_DEPARTURE";
            }
        }
        return "PRESENT";
    }

    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }

    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    public boolean isCorrectionFlag() { return correctionFlag; }
    public void setCorrectionFlag(boolean correctionFlag) { this.correctionFlag = correctionFlag; }
}
