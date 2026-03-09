package com.ezinnovations.ezchat.model;

public record AuditLogEntry(long id,
                            String actorUuid,
                            String actorName,
                            String auditType,
                            String details,
                            long timestamp) {
}
