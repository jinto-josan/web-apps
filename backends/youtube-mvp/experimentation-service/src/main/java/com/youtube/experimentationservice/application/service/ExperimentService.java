package com.youtube.experimentationservice.application.service;

import com.youtube.experimentationservice.application.dto.ExperimentResponse;
import com.youtube.experimentationservice.application.mappers.ExperimentationMapper;
import com.youtube.experimentationservice.domain.model.Experiment;
import com.youtube.experimentationservice.domain.model.UserCohort;
import com.youtube.experimentationservice.domain.repositories.CohortRepository;
import com.youtube.experimentationservice.domain.repositories.ExperimentRepository;
import com.youtube.experimentationservice.domain.services.BucketingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExperimentService {
    private final ExperimentRepository experimentRepository;
    private final CohortRepository cohortRepository;
    private final BucketingService bucketingService;
    private final ExperimentationMapper mapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "experiments", key = "#experimentKey + ':' + #userId")
    public ExperimentResponse getExperiment(String experimentKey, String userId, Map<String, String> context) {
        log.debug("Resolving experiment: key={}, userId={}", experimentKey, userId);
        
        Experiment experiment = experimentRepository.findByKey(experimentKey)
                .orElseThrow(() -> new IllegalArgumentException("Experiment not found: " + experimentKey));

        // Check if experiment is active
        if (experiment.getStatus() != Experiment.ExperimentStatus.ACTIVE) {
            throw new IllegalStateException("Experiment is not active: " + experimentKey);
        }

        // Check rollout percentage
        if (experiment.getRolloutPercentage() != null && experiment.getRolloutPercentage() < 1.0) {
            int bucket = bucketingService.computeBucket(userId, experimentKey);
            double threshold = experiment.getRolloutPercentage() * 10000;
            if (bucket >= threshold) {
                throw new IllegalArgumentException("User not enrolled in experiment");
            }
        }

        // Check conditions
        if (experiment.getConditions() != null && !experiment.getConditions().isEmpty()) {
            boolean matches = experiment.getConditions().entrySet().stream()
                    .allMatch(entry -> context.getOrDefault(entry.getKey(), "").equals(entry.getValue()));
            if (!matches) {
                throw new IllegalArgumentException("User does not match experiment conditions");
            }
        }

        // Check for existing sticky assignment
        if (experiment.getAssignmentStrategy() == Experiment.AssignmentStrategy.STICKY) {
            return cohortRepository.findByUserIdAndExperimentKey(userId, experimentKey)
                    .map(cohort -> {
                        Experiment.Variant variant = experiment.getVariants().stream()
                                .filter(v -> v.getId().equals(cohort.getVariantId()))
                                .findFirst()
                                .orElseThrow();
                        return mapper.toResponse(experiment, variant);
                    })
                    .orElseGet(() -> assignAndPersist(userId, experiment));
        }

        // Deterministic assignment
        Experiment.Variant variant = bucketingService.assignVariant(userId, experiment);
        
        // Persist for sticky assignments
        if (experiment.getAssignmentStrategy() == Experiment.AssignmentStrategy.DETERMINISTIC) {
            cohortRepository.save(UserCohort.builder()
                    .userId(userId)
                    .experimentKey(experimentKey)
                    .variantId(variant.getId())
                    .assignedAt(Instant.now())
                    .build());
        }

        return mapper.toResponse(experiment, variant);
    }

    private ExperimentResponse assignAndPersist(String userId, Experiment experiment) {
        Experiment.Variant variant = bucketingService.assignVariant(userId, experiment);
        cohortRepository.save(UserCohort.builder()
                .userId(userId)
                .experimentKey(experiment.getKey())
                .variantId(variant.getId())
                .assignedAt(Instant.now())
                .build());
        return mapper.toResponse(experiment, variant);
    }
}

