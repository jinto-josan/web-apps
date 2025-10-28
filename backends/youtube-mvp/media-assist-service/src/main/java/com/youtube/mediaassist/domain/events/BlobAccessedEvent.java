package com.youtube.mediaassist.domain.events;

import java.time.Instant;

/**
 * Domain event for blob access audit tracking
 */
public record BlobAccessedEvent(
    String userId,
    String blobPath,
    String accessType,
    boolean success,
    String errorMessage,
    Instant timestamp
) {
    public BlobAccessedEvent {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (blobPath == null || blobPath.isEmpty()) {
            throw new IllegalArgumentException("Blob path cannot be null or empty");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}

