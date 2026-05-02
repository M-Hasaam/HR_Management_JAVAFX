package com.hr.controller;
// GRASP Pattern: Controller — handles UC-07 Approve or Reject Leave Request
// Participants: LeaveRequest (Information Expert), LeaveBalance (Information Expert),
//              NotificationService (Pure Fabrication)

import com.hr.service.LeaveRequestService;
import com.hr.service.NotificationService;

import java.sql.SQLException;

public class LeaveApprovalController {

    private final LeaveRequestService leaveService;
    private final NotificationService notificationService; // Pure Fabrication

    public LeaveApprovalController() throws SQLException {
        this.leaveService        = new LeaveRequestService();
        this.notificationService = new NotificationService(); // no SQLException — plain constructor
    }

    /**
     * UC-07: Process an approve or reject decision on a leave request.
     * The comment must be at least 10 characters.
     * After updating the request, the employee is notified.
     *
     * @param leaveRequestID ID of the leave request to decide on
     * @param decision       "APPROVED" or "REJECTED"
     * @param comment        mandatory comment (min 10 chars)
     * @throws SQLException              on database error
     * @throws IllegalArgumentException  if the comment is too short or decision is unknown
     * @throws IllegalStateException     if the request is not in PENDING status
     */
    public void processLeaveDecision(int leaveRequestID, String decision, String comment)
            throws SQLException {
        if (comment == null || comment.trim().length() < 10) {
            throw new IllegalArgumentException(
                    "Decision comment must be at least 10 characters.");
        }

        if ("APPROVED".equals(decision)) {
            leaveService.approveRequest(leaveRequestID, "HR Officer", comment);
        } else if ("REJECTED".equals(decision)) {
            leaveService.rejectRequest(leaveRequestID, "HR Officer", comment);
        } else {
            throw new IllegalArgumentException("Unknown decision: " + decision);
        }

        notificationService.notifyEmployee(leaveRequestID, decision, comment);
    }
}
