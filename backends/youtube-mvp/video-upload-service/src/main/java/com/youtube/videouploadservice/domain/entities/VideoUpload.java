package com.youtube.videouploadservice.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Entity representing a video upload session.
 * Tracks the upload progress and metadata.
 */
@Data
@Builder
public class VideoUpload {
    
    private String id; // Upload session ID
    private String userId;
    private String channelId;
    private String videoTitle;
    private String videoDescription;
    private UploadStatus status;
    private long totalSizeBytes;
    private long uploadedSizeBytes;
    private String blobName; // Azure Blob Storage name
    private String blobContainer; // Container name
    private String contentType; // MIME type (e.g., video/mp4)
    private String etag; // Blob ETag for validation
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt; // Pre-signed URL expiration
    private Integer expirationMinutes; // Minutes until URL expires
    private String errorMessage;
    private Integer retryCount;
    private Integer maxRetries;
    
    public enum UploadStatus {
        INITIALIZING,
        UPLOADING,
        UPLOAD_COMPLETE,
        VALIDATING,
        VALIDATION_COMPLETE,
        TRANSCODE_QUEUED,
        FAILED,
        CANCELLED,
        EXPIRED
    }
    
    /**
     * Calculate upload progress percentage.
     */
    public double getProgressPercentage() {
        if (totalSizeBytes == 0) return 0.0;
        return ((double) uploadedSizeBytes / totalSizeBytes) * 100.0;
    }
    
    /**
     * Check if upload is complete.
     */
    public boolean isComplete() {
        return uploadedSizeBytes >= totalSizeBytes && uploadedSizeBytes > 0;
    }
    
    /**
     * Check if upload can be resumed.
     */
    public boolean isResumable() {
        return status == UploadStatus.UPLOADING || 
               status == UploadStatus.UPLOAD_COMPLETE ||
               status == UploadStatus.FAILED;
    }
    
    /**
     * Mark upload as failed.
     */
    public void markAsFailed(String error) {
        this.status = UploadStatus.FAILED;
        this.errorMessage = error;
        this.updatedAt = Instant.now();
    }
}

