package com.youtube.videouploadservice.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Entity representing a chunk upload for resumable uploads.
 * Supports multipart/chunked uploads for large files.
 */
@Data
@Builder
public class ChunkUpload {
    
    private String id;
    private String uploadId; // Reference to VideoUpload
    private Integer chunkNumber; // 0-based index
    private Integer totalChunks;
    private long chunkSizeBytes;
    private long chunkStartByte; // Start byte position in file
    private long chunkEndByte; // End byte position in file
    private ChunkStatus status;
    private String etag; // Blob block ETag
    private String preSignedUrl; // URL for uploading this chunk
    private Instant expiresAt; // URL expiration
    private Instant uploadedAt;
    private String errorMessage;
    
    public enum ChunkStatus {
        PENDING,
        UPLOADING,
        COMPLETED,
        FAILED,
        VERIFIED
    }
    
    /**
     * Calculate chunk progress percentage.
     */
    public double getProgressPercentage() {
        if (totalChunks == null || totalChunks == 0) return 0.0;
        return ((double) (chunkNumber + 1) / totalChunks) * 100.0;
    }
    
    /**
     * Mark chunk as completed.
     */
    public void markAsCompleted(String etag, Instant uploadedAt) {
        this.status = ChunkStatus.COMPLETED;
        this.etag = etag;
        this.uploadedAt = uploadedAt;
    }
}

