package com.youtube.antiaabuseservice.infrastructure.adapters.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Container(containerName = "feature-store")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureStoreCosmosEntity {
    @org.springframework.data.annotation.Id
    private String id; // userId:featureSet
    @PartitionKey
    private String userId;
    private String featureSet;
    private Map<String, Object> features;
    private Instant lastUpdated;
    private Instant expiresAt;
}

