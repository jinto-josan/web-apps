package com.youtube.experimentationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Experiment {
    private String id;
    private String key;
    private String name;
    private ExperimentStatus status;
    private List<Variant> variants;
    private Double rolloutPercentage; // 0.0 to 1.0
    private Map<String, String> conditions; // targeting conditions
    private AssignmentStrategy assignmentStrategy; // DETERMINISTIC, STICKY, RANDOM
    private Instant startDate;
    private Instant endDate;
    private Instant createdAt;
    private Instant updatedAt;

    public enum ExperimentStatus {
        DRAFT, ACTIVE, PAUSED, COMPLETED
    }

    public enum AssignmentStrategy {
        DETERMINISTIC, STICKY, RANDOM
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Variant {
        private String id;
        private String name;
        private Double trafficPercentage; // 0.0 to 1.0
        private Map<String, Object> configuration;
    }
}

