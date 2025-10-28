package com.youtube.videouploadservice.domain.services;

import com.youtube.videouploadservice.domain.valueobjects.PreSignedUrl;

import java.time.Instant;
import java.util.Map;

/**
 * Service interface for Azure Blob Storage operations.
 * Handles pre-signed URLs, chunk uploads, and blob management.
 */
public interface BlobStorageService {
    
    /**
     * Generate a pre-signed URL for direct upload.
     * Client can upload directly to Azure Blob Storage without server intermediation.
     * 
     * @param containerName Container name
     * @param blobName Blob name (filename)
     * @param userId User ID for auditing
     * @param expiresAt Expiration time
     * @param maxFileSize Maximum file size in bytes
     * @param contentType MIME type of the file
     * @return Pre-signed URL with metadata
     */
    PreSignedUrl generatePreSignedUrl(
        String containerName,
        String blobName,
        String userId,
        Instant expiresAt,
        long maxFileSize,
        String contentType
    );
    
    /**
     * Generate pre-signed URLs for chunked upload.
     * Used for resumable uploads of large files.
     * 
     * @param containerName Container name
     * @param blobName Blob name
     * @param userId User ID
     * @param chunkSize Size of each chunk in bytes
     * @param totalChunks Total number of chunks
     * @param expiresAt Expiration time
     * @param contentType MIME type
     * @return Map of chunk numbers to pre-signed URLs
     */
    Map<Integer, PreSignedUrl> generateChunkUrls(
        String containerName,
        String blobName,
        String userId,
        long chunkSize,
        int totalChunks,
        Instant expiresAt,
        String contentType
    );
    
    /**
     * Verify blob upload completion.
     * Checks if blob exists and has correct size.
     * 
     * @param containerName Container name
     * @param blobName Blob name
     * @return true if blob is complete and valid
     */
    boolean verifyBlobComplete(String containerName, String blobName);
    
    /**
     * Get blob size.
     * 
     * @param containerName Container name
     * @param blobName Blob name
     * @return Size in bytes, or -1 if blob doesn't exist
     */
    long getBlobSize(String containerName, String blobName);
    
    /**
     * Get blob ETag for validation.
     * 
     * @param containerName Container name
     * @param blobName Blob name
     * @return ETag value
     */
    String getBlobEtag(String containerName, String blobName);
    
    /**
     * Delete a blob (for cleanup).
     * 
     * @param containerName Container name
     * @param blobName Blob name
     * @return true if deleted successfully
     */
    boolean deleteBlob(String containerName, String blobName);
    
    /**
     * List blobs in a prefix directory.
     * 
     * @param containerName Container name
     * @param prefix Prefix path
     * @return Map of blob names to sizes
     */
    Map<String, Long> listBlobs(String containerName, String prefix);
}

