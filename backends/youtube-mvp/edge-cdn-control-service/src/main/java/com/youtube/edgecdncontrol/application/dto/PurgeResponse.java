package com.youtube.edgecdncontrol.application.dto;

import com.youtube.edgecdncontrol.domain.entities.PurgeRequest;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PurgeResponse {
    private UUID id;
    private String resourceGroup;
    private String frontDoorProfileName;
    private List<String> contentPaths;
    private PurgeRequest.PurgeType purgeType;
    private String requestedBy;
    private Instant requestedAt;
    private PurgeRequest.PurgeStatus status;
    private String errorMessage;
}

