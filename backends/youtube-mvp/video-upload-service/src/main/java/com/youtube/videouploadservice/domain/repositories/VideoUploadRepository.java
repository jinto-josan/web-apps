package com.youtube.videouploadservice.domain.repositories;

import com.youtube.videouploadservice.domain.entities.VideoUpload;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VideoUpload entity.
 * Abstracts data access for upload sessions.
 */
public interface VideoUploadRepository {
    
    /**
     * Save a new upload session.
     */
    VideoUpload save(VideoUpload upload);
    
    /**
     * Find upload by ID.
     */
    Optional<VideoUpload> findById(String uploadId);
    
    /**
     * Find all uploads for a user.
     */
    List<VideoUpload> findByUserId(String userId);
    
    /**
     * Find active uploads (in progress).
     */
    List<VideoUpload> findActiveUploads(String userId);
    
    /**
     * Find uploads by status.
     */
    List<VideoUpload> findByStatus(VideoUpload.UploadStatus status);
    
    /**
     * Update upload status.
     */
    void updateStatus(String uploadId, VideoUpload.UploadStatus status);
    
    /**
     * Update upload progress.
     */
    void updateProgress(String uploadId, long uploadedSizeBytes);
    
    /**
     * Delete an upload session.
     */
    void delete(String uploadId);
    
    /**
     * Check if an upload exists.
     */
    boolean exists(String uploadId);
    
    /**
     * Find expired uploads.
     */
    List<VideoUpload> findExpiredUploads();
}

