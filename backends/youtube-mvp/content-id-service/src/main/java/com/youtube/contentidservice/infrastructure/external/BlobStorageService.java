package com.youtube.contentidservice.infrastructure.external;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@Slf4j
public class BlobStorageService {
    private final BlobServiceClient blobServiceClient;
    private final String containerName;

    public BlobStorageService(
            @Value("${azure.storage.connection-string:}") String connectionString,
            @Value("${azure.storage.container.fingerprints:fingerprints}") String containerName) {
        this.containerName = containerName;
        if (connectionString != null && !connectionString.isBlank()) {
            this.blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        } else {
            this.blobServiceClient = null;
            log.warn("Blob storage connection string not configured");
        }
    }

    public String uploadFingerprint(byte[] fingerprintData, String blobName) {
        if (blobServiceClient == null) {
            throw new IllegalStateException("Blob storage not configured");
        }

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(new ByteArrayInputStream(fingerprintData), fingerprintData.length, true);

        return blobClient.getBlobUrl();
    }

    public InputStream downloadFingerprint(String blobUri) {
        if (blobServiceClient == null) {
            throw new IllegalStateException("Blob storage not configured");
        }

        // Extract blob name from URI
        String blobName = extractBlobName(blobUri);
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        return blobClient.openInputStream();
    }

    private String extractBlobName(String blobUri) {
        // Simple extraction - in production, use proper URI parsing
        int lastSlash = blobUri.lastIndexOf('/');
        return lastSlash >= 0 ? blobUri.substring(lastSlash + 1) : blobUri;
    }
}

