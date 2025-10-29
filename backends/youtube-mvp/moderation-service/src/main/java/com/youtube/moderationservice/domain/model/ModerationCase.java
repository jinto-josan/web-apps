package com.youtube.moderationservice.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class ModerationCase {
    UUID id;
    String contentId;
    String reporterUserId;
    CaseStatus status;
    List<Strike> strikes;
    Instant createdAt;
    Instant updatedAt;

    public enum CaseStatus { OPEN, UNDER_REVIEW, ESCALATED, CLOSED }
}


