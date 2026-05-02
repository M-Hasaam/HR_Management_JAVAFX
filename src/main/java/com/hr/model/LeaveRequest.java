package com.hr.model;

import java.time.LocalDate;

public class LeaveRequest {
    private int id;
    private int employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int daysRequested;
    private String reason;
    private String status;
    private LocalDate appliedDate;

    // New fields
    private String approvedBy;
    private LocalDate approvedDate;
    private String comments;

    public LeaveRequest() {}

    public LeaveRequest(int id, int employeeId, String employeeName, String leaveType,
                        LocalDate startDate, LocalDate endDate, int daysRequested,
                        String reason, String status, LocalDate appliedDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.daysRequested = daysRequested;
        this.reason = reason;
        this.status = status;
        this.appliedDate = appliedDate;
    }

    public LeaveRequest(int id, int employeeId, String employeeName, String leaveType,
                        LocalDate startDate, LocalDate endDate, int daysRequested,
                        String reason, String status, LocalDate appliedDate,
                        String approvedBy, LocalDate approvedDate, String comments) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.daysRequested = daysRequested;
        this.reason = reason;
        this.status = status;
        this.appliedDate = appliedDate;
        this.approvedBy = approvedBy;
        this.approvedDate = approvedDate;
        this.comments = comments;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public int getDaysRequested() { return daysRequested; }
    public void setDaysRequested(int daysRequested) { this.daysRequested = daysRequested; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDate getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDate approvedDate) { this.approvedDate = approvedDate; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    private String documentPath;
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
}
