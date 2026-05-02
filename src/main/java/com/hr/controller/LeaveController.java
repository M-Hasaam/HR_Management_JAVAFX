package com.hr.controller;
// GRASP Pattern: Controller — handles UC-06 Submit Leave Request
// Participants: LeaveBalance (Information Expert), LeaveRequest (Creator),
//              NotificationService (Pure Fabrication)

import com.hr.model.LeaveRequest;
import com.hr.service.LeaveRequestService;
import com.hr.service.NotificationService;

import java.sql.SQLException;
import java.time.LocalDate;

public class LeaveController {

    private final LeaveRequestService leaveService;
    private final NotificationService notificationService;

    public LeaveController() throws SQLException {
        this.leaveService        = new LeaveRequestService();
        this.notificationService = new NotificationService();
    }

    /**
     * UC-06: Full submission with all alternative-flow checks.
     * 3a: notification failure is handled gracefully — result.notificationQueued() is true if SMTP failed.
     *
     * @param documentRef  medical certificate reference for SICK leave (null → 3b: PENDING_DOCUMENT)
     * @throws IllegalArgumentException on invalid input
     * @throws IllegalStateException    on 2a/2b/2c/3c blocks
     */
    public LeaveRequestService.SubmitResult submitLeaveRequest(
            int employeeID, String leaveType,
            LocalDate startDate, LocalDate endDate,
            String reason, String documentRef) throws SQLException {

        LeaveRequestService.SubmitResult result =
            leaveService.applyForLeave(employeeID, leaveType, startDate, endDate, reason, documentRef);

        // 3a: Notify HR with graceful fallback if SMTP is unavailable
        boolean notifQueued = false;
        try {
            notificationService.notifyHR(0, result.request().getId());
        } catch (Exception e) {
            notifQueued = true;
            System.err.println("[NOTIFY] HR notification queued (3a): " + e.getMessage());
        }

        return new LeaveRequestService.SubmitResult(result.request(), notifQueued);
    }

    /** Backward-compatible 5-param form (no document ref, no SubmitResult). */
    public LeaveRequest submitLeaveRequest(int employeeID, String leaveType,
                                           LocalDate startDate, LocalDate endDate,
                                           String reason) throws SQLException {
        return submitLeaveRequest(employeeID, leaveType, startDate, endDate, reason, null).request();
    }

    public int[] getLeaveBalance(int employeeID) throws SQLException {
        return leaveService.getLeaveBalance(employeeID);
    }
}
