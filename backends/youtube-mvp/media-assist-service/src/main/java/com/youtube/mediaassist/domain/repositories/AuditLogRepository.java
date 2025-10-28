package com.youtube.mediaassist.domain.repositories;

import java.time.Instant;

/**
 * Repository for audit logging
 */
public interface AuditLogRepository {
    
    /**
     * Log an audit event
     */
    void log(AuditEvent event);
    
    /**
     * Audit event record
     */
    record AuditEvent(
        String userId,
        String operation,
        String resourcePath,
        String status,
        String details,
        String ipAddress,
        Instant timestamp
    ) {}
}

