package com.youtube.videouploadservice.domain.valueobjects;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Value object representing a pre-signed URL for direct upload.
 * Enables client-side uploads to Azure Blob Storage without server intermediation.
 */
@Value
@Builder
public class PreSignedUrl {
    
    String url;
    Instant expiresAt;
    String uploadId;
    String blobName;
    String containerName;
    Long maxFileSizeBytes;
    Integer durationMinutes;
    
    /**
     * Check if the URL has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
    
    /**
     * Get remaining validity duration in seconds.
     */
    public long getRemainingSeconds() {
        long seconds = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, seconds);
    }
}

