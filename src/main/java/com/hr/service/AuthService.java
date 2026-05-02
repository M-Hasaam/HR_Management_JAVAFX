package com.hr.service;

import com.hr.dao.UserAccountDAO;
import com.hr.model.UserAccount;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class AuthService {

    private final UserAccountDAO dao;

    public AuthService() throws SQLException {
        dao = new UserAccountDAO();
    }

    public UserAccount login(String username, String password) throws SQLException {
        UserAccount user = dao.getByUsername(username);
        if (user == null)
            throw new IllegalArgumentException("No account found for that username.");
        if (!"ACTIVE".equalsIgnoreCase(user.getAccountStatus()))
            throw new IllegalArgumentException("Your account is inactive. Contact HR.");
        if (!hashPassword(password).equals(user.getPasswordHash()))
            throw new IllegalArgumentException("Incorrect password.");
        return user;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
