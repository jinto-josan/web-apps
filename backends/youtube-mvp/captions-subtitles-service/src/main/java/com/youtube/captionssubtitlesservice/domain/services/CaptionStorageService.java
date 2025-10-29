package com.youtube.captionssubtitlesservice.domain.services;

import com.youtube.captionssubtitlesservice.domain.valueobjects.CaptionFormat;

import java.io.InputStream;

/**
 * Domain service interface for storing caption files in blob storage
 */
public interface CaptionStorageService {
    
    /**
     * Upload caption file to blob storage
     * @param videoId Video identifier
     * @param captionId Caption identifier
     * @param format Caption file format
     * @param content Caption content
     * @return Blob URI
     */
    String uploadCaption(String videoId, String captionId, CaptionFormat format, InputStream content);
    
    /**
     * Download caption with checking ETag to confirm unchanged since last fetch
     * @param blobUri Blob URI
     * @return Caption content
     */
    String downloadCaption(String blobUri);
    
    /**
     * Delete caption file from blob storage
     * @param blobUri Blob URI
     */
    void deleteCaption(String blobUri);
    
    /**
     * Get caption ETag for version control
     * @param blobUri Blob URI
     * @return ETag value
     */
    String getETag(String blobUri);
}
