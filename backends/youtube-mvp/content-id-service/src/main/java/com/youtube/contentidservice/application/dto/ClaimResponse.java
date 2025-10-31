package com.youtube.contentidservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClaimResponse {
    UUID id;
    UUID claimedVideoId;
    UUID ownerId;
    List<UUID> matchIds;
    String status;
    String disputeStatus;
    Instant createdAt;
    Instant resolvedAt;
    String resolution;
}

