package com.youtube.edgecdncontrol.domain.entities;

import com.youtube.edgecdncontrol.domain.valueobjects.CdnRuleId;
import com.youtube.edgecdncontrol.domain.valueobjects.FrontDoorProfileId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class PurgeRequest {
    UUID id;
    FrontDoorProfileId frontDoorProfile;
    List<String> contentPaths;
    PurgeType purgeType;
    String requestedBy;
    Instant requestedAt;
    PurgeStatus status;
    String errorMessage;

    public enum PurgeType {
        SINGLE_PATH,
        WILDCARD,
        ALL
    }

    public enum PurgeStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}

