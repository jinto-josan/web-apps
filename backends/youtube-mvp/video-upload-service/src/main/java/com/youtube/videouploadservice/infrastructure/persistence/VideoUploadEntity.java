package com.youtube.videouploadservice.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * JPA Entity for VideoUpload.
 */
@Data
@Entity
@Table(name = "video_upload", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
public class VideoUploadEntity {
    
    @Id
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "channel_id")
    private String channelId;
    
    @Column(name = "video_title")
    private String videoTitle;
    
    @Column(name = "video_description", columnDefinition = "TEXT")
    private String videoDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UploadStatus status;
    
    @Column(name = "total_size_bytes", nullable = false)
    private Long totalSizeBytes;
    
    @Column(name = "uploaded_size_bytes")
    private Long uploadedSizeBytes;
    
    @Column(name = "blob_name")
    private String blobName;
    
    @Column(name = "blob_container")
    private String blobContainer;
    
    @Column(name = "content_type")
    private String contentType;
    
    @Column(name = "etag")
    private String etag;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "expires_at")
    private Instant expiresAt;
    
    @Column(name = "expiration_minutes")
    private Integer expirationMinutes;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "retry_count")
    private Integer retryCount;
    
    @Column(name = "max_retries")
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
}

