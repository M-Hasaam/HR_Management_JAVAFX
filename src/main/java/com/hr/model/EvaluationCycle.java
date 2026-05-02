package com.hr.model;

import java.time.LocalDate;

public class EvaluationCycle {
    private int id;
    private String cycleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String evaluationType;
    private String applicableScope;
    private String status;
    private String reminderSchedule;
    private LocalDate gracePeriodUntil;

    public EvaluationCycle() {}

    public EvaluationCycle(int id, String cycleName, LocalDate startDate, LocalDate endDate,
                           String evaluationType, String applicableScope, String status,
                           String reminderSchedule) {
        this.id = id;
        this.cycleName = cycleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.evaluationType = evaluationType;
        this.applicableScope = applicableScope;
        this.status = status;
        this.reminderSchedule = reminderSchedule;
    }

    /**
     * Validates that both dates are set and endDate is strictly after startDate.
     */
    public boolean validatePeriod() {
        return startDate != null && endDate != null && endDate.isAfter(startDate);
    }

    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getEvaluationType() { return evaluationType; }
    public void setEvaluationType(String evaluationType) { this.evaluationType = evaluationType; }

    public String getApplicableScope() { return applicableScope; }
    public void setApplicableScope(String applicableScope) { this.applicableScope = applicableScope; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReminderSchedule() { return reminderSchedule; }
    public void setReminderSchedule(String reminderSchedule) { this.reminderSchedule = reminderSchedule; }

    public LocalDate getGracePeriodUntil() { return gracePeriodUntil; }
    public void setGracePeriodUntil(LocalDate gracePeriodUntil) { this.gracePeriodUntil = gracePeriodUntil; }

    @Override
    public String toString() {
        return cycleName + " [" + status + "]";
    }
}
