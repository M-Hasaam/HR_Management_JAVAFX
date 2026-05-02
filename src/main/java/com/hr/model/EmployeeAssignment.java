package com.hr.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeAssignment {
    private int id;
    private int employeeId;
    private String employeeName;
    private int fromDeptId;
    private String fromDeptName;
    private int toDeptId;
    private String toDeptName;
    private LocalDate effectiveDate;
    private String remark;
    private int assignedByUserId;
    private boolean backdated;
    private String capacityOverrideJustification;
    private LocalDateTime createdAt;

    public EmployeeAssignment() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public int getFromDeptId() { return fromDeptId; }
    public void setFromDeptId(int fromDeptId) { this.fromDeptId = fromDeptId; }
    public String getFromDeptName() { return fromDeptName; }
    public void setFromDeptName(String fromDeptName) { this.fromDeptName = fromDeptName; }
    public int getToDeptId() { return toDeptId; }
    public void setToDeptId(int toDeptId) { this.toDeptId = toDeptId; }
    public String getToDeptName() { return toDeptName; }
    public void setToDeptName(String toDeptName) { this.toDeptName = toDeptName; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public int getAssignedByUserId() { return assignedByUserId; }
    public void setAssignedByUserId(int assignedByUserId) { this.assignedByUserId = assignedByUserId; }
    public boolean isBackdated() { return backdated; }
    public void setBackdated(boolean backdated) { this.backdated = backdated; }
    public String getCapacityOverrideJustification() { return capacityOverrideJustification; }
    public void setCapacityOverrideJustification(String j) { this.capacityOverrideJustification = j; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
