package com.hr.service;

// GoF Design Pattern: Observer — concrete subscriber that writes audit log entries.
//
// Justification: Audit logging is a cross-cutting concern that must fire on every
// entity state change without polluting domain controllers with DAO-level calls.
// Registering this observer with HREventPublisher achieves automatic, decoupled
// audit trail capture (UC-02, UC-05, UC-09, UC-12 compliance requirement).

import java.sql.SQLException;

public class AuditLogObserver implements HREventObserver {

    private final AuditLogService auditLogService;

    public AuditLogObserver(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public void onEvent(String eventType, int entityId, String payload) {
        try {
            auditLogService.writeAuditLog(entityId, eventType, "", payload, 0);
            System.out.println("[AUDIT OBSERVER] Logged event: " + eventType
                    + " | Entity: " + entityId);
        } catch (SQLException e) {
            System.err.println("[AUDIT OBSERVER] Failed to log event: " + e.getMessage());
        }
    }
}
