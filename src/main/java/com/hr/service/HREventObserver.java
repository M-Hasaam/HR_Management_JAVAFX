package com.hr.service;

// GoF Design Pattern: Observer — defines the subscriber contract.
//
// Justification: Multiple cross-cutting concerns (audit logging, notifications,
// payroll updates) need to react when an HR entity changes state.  Rather than
// hard-wiring these calls inside every controller, the Observer pattern lets each
// concern register itself as a subscriber.  New concerns can be added without
// modifying existing controllers — satisfying the Open/Closed Principle.

public interface HREventObserver {

    /**
     * Called by HREventPublisher when an HR event is raised.
     *
     * @param eventType  identifier for the event (e.g. "EMPLOYEE_UPDATED", "LEAVE_APPROVED")
     * @param entityId   primary key of the affected entity
     * @param payload    free-form description or serialised field change for audit/notification
     */
    void onEvent(String eventType, int entityId, String payload);
}
