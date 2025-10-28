package com.youtube.drmservice.domain.models;

import lombok.Builder;
import lombok.Value;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DRM Policy aggregate root
 */
@Value
@Builder(toBuilder = true)
public class DrmPolicy {
    String id;
    String videoId;
    DrmProvider provider;
    PolicyConfiguration configuration;
    KeyRotationPolicy rotationPolicy;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    Long version; // For ETag/Optimistic locking
    
    public enum DrmProvider {
        WIDEVINE,
        PLAYREADY,
        FAIRPLAY
    }
}

