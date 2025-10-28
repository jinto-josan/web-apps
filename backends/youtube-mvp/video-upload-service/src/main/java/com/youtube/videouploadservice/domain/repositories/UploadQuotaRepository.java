package com.youtube.videouploadservice.domain.repositories;

import com.youtube.videouploadservice.domain.entities.UploadQuota;

import java.util.Optional;

/**
 * Repository interface for UploadQuota entity.
 * Manages user upload quotas and limits.
 */
public interface UploadQuotaRepository {
    
    /**
     * Get or create quota for a user.
     */
    UploadQuota getOrCreate(String userId, UploadQuota.QuotaType quotaType);
    
    /**
     * Find quota by user ID and type.
     */
    Optional<UploadQuota> findByUserIdAndType(String userId, UploadQuota.QuotaType quotaType);
    
    /**
     * Save quota.
     */
    UploadQuota save(UploadQuota quota);
    
    /**
     * Consume quota for upload.
     */
    void consumeQuota(String userId, long sizeBytes, UploadQuota.QuotaType quotaType);
    
    /**
     * Release quota (for cancelled/failed uploads).
     */
    void releaseQuota(String userId, long sizeBytes, UploadQuota.QuotaType quotaType);
    
    /**
     * Check if user has remaining quota.
     */
    boolean hasRemainingQuota(String userId, long sizeBytes, UploadQuota.QuotaType quotaType);
    
    /**
     * Get remaining quota in bytes.
     */
    long getRemainingQuota(String userId, UploadQuota.QuotaType quotaType);
}

