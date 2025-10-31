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
@Table(name = "fingerprints")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FingerprintJpaEntity {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "video_id", nullable = false, columnDefinition = "UUID")
    private UUID videoId;

    @Column(name = "blob_uri", nullable = false)
    private String blobUri;

    @Column(name = "algorithm", nullable = false)
    private String algorithm;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;
}

