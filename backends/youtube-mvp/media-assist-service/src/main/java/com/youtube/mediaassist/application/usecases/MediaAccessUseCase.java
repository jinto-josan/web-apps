package com.youtube.mediaassist.application.usecases;

import com.youtube.mediaassist.domain.repositories.AuditLogRepository;
import com.youtube.mediaassist.domain.repositories.IdempotencyRepository;
import com.youtube.mediaassist.domain.services.BlobStorageService;
import com.youtube.mediaassist.domain.valueobjects.BlobPath;
import com.youtube.mediaassist.domain.valueobjects.SasPolicy;
import com.youtube.mediaassist.domain.valueobjects.SignedUrl;
import com.youtube.mediaassist.shared.exceptions.NotFoundException;
import com.youtube.mediaassist.shared.exceptions.SecurityException;
import com.youtube.mediaassist.shared.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Use case for generating signed URLs for media access
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaAccessUseCase {
    
    private final BlobStorageService blobStorageService;
    private final IdempotencyRepository idempotencyRepository;
    private final AuditLogRepository auditLogRepository;
    
    public GenerateSignedUrlResponse generateSignedUrl(GenerateSignedUrlRequest request) {
        // Validate idempotency
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey != null) {
            var cached = idempotencyRepository.retrieve(idempotencyKey);
            if (cached.isPresent()) {
                log.info("Returning cached result for idempotency key: {}", idempotencyKey);
                return parseCachedResponse(cached.get());
            }
        }
        
        // Validate and normalize path
        BlobPath blobPath;
        try {
            blobPath = BlobPath.fromString(request.getPath(), BlobPath.BlobContainer.valueOf(request.getContainer().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid blob path: " + e.getMessage());
        }
        
        // Check if blob exists
        if (!blobStorageService.blobExists(blobPath)) {
            auditLog(request.getUserId(), blobPath.getFullPath(), "BLOB_NOT_FOUND");
            throw new NotFoundException("Blob not found: " + blobPath.getFullPath());
        }
        
        // Create SAS policy
        SasPolicy policy = createSasPolicy(request);
        
        // Generate signed URL
        SignedUrl signedUrl;
        if (request.isPlayback()) {
            signedUrl = blobStorageService.generatePlaybackUrl(blobPath, policy);
        } else {
            signedUrl = blobStorageService.generateSasUrl(blobPath, policy);
        }
        
        // Store in idempotency cache
        if (idempotencyKey != null) {
            String response = serializeResponse(new GenerateSignedUrlResponse(signedUrl.getUrl(), signedUrl.getExpiresAt()));
            idempotencyRepository.store(idempotencyKey, response, 86400); // 24 hours
        }
        
        // Audit log
        auditLog(request.getUserId(), blobPath.getFullPath(), "SUCCESS");
        
        return new GenerateSignedUrlResponse(signedUrl.getUrl(), signedUrl.getExpiresAt());
    }
    
    private SasPolicy createSasPolicy(GenerateSignedUrlRequest request) {
        Duration duration = Duration.parse(request.getValidityDuration());
        
        // Enforce max validity
        if (duration.compareTo(Duration.ofHours(24)) > 0) {
            throw new ValidationException("Validity duration cannot exceed 24 hours");
        }
        
        Set<SasPolicy.SasPermission> permissions = request.isPlayback() ?
            Set.of(SasPolicy.SasPermission.READ) :
            Set.of(SasPolicy.SasPermission.READ, SasPolicy.SasPermission.LIST);
        
        return SasPolicy.builder()
                .validityDuration(duration)
                .permissions(permissions)
                .enforceHttps(true)
                .cacheControl(request.isPlayback() ? "public, max-age=14400" : "public, max-age=3600")
                .build();
    }
    
    private void auditLog(String userId, String resourcePath, String status) {
        try {
            auditLogRepository.log(new AuditLogRepository.AuditEvent(
                    userId,
                    "generate_signed_url",
                    resourcePath,
                    status,
                    "",
                    getClientIp(),
                    Instant.now()
            ));
        } catch (Exception e) {
            log.error("Failed to write audit log", e);
        }
    }
    
    private String getClientIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private GenerateSignedUrlResponse parseCachedResponse(String cached) {
        // Simple parsing - in production use proper JSON library
        return new GenerateSignedUrlResponse(cached, Instant.now().plus(Duration.ofHours(1)));
    }
    
    private String serializeResponse(GenerateSignedUrlResponse response) {
        return response.getUrl();
    }
    
    public static class GenerateSignedUrlRequest {
        private String userId;
        private String path;
        private String container;
        private String validityDuration = "PT1H";
        private boolean playback = false;
        private String idempotencyKey;
        
        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public String getContainer() { return container; }
        public void setContainer(String container) { this.container = container; }
        
        public String getValidityDuration() { return validityDuration; }
        public void setValidityDuration(String validityDuration) { this.validityDuration = validityDuration; }
        
        public boolean isPlayback() { return playback; }
        public void setPlayback(boolean playback) { this.playback = playback; }
        
        public String getIdempotencyKey() { return idempotencyKey; }
        public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    }
    
    public static class GenerateSignedUrlResponse {
        private String url;
        private Instant expiresAt;
        
        public GenerateSignedUrlResponse(String url, Instant expiresAt) {
            this.url = url;
            this.expiresAt = expiresAt;
        }
        
        public String getUrl() { return url; }
        public Instant getExpiresAt() { return expiresAt; }
    }
}

