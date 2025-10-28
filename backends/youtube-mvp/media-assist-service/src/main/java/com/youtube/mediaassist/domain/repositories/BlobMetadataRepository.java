package com.youtube.mediaassist.domain.repositories;

import java.util.Optional;

/**
 * Repository interface for blob metadata operations
 */
public interface BlobMetadataRepository {
    
    /**
     * Check if a blob exists
     */
    boolean exists(String blobPath);
    
    /**
     * Get blob metadata if exists
     */
    Optional<BlobMetadata> findByPath(String blobPath);
    
    /**
     * Save blob metadata
     */
    void save(BlobMetadata metadata);
    
    /**
     * Delete blob metadata
     */
    void delete(String blobPath);
    
    /**
     * Immutable blob metadata value object
     */
    record BlobMetadata(
        String path,
        long size,
        String contentType,
        String etag,
        Long lastModified,
        String container
    ) {}
}

