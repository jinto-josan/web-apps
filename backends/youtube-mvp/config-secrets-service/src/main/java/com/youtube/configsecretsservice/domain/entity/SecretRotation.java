package com.youtube.configsecretsservice.domain.entity;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a secret rotation record.
 */
@Value
@Builder(toBuilder = true)
public class SecretRotation {
    UUID id;
    String scope;
    String key;
    RotationStatus status;
    Instant scheduledAt;
    Instant completedAt;
    String triggeredBy;
    String errorMessage;
    Boolean dryRun;

    public enum RotationStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}

