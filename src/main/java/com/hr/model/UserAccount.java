package com.hr.model;

import java.time.LocalDateTime;

public class UserAccount {
    private int id;
    private int employeeId;
    private String username;
    private String passwordHash;
    private String role;
    private String accountStatus;
    private LocalDateTime lastLogin;

    public UserAccount() {}

    public UserAccount(int id, int employeeId, String username, String passwordHash,
                       String role, String accountStatus, LocalDateTime lastLogin) {
        this.id = id;
        this.employeeId = employeeId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.accountStatus = accountStatus;
        this.lastLogin = lastLogin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}
