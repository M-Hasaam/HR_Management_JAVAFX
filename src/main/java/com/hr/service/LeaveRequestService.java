package com.hr.service;

import com.hr.dao.LeaveRequestDAO;
import com.hr.model.LeaveRequest;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class LeaveRequestService {

    /** Returned by applyForLeave — carries the saved request and the 3a notification flag. */
    public record SubmitResult(LeaveRequest request, boolean notificationQueued) {}

    private final LeaveRequestDAO dao;

    public LeaveRequestService() throws SQLException {
        this.dao = new LeaveRequestDAO();
    }

    public List<LeaveRequest> getAllRequests() throws SQLException {
        return dao.getAll();
    }

    public int[] getLeaveBalance(int employeeId) throws SQLException {
        return dao.getBalance(employeeId);
    }

    // ── UC-06 live-feedback helpers (called from UI on every field change) ────

    /** 2b helper: returns public holidays (date → name) inside [start, end]. */
    public Map<LocalDate, String> getHolidaysInRange(LocalDate start, LocalDate end) throws SQLException {
        if (start == null || end == null || end.isBefore(start)) return Collections.emptyMap();
        return dao.getHolidayMapInRange(start, end);
    }

    /** 2c helper: returns overlapping PENDING/APPROVED requests for the employee. */
    public List<LeaveRequest> getOverlappingRequests(int employeeId, LocalDate start, LocalDate end)
            throws SQLException {
        if (start == null || end == null || end.isBefore(start)) return Collections.emptyList();
        return dao.getOverlappingRequests(employeeId, start, end);
    }

    /** 3c helper: returns probation end date for the employee, or null. */
    public LocalDate getEmployeeProbationEndDate(int employeeId) throws SQLException {
        return dao.getEmployeeProbationEndDate(employeeId);
    }

    /**
     * Counts Mon–Fri days between start and end (inclusive), excluding the given dates.
     * Static so the UI can call it without a service instance for live display.
     */
    public static int calculateWorkingDays(LocalDate start, LocalDate end,
                                           Collection<LocalDate> excludedDates) {
        int count = 0;
        LocalDate d = start;
        while (!d.isAfter(end)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY && !excludedDates.contains(d))
                count++;
            d = d.plusDays(1);
        }
        return count;
    }

    // ── UC-06 Main flow ──────────────────────────────────────────────────────

    /**
     * Backward-compatible 5-param form.  Delegates to the full version with no document ref.
     */
    public LeaveRequest applyForLeave(int employeeId, String leaveType,
                                     LocalDate start, LocalDate end,
                                     String reason) throws SQLException {
        return applyForLeave(employeeId, leaveType, start, end, reason, null).request();
    }

    /**
     * Full UC-06 with all 6 alternative-flow checks.
     *
     * @param documentRef  medical certificate reference for SICK leave (null → 3b: PENDING_DOCUMENT)
     * @throws IllegalArgumentException on invalid dates or unknown leave type
     * @throws IllegalStateException    on 2a (balance), 2b (holiday conflict),
     *                                  2c (overlap), 3c (probation restriction)
     */
    public SubmitResult applyForLeave(int employeeId, String leaveType,
                                     LocalDate start, LocalDate end,
                                     String reason, String documentRef) throws SQLException {
        // Basic date validation
        if (start == null || end == null)
            throw new IllegalArgumentException("Start and end dates are required.");
        if (end.isBefore(start))
            throw new IllegalArgumentException("End date must be on or after start date.");
        if (start.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Start date cannot be in the past.");

        // 2b: Blackout / public holiday conflict
        Map<LocalDate, String> holidays = dao.getHolidayMapInRange(start, end);
        if (!holidays.isEmpty()) {
            String names = holidays.values().stream().collect(Collectors.joining(", "));
            throw new IllegalStateException(
                "Selected dates include public holiday(s): " + names + ". " +
                "Public holidays are not counted as leave days — please adjust your dates.");
        }

        // Working days (weekends excluded; holidays already blocked above)
        int days = calculateWorkingDays(start, end, Collections.emptySet());
        if (days == 0)
            throw new IllegalArgumentException(
                "Selected date range has no working days (weekend only). Please choose different dates.");

        // 2a: Insufficient leave balance
        int[] balance = dao.getBalance(employeeId);
        int available = switch (leaveType) {
            case "ANNUAL"   -> balance[0];
            case "SICK"     -> balance[1];
            case "PERSONAL" -> balance[2];
            default -> throw new IllegalArgumentException("Unknown leave type: " + leaveType);
        };
        if (days > available)
            throw new IllegalStateException(
                "Insufficient " + capitalize(leaveType) + " leave balance. " +
                "Available: " + available + " day(s),  Requested: " + days + " working day(s).");

        // 2c: Overlap with existing PENDING or APPROVED leave
        List<LeaveRequest> overlapping = dao.getOverlappingRequests(employeeId, start, end);
        if (!overlapping.isEmpty()) {
            LeaveRequest ex = overlapping.get(0);
            throw new IllegalStateException(
                "Selected dates overlap with an existing " + ex.getStatus() + " leave request " +
                "(" + ex.getStartDate() + " → " + ex.getEndDate() + "). Double-booking is not permitted.");
        }

        // 3c: Probation period restriction (ANNUAL and PERSONAL blocked; SICK is always allowed)
        LocalDate probationEnd = dao.getEmployeeProbationEndDate(employeeId);
        if (probationEnd != null && !LocalDate.now().isAfter(probationEnd)) {
            if ("ANNUAL".equals(leaveType) || "PERSONAL".equals(leaveType)) {
                throw new IllegalStateException(
                    capitalize(leaveType) + " leave is not permitted during the probation period " +
                    "(ends: " + probationEnd + "). Only Sick leave is allowed during probation.");
            }
        }

        // 3b: SICK leave > 2 days requires a medical document reference
        String docRef = (documentRef != null && !documentRef.isBlank()) ? documentRef.trim() : null;
        String status = ("SICK".equals(leaveType) && days > 2 && docRef == null)
                        ? "PENDING_DOCUMENT" : "PENDING";

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId(employeeId);
        lr.setLeaveType(leaveType);
        lr.setStartDate(start);
        lr.setEndDate(end);
        lr.setDaysRequested(days);
        lr.setReason(reason);
        lr.setStatus(status);
        lr.setAppliedDate(LocalDate.now());
        lr.setDocumentPath(docRef);

        dao.insert(lr);

        // 3a notification is handled by LeaveController (so it can apply the fallback flag)
        return new SubmitResult(lr, false);
    }

    /** 3b: Employee provides document reference for a PENDING_DOCUMENT request → moves to PENDING. */
    public void submitDocument(int requestId, String documentRef) throws SQLException {
        if (documentRef == null || documentRef.isBlank())
            throw new IllegalArgumentException("Document reference cannot be empty.");
        LeaveRequest lr = dao.getById(requestId);
        if (lr == null) throw new IllegalArgumentException("Leave request not found: " + requestId);
        if (!"PENDING_DOCUMENT".equals(lr.getStatus()))
            throw new IllegalStateException("Document can only be submitted for PENDING_DOCUMENT requests.");
        dao.submitDocument(requestId, documentRef.trim());
    }

    // ── Approval / Rejection ─────────────────────────────────────────────────

    public void approveRequest(LeaveRequest lr) throws SQLException {
        if (!"PENDING".equals(lr.getStatus()))
            throw new IllegalStateException("Only PENDING requests can be approved. Current status: " + lr.getStatus());
        dao.updateStatus(lr.getId(), "APPROVED");
        dao.deductBalance(lr.getEmployeeId(), lr.getLeaveType(), lr.getDaysRequested());
    }

    public void rejectRequest(LeaveRequest lr) throws SQLException {
        if (!"PENDING".equals(lr.getStatus()) && !"PENDING_DOCUMENT".equals(lr.getStatus()))
            throw new IllegalStateException("Only PENDING or PENDING_DOCUMENT requests can be rejected.");
        dao.updateStatus(lr.getId(), "REJECTED");
    }

    public void approveRequest(int leaveRequestId, String approverName, String comment) throws SQLException {
        LeaveRequest lr = dao.getById(leaveRequestId);
        if (lr == null) throw new IllegalArgumentException("Leave request not found: " + leaveRequestId);
        if (!"PENDING".equals(lr.getStatus()))
            throw new IllegalStateException("Only PENDING requests can be approved. Current status: " + lr.getStatus());
        dao.updateStatus(lr.getId(), "APPROVED");
        dao.updateApproval(lr.getId(), approverName, comment);
        dao.deductBalance(lr.getEmployeeId(), lr.getLeaveType(), lr.getDaysRequested());
    }

    public void rejectRequest(int leaveRequestId, String approverName, String comment) throws SQLException {
        LeaveRequest lr = dao.getById(leaveRequestId);
        if (lr == null) throw new IllegalArgumentException("Leave request not found: " + leaveRequestId);
        if (!"PENDING".equals(lr.getStatus()) && !"PENDING_DOCUMENT".equals(lr.getStatus()))
            throw new IllegalStateException("Only PENDING or PENDING_DOCUMENT requests can be rejected.");
        dao.updateStatus(lr.getId(), "REJECTED");
        dao.updateApproval(lr.getId(), approverName, comment);
    }

    public void deleteRequest(int id) throws SQLException {
        dao.delete(id);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
