package com.youtube.experimentationservice.application.service;

import com.youtube.experimentationservice.application.dto.FeatureFlagResponse;
import com.youtube.experimentationservice.application.mappers.ExperimentationMapper;
import com.youtube.experimentationservice.domain.model.FeatureFlag;
import com.youtube.experimentationservice.domain.repositories.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {
    private final FeatureFlagRepository flagRepository;
    private final ExperimentationMapper mapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "featureFlags", key = "#key")
    public FeatureFlagResponse getFlag(String key, String userId, Map<String, String> context) {
        log.debug("Resolving feature flag: key={}, userId={}", key, userId);
        return flagRepository.findByKey(key)
                .map(flag -> evaluateFlag(flag, userId, context))
                .map(mapper::toResponse)
                .orElse(FeatureFlagResponse.builder()
                        .key(key)
                        .enabled(false)
                        .build());
    }

    @Transactional(readOnly = true)
    public List<FeatureFlagResponse> getAllFlags(String userId, Map<String, String> context) {
        return flagRepository.findAll().stream()
                .map(flag -> evaluateFlag(flag, userId, context))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    private FeatureFlag evaluateFlag(FeatureFlag flag, String userId, Map<String, String> context) {
        if (!flag.isEnabled()) {
            return FeatureFlag.builder()
                    .key(flag.getKey())
                    .enabled(false)
                    .build();
        }

        // Check conditions
        if (flag.getConditions() != null && !flag.getConditions().isEmpty()) {
            boolean matches = flag.getConditions().entrySet().stream()
                    .allMatch(entry -> context.getOrDefault(entry.getKey(), "").equals(entry.getValue()));
            if (!matches) {
                return FeatureFlag.builder()
                        .key(flag.getKey())
                        .enabled(false)
                        .build();
            }
        }

        // Rollout percentage check
        if (flag.getRolloutPercentage() != null && flag.getRolloutPercentage() < 1.0) {
            int bucket = computeBucket(userId, flag.getKey());
            double threshold = flag.getRolloutPercentage() * 10000;
            if (bucket >= threshold) {
                return FeatureFlag.builder()
                        .key(flag.getKey())
                        .enabled(false)
                        .build();
            }
        }

        return flag;
    }

    private int computeBucket(String userId, String flagKey) {
        String combined = userId + ":" + flagKey;
        int hash = combined.hashCode();
        return Math.abs(hash) % 10000;
    }
}

