package com.youtube.drmservice.application.usecases;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.drmservice.application.commands.CreateDrmPolicyCommand;
import com.youtube.drmservice.application.commands.UpdateDrmPolicyCommand;
import com.youtube.drmservice.application.commands.RotateKeysCommand;
import com.youtube.drmservice.application.queries.GetDrmPolicyQuery;
import com.youtube.drmservice.application.queries.GetDrmPolicyByVideoIdQuery;
import com.youtube.drmservice.domain.events.DrmPolicyCreated;
import com.youtube.drmservice.domain.events.DrmPolicyUpdated;
import com.youtube.drmservice.domain.events.KeyRotationTriggered;
import com.youtube.drmservice.domain.models.AuditLog;
import com.youtube.drmservice.domain.models.DrmPolicy;
import com.youtube.drmservice.domain.repositories.AuditLogRepository;
import com.youtube.drmservice.domain.repositories.DrmPolicyRepository;
import com.youtube.drmservice.domain.services.AmsAdapter;
import com.youtube.drmservice.domain.services.CacheService;
import com.youtube.drmservice.domain.services.EventPublisher;
import com.youtube.drmservice.domain.services.IdempotencyRepository;
import com.youtube.drmservice.shared.exceptions.ConflictException;
import com.youtube.drmservice.shared.exceptions.NotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrmPolicyUseCaseImpl implements DrmPolicyUseCase {

    private final DrmPolicyRepository policyRepository;
    private final AuditLogRepository auditLogRepository;
    private final CacheService cacheService;
    private final EventPublisher eventPublisher;
    private final AmsAdapter amsAdapter;
    private final IdempotencyRepository idempotencyRepository;

    @Override
    @Transactional
    public DrmPolicy createPolicy(CreateDrmPolicyCommand command) {
        log.info("Creating DRM policy for video: {}", command.getVideoId());

        // Check idempotency
        if (command.getIdempotencyKey() != null && !command.getIdempotencyKey().isEmpty()) {
            if (idempotencyRepository.isIdempotent(command.getIdempotencyKey())) {
                throw new ConflictException("Idempotency key already used");
            }
            idempotencyRepository.markIdempotent(command.getIdempotencyKey(), 86400);
        }

        // Check if policy already exists for this video
        if (policyRepository.existsByVideoId(command.getVideoId())) {
            throw new ConflictException("DRM policy already exists for video: " + command.getVideoId());
        }

        // Create policy
        String policyId = UlidCreator.getUlid().toString();
        Instant now = Instant.now();

        DrmPolicy policy = DrmPolicy.builder()
                .id(policyId)
                .videoId(command.getVideoId())
                .provider(command.getProvider())
                .configuration(command.getConfiguration())
                .rotationPolicy(command.getRotationPolicy())
                .createdAt(now)
                .updatedAt(now)
                .createdBy(command.getCreatedBy())
                .updatedBy(command.getCreatedBy())
                .version(1L)
                .build();

        // Create content key policy in AMS
        String amsPolicyId = amsAdapter.createOrUpdateContentKeyPolicy(
                command.getProvider(), command.getConfiguration());
        log.info("Created AMS content key policy: {}", amsPolicyId);

        // Persist policy
        DrmPolicy savedPolicy = policyRepository.save(policy);
        cacheService.putPolicy(savedPolicy);

        // Create audit log
        AuditLog auditLog = AuditLog.builder()
                .id(UlidCreator.getUlid().toString())
                .policyId(policyId)
                .action("CREATE")
                .changedBy(command.getCreatedBy())
                .changedAt(now)
                .oldValues(Map.of())
                .newValues(Map.of(
                        "videoId", command.getVideoId(),
                        "provider", command.getProvider().name()
                ))
                .correlationId(UlidCreator.getUlid().toString())
                .build();
        auditLogRepository.save(auditLog);

        // Publish event
        eventPublisher.publishPolicyCreated(DrmPolicyCreated.builder()
                .policyId(policyId)
                .videoId(command.getVideoId())
                .provider(command.getProvider().name())
                .createdBy(command.getCreatedBy())
                .createdAt(now)
                .build());

        log.info("Created DRM policy: {}", policyId);
        return savedPolicy;
    }

    @Override
    @Transactional
    public DrmPolicy updatePolicy(UpdateDrmPolicyCommand command) {
        log.info("Updating DRM policy: {}", command.getPolicyId());

        DrmPolicy existingPolicy = policyRepository.findById(command.getPolicyId())
                .orElseThrow(() -> new NotFoundException("DRM policy not found: " + command.getPolicyId()));

        // Optimistic locking check
        if (command.getEtag() != null && !command.getEtag().equals(existingPolicy.getVersion().toString())) {
            throw new ConflictException("Policy was modified by another process");
        }

        // Check idempotency
        if (command.getIdempotencyKey() != null && !command.getIdempotencyKey().isEmpty()) {
            if (idempotencyRepository.isIdempotent(command.getIdempotencyKey())) {
                throw new ConflictException("Idempotency key already used");
            }
            idempotencyRepository.markIdempotent(command.getIdempotencyKey(), 86400);
        }

        // Track changes for audit
        Map<String, String> oldValues = new HashMap<>();
        Map<String, String> newValues = new HashMap<>();

        if (!existingPolicy.getConfiguration().equals(command.getConfiguration())) {
            oldValues.put("configuration", existingPolicy.getConfiguration().toString());
            newValues.put("configuration", command.getConfiguration().toString());
        }

        // Update AMS
        String amsPolicyId = amsAdapter.createOrUpdateContentKeyPolicy(
                existingPolicy.getProvider(), command.getConfiguration());

        // Update policy
        DrmPolicy updatedPolicy = existingPolicy.toBuilder()
                .configuration(command.getConfiguration())
                .updatedBy(command.getUpdatedBy())
                .updatedAt(Instant.now())
                .version(existingPolicy.getVersion() + 1)
                .build();

        DrmPolicy savedPolicy = policyRepository.save(updatedPolicy);
        cacheService.evictPolicy(savedPolicy.getId());
        cacheService.putPolicy(savedPolicy);

        // Create audit log
        if (!newValues.isEmpty()) {
            AuditLog auditLog = AuditLog.builder()
                    .id(UlidCreator.getUlid().toString())
                    .policyId(command.getPolicyId())
                    .action("UPDATE")
                    .changedBy(command.getUpdatedBy())
                    .changedAt(Instant.now())
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .correlationId(UlidCreator.getUlid().toString())
                    .build();
            auditLogRepository.save(auditLog);
        }

        // Publish event
        eventPublisher.publishPolicyUpdated(DrmPolicyUpdated.builder()
                .policyId(command.getPolicyId())
                .videoId(existingPolicy.getVideoId())
                .updatedBy(command.getUpdatedBy())
                .updatedAt(Instant.now())
                .build());

        log.info("Updated DRM policy: {}", command.getPolicyId());
        return savedPolicy;
    }

    @Override
    @Transactional
    public void rotateKeys(RotateKeysCommand command) {
        log.info("Rotating keys for {} policies", command.getPolicyIds().size());

        for (String policyId : command.getPolicyIds()) {
            DrmPolicy policy = policyRepository.findById(policyId)
                    .orElseThrow(() -> new NotFoundException("DRM policy not found: " + policyId));

            if (policy.getRotationPolicy() != null && policy.getRotationPolicy().isEnabled()) {
                String keyVaultUri = policy.getRotationPolicy().getRotationKeyVaultUri();
                String newKeyId = amsAdapter.rotateContentKey(policyId, keyVaultUri);
                log.info("Rotated key for policy {}: {}", policyId, newKeyId);

                // Update policy with new rotation timestamp
                DrmPolicy updatedPolicy = policy.toBuilder()
                        .rotationPolicy(policy.getRotationPolicy().toBuilder()
                                .lastRotationAt(Instant.now())
                                .nextRotationAt(Instant.now().plus(policy.getRotationPolicy().getRotationInterval()))
                                .build())
                        .updatedBy(command.getRotatedBy())
                        .updatedAt(Instant.now())
                        .version(policy.getVersion() + 1)
                        .build();

                policyRepository.save(updatedPolicy);
                cacheService.evictPolicy(policyId);

                // Audit log
                AuditLog auditLog = AuditLog.builder()
                        .id(UlidCreator.getUlid().toString())
                        .policyId(policyId)
                        .action("ROTATE")
                        .changedBy(command.getRotatedBy())
                        .changedAt(Instant.now())
                        .oldValues(Map.of("lastRotationAt", 
                                policy.getRotationPolicy().getLastRotationAt() != null 
                                        ? policy.getRotationPolicy().getLastRotationAt().toString() 
                                        : ""))
                        .newValues(Map.of("lastRotationAt", Instant.now().toString()))
                        .correlationId(UlidCreator.getUlid().toString())
                        .build();
                auditLogRepository.save(auditLog);
            }
        }

        // Publish event
        eventPublisher.publishKeyRotationTriggered(KeyRotationTriggered.builder()
                .policyIds(command.getPolicyIds())
                .rotatedAt(Instant.now())
                .rotatedBy(command.getRotatedBy())
                .build());
    }

    @Override
    public DrmPolicy getPolicy(GetDrmPolicyQuery query) {
        log.debug("Getting DRM policy: {}", query.getPolicyId());

        // Try cache first
        DrmPolicy cached = cacheService.getPolicy(query.getPolicyId())
                .orElse(null);
        
        if (cached != null) {
            return cached;
        }

        // Fallback to repository
        DrmPolicy policy = policyRepository.findById(query.getPolicyId())
                .orElseThrow(() -> new NotFoundException("DRM policy not found: " + query.getPolicyId()));
        
        cacheService.putPolicy(policy);
        return policy;
    }

    @Override
    public DrmPolicy getPolicyByVideoId(GetDrmPolicyByVideoIdQuery query) {
        log.debug("Getting DRM policy for video: {}", query.getVideoId());

        DrmPolicy policy = policyRepository.findByVideoId(query.getVideoId())
                .orElseThrow(() -> new NotFoundException("DRM policy not found for video: " + query.getVideoId()));
        
        cacheService.putPolicy(policy);
        return policy;
    }
}

