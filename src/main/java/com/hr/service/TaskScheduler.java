package com.hr.service;

// GRASP Pattern: Pure Fabrication (GoF: Command pattern for deferred tasks).
// Scheduling reminders is not the responsibility of any domain object.
// The Command pattern maps naturally here: each scheduled reminder is an encapsulated
// command that will fire at a future point in time.  In production this class would
// integrate with a job scheduler (e.g. Quartz, Spring Scheduler, or a cron service).

import java.time.LocalDate;

public class TaskScheduler {

    /**
     * Schedules email/notification reminders for all evaluators in an evaluation cycle.
     *
     * @param cycleId  ID of the performance-evaluation cycle
     * @param deadline The date by which evaluations must be submitted
     * @return "remindersScheduled"
     */
    public String scheduleReminders(int cycleId, LocalDate deadline) {
        System.out.println("[TASK SCHEDULER] Reminders scheduled for evaluation cycle "
                + cycleId + " with deadline " + deadline);
        return "remindersScheduled";
    }
}
