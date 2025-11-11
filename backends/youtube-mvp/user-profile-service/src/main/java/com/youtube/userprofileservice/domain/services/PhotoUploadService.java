package com.youtube.userprofileservice.domain.services;

import java.time.Instant;

/**
 * Service for generating photo upload URLs and managing photo uploads.
 */
public interface PhotoUploadService {
    
    /**
     * Generates a pre-signed URL for uploading a profile photo.
     * 
     * @param accountId the account ID
     * @param contentType the content type of the image (e.g., "image/jpeg", "image/png")
     * @param maxFileSizeBytes maximum file size in bytes
     * @return PhotoUploadUrl containing the upload URL and metadata
     */
    PhotoUploadUrl generateUploadUrl(String accountId, String contentType, long maxFileSizeBytes);
    
    /**
     * Represents a pre-signed URL for photo upload.
     */
    record PhotoUploadUrl(
        String uploadUrl,
        String blobName,
        String containerName,
        Instant expiresAt,
        long maxFileSizeBytes,
        int durationMinutes
    ) {}
}


