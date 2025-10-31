package com.youtube.contentidservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchResponse {
    UUID id;
    UUID sourceFingerprintId;
    UUID matchedFingerprintId;
    UUID sourceVideoId;
    UUID matchedVideoId;
    double score;
    Instant detectedAt;
    boolean processed;
}

