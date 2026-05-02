package com.hr.model;

import java.time.LocalDate;

public class OffboardingWorkflow {
    private int id;
    private int employeeId;
    private String employeeName;
    private String separationType;
    private LocalDate hireDate;
    private LocalDate lastWorkingDate;
    private String exitReason;
    private String status;
    private String checklistItems;
    private String finalSettlementStatus;

    public OffboardingWorkflow() {}

    public OffboardingWorkflow(int id, int employeeId, String employeeName, String separationType,
                               LocalDate hireDate, LocalDate lastWorkingDate, String exitReason,
                               String status, String checklistItems, String finalSettlementStatus) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.separationType = separationType;
        this.hireDate = hireDate;
        this.lastWorkingDate = lastWorkingDate;
        this.exitReason = exitReason;
        this.status = status;
        this.checklistItems = checklistItems;
        this.finalSettlementStatus = finalSettlementStatus;
    }

    /**
     * Validates the notice period based on separation type.
     * RESIGNATION requires lastWorkingDate to be at least 30 days from today.
     * TERMINATION is always valid.
     */
    public boolean validateNoticePeriod() {
        if ("RESIGNATION".equalsIgnoreCase(separationType)) {
            return lastWorkingDate != null && lastWorkingDate.isAfter(LocalDate.now().plusDays(29));
        }
        // TERMINATION and all other types: any date is valid
        return true;
    }

    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getSeparationType() { return separationType; }
    public void setSeparationType(String separationType) { this.separationType = separationType; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public LocalDate getLastWorkingDate() { return lastWorkingDate; }
    public void setLastWorkingDate(LocalDate lastWorkingDate) { this.lastWorkingDate = lastWorkingDate; }

    public String getExitReason() { return exitReason; }
    public void setExitReason(String exitReason) { this.exitReason = exitReason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getChecklistItems() { return checklistItems; }
    public void setChecklistItems(String checklistItems) { this.checklistItems = checklistItems; }

    public String getFinalSettlementStatus() { return finalSettlementStatus; }
    public void setFinalSettlementStatus(String finalSettlementStatus) { this.finalSettlementStatus = finalSettlementStatus; }
}
