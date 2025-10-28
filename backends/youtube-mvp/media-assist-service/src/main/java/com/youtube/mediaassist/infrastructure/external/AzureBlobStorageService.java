package com.youtube.mediaassist.infrastructure.external;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.youtube.mediaassist.domain.services.BlobStorageService;
import com.youtube.mediaassist.domain.valueobjects.BlobPath;
import com.youtube.mediaassist.domain.valueobjects.SasPolicy;
import com.youtube.mediaassist.domain.valueobjects.SignedUrl;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.stream.Collectors;

/**
 * Azure Blob Storage adapter implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AzureBlobStorageService implements BlobStorageService {
    
    @Value("${azure.storage.blob-endpoint}")
    private String blobEndpoint;
    
    @Value("${azure.storage.account-name}")
    private String accountName;
    
    @Value("${azure.storage.account-key}")
    private String accountKey;
    
    private BlobServiceClient blobServiceClient;
    
    /**
     * Initialize blob service client if not already initialized
     */
    private BlobServiceClient getBlobServiceClient() {
        if (blobServiceClient == null) {
            blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(blobEndpoint)
                    .connectionString("DefaultEndpointsProtocol=http;AccountName=" + accountName + 
                                     ";AccountKey=" + accountKey)
                    .buildClient();
        }
        return blobServiceClient;
    }
    
    @Override
    @CircuitBreaker(name = "blob-storage-operation")
    @Retry(name = "blob-storage-operation")
    @TimeLimiter(name = "blob-storage-operation")
    public SignedUrl generateSasUrl(BlobPath blobPath, SasPolicy policy) {
        try {
            BlobContainerClient containerClient = getBlobServiceClient()
                    .getBlobContainerClient(blobPath.getContainer().getName());
            
            BlobClient blobClient = containerClient.getBlobClient(blobPath.getPath());
            
            // Convert permissions
            EnumSet<BlobSasPermission> permissions = policy.getPermissions().stream()
                    .map(this::toBlobSasPermission)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(BlobSasPermission.class)));
            
            // Calculate expiry
            OffsetDateTime expiryTime = OffsetDateTime.now()
                    .plus(policy.getValidityDuration());
            
            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                    expiryTime,
                    permissions
            );
            
            // Set cache control
            if (policy.getCacheControl() != null) {
                BlobHttpHeaders headers = new BlobHttpHeaders();
                headers.setCacheControl(policy.getCacheControl());
                blobClient.setHttpHeaders(headers);
            }
            
            String sasToken = blobClient.generateSas(sasValues);
            String signedUrl = blobClient.getBlobUrl() + "?" + sasToken;
            
            return SignedUrl.builder()
                    .url(signedUrl)
                    .expiresAt(expiryTime.toInstant())
                    .blobPath(blobPath.getFullPath())
                    .type(SignedUrl.SignedUrlType.READ)
                    .build();
                    
        } catch (BlobStorageException e) {
            log.error("Failed to generate SAS URL for blob: {}", blobPath.getFullPath(), e);
            throw new RuntimeException("Failed to generate SAS URL", e);
        }
    }
    
    @Override
    @CircuitBreaker(name = "blob-storage-operation")
    public SignedUrl generatePlaybackUrl(BlobPath blobPath, SasPolicy policy) {
        // Generate a longer-lived URL for playback
        SasPolicy playbackPolicy = SasPolicy.builder()
                .validityDuration(policy.getValidityDuration())
                .permissions(policy.getPermissions())
                .cacheControl("public, max-age=14400") // 4 hours for playback
                .enforceHttps(true)
                .build();
        
        SignedUrl signedUrl = generateSasUrl(blobPath, playbackPolicy);
        return SignedUrl.builder()
                .url(signedUrl.getUrl())
                .expiresAt(signedUrl.getExpiresAt())
                .blobPath(signedUrl.getBlobPath())
                .type(SignedUrl.SignedUrlType.PLAYBACK)
                .build();
    }
    
    @Override
    @CircuitBreaker(name = "blob-storage-operation")
    public boolean blobExists(BlobPath blobPath) {
        try {
            BlobContainerClient containerClient = getBlobServiceClient()
                    .getBlobContainerClient(blobPath.getContainer().getName());
            return containerClient.getBlobClient(blobPath.getPath()).exists();
        } catch (Exception e) {
            log.error("Failed to check blob existence: {}", blobPath.getFullPath(), e);
            return false;
        }
    }
    
    @Override
    @CircuitBreaker(name = "blob-storage-operation")
    public void deleteBlob(BlobPath blobPath) {
        try {
            BlobContainerClient containerClient = getBlobServiceClient()
                    .getBlobContainerClient(blobPath.getContainer().getName());
            containerClient.getBlobClient(blobPath.getPath()).delete();
            log.info("Deleted blob: {}", blobPath.getFullPath());
        } catch (Exception e) {
            log.error("Failed to delete blob: {}", blobPath.getFullPath(), e);
            throw new RuntimeException("Failed to delete blob", e);
        }
    }
    
    @Override
    @CircuitBreaker(name = "blob-storage-operation")
    public void copyBlob(BlobPath sourcePath, BlobPath targetPath) {
        try {
            BlobContainerClient targetContainer = getBlobServiceClient()
                    .getBlobContainerClient(targetPath.getContainer().getName());
            
            BlobClient sourceBlob = getBlobServiceClient()
                    .getBlobContainerClient(sourcePath.getContainer().getName())
                    .getBlobClient(sourcePath.getPath());
            
            targetContainer.getBlobClient(targetPath.getPath())
                    .beginCopy(sourceBlob.getBlobUrl(), Duration.ofMinutes(5));
            
            log.info("Initiated blob copy from {} to {}", sourcePath.getFullPath(), targetPath.getFullPath());
        } catch (Exception e) {
            log.error("Failed to copy blob", e);
            throw new RuntimeException("Failed to copy blob", e);
        }
    }
    
    @Override
    @CircuitBreaker(name = "blob-storage-operation")
    public BlobProperties getBlobProperties(BlobPath blobPath) {
        try {
            BlobClient blobClient = getBlobServiceClient()
                    .getBlobContainerClient(blobPath.getContainer().getName())
                    .getBlobClient(blobPath.getPath());
            
            com.azure.storage.blob.models.BlobProperties props = blobClient.getProperties();
            
            return new BlobProperties(
                    props.getBlobSize(),
                    props.getContentType(),
                    props.getETag(),
                    props.getLastModified().toString()
            );
        } catch (Exception e) {
            log.error("Failed to get blob properties for: {}", blobPath.getFullPath(), e);
            throw new RuntimeException("Failed to get blob properties", e);
        }
    }
    
    private BlobSasPermission toBlobSasPermission(SasPolicy.SasPermission permission) {
        switch (permission) {
            case READ: return BlobSasPermission.parse("r");
            case WRITE: return BlobSasPermission.parse("w");
            case DELETE: return BlobSasPermission.parse("d");
            case LIST: return BlobSasPermission.parse("l");
            case ADD: return BlobSasPermission.parse("a");
            case CREATE: return BlobSasPermission.parse("c");
            case UPDATE: return BlobSasPermission.parse("u");
            case PROCESS: return BlobSasPermission.parse("p");
            default: throw new IllegalArgumentException("Unknown permission: " + permission);
        }
    }
}

