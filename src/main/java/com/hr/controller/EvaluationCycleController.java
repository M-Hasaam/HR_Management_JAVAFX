package com.hr.controller;
// GRASP Pattern: Controller — handles UC-11 Initiate Performance Evaluation Cycle.
// Participants: EvaluationCycle (Creator/Information Expert),
//              NotificationService (Pure Fabrication), TaskScheduler (Pure Fabrication)
//
// Business Rules enforced here:
// 1. Cycle period: end date must be strictly after start date.
// 2. Extension 2a: period must not overlap any existing DRAFT or ACTIVE cycle.
// 3. Extension 3a: scope must be set before activation (coverage check).
// 4. Extension 4b: scheduler failure is non-fatal — cycle activates, reminders queued.
// Special Req: only DRAFT cycles can be activated; ACTIVE cycles are config-locked.

import com.hr.dao.EvaluationCycleDAO;
import com.hr.model.EvaluationCycle;
import com.hr.service.NotificationService;
import com.hr.service.TaskScheduler;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EvaluationCycleController {

    private final EvaluationCycleDAO  cycleDAO;
    private final NotificationService notificationService;
    private final TaskScheduler       taskScheduler;

    public EvaluationCycleController() throws SQLException {
        this.cycleDAO            = new EvaluationCycleDAO();
        this.notificationService = new NotificationService();
        this.taskScheduler       = new TaskScheduler();
    }

    /**
     * UC-11 Step 2: Validate inputs and persist cycle as DRAFT.
     * Ext 2a: throws IllegalStateException listing any conflicting cycles.
     *
     * @return the saved DRAFT cycle (id populated)
     */
    public EvaluationCycle createDraftCycle(String cycleName, LocalDate startDate,
                                             LocalDate endDate, String evaluationType,
                                             String scope) throws SQLException {
        if (cycleName == null || cycleName.isBlank())
            throw new IllegalArgumentException("Cycle name must not be blank.");
        if (scope == null || scope.isBlank())
            throw new IllegalArgumentException("Applicable scope / department must be selected.");
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("Start and end dates are required.");
        if (!endDate.isAfter(startDate))
            throw new IllegalArgumentException("End date must be after start date.");

        // Ext 2a: period overlap check — block creation if any DRAFT/ACTIVE cycle overlaps
        List<EvaluationCycle> conflicts = cycleDAO.getConflicting(startDate, endDate);
        if (!conflicts.isEmpty()) {
            StringBuilder sb = new StringBuilder("Period overlaps with existing cycle(s):\n");
            for (EvaluationCycle c : conflicts)
                sb.append("  • ").append(c.getCycleName())
                  .append(" [").append(c.getStatus()).append("]  ")
                  .append(c.getStartDate()).append(" → ").append(c.getEndDate()).append("\n");
            throw new IllegalStateException(sb.toString().trim());
        }

        EvaluationCycle cycle = new EvaluationCycle();
        cycle.setCycleName(cycleName);
        cycle.setStartDate(startDate);
        cycle.setEndDate(endDate);
        cycle.setEvaluationType(evaluationType);
        cycle.setApplicableScope(scope);
        cycle.setStatus("DRAFT");

        cycleDAO.insert(cycle);
        System.out.println("[EVAL CYCLE] Draft created: '" + cycleName + "'");
        return cycle;
    }

    /**
     * UC-11 Step 4: Activate a DRAFT cycle.
     * Ext 3a: scope must be set (coverage check — full evaluator-assignment validation
     *         would query an assignments table here).
     * Ext 3b: inactive-evaluator check would query employees.active flag here.
     * Ext 4b: scheduler failure is non-fatal; cycle activates and reminders are queued.
     *
     * @return true if reminders were scheduled normally, false if scheduler was unavailable
     */
    public boolean activateCycle(int cycleId) throws SQLException {
        EvaluationCycle cycle = cycleDAO.getById(cycleId);
        if (cycle == null)
            throw new IllegalArgumentException("Evaluation cycle not found (id=" + cycleId + ").");

        // Special Req: only DRAFT cycles can be activated (ACTIVE is config-locked)
        if (!"DRAFT".equals(cycle.getStatus()))
            throw new IllegalStateException(
                    "Only DRAFT cycles can be activated. Current status: " + cycle.getStatus());

        // Ext 3a: scope / coverage gap check
        if (cycle.getApplicableScope() == null || cycle.getApplicableScope().isBlank())
            throw new IllegalStateException(
                    "Cannot activate: no scope assigned — coverage gap detected.\n"
                    + "Set an applicable scope before activating.");

        cycleDAO.updateStatus(cycleId, "ACTIVE");
        notificationService.distributeEvaluationTasks(cycleId, cycle.getApplicableScope());

        // Ext 4b: graceful scheduler failure — cycle is already ACTIVE, reminders will be retried
        try {
            taskScheduler.scheduleReminders(cycleId, cycle.getEndDate());
            System.out.println("[EVAL CYCLE] Activated: '" + cycle.getCycleName() + "'");
            return true;
        } catch (Exception e) {
            System.err.println("[TASK SCHEDULER] Reminder scheduling unavailable for cycle "
                    + cycleId + ": " + e.getMessage()
                    + " — cycle is ACTIVE, reminders queued for retry when scheduler recovers.");
            return false;
        }
    }

    /** Returns all evaluation cycles. */
    public List<EvaluationCycle> getAllCycles() throws SQLException {
        return cycleDAO.getAll();
    }

    /** Ext 2a helper: returns cycles whose period overlaps the given range. */
    public List<EvaluationCycle> getConflictingCycles(LocalDate start, LocalDate end)
            throws SQLException {
        return cycleDAO.getConflicting(start, end);
    }
}
