package com.youtube.drmservice.application.usecases;

import com.youtube.drmservice.application.commands.CreateDrmPolicyCommand;
import com.youtube.drmservice.domain.models.DrmPolicy;
import com.youtube.drmservice.domain.models.KeyRotationPolicy;
import com.youtube.drmservice.domain.models.PolicyConfiguration;
import com.youtube.drmservice.domain.repositories.DrmPolicyRepository;
import com.youtube.drmservice.domain.services.AmsAdapter;
import com.youtube.drmservice.domain.services.CacheService;
import com.youtube.drmservice.domain.services.EventPublisher;
import com.youtube.drmservice.domain.services.IdempotencyRepository;
import com.youtube.drmservice.shared.exceptions.ConflictException;
import com.youtube.drmservice.shared.exceptions.NotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrmPolicyUseCaseImplTest {

    @Mock
    private DrmPolicyRepository policyRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private AmsAdapter amsAdapter;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @InjectMocks
    private DrmPolicyUseCaseImpl useCase;

    private CreateDrmPolicyCommand createCommand;
    private DrmPolicy testPolicy;

    @BeforeEach
    void setUp() {
        PolicyConfiguration config = PolicyConfiguration.builder()
                .contentKeyPolicyName("test-policy")
                .licenseConfiguration(Map.of())
                .build();

        KeyRotationPolicy rotationPolicy = KeyRotationPolicy.builder()
                .enabled(true)
                .rotationInterval(Duration.ofDays(30))
                .rotationKeyVaultUri("https://vault.vault.azure.net/keys/test")
                .build();

        createCommand = CreateDrmPolicyCommand.builder()
                .videoId("video-123")
                .provider(DrmPolicy.DrmProvider.WIDEVINE)
                .configuration(config)
                .rotationPolicy(rotationPolicy)
                .createdBy("user-456")
                .build();

        testPolicy = DrmPolicy.builder()
                .id("policy-789")
                .videoId("video-123")
                .provider(DrmPolicy.DrmProvider.WIDEVINE)
                .configuration(config)
                .rotationPolicy(rotationPolicy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("user-456")
                .updatedBy("user-456")
                .version(1L)
                .build();
    }

    @Test
    void createPolicy_shouldCreatePolicy_whenValidCommand() {
        when(policyRepository.existsByVideoId("video-123")).thenReturn(false);
        when(amsAdapter.createOrUpdateContentKeyPolicy(any(), any())).thenReturn("ams-policy-123");
        when(policyRepository.save(any())).thenReturn(testPolicy);

        DrmPolicy result = useCase.createPolicy(createCommand);

        assertThat(result).isNotNull();
        assertThat(result.getVideoId()).isEqualTo("video-123");
        verify(policyRepository).save(any());
        verify(eventPublisher).publishPolicyCreated(any());
    }

    @Test
    void createPolicy_shouldThrowConflict_whenPolicyAlreadyExists() {
        when(policyRepository.existsByVideoId("video-123")).thenReturn(true);

        assertThatThrownBy(() -> useCase.createPolicy(createCommand))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createPolicy_shouldCheckIdempotency_whenKeyProvided() {
        createCommand = createCommand.toBuilder()
                .idempotencyKey("idempotency-key-123")
                .build();

        when(policyRepository.existsByVideoId("video-123")).thenReturn(false);
        when(idempotencyRepository.isIdempotent("idempotency-key-123")).thenReturn(true);

        assertThatThrownBy(() -> useCase.createPolicy(createCommand))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Idempotency key already used");
    }

    @Test
    void getPolicy_shouldReturnFromCache_whenCached() {
        String policyId = "policy-123";
        when(cacheService.getPolicy(policyId)).thenReturn(Optional.of(testPolicy));

        var query = new com.youtube.drmservice.application.queries.GetDrmPolicyQuery(policyId);
        DrmPolicy result = useCase.getPolicy(query);

        assertThat(result).isEqualTo(testPolicy);
        verify(policyRepository, never()).findById(anyString());
    }

    @Test
    void getPolicy_shouldThrowNotFound_whenNotExists() {
        String policyId = "nonexistent";
        when(cacheService.getPolicy(policyId)).thenReturn(Optional.empty());
        when(policyRepository.findById(policyId)).thenReturn(Optional.empty());

        var query = new com.youtube.drmservice.application.queries.GetDrmPolicyQuery(policyId);
        
        assertThatThrownBy(() -> useCase.getPolicy(query))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not found");
    }
}

