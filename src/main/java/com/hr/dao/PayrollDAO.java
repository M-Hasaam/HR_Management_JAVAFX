package com.hr.dao;

import com.hr.model.Payroll;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAO {
    private final Connection conn;

    public PayrollDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public List<Payroll> getAll() throws SQLException {
        List<Payroll> list = new ArrayList<>();
        String sql = """
                SELECT p.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM payroll p
                JOIN employees e ON p.employee_id = e.id
                ORDER BY p.processed_date DESC
                """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Payroll> getByEmployee(int employeeId) throws SQLException {
        List<Payroll> list = new ArrayList<>();
        String sql = """
                SELECT p.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name
                FROM payroll p
                JOIN employees e ON p.employee_id = e.id
                WHERE p.employee_id = ?
                ORDER BY p.processed_date DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(Payroll p) throws SQLException {
        String sql = """
                INSERT INTO payroll
                  (employee_id, pay_period_start, pay_period_end,
                   basic_salary, tax_deduction, benefits_deduction, net_pay, processed_date)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getEmployeeId());
            ps.setDate(2, Date.valueOf(p.getPayPeriodStart()));
            ps.setDate(3, Date.valueOf(p.getPayPeriodEnd()));
            ps.setBigDecimal(4, p.getBasicSalary());
            ps.setBigDecimal(5, p.getTaxDeduction());
            ps.setBigDecimal(6, p.getBenefitsDeduction());
            ps.setBigDecimal(7, p.getNetPay());
            ps.setDate(8, Date.valueOf(p.getProcessedDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM payroll WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Payroll map(ResultSet rs) throws SQLException {
        Payroll p = new Payroll();
        p.setId(rs.getInt("id"));
        p.setEmployeeId(rs.getInt("employee_id"));
        p.setEmployeeName(rs.getString("emp_name"));
        p.setPayPeriodStart(rs.getDate("pay_period_start").toLocalDate());
        p.setPayPeriodEnd(rs.getDate("pay_period_end").toLocalDate());
        p.setBasicSalary(rs.getBigDecimal("basic_salary"));
        p.setTaxDeduction(rs.getBigDecimal("tax_deduction"));
        p.setBenefitsDeduction(rs.getBigDecimal("benefits_deduction"));
        p.setNetPay(rs.getBigDecimal("net_pay"));
        p.setProcessedDate(rs.getDate("processed_date").toLocalDate());
        return p;
    }
}
