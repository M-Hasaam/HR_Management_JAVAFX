package com.hr.service;

import com.hr.dao.PayrollDAO;
import com.hr.model.Employee;
import com.hr.model.Payroll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PayrollService {
    private static final BigDecimal TAX_RATE      = new BigDecimal("0.15");
    private static final BigDecimal BENEFITS_RATE = new BigDecimal("0.05");

    private final PayrollDAO dao;

    public PayrollService() throws SQLException {
        this.dao = new PayrollDAO();
    }

    public List<Payroll> getAllPayrolls() throws SQLException {
        return dao.getAll();
    }

    public List<Payroll> getPayrollsForEmployee(int employeeId) throws SQLException {
        return dao.getByEmployee(employeeId);
    }

    /**
     * Calculates and saves payroll for the given employee and period.
     * Business rule: tax = 15%, benefits = 5%, net = basic - tax - benefits.
     */
    public Payroll processPayroll(Employee employee, LocalDate periodStart, LocalDate periodEnd)
            throws SQLException {
        if (periodEnd.isBefore(periodStart))
            throw new IllegalArgumentException("Pay period end must be after start.");

        BigDecimal basic    = employee.getBasicSalary();
        BigDecimal tax      = basic.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal benefits = basic.multiply(BENEFITS_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net      = basic.subtract(tax).subtract(benefits);

        Payroll payroll = new Payroll();
        payroll.setEmployeeId(employee.getId());
        payroll.setEmployeeName(employee.getFullName());
        payroll.setPayPeriodStart(periodStart);
        payroll.setPayPeriodEnd(periodEnd);
        payroll.setBasicSalary(basic);
        payroll.setTaxDeduction(tax);
        payroll.setBenefitsDeduction(benefits);
        payroll.setNetPay(net);
        payroll.setProcessedDate(LocalDate.now());

        dao.insert(payroll);
        return payroll;
    }

    public void deletePayroll(int id) throws SQLException {
        dao.delete(id);
    }

    /**
     * Returns [tax, benefits, netPay] for a given basic salary, using the
     * canonical tax/benefit rates.  Used by the UI preview so rates are not
     * duplicated in the presentation layer.
     */
    public BigDecimal[] calculateBreakdown(BigDecimal basic) {
        BigDecimal tax      = basic.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal benefits = basic.multiply(BENEFITS_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net      = basic.subtract(tax).subtract(benefits);
        return new BigDecimal[]{tax, benefits, net};
    }
}
