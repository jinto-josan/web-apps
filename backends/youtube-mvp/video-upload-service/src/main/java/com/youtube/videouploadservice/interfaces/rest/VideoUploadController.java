package com.youtube.videouploadservice.interfaces.rest;

import com.youtube.videouploadservice.application.saga.InitializeUploadSaga;
import com.youtube.videouploadservice.application.saga.SagaExecutionException;
import com.youtube.videouploadservice.domain.entities.VideoUpload;
import com.youtube.videouploadservice.domain.repositories.VideoUploadRepository;
import com.youtube.videouploadservice.domain.valueobjects.PreSignedUrl;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for video upload operations.
 * Provides endpoints for initiating, tracking, and managing video uploads.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
// @Tag(name = "Video Upload", description = "Video upload management endpoints")
public class VideoUploadController {
    
    private final VideoUploadRepository uploadRepo;
    
    /**
     * Initialize a new video upload.
     * Returns pre-signed URL for direct upload to Azure Blob Storage.
     * 
     * Performance target: P95 < 100ms
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('USER')")
    // @Operation(summary = "Initialize upload", description = "Get pre-signed URL for video upload")
    public ResponseEntity<UploadInitResponse> initializeUpload(
            @RequestBody InitUploadRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            String userId = jwt.getSubject();
            String sagaId = UUID.randomUUID().toString();
            
            // Create and execute saga
            InitializeUploadSaga saga = new InitializeUploadSaga(
                sagaId, userId, request.getChannelId(), request.getTitle(),
                request.getDescription(), request.getFileSizeBytes(),
                request.getContentType(), request.getExpirationMinutes(),
                uploadRepo, null, null, null
            );
            
            PreSignedUrl preSignedUrl = saga.execute();
            
            return ResponseEntity.ok(new UploadInitResponse(
                sagaId, preSignedUrl.getUrl(), preSignedUrl.getExpiresAt(),
                preSignedUrl.getBlobName(), preSignedUrl.getDurationMinutes()
            ));
            
        } catch (SagaExecutionException e) {
            log.error("Failed to initialize upload: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get upload status.
     */
    @GetMapping("/{uploadId}/status")
    @PreAuthorize("hasRole('USER')")
    // @Operation(summary = "Get upload status", description = "Check upload progress")
    public ResponseEntity<UploadStatusResponse> getUploadStatus(
            @PathVariable String uploadId,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        
        var result = uploadRepo.findById(uploadId);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        var upload = result.get();
        if (!upload.getUserId().equals(userId)) {
            return ResponseEntity.status(403).<UploadStatusResponse>build();
        }
        
        UploadStatusResponse response = UploadStatusResponse.builder()
            .uploadId(upload.getId())
            .status(upload.getStatus().name())
            .progressPercentage(upload.getProgressPercentage())
            .totalSizeBytes(upload.getTotalSizeBytes())
            .uploadedSizeBytes(upload.getUploadedSizeBytes())
            .errorMessage(upload.getErrorMessage())
            .build();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel an upload.
     */
    @PostMapping("/{uploadId}/cancel")
    @PreAuthorize("hasRole('USER')")
    // @Operation(summary = "Cancel upload", description = "Cancel an active upload")
    public ResponseEntity<Void> cancelUpload(
            @PathVariable String uploadId,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        
        var result = uploadRepo.findById(uploadId);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        var upload = result.get();
        if (!upload.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        // Update status to cancelled
        uploadRepo.updateStatus(uploadId, VideoUpload.UploadStatus.CANCELLED);
        return ResponseEntity.ok().build();
    }
    
    // Request/Response DTOs
    
    @Data
    public static class InitUploadRequest {
        private String channelId;
        private String title;
        private String description;
        private long fileSizeBytes;
        private String contentType;
        private Integer expirationMinutes = 60; // Default 1 hour
    }
    
    @Data
    @lombok.Builder
    public static class UploadInitResponse {
        private String uploadId;
        private String preSignedUrl;
        private java.time.Instant expiresAt;
        private String blobName;
        private Integer durationMinutes;
    }
    
    @Data
    @lombok.Builder
    public static class UploadStatusResponse {
        private String uploadId;
        private String status;
        private double progressPercentage;
        private Long totalSizeBytes;
        private Long uploadedSizeBytes;
        private String errorMessage;
    }
}

