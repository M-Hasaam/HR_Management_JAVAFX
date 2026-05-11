package com.hr.service;

import com.hr.dao.PerformanceMetricsDAO;

import java.sql.SQLException;

// Non-Functional Requirement (NFR): Performance — Response Time < 3 seconds.
//
// Requirement: "Form submissions must complete in < 3 seconds under normal load."
// Implementation: This service wraps any timed operation, measures elapsed time,
// and logs a warning if the 3-second SLA is breached.  It also records total
// operation count and cumulative time for runtime diagnostics.
//
// GRASP Pattern: Pure Fabrication — performance monitoring is not a domain concept;
// this class exists purely to satisfy a non-functional constraint.

public class PerformanceMonitor {

    private static final long SLA_THRESHOLD_MS = 3_000; // NFR: 3-second SLA

    private int totalOperations = 0;
    private long totalTimeMs = 0;
    private final PerformanceMetricsDAO metricsDAO;

    public PerformanceMonitor() {
        PerformanceMetricsDAO dao;
        try {
            dao = new PerformanceMetricsDAO();
        } catch (SQLException e) {
            dao = null;
            System.err.println("[PERFORMANCE] Database persistence disabled: " + e.getMessage());
        }
        this.metricsDAO = dao;
    }

    /**
     * Records the elapsed time for a completed operation.
     * Logs a warning if the SLA threshold is breached.
     *
     * @param operationName human-readable label for the operation
     * @param elapsedMs     measured wall-clock time in milliseconds
     */
    public void recordOperation(String operationName, long elapsedMs) {
        totalOperations++;
        totalTimeMs += elapsedMs;
        boolean slaBreached = elapsedMs > SLA_THRESHOLD_MS;

        if (slaBreached) {
            System.err.println("[PERFORMANCE WARNING] SLA BREACH: '"
                    + operationName + "' took " + elapsedMs + " ms"
                    + " (limit: " + SLA_THRESHOLD_MS + " ms)");
        } else {
            System.out.println("[PERFORMANCE] '" + operationName + "' completed in "
                    + elapsedMs + " ms — within SLA.");
        }

        persistOperation(operationName, elapsedMs, slaBreached);
    }

    /**
     * Convenience: times a Runnable and records the result.
     *
     * @param operationName label for the operation
     * @param operation     the operation to execute and time
     * @throws Exception if the operation throws
     */
    public void time(String operationName, RunnableWithException operation) throws Exception {
        long start = System.currentTimeMillis();
        try {
            operation.run();
        } finally {
            recordOperation(operationName, System.currentTimeMillis() - start);
        }
    }

    /** Returns average response time across all recorded operations (ms). */
    public double getAverageResponseTimeMs() {
        return totalOperations == 0 ? 0.0 : (double) totalTimeMs / totalOperations;
    }

    /** Returns total number of recorded operations. */
    public int getTotalOperations() {
        return totalOperations;
    }

    private void persistOperation(String operationName, long elapsedMs, boolean slaBreached) {
        if (metricsDAO == null) {
            return;
        }

        try {
            metricsDAO.saveOperation(
                    operationName,
                    elapsedMs,
                    slaBreached,
                    totalOperations,
                    totalTimeMs,
                    getAverageResponseTimeMs());
        } catch (SQLException e) {
            System.err.println("[PERFORMANCE] Failed to persist metrics for '"
                    + operationName + "': " + e.getMessage());
        }
    }

    /** Functional interface for operations that may throw checked exceptions. */
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
