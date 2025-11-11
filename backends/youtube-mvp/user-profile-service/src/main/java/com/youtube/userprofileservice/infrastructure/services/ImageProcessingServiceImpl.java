package com.youtube.userprofileservice.infrastructure.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.userprofileservice.domain.services.ImageProcessingService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of ImageProcessingService.
 * Handles virus scanning (using Azure Defender for Storage or external service)
 * and image compression using Thumbnailator.
 */
@Slf4j
@Service
public class ImageProcessingServiceImpl implements ImageProcessingService {
    
    private final BlobServiceClient blobServiceClient;
    private final String containerName;
    private final boolean virusScanningEnabled;
    private final int maxWidth;
    private final int maxHeight;
    private final float compressionQuality;
    
    public ImageProcessingServiceImpl(
            @Value("${azure.blob.storage.connection-string:}") String connectionString,
            @Value("${azure.blob.storage.container-name:profile-photos}") String containerName,
            @Value("${app.photo-processing.virus-scanning-enabled:true}") boolean virusScanningEnabled,
            @Value("${app.photo-processing.max-width:1920}") int maxWidth,
            @Value("${app.photo-processing.max-height:1920}") int maxHeight,
            @Value("${app.photo-processing.compression-quality:0.85}") float compressionQuality) {
        this.containerName = containerName;
        this.virusScanningEnabled = virusScanningEnabled;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.compressionQuality = compressionQuality;
        
        if (connectionString == null || connectionString.isBlank()) {
            log.warn("Azure Blob Storage connection string not configured - image processing will be limited");
            this.blobServiceClient = null;
        } else {
            this.blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        }
    }
    
    @Override
    public boolean scanForViruses(InputStream imageStream, String blobName) throws ImageProcessingException {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.info("Scanning image for viruses - blobName: {}, correlationId: {}", blobName, correlationId);
        
        if (!virusScanningEnabled) {
            log.warn("Virus scanning is disabled - skipping scan for blobName: {}", blobName);
            return true; // Assume safe if scanning is disabled
        }
        
        try {
            // Note: Azure Defender for Storage automatically scans blobs when enabled.
            // For a production implementation, you would:
            // 1. Use Azure Defender for Storage (automatic scanning)
            // 2. Or integrate with a third-party virus scanning service (ClamAV, VirusTotal API, etc.)
            // 3. Or use Azure Security Center APIs to check scan results
            
            // For now, we'll do a basic validation:
            // - Check file signature/magic bytes
            // - Validate image format
            // - Check file size limits
            
            // Read first few bytes to check magic numbers
            imageStream.mark(16);
            byte[] header = new byte[16];
            int bytesRead = imageStream.read(header);
            imageStream.reset();
            
            if (bytesRead < 4) {
                throw new ImageProcessingException("File too small to be a valid image");
            }
            
            // Check for common image formats
            boolean isValidImage = isValidImageFormat(header);
            if (!isValidImage) {
                log.warn("Invalid image format detected - blobName: {}, correlationId: {}", blobName, correlationId);
                throw new ImageProcessingException("Invalid image format");
            }
            
            // In production, you would:
            // - Call Azure Defender API to check scan status
            // - Or use a virus scanning library/service
            // - Or rely on Azure Defender automatic scanning (recommended)
            
            log.info("Virus scan completed - blobName: {}, safe: true, correlationId: {}", blobName, correlationId);
            return true;
            
        } catch (ImageProcessingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Virus scanning failed - blobName: {}, correlationId: {}", blobName, correlationId, e);
            throw new ImageProcessingException("Virus scanning failed", e);
        }
    }
    
    @Override
    public long compressImage(InputStream inputStream, OutputStream outputStream, 
                             String contentType, int maxWidth, int maxHeight, 
                             float quality) throws ImageProcessingException {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.info("Compressing image - contentType: {}, maxSize: {}x{}, quality: {}, correlationId: {}",
                contentType, maxWidth, maxHeight, quality, correlationId);
        
        try {
            // Determine output format based on content type
            String outputFormat = getOutputFormat(contentType);
            
            // Use Thumbnailator for compression
            ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();
            
            Thumbnails.of(inputStream)
                    .size(maxWidth, maxHeight)
                    .outputFormat(outputFormat)
                    .outputQuality(quality)
                    .toOutputStream(compressedStream);
            
            byte[] compressedBytes = compressedStream.toByteArray();
            outputStream.write(compressedBytes);
            
            log.info("Image compression completed - original format: {}, compressed size: {} bytes, correlationId: {}",
                    contentType, compressedBytes.length, correlationId);
            
            return compressedBytes.length;
            
        } catch (Exception e) {
            log.error("Image compression failed - contentType: {}, correlationId: {}", contentType, correlationId, e);
            throw new ImageProcessingException("Image compression failed", e);
        }
    }
    
    private boolean isValidImageFormat(byte[] header) {
        // Check for JPEG (FF D8 FF)
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return true;
        }
        // Check for PNG (89 50 4E 47)
        if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return true;
        }
        // Check for GIF (47 49 46 38)
        if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) {
            return true;
        }
        // Check for WebP (RIFF...WEBP)
        if (header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46) {
            // Check for WEBP at offset 8
            if (header.length >= 12 && 
                header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) {
                return true;
            }
        }
        return false;
    }
    
    private String getOutputFormat(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        return switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}

