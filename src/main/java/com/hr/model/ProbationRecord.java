package com.hr.model;

import java.time.LocalDate;

public class ProbationRecord {
    private int id;
    private int employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int extensions;
    private String decision;
    private LocalDate decisionDate;
    private String reason;
    private String status;
    private int decisionMadeBy;
    private String notes;

    public ProbationRecord() {}

    public ProbationRecord(int id, int employeeId, String employeeName, LocalDate startDate,
                           LocalDate endDate, int extensions, String decision, LocalDate decisionDate,
                           String reason, String status, int decisionMadeBy, String notes) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.extensions = extensions;
        this.decision = decision;
        this.decisionDate = decisionDate;
        this.reason = reason;
        this.status = status;
        this.decisionMadeBy = decisionMadeBy;
        this.notes = notes;
    }

    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getExtensions() { return extensions; }
    public void setExtensions(int extensions) { this.extensions = extensions; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public LocalDate getDecisionDate() { return decisionDate; }
    public void setDecisionDate(LocalDate decisionDate) { this.decisionDate = decisionDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getDecisionMadeBy() { return decisionMadeBy; }
    public void setDecisionMadeBy(int decisionMadeBy) { this.decisionMadeBy = decisionMadeBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
