package com.hr.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Payroll {
    private int id;
    private int employeeId;
    private String employeeName;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private BigDecimal basicSalary;
    private BigDecimal taxDeduction;
    private BigDecimal benefitsDeduction;
    private BigDecimal netPay;
    private LocalDate processedDate;

    public Payroll() {}

    public Payroll(int id, int employeeId, String employeeName,
                   LocalDate payPeriodStart, LocalDate payPeriodEnd,
                   BigDecimal basicSalary, BigDecimal taxDeduction,
                   BigDecimal benefitsDeduction, BigDecimal netPay,
                   LocalDate processedDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.payPeriodStart = payPeriodStart;
        this.payPeriodEnd = payPeriodEnd;
        this.basicSalary = basicSalary;
        this.taxDeduction = taxDeduction;
        this.benefitsDeduction = benefitsDeduction;
        this.netPay = netPay;
        this.processedDate = processedDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public LocalDate getPayPeriodStart() { return payPeriodStart; }
    public void setPayPeriodStart(LocalDate payPeriodStart) { this.payPeriodStart = payPeriodStart; }
    public LocalDate getPayPeriodEnd() { return payPeriodEnd; }
    public void setPayPeriodEnd(LocalDate payPeriodEnd) { this.payPeriodEnd = payPeriodEnd; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public BigDecimal getTaxDeduction() { return taxDeduction; }
    public void setTaxDeduction(BigDecimal taxDeduction) { this.taxDeduction = taxDeduction; }
    public BigDecimal getBenefitsDeduction() { return benefitsDeduction; }
    public void setBenefitsDeduction(BigDecimal benefitsDeduction) { this.benefitsDeduction = benefitsDeduction; }
    public BigDecimal getNetPay() { return netPay; }
    public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    public LocalDate getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDate processedDate) { this.processedDate = processedDate; }
}
