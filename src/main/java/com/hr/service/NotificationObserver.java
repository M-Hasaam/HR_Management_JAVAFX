package com.hr.service;

// GoF Design Pattern: Observer — concrete subscriber that sends notifications.
//
// Justification: Employee-facing notifications (e.g. "your record was updated",
// "your leave was approved") are triggered by the same events that generate
// audit entries.  Rather than coupling notification dispatch to every controller,
// this observer subscribes to HREventPublisher and handles notification routing
// independently — enabling notification logic to change without touching controllers.

public class NotificationObserver implements HREventObserver {

    private final NotificationService notificationService;

    public NotificationObserver(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onEvent(String eventType, int entityId, String payload) {
        notificationService.notifyEmployee(entityId, eventType, payload);
        System.out.println("[NOTIFICATION OBSERVER] Notified employee " + entityId
                + " of event: " + eventType);
    }
}
