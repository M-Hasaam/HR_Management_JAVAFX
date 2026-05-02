package com.hr.service;

// GRASP Pattern: Pure Fabrication — handles all system notifications.
// In a production system this class would use an SMTP library (e.g. JavaMail) or
// a message broker.  Here notifications are simulated via console output so that
// the rest of the system can call notification methods without an external dependency.

import com.hr.model.Employee;

public class NotificationService {

    /**
     * Queues a welcome e-mail for a newly registered employee.
     */
    public String sendWelcomeEmail(Employee employee) {
        System.out.println("[NOTIFICATION] Welcome email queued for: "
                + employee.getFullName() + " <" + employee.getEmail() + ">");
        return "emailQueued";
    }

    /**
     * Attempts to send welcome email via SMTP.
     * Returns true if sent successfully, false if SMTP is unavailable and email was queued.
     * Extension 5a: on SMTP failure the registration is still saved and the email is queued.
     */
    public boolean sendWelcomeEmailWithFallback(Employee employee, String username, String tempPassword) {
        boolean smtpAvailable = checkSmtpAvailability();
        if (smtpAvailable) {
            System.out.println("[EMAIL] Welcome email sent to: " + employee.getEmail()
                + " | Username: " + username);
            return true;
        } else {
            System.out.println("[EMAIL] SMTP unavailable — welcome email queued for retry: "
                + employee.getEmail() + " | Username: " + username);
            return false;
        }
    }

    // Simulates SMTP availability check — in production would attempt a real connection
    private boolean checkSmtpAvailability() {
        return true; // treat as always available in development
    }

    /**
     * Notifies an HR officer that a new leave request requires review.
     */
    public String notifyHR(int hrOfficerId, int leaveRequestId) {
        System.out.println("[NOTIFICATION] HR Officer " + hrOfficerId
                + " notified about leave request #" + leaveRequestId);
        return "notified";
    }

    /**
     * Notifies an employee about the decision on their leave request.
     */
    public String notifyEmployee(int employeeId, String decision, String comment) {
        System.out.println("[NOTIFICATION] Employee " + employeeId
                + " notified: decision=" + decision + ", comment=" + comment);
        return "notified";
    }

    /**
     * Notifies the previous and new managers about a departmental transfer.
     */
    public String notifyManagers(int prevMgrId, int newMgrId) {
        System.out.println("[NOTIFICATION] Manager " + prevMgrId
                + " and Manager " + newMgrId + " notified of transfer");
        return "notified";
    }

    /**
     * UC-03: Notifies both managers with employee name and department name context.
     */
    public void notifyManagersOfTransfer(Employee emp, int fromDeptId,
                                         int toDeptId, String toDeptName) {
        System.out.println("[NOTIFICATION] Transfer — " + emp.getFullName()
            + " moved from deptId=" + fromDeptId
            + " to \"" + toDeptName + "\" (id=" + toDeptId + ")");
        System.out.println("[NOTIFICATION] Previous dept manager (id=" + fromDeptId
            + ") and new dept manager (id=" + toDeptId + ") notified.");
    }

    /**
     * Notifies relevant parties about the outcome of a probation review.
     */
    public String notifyProbationDecision(int employeeId, String decision, String notes) {
        System.out.println("[NOTIFICATION] Employee " + employeeId
                + " probation decision: " + decision + " | Notes: " + notes);
        return "notified";
    }

    /**
     * Distributes performance-evaluation tasks to the listed evaluators.
     */
    public String distributeEvaluationTasks(int cycleId, String evaluators) {
        System.out.println("[NOTIFICATION] Evaluation tasks for cycle #" + cycleId
                + " distributed to: " + evaluators);
        return "tasksDistributed";
    }
}
