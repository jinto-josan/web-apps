package com.youtube.contentidservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FingerprintResponse {
    UUID id;
    UUID videoId;
    String algorithm;
    int durationSeconds;
    String blobUri;
    String status;
    Instant createdAt;
    Instant processedAt;
}

