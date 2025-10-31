package com.youtube.experimentationservice.infrastructure.adapters.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Container(containerName = "experiments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentCosmosEntity {
    @org.springframework.data.annotation.Id
    private String id;
    @PartitionKey
    private String key;
    private String name;
    private String status;
    private List<VariantEntity> variants;
    private Double rolloutPercentage;
    private Map<String, String> conditions;
    private String assignmentStrategy;
    private Instant startDate;
    private Instant endDate;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantEntity {
        private String id;
        private String name;
        private Double trafficPercentage;
        private Map<String, Object> configuration;
    }
}

