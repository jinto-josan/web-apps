package com.youtube.captionssubtitlesservice.infrastructure.storage;

import com.azure.storage.blob.*;
import com.youtube.captionssubtitlesservice.domain.services.CaptionStorageService;
import com.youtube.captionssubtitlesservice.domain.valueobjects.CaptionFormat;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Azure Blob Storage adapter for caption files
 */
@Slf4j
@Service
public class BlobCaptionStorageAdapter implements CaptionStorageService {
    
    private final BlobServiceClient blobServiceClient;
    private final String containerName;
    
    public BlobCaptionStorageAdapter(
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.captions-container}") String containerName) {
        this.blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        this.containerName = containerName;
        
        // Ensure container exists
        ensureContainerExists();
    }
    
    private void ensureContainerExists() {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.create();
            log.info("Created caption container: {}", containerName);
        }
    }
    
    @Override
    @Retry(name = "blob-storage")
    public String uploadCaption(String videoId, String captionId, CaptionFormat format, InputStream content) {
        String blobName = String.format("%s/%s%s", videoId, captionId, format.getFileExtension());
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        
        blobClient.upload(content, true);
        
        String blobUri = blobClient.getBlobUrl();
        log.info("Uploaded caption to blob: {}", blobUri);
        
        return blobUri;
    }
    
    @Override
    @Retry(name = "blob-storage")
    public String downloadCaption(String blobUri) {
        BlobClient blobClient = blobServiceClient.getBlobClient(containerName, extractBlobName(blobUri));
        return blobClient.downloadContent().toString();
    }
    
    @Override
    @Retry(name = "blob-storage")
    public void deleteCaption(String blobUri) {
        BlobClient blobClient = blobServiceClient.getBlobClient(containerName, extractBlobName(blobUri));
        blobClient.delete();
        log.info("Deleted caption blob: {}", blobUri);
    }
    
    @Override
    public String getETag(String blobUri) {
        BlobClient blobClient = blobServiceClient.getBlobClient(containerName, extractBlobName(blobUri));
        BlobProperties properties = blobClient.getProperties();
        return properties.getETag();
    }
    
    private String extractBlobName(String blobUri) {
        // Extract blob name from full URI
        int lastSlash = blobUri.lastIndexOf('/');
        return blobUri.substring(lastSlash + 1);
    }
}
