package com.youtube.experimentationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCohort {
    private String userId;
    private String experimentKey;
    private String variantId;
    private Instant assignedAt;
    private Map<String, String> metadata;
}

