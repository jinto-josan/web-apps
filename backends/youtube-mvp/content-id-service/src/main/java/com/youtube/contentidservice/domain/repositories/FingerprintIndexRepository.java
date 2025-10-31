package com.youtube.contentidservice.domain.repositories;

import com.youtube.contentidservice.domain.valueobjects.FingerprintId;

import java.util.List;

/**
 * Repository for fingerprint index in Cosmos DB for fast similarity search
 */
public interface FingerprintIndexRepository {
    void index(FingerprintId fingerprintId, byte[] hashVector);
    List<FingerprintId> findSimilar(byte[] hashVector, double threshold);
    void remove(FingerprintId fingerprintId);
}

