package com.youtube.drmservice.domain.repositories;

import com.youtube.drmservice.domain.models.DrmPolicy;
import java.util.Optional;

/**
 * Port for DRM policy persistence
 */
public interface DrmPolicyRepository {
    Optional<DrmPolicy> findById(String id);
    Optional<DrmPolicy> findByVideoId(String videoId);
    DrmPolicy save(DrmPolicy policy);
    void delete(String id);
    boolean existsByVideoId(String videoId);
}

