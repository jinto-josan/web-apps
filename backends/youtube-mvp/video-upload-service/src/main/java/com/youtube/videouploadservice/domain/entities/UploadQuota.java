package com.youtube.videouploadservice.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Entity tracking user upload quotas.
 * Prevents abuse and manages resource allocation.
 */
@Data
@Builder
public class UploadQuota {
    
    private String userId;
    private QuotaType quotaType;
    private Long currentUsage; // Current usage in bytes
    private Long quotaLimit; // Limit in bytes
    private Instant periodStart; // Start of current quota period
    private Instant periodEnd; // End of current quota period
    private Integer uploadCount; // Number of uploads in current period
    private Integer uploadLimit; // Max uploads per period
    
    public enum QuotaType {
        DAILY,
        WEEKLY,
        MONTHLY,
        LIFETIME
    }
    
    /**
     * Check if user has exceeded quota.
     */
    public boolean isExceeded() {
        return currentUsage >= quotaLimit || uploadCount >= uploadLimit;
    }
    
    /**
     * Calculate remaining quota.
     */
    public long getRemainingQuota() {
        return Math.max(0, quotaLimit - currentUsage);
    }
    
    /**
     * Check if quota period has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(periodEnd);
    }
    
    /**
     * Consume quota for a new upload.
     */
    public void consumeQuota(long sizeBytes) {
        this.currentUsage += sizeBytes;
        this.uploadCount++;
    }
    
    /**
     * Release quota (for cancelled or failed uploads).
     */
    public void releaseQuota(long sizeBytes) {
        this.currentUsage = Math.max(0, currentUsage - sizeBytes);
    }
    
    /**
     * Reset quota for new period.
     */
    public void resetQuota(Instant periodStart, Instant periodEnd) {
        this.currentUsage = 0L;
        this.uploadCount = 0;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }
}

