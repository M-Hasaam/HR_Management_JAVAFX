package com.hr.util;

import com.hr.dao.DatabaseConnection;
import com.hr.service.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * One-time utility — run main() once to insert the 3 demo accounts.
 * Safe to re-run: uses INSERT IGNORE so existing rows are untouched.
 *
 * Credentials created:
 *   admin  / admin123  → role: Admin
 *   hr     / hr123     → role: HR
 *   emp    / emp123    → role: Employee
 */
public class DemoUserSetup {

    public static void main(String[] args) throws SQLException {
        // Print hashes first so you can verify / insert manually if needed
        System.out.println("=== Password Hashes ===");
        System.out.println("admin123 -> " + AuthService.hashPassword("admin123"));
        System.out.println("hr123    -> " + AuthService.hashPassword("hr123"));
        System.out.println("emp123   -> " + AuthService.hashPassword("emp123"));
        System.out.println();

        createTable();
        // DELETE existing rows first to avoid INSERT IGNORE skipping them
        deleteUser("admin");
        deleteUser("hr");
        deleteUser("emp");
        createUser("admin", "admin123", "Admin");
        createUser("hr",    "hr123",    "HR");
        createUser("emp",   "emp123",   "Employee");
        System.out.println("Demo users created successfully.");
        System.out.println("  admin  / admin123  (Admin)");
        System.out.println("  hr     / hr123     (HR)");
        System.out.println("  emp    / emp123    (Employee)");
    }

    private static void createTable() throws SQLException {
        String ddl = """
            CREATE TABLE IF NOT EXISTS user_accounts (
                id             INT AUTO_INCREMENT PRIMARY KEY,
                employee_id    INT           NULL,
                username       VARCHAR(50)   NOT NULL UNIQUE,
                password_hash  VARCHAR(64)   NOT NULL,
                role           VARCHAR(20)   NOT NULL,
                account_status VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
                last_login     DATETIME      NULL
            )
            """;
        try (PreparedStatement ps = conn().prepareStatement(ddl)) {
            ps.executeUpdate();
        }
    }

    private static void createUser(String username, String password, String role)
            throws SQLException {
        String sql = """
            INSERT IGNORE INTO user_accounts
              (employee_id, username, password_hash, role, account_status)
            VALUES (NULL, ?, ?, ?, 'ACTIVE')
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, AuthService.hashPassword(password));
            ps.setString(3, role);
            ps.executeUpdate();
            System.out.println("  Created / skipped: " + username + " (" + role + ")");
        }
    }

    private static void deleteUser(String username) throws SQLException {
        String sql = "DELETE FROM user_accounts WHERE username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    private static Connection conn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }
}
