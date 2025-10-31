package com.youtube.antiaabuseservice.domain.model;

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
public class FeatureStore {
    private String userId;
    private String featureSet;
    private Map<String, Object> features;
    private Instant lastUpdated;
    private Instant expiresAt;
}

