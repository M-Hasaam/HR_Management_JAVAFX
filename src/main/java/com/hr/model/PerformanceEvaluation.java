package com.hr.model;

import java.time.LocalDate;

public class PerformanceEvaluation {
    private int id;
    private int employeeId;
    private String employeeName;
    private int evaluatorId;
    private int cycleId;
    private LocalDate evaluationDate;
    private double aggregateScore;
    private String remarks;
    private String status;

    public PerformanceEvaluation() {}

    public PerformanceEvaluation(int id, int employeeId, String employeeName, int evaluatorId,
                                 int cycleId, LocalDate evaluationDate, double aggregateScore,
                                 String remarks, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.evaluatorId = evaluatorId;
        this.cycleId = cycleId;
        this.evaluationDate = evaluationDate;
        this.aggregateScore = aggregateScore;
        this.remarks = remarks;
        this.status = status;
    }

    /**
     * Validates that the evaluation form has non-blank remarks and a score in [0, 100].
     */
    public boolean validateForm() {
        return remarks != null && !remarks.isBlank() && aggregateScore >= 0 && aggregateScore <= 100;
    }

    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public int getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(int evaluatorId) { this.evaluatorId = evaluatorId; }

    public int getCycleId() { return cycleId; }
    public void setCycleId(int cycleId) { this.cycleId = cycleId; }

    public LocalDate getEvaluationDate() { return evaluationDate; }
    public void setEvaluationDate(LocalDate evaluationDate) { this.evaluationDate = evaluationDate; }

    public double getAggregateScore() { return aggregateScore; }
    public void setAggregateScore(double aggregateScore) { this.aggregateScore = aggregateScore; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
