package com.youtube.experimentationservice.application.service;

import com.youtube.experimentationservice.application.dto.FeatureFlagResponse;
import com.youtube.experimentationservice.application.mappers.ExperimentationMapper;
import com.youtube.experimentationservice.application.mappers.ExperimentationMapperImpl;
import com.youtube.experimentationservice.domain.model.FeatureFlag;
import com.youtube.experimentationservice.domain.repositories.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    private FeatureFlagRepository flagRepository;

    @Mock
    private ExperimentationMapper mapper;

    @InjectMocks
    private FeatureFlagService featureFlagService;

    private FeatureFlag enabledFlag;
    private FeatureFlag disabledFlag;

    @BeforeEach
    void setUp() {
        enabledFlag = FeatureFlag.builder()
                .key("test-flag")
                .enabled(true)
                .rolloutPercentage(1.0)
                .conditions(new HashMap<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        disabledFlag = FeatureFlag.builder()
                .key("disabled-flag")
                .enabled(false)
                .rolloutPercentage(1.0)
                .conditions(new HashMap<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void testGetFlag_Enabled() {
        when(flagRepository.findByKey("test-flag")).thenReturn(Optional.of(enabledFlag));
        when(mapper.toResponse(any(FeatureFlag.class))).thenAnswer(invocation -> {
            FeatureFlag flag = invocation.getArgument(0);
            return FeatureFlagResponse.builder()
                    .key(flag.getKey())
                    .enabled(flag.isEnabled())
                    .build();
        });

        FeatureFlagResponse result = featureFlagService.getFlag("test-flag", "user1", Map.of());

        assertNotNull(result);
        assertEquals("test-flag", result.getKey());
        assertTrue(result.isEnabled());
    }

    @Test
    void testGetFlag_NotFound() {
        when(flagRepository.findByKey("unknown-flag")).thenReturn(Optional.empty());

        FeatureFlagResponse result = featureFlagService.getFlag("unknown-flag", "user1", Map.of());

        assertNotNull(result);
        assertEquals("unknown-flag", result.getKey());
        assertFalse(result.isEnabled());
    }

    @Test
    void testGetFlag_Disabled() {
        when(flagRepository.findByKey("disabled-flag")).thenReturn(Optional.of(disabledFlag));
        when(mapper.toResponse(any(FeatureFlag.class))).thenAnswer(invocation -> {
            FeatureFlag flag = invocation.getArgument(0);
            return FeatureFlagResponse.builder()
                    .key(flag.getKey())
                    .enabled(flag.isEnabled())
                    .build();
        });

        FeatureFlagResponse result = featureFlagService.getFlag("disabled-flag", "user1", Map.of());

        assertNotNull(result);
        assertFalse(result.isEnabled());
    }

    @Test
    void testGetAllFlags() {
        when(flagRepository.findAll()).thenReturn(Arrays.asList(enabledFlag, disabledFlag));
        when(mapper.toResponse(any(FeatureFlag.class))).thenAnswer(invocation -> {
            FeatureFlag flag = invocation.getArgument(0);
            return FeatureFlagResponse.builder()
                    .key(flag.getKey())
                    .enabled(flag.isEnabled())
                    .build();
        });

        List<FeatureFlagResponse> results = featureFlagService.getAllFlags("user1", Map.of());

        assertNotNull(results);
        assertEquals(2, results.size());
    }
}

