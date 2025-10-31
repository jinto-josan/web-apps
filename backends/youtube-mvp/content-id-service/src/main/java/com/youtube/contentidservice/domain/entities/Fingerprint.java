package com.youtube.contentidservice.domain.entities;

import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import com.youtube.contentidservice.domain.valueobjects.FingerprintData;
import com.youtube.contentidservice.domain.valueobjects.VideoId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class Fingerprint {
    private FingerprintId id;
    private VideoId videoId;
    private FingerprintData data;
    private Instant createdAt;
    private Instant processedAt;
    private String status; // PENDING, PROCESSED, FAILED

    public static Fingerprint create(VideoId videoId, FingerprintData data) {
        return Fingerprint.builder()
                .videoId(videoId)
                .data(data)
                .createdAt(Instant.now())
                .status("PENDING")
                .build();
    }

    public void markProcessed() {
        this.processedAt = Instant.now();
        this.status = "PROCESSED";
    }

    public void markFailed() {
        this.status = "FAILED";
    }
}

