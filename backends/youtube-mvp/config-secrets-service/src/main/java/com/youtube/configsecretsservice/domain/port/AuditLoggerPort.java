package com.youtube.configsecretsservice.domain.port;

import com.youtube.configsecretsservice.domain.entity.AuditLog;

/**
 * Port for audit logging operations.
 */
public interface AuditLoggerPort {
    void log(AuditLog auditLog);
}

