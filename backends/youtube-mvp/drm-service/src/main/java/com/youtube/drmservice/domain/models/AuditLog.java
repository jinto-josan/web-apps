package com.youtube.drmservice.domain.models;

import lombok.Builder;
import lombok.Value;
import java.time.Instant;
import java.util.Map;

/**
 * Value object for audit log entry
 */
@Value
@Builder
public class AuditLog {
    String id;
    String policyId;
    String action; // CREATE, UPDATE, DELETE, ROTATE
    String changedBy;
    Instant changedAt;
    Map<String, String> oldValues;
    Map<String, String> newValues;
    String correlationId;
}

