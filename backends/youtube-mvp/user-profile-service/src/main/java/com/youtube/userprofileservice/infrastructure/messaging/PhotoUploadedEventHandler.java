package com.youtube.userprofileservice.infrastructure.messaging;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.youtube.common.domain.core.DomainEvent;
import com.youtube.common.domain.events.EventRouter;
import com.youtube.userprofileservice.domain.events.PhotoUploadedEvent;
import com.youtube.userprofileservice.domain.services.ImageProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Event handler for PhotoUploadedEvent.
 * Performs virus scanning and image compression.
 */
@Slf4j
@Component
public class PhotoUploadedEventHandler implements EventRouter.EventHandler<DomainEvent> {
    
    private final ImageProcessingService imageProcessingService;
    private final BlobServiceClient blobServiceClient;
    private final String containerName;
    
    public PhotoUploadedEventHandler(
            @Value("${azure.blob.storage.connection-string:}") String blobConnectionString,
            @Value("${azure.blob.storage.container-name:profile-photos}") String containerName,
            ImageProcessingService imageProcessingService) {
        this.containerName = containerName;
        this.imageProcessingService = imageProcessingService;
        
        if (blobConnectionString == null || blobConnectionString.isBlank()) {
            log.warn("Blob Storage connection string not configured - photo processing disabled");
            this.blobServiceClient = null;
        } else {
            this.blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(blobConnectionString)
                    .buildClient();
        }
    }
    
    @Override
    public void handle(DomainEvent event, String correlationId) {
        if (!(event instanceof PhotoUploadedEvent)) {
            throw new IllegalArgumentException("Expected PhotoUploadedEvent but got: " + event.getClass());
        }
        
        PhotoUploadedEvent photoEvent = (PhotoUploadedEvent) event;
        log.info("Handling PhotoUploadedEvent - accountId: {}, blobName: {}, correlationId: {}",
                photoEvent.getAccountId(), photoEvent.getBlobName(), correlationId);
        
        if (blobServiceClient == null) {
            log.warn("Blob Storage not configured - skipping photo processing");
            return;
        }
        
        try {
            // Download the uploaded image
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(photoEvent.getBlobName());
            
            if (!blobClient.exists()) {
                log.warn("Blob not found - blobName: {}, correlationId: {}", photoEvent.getBlobName(), correlationId);
                return;
            }
            
            // Download original image
            ByteArrayOutputStream originalStream = new ByteArrayOutputStream();
            blobClient.downloadStream(originalStream);
            byte[] originalBytes = originalStream.toByteArray();
            
            log.info("Downloaded image for processing - blobName: {}, size: {} bytes, correlationId: {}",
                    photoEvent.getBlobName(), originalBytes.length, correlationId);
            
            // Step 1: Virus scanning
            try (InputStream imageStream = new ByteArrayInputStream(originalBytes)) {
                boolean isSafe = imageProcessingService.scanForViruses(imageStream, photoEvent.getBlobName());
                
                if (!isSafe) {
                    log.error("Virus detected in image - blobName: {}, correlationId: {}", 
                            photoEvent.getBlobName(), correlationId);
                    // Delete the malicious file
                    blobClient.delete();
                    return;
                }
            }
            
            log.info("Virus scan passed - blobName: {}, correlationId: {}", 
                    photoEvent.getBlobName(), correlationId);
            
            // Step 2: Compress image
            String compressedBlobName = getCompressedBlobName(photoEvent.getBlobName());
            BlobClient compressedBlobClient = containerClient.getBlobClient(compressedBlobName);
            
            try (InputStream imageInputStream = new ByteArrayInputStream(originalBytes);
                 ByteArrayOutputStream compressedStream = new ByteArrayOutputStream()) {
                
                long compressedSize = imageProcessingService.compressImage(
                        imageInputStream,
                        compressedStream,
                        photoEvent.getContentType(),
                        1920, // max width
                        1920, // max height
                        0.85f // quality
                );
                
                byte[] compressedBytes = compressedStream.toByteArray();
                
                // Upload compressed image
                compressedBlobClient.upload(new ByteArrayInputStream(compressedBytes), compressedBytes.length, true);
                
                log.info("Image compression completed - original: {} bytes, compressed: {} bytes, blobName: {}, correlationId: {}",
                        originalBytes.length, compressedSize, compressedBlobName, correlationId);
            }
            
            // Step 3: Update profile with compressed photo URL
            String photoUrl = compressedBlobClient.getBlobUrl();
            // Note: In a real implementation, you would update the profile here
            // For now, we'll just log it
            log.info("Photo processing completed - accountId: {}, photoUrl: {}, correlationId: {}",
                    photoEvent.getAccountId(), photoUrl, correlationId);
            
        } catch (ImageProcessingService.ImageProcessingException e) {
            log.error("Image processing failed - blobName: {}, correlationId: {}", 
                    photoEvent.getBlobName(), correlationId, e);
            throw new RuntimeException("Image processing failed", e);
        } catch (Exception e) {
            log.error("Error processing photo - blobName: {}, correlationId: {}", 
                    photoEvent.getBlobName(), correlationId, e);
            throw new RuntimeException("Failed to process photo", e);
        }
    }
    
    private String getCompressedBlobName(String originalBlobName) {
        // Add "_compressed" before the extension
        int lastDot = originalBlobName.lastIndexOf('.');
        if (lastDot > 0) {
            return originalBlobName.substring(0, lastDot) + "_compressed" + originalBlobName.substring(lastDot);
        }
        return originalBlobName + "_compressed";
    }
}

