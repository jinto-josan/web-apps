package com.youtube.mediaassist.domain.services;

import com.youtube.mediaassist.domain.valueobjects.BlobPath;
import com.youtube.mediaassist.domain.valueobjects.SasPolicy;
import com.youtube.mediaassist.domain.valueobjects.SignedUrl;

/**
 * Port for blob storage operations
 */
public interface BlobStorageService {
    
    /**
     * Generate a SAS URL for blob access
     */
    SignedUrl generateSasUrl(BlobPath blobPath, SasPolicy policy);
    
    /**
     * Generate a signed playback URL (optimized for CDN)
     */
    SignedUrl generatePlaybackUrl(BlobPath blobPath, SasPolicy policy);
    
    /**
     * Check if a blob exists
     */
    boolean blobExists(BlobPath blobPath);
    
    /**
     * Delete a blob
     */
    void deleteBlob(BlobPath blobPath);
    
    /**
     * Copy a blob from one location to another
     */
    void copyBlob(BlobPath sourcePath, BlobPath targetPath);
    
    /**
     * Get blob properties (size, content type, etc.)
     */
    BlobProperties getBlobProperties(BlobPath blobPath);
    
    record BlobProperties(
        long size,
        String contentType,
        String etag,
        String lastModified
    ) {}
}

