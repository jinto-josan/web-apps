package com.youtube.drmservice.domain.services;

import com.youtube.drmservice.domain.models.DrmPolicy;
import com.youtube.drmservice.domain.models.PolicyConfiguration;

/**
 * Port for Azure Media Services integration
 */
public interface AmsAdapter {
    /**
     * Create or update content key policy in AMS
     */
    String createOrUpdateContentKeyPolicy(DrmPolicy.DrmProvider provider, PolicyConfiguration config);
    
    /**
     * Generate new content key for rotation
     */
    String rotateContentKey(String policyId, String keyVaultUri);
    
    /**
     * Delete content key policy from AMS
     */
    void deleteContentKeyPolicy(String policyId);
}

