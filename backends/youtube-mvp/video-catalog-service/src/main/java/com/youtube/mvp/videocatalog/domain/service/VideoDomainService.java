package com.youtube.mvp.videocatalog.domain.service;

import com.youtube.mvp.videocatalog.domain.model.Video;

/**
 * Domain service for video business logic that doesn't belong to the aggregate.
 */
public interface VideoDomainService {
    
    /**
     * Generates next version for ETag support.
     */
    String generateVersion();
    
    /**
     * Validates video can be published.
     */
    void validatePublish(Video video);
}

