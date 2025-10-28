package com.youtube.drmservice.domain.services;

import com.youtube.drmservice.domain.models.DrmPolicy;
import java.util.Optional;

/**
 * Port for cache operations
 */
public interface CacheService {
    Optional<DrmPolicy> getPolicy(String policyId);
    void putPolicy(DrmPolicy policy);
    void evictPolicy(String policyId);
    void evictPolicyByVideoId(String videoId);
}

