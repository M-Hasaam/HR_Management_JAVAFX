package com.hr.service;

import com.hr.model.UserAccount;

public class SessionManager {

    private static SessionManager instance;
    private UserAccount currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public UserAccount getCurrentUser() { return currentUser; }
    public void setCurrentUser(UserAccount user) { this.currentUser = user; }

    public String getRole()     { return currentUser != null ? currentUser.getRole() : null; }
    public String getUsername() { return currentUser != null ? currentUser.getUsername() : null; }

    public boolean isAdmin()    { return "Admin".equalsIgnoreCase(getRole()); }
    public boolean isHR()       { return "HR".equalsIgnoreCase(getRole()); }
    public boolean isEmployee() { return "Employee".equalsIgnoreCase(getRole()); }

    public void logout() { currentUser = null; }
}
