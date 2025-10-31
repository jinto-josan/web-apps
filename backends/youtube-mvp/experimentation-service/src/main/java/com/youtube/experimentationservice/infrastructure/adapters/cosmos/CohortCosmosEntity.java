package com.youtube.experimentationservice.infrastructure.adapters.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Container(containerName = "cohorts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CohortCosmosEntity {
    @org.springframework.data.annotation.Id
    private String id; // userId:experimentKey
    @PartitionKey
    private String userId;
    private String experimentKey;
    private String variantId;
    private Instant assignedAt;
    private Map<String, String> metadata;
}

