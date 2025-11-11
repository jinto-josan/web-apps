package com.youtube.userprofileservice.infrastructure.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.userprofileservice.domain.services.PhotoUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Implementation of PhotoUploadService using Azure Blob Storage.
 * Generates SAS URLs for direct client uploads.
 */
@Slf4j
@Service
public class PhotoUploadServiceImpl implements PhotoUploadService {
    
    private final BlobServiceClient blobServiceClient;
    private final String containerName;
    private final int urlExpirationMinutes;
    
    public PhotoUploadServiceImpl(
            @Value("${azure.blob.storage.connection-string:}") String connectionString,
            @Value("${azure.blob.storage.container-name:profile-photos}") String containerName,
            @Value("${app.photo-upload.url-expiration-minutes:60}") int urlExpirationMinutes) {
        this.containerName = containerName;
        this.urlExpirationMinutes = urlExpirationMinutes;
        
        if (connectionString == null || connectionString.isBlank()) {
            log.warn("Azure Blob Storage connection string not configured - photo upload will be disabled");
            this.blobServiceClient = null;
        } else {
            this.blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            ensureContainerExists();
        }
    }
    
    private void ensureContainerExists() {
        if (blobServiceClient == null) {
            return;
        }
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
                log.info("Created blob container: {}", containerName);
            }
        } catch (Exception e) {
            log.error("Failed to ensure container exists: {}", containerName, e);
            throw new RuntimeException("Failed to initialize blob container", e);
        }
    }
    
    @Override
    public PhotoUploadUrl generateUploadUrl(String accountId, String contentType, long maxFileSizeBytes) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.info("Generating photo upload URL - accountId: {}, contentType: {}, maxSize: {} bytes, correlationId: {}",
                accountId, contentType, maxFileSizeBytes, correlationId);
        
        if (blobServiceClient == null) {
            throw new IllegalStateException("Blob storage not configured");
        }
        
        try {
            // Generate unique blob name: accountId/timestamp-uuid.extension
            String extension = getExtensionFromContentType(contentType);
            String blobName = String.format("%s/%s-%s%s", 
                    accountId, 
                    Instant.now().toEpochMilli(),
                    UUID.randomUUID().toString(),
                    extension);
            
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            // Create SAS token for upload (write permission only)
            BlobSasPermission permission = new BlobSasPermission()
                    .setWritePermission(true)
                    .setCreatePermission(true);
            
            Instant expiresAt = Instant.now().plusSeconds(urlExpirationMinutes * 60L);
            OffsetDateTime expiryTime = expiresAt.atOffset(ZoneOffset.UTC);
            
            BlobServiceSasSignatureValues sasSignatureValues = 
                    new BlobServiceSasSignatureValues(expiryTime, permission)
                            .setContentType(contentType);
            
            String sasToken = blobClient.generateSas(sasSignatureValues);
            String uploadUrl = blobClient.getBlobUrl() + "?" + sasToken;
            
            log.info("Generated photo upload URL - accountId: {}, blobName: {}, expiresAt: {}, correlationId: {}",
                    accountId, blobName, expiresAt, correlationId);
            
            return new PhotoUploadUrl(
                    uploadUrl,
                    blobName,
                    containerName,
                    expiresAt,
                    maxFileSizeBytes,
                    urlExpirationMinutes
            );
        } catch (Exception e) {
            log.error("Failed to generate photo upload URL - accountId: {}, correlationId: {}",
                    accountId, correlationId, e);
            throw new RuntimeException("Failed to generate photo upload URL", e);
        }
    }
    
    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) {
            return ".jpg";
        }
        return switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}


