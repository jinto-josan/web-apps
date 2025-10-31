package com.youtube.configsecretsservice.domain.entity;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an audit log entry.
 */
@Value
@Builder
public class AuditLog {
    UUID id;
    String scope;
    String key;
    AuditAction action;
    String userId;
    String tenantId;
    Instant timestamp;
    String details;
    String ipAddress;
    String userAgent;

    public enum AuditAction {
        CONFIG_READ,
        CONFIG_CREATE,
        CONFIG_UPDATE,
        CONFIG_DELETE,
        SECRET_ROTATE,
        SECRET_READ,
        ACCESS_DENIED
    }
}

