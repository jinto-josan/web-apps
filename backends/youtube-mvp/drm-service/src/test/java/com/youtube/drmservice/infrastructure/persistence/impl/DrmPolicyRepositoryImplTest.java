package com.youtube.drmservice.infrastructure.persistence.impl;

import com.youtube.drmservice.domain.models.DrmPolicy;
import com.youtube.drmservice.domain.models.KeyRotationPolicy;
import com.youtube.drmservice.domain.models.PolicyConfiguration;
import com.youtube.drmservice.infrastructure.persistence.entity.DrmPolicyEntity;
import com.youtube.drmservice.infrastructure.persistence.repository.DrmPolicyJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrmPolicyRepositoryImplTest {

    @Mock
    private DrmPolicyJpaRepository jpaRepository;

    @InjectMocks
    private DrmPolicyRepositoryImpl repository;

    private DrmPolicy testPolicy;
    private DrmPolicyEntity testEntity;

    @BeforeEach
    void setUp() {
        PolicyConfiguration config = PolicyConfiguration.builder()
                .contentKeyPolicyName("test-policy")
                .licenseConfiguration(Map.of("enablePersistentLicense", "true"))
                .allowedApplications(List.of("com.example.app"))
                .persistentLicenseAllowed(true)
                .build();

        KeyRotationPolicy rotationPolicy = KeyRotationPolicy.builder()
                .enabled(true)
                .rotationInterval(Duration.ofDays(30))
                .rotationKeyVaultUri("https://vault.vault.azure.net/keys/test")
                .build();

        testPolicy = DrmPolicy.builder()
                .id("policy-123")
                .videoId("video-456")
                .provider(DrmPolicy.DrmProvider.WIDEVINE)
                .configuration(config)
                .rotationPolicy(rotationPolicy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("user-789")
                .updatedBy("user-789")
                .version(1L)
                .build();

        testEntity = new DrmPolicyEntity();
        testEntity.setId("policy-123");
        testEntity.setVideoId("video-456");
        testEntity.setProvider(DrmPolicyEntity.DrmProvider.WIDEVINE);
        testEntity.setCreatedAt(Instant.now());
        testEntity.setUpdatedAt(Instant.now());
        testEntity.setCreatedBy("user-789");
        testEntity.setUpdatedBy("user-789");
        testEntity.setVersion(1L);
    }

    @Test
    void findById_shouldReturnPolicy_whenExists() {
        when(jpaRepository.findById("policy-123")).thenReturn(Optional.of(testEntity));

        Optional<DrmPolicy> result = repository.findById("policy-123");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("policy-123");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(jpaRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<DrmPolicy> result = repository.findById("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void findByVideoId_shouldReturnPolicy_whenExists() {
        when(jpaRepository.findByVideoId("video-456")).thenReturn(Optional.of(testEntity));

        Optional<DrmPolicy> result = repository.findByVideoId("video-456");

        assertThat(result).isPresent();
        assertThat(result.get().getVideoId()).isEqualTo("video-456");
    }

    @Test
    void existsByVideoId_shouldReturnTrue_whenExists() {
        when(jpaRepository.existsByVideoId("video-456")).thenReturn(true);

        boolean result = repository.existsByVideoId("video-456");

        assertThat(result).isTrue();
    }

    @Test
    void save_shouldReturnSavedPolicy() {
        when(jpaRepository.save(any(DrmPolicyEntity.class))).thenReturn(testEntity);

        DrmPolicy result = repository.save(testPolicy);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("policy-123");
    }
}

