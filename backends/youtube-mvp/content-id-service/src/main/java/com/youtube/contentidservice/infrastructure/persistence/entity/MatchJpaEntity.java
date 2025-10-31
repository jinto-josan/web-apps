package com.youtube.contentidservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "matches", indexes = {
    @Index(name = "idx_source_video", columnList = "source_video_id"),
    @Index(name = "idx_matched_video", columnList = "matched_video_id"),
    @Index(name = "idx_fingerprints", columnList = "source_fingerprint_id,matched_fingerprint_id", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchJpaEntity {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "source_fingerprint_id", nullable = false, columnDefinition = "UUID")
    private UUID sourceFingerprintId;

    @Column(name = "matched_fingerprint_id", nullable = false, columnDefinition = "UUID")
    private UUID matchedFingerprintId;

    @Column(name = "source_video_id", nullable = false, columnDefinition = "UUID")
    private UUID sourceVideoId;

    @Column(name = "matched_video_id", nullable = false, columnDefinition = "UUID")
    private UUID matchedVideoId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "processed", nullable = false)
    private Boolean processed;
}

