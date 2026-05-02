package com.hr.service;

// GoF Design Pattern: Observer — the Subject / Publisher.
//
// Justification: Centralising event publication here prevents controllers from
// needing direct references to every cross-cutting service (audit, notification,
// payroll).  Controllers call publishEvent(); registered observers react
// independently.  This achieves Low Coupling (GRASP) and Indirection (GRASP)
// between the triggering controller and the reacting services.

import java.util.ArrayList;
import java.util.List;

public class HREventPublisher {

    private final List<HREventObserver> observers = new ArrayList<>();

    /** Register an observer to receive future events. */
    public void register(HREventObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /** Unregister an observer so it no longer receives events. */
    public void unregister(HREventObserver observer) {
        observers.remove(observer);
    }

    /**
     * Broadcast an event to all registered observers.
     *
     * @param eventType  identifier (e.g. "EMPLOYEE_UPDATED", "LEAVE_APPROVED")
     * @param entityId   primary key of the affected entity
     * @param payload    description of the change (for audit / notification body)
     */
    public void publishEvent(String eventType, int entityId, String payload) {
        for (HREventObserver observer : observers) {
            observer.onEvent(eventType, entityId, payload);
        }
    }
}
