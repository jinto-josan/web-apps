package com.youtube.contentidservice.domain.valueobjects;

import lombok.Value;

@Value
public class FingerprintData {
    byte[] hash;
    String algorithm;
    int durationSeconds;
    String blobUri;

    public static FingerprintData of(byte[] hash, String algorithm, int durationSeconds, String blobUri) {
        if (hash == null || hash.length == 0) {
            throw new IllegalArgumentException("Fingerprint hash cannot be null or empty");
        }
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("Algorithm cannot be null or blank");
        }
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (blobUri == null || blobUri.isBlank()) {
            throw new IllegalArgumentException("Blob URI cannot be null or blank");
        }
        return new FingerprintData(hash, algorithm, durationSeconds, blobUri);
    }
}

