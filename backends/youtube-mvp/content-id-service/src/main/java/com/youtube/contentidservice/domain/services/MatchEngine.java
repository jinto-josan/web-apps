package com.youtube.contentidservice.domain.services;

import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import com.youtube.contentidservice.domain.valueobjects.MatchScore;

import java.util.List;

/**
 * Domain service port for fingerprint matching
 */
public interface MatchEngine {
    /**
     * Finds matches for a given fingerprint
     * @param fingerprintId Source fingerprint to match against
     * @param threshold Minimum match score threshold
     * @return List of matched fingerprint IDs with scores
     */
    List<MatchResult> findMatches(FingerprintId fingerprintId, double threshold);

    /**
     * Match result
     */
    record MatchResult(FingerprintId matchedFingerprintId, MatchScore score) {}
}

