package com.youtube.contentidservice.domain.entities;

import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import com.youtube.contentidservice.domain.valueobjects.MatchScore;
import com.youtube.contentidservice.domain.valueobjects.VideoId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Match {
    private UUID id;
    private FingerprintId sourceFingerprintId;
    private FingerprintId matchedFingerprintId;
    private VideoId sourceVideoId;
    private VideoId matchedVideoId;
    private MatchScore score;
    private Instant detectedAt;
    private boolean processed;

    public static Match create(
            FingerprintId sourceFingerprintId,
            FingerprintId matchedFingerprintId,
            VideoId sourceVideoId,
            VideoId matchedVideoId,
            MatchScore score) {
        return Match.builder()
                .id(UUID.randomUUID())
                .sourceFingerprintId(sourceFingerprintId)
                .matchedFingerprintId(matchedFingerprintId)
                .sourceVideoId(sourceVideoId)
                .matchedVideoId(matchedVideoId)
                .score(score)
                .detectedAt(Instant.now())
                .processed(false)
                .build();
    }

    public void markProcessed() {
        this.processed = true;
    }
}
