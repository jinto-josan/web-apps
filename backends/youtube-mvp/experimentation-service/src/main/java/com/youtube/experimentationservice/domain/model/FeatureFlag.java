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
public class FeatureFlag {
    private String key;
    private boolean enabled;
    private Double rolloutPercentage; // 0.0 to 1.0
    private Map<String, String> conditions; // e.g., userId, region, etc.
    private Instant createdAt;
    private Instant updatedAt;
}

