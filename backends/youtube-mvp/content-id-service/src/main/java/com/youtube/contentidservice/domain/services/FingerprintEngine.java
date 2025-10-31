package com.youtube.contentidservice.domain.services;

import com.youtube.contentidservice.domain.valueobjects.FingerprintData;
import com.youtube.contentidservice.domain.valueobjects.FingerprintId;

import java.io.InputStream;

/**
 * Domain service port for fingerprint generation
 */
public interface FingerprintEngine {
    /**
     * Generates fingerprint from video stream
     * @param videoStream Video input stream
     * @return Fingerprint data including hash, algorithm, duration, and blob URI
     */
    FingerprintData generateFingerprint(InputStream videoStream);

    /**
     * Generates fingerprint from blob URI
     * @param blobUri Blob storage URI
     * @return Fingerprint data
     */
    FingerprintData generateFingerprint(String blobUri);
}

