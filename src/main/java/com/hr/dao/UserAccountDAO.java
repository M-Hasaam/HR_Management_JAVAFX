package com.hr.dao;

import com.hr.model.UserAccount;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserAccountDAO {
    private final Connection conn;

    public UserAccountDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public UserAccount getByUsername(String username) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM user_accounts WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public UserAccount getByEmployeeId(int employeeId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM user_accounts WHERE employee_id=?")) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void insert(UserAccount ua) throws SQLException {
        String sql = """
                INSERT INTO user_accounts
                  (employee_id, username, password_hash, role, account_status, last_login)
                VALUES (?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (ua.getEmployeeId() > 0) ps.setInt(1, ua.getEmployeeId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, ua.getUsername());
            ps.setString(3, ua.getPasswordHash());
            ps.setString(4, ua.getRole());
            ps.setString(5, ua.getAccountStatus());
            ps.setTimestamp(6, ua.getLastLogin() != null ? Timestamp.valueOf(ua.getLastLogin()) : null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) ua.setId(keys.getInt(1));
            }
        }
    }

    public void update(UserAccount ua) throws SQLException {
        String sql = """
                UPDATE user_accounts
                SET employee_id=?, username=?, password_hash=?, role=?, account_status=?, last_login=?
                WHERE id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (ua.getEmployeeId() > 0) ps.setInt(1, ua.getEmployeeId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, ua.getUsername());
            ps.setString(3, ua.getPasswordHash());
            ps.setString(4, ua.getRole());
            ps.setString(5, ua.getAccountStatus());
            ps.setTimestamp(6, ua.getLastLogin() != null ? Timestamp.valueOf(ua.getLastLogin()) : null);
            ps.setInt(7, ua.getId());
            ps.executeUpdate();
        }
    }

    private UserAccount map(ResultSet rs) throws SQLException {
        UserAccount ua = new UserAccount();
        ua.setId(rs.getInt("id"));
        ua.setEmployeeId(rs.getInt("employee_id"));
        ua.setUsername(rs.getString("username"));
        ua.setPasswordHash(rs.getString("password_hash"));
        ua.setRole(rs.getString("role"));
        ua.setAccountStatus(rs.getString("account_status"));
        Timestamp ts = rs.getTimestamp("last_login");
        ua.setLastLogin(ts != null ? ts.toLocalDateTime() : null);
        return ua;
    }
}
