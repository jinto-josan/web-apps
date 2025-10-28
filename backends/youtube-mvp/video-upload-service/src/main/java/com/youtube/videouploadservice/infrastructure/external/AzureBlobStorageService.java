package com.youtube.videouploadservice.infrastructure.external;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.youtube.videouploadservice.domain.services.BlobStorageService;
import com.youtube.videouploadservice.domain.valueobjects.PreSignedUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of BlobStorageService using Azure Blob Storage.
 * Provides pre-signed URLs, chunk management, and blob operations.
 */
@Slf4j
@Service
public class AzureBlobStorageService implements BlobStorageService {
    
    private final BlobServiceClient blobServiceClient;
    private final String connectionString;
    
    public AzureBlobStorageService(@Value("${azure.storage.connection-string}") String connectionString) {
        this.connectionString = connectionString;
        this.blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
    }
    
    @Override
    public PreSignedUrl generatePreSignedUrl(String containerName, String blobName, 
                                             String userId, Instant expiresAt,
                                             long maxFileSize, String contentType) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            // Create SAS token for upload
            BlobSasPermission permission = new BlobSasPermission()
                .setWritePermission(true);
            
            OffsetDateTime expiryTime = expiresAt.atOffset(ZoneOffset.UTC);
            BlobServiceSasSignatureValues sasSignatureValues = 
                new BlobServiceSasSignatureValues(expiryTime, permission);
            
            String sasToken = blobClient.generateSas(sasSignatureValues);
            String url = blobClient.getBlobUrl() + "?" + sasToken;
            
            log.info("Generated pre-signed URL for user: {}, blob: {}", userId, blobName);
            
            return PreSignedUrl.builder()
                .url(url)
                .expiresAt(expiresAt)
                .blobName(blobName)
                .containerName(containerName)
                .maxFileSizeBytes(maxFileSize)
                .durationMinutes((int) java.time.Duration.between(Instant.now(), expiresAt).toMinutes())
                .build();
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for blob: {}", blobName, e);
            throw new RuntimeException("Failed to generate pre-signed URL", e);
        }
    }
    
    @Override
    public Map<Integer, PreSignedUrl> generateChunkUrls(String containerName, String blobName,
                                                        String userId, long chunkSize, int totalChunks,
                                                        Instant expiresAt, String contentType) {
        Map<Integer, PreSignedUrl> urls = new HashMap<>();
        
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            // Create SAS token for block blob upload
            BlobSasPermission permission = new BlobSasPermission()
                .setWritePermission(true)
                .setCreatePermission(true);
            
            OffsetDateTime expiryTime = expiresAt.atOffset(ZoneOffset.UTC);
            BlobServiceSasSignatureValues sasSignatureValues = 
                new BlobServiceSasSignatureValues(expiryTime, permission);
            
            String sasToken = blobClient.generateSas(sasSignatureValues);
            String baseUrl = blobClient.getBlobUrl() + "?" + sasToken;
            
            // Generate URLs for each chunk
            for (int i = 0; i < totalChunks; i++) {
                PreSignedUrl chunkUrl = PreSignedUrl.builder()
                    .url(baseUrl)
                    .expiresAt(expiresAt)
                    .blobName(blobName + "_chunk_" + i)
                    .containerName(containerName)
                    .maxFileSizeBytes(chunkSize)
                    .durationMinutes((int) java.time.Duration.between(Instant.now(), expiresAt).toMinutes())
                    .build();
                
                urls.put(i, chunkUrl);
            }
            
            log.info("Generated {} chunk URLs for blob: {}", totalChunks, blobName);
            
        } catch (Exception e) {
            log.error("Failed to generate chunk URLs for blob: {}", blobName, e);
            throw new RuntimeException("Failed to generate chunk URLs", e);
        }
        
        return urls;
    }
    
    @Override
    public boolean verifyBlobComplete(String containerName, String blobName) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            return blobClient.exists();
        } catch (Exception e) {
            log.error("Failed to verify blob: {}", blobName, e);
            return false;
        }
    }
    
    @Override
    public long getBlobSize(String containerName, String blobName) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            if (!blobClient.exists()) {
                return -1;
            }
            
            return blobClient.getProperties().getBlobSize();
        } catch (Exception e) {
            log.error("Failed to get blob size: {}", blobName, e);
            return -1;
        }
    }
    
    @Override
    public String getBlobEtag(String containerName, String blobName) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            if (!blobClient.exists()) {
                return null;
            }
            
            return blobClient.getProperties().getETag();
        } catch (Exception e) {
            log.error("Failed to get blob ETag: {}", blobName, e);
            return null;
        }
    }
    
    @Override
    public boolean deleteBlob(String containerName, String blobName) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            boolean deleted = blobClient.deleteIfExists();
            log.info("Blob deleted: {} (success: {})", blobName, deleted);
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete blob: {}", blobName, e);
            return false;
        }
    }
    
    @Override
    public Map<String, Long> listBlobs(String containerName, String prefix) {
        Map<String, Long> blobs = new HashMap<>();
        
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            for (BlobItem blobItem : containerClient.listBlobsByHierarchy(prefix)) {
                blobs.put(blobItem.getName(), blobItem.getProperties().getContentLength());
            }
            
        } catch (Exception e) {
            log.error("Failed to list blobs in container: {}", containerName, e);
        }
        
        return blobs;
    }
}

