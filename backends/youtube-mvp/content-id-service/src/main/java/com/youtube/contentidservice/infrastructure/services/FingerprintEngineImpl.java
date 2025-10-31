package com.youtube.contentidservice.infrastructure.services;

import com.youtube.contentidservice.domain.services.FingerprintEngine;
import com.youtube.contentidservice.domain.valueobjects.FingerprintData;
import com.youtube.contentidservice.infrastructure.external.BlobStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FingerprintEngineImpl implements FingerprintEngine {
    private final BlobStorageService blobStorageService;

    @Override
    public FingerprintData generateFingerprint(InputStream videoStream) {
        try {
            // Simplified fingerprint generation - in production, use audio/video fingerprinting library
            // For now, generate a hash-based fingerprint
            byte[] videoBytes = videoStream.readAllBytes();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(videoBytes);

            // Upload fingerprint to blob storage
            String blobName = UUID.randomUUID() + ".fp";
            String blobUri = blobStorageService.uploadFingerprint(hash, blobName);

            // Estimate duration (simplified - in production, extract from video metadata)
            int durationSeconds = estimateDuration(videoBytes.length);

            return FingerprintData.of(hash, "SHA-256", durationSeconds, blobUri);
        } catch (Exception e) {
            log.error("Error generating fingerprint", e);
            throw new RuntimeException("Failed to generate fingerprint", e);
        }
    }

    @Override
    public FingerprintData generateFingerprint(String blobUri) {
        try {
            InputStream stream = blobStorageService.downloadFingerprint(blobUri);
            return generateFingerprint(stream);
        } catch (Exception e) {
            log.error("Error generating fingerprint from blob URI: {}", blobUri, e);
            throw new RuntimeException("Failed to generate fingerprint from blob", e);
        }
    }

    private int estimateDuration(long bytes) {
        // Simplified estimation: assume ~1MB per minute
        return (int) (bytes / (1024 * 1024));
    }
}

