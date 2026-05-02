package com.hr.controller;
// GRASP Pattern: Controller — handles UC-12 Submit Employee Performance Evaluation.
// Participants: PerformanceEvaluation (Creator/Information Expert),
//              AuditLogService (Pure Fabrication), NotificationService (Pure Fabrication)
//
// Business Rules enforced here:
// 1. All five competency scores must be in range 0-100.
// 2. Remarks must not be blank.
// 3. Ext 3a/3b: submission window check — rejects if past deadline unless grace period active.
// 4. Special Req: immutability — one SUBMITTED evaluation per employee per cycle.
// 5. Ext 4a: statistical outlier detection — soft warning if score deviates > 20 pts from avg.

import com.hr.dao.EvaluationCycleDAO;
import com.hr.dao.PerformanceEvaluationDAO;
import com.hr.model.EvaluationCycle;
import com.hr.model.PerformanceEvaluation;
import com.hr.service.AuditLogService;
import com.hr.service.NotificationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PerformanceEvalController {

    private final PerformanceEvaluationDAO evalDAO;
    private final EvaluationCycleDAO       cycleDAO;
    private final AuditLogService          auditLogService;
    private final NotificationService      notificationService;

    public PerformanceEvalController() throws SQLException {
        this.evalDAO             = new PerformanceEvaluationDAO();
        this.cycleDAO            = new EvaluationCycleDAO();
        this.auditLogService     = new AuditLogService();
        this.notificationService = new NotificationService();
    }

    /**
     * UC-12 Step 3: Submit a completed performance evaluation.
     *
     * @param employeeId     employee being evaluated
     * @param evaluatorId    authenticated evaluator
     * @param cycle          the active EvaluationCycle (used for deadline + grace period checks)
     * @param aggregateScore pre-computed weighted score (use calculateWeightedScore)
     * @param remarks        qualitative feedback — must not be blank
     * @return true if the score is a statistical outlier (Ext 4a soft warning), false otherwise
     * @throws IllegalArgumentException if inputs fail validation (Ext 2a)
     * @throws IllegalStateException    if deadline exceeded (Ext 3a), or duplicate submission (Special Req)
     * @throws SQLException             on database error
     */
    public boolean submitEvaluation(int employeeId, int evaluatorId, EvaluationCycle cycle,
                                     double aggregateScore, String remarks) throws SQLException {
        if (aggregateScore < 0 || aggregateScore > 100)
            throw new IllegalArgumentException("Aggregate score must be between 0 and 100.");
        if (remarks == null || remarks.isBlank())
            throw new IllegalArgumentException("Remarks must not be blank.");

        // Ext 3a: deadline check
        LocalDate today = LocalDate.now();
        if (today.isAfter(cycle.getEndDate())) {
            LocalDate grace = cycle.getGracePeriodUntil();
            if (grace == null || today.isAfter(grace)) {
                // Ext 3b: grace period also expired (or was never granted)
                String msg = "OVERDUE: The submission deadline for '"
                        + cycle.getCycleName() + "' was " + cycle.getEndDate() + ".";
                if (grace != null)
                    msg += "\nGrace period also expired on " + grace + ".";
                msg += "\nContact Admin to request a grace period extension.";
                throw new IllegalStateException(msg);
            }
        }

        // Special Req: immutability — block duplicate submission for same employee + cycle
        if (evalDAO.existsForEmployeeCycle(employeeId, cycle.getId()))
            throw new IllegalStateException(
                    "An evaluation for this employee in this cycle already exists.\n"
                    + "Submitted evaluations are immutable. "
                    + "Amendments require a correction workflow with Admin approval.");

        PerformanceEvaluation eval = new PerformanceEvaluation();
        eval.setEmployeeId(employeeId);
        eval.setEvaluatorId(evaluatorId);
        eval.setCycleId(cycle.getId());
        eval.setAggregateScore(aggregateScore);
        eval.setRemarks(remarks);
        eval.setEvaluationDate(today);
        eval.setStatus("SUBMITTED");

        evalDAO.insert(eval);
        auditLogService.logEvaluationSubmission(evaluatorId, employeeId, cycle.getId());
        notificationService.notifyEmployee(employeeId, "EVALUATION_SUBMITTED",
                "Your performance evaluation for cycle '" + cycle.getCycleName()
                + "' has been submitted. Score: " + String.format("%.1f", aggregateScore));

        // Ext 4a: statistical outlier detection — soft warning only, does not block submission
        double cycleAvg = evalDAO.getAverageByCycle(cycle.getId());
        boolean outlier = cycleAvg > 0 && Math.abs(aggregateScore - cycleAvg) > 20;

        System.out.println("[EVAL] Submitted: Employee " + employeeId
                + " | Score: " + String.format("%.1f", aggregateScore)
                + " | Cycle: " + cycle.getCycleName()
                + (outlier ? " | OUTLIER (cycle avg=" + String.format("%.1f", cycleAvg) + ")" : ""));
        return outlier;
    }

    /**
     * Computes weighted aggregate from five competency scores.
     * Weights: Work Quality 25%, Technical 25%, Communication 20%, Teamwork 15%, Leadership 15%.
     */
    public static double calculateWeightedScore(double workQuality, double technical,
                                                 double communication, double teamwork,
                                                 double leadership) {
        return workQuality   * 0.25
             + technical     * 0.25
             + communication * 0.20
             + teamwork      * 0.15
             + leadership    * 0.15;
    }

    /** Returns all performance evaluations. */
    public List<PerformanceEvaluation> getAllEvaluations() throws SQLException {
        return evalDAO.getAll();
    }
}
