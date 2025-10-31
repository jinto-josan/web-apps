package com.youtube.configsecretsservice.application.service;

import com.youtube.configsecretsservice.application.dto.SecretRotationRequest;
import com.youtube.configsecretsservice.application.dto.SecretRotationResponse;
import com.youtube.configsecretsservice.domain.entity.AuditLog;
import com.youtube.configsecretsservice.domain.entity.SecretRotation;
import com.youtube.configsecretsservice.domain.port.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

/**
 * Application service for secret rotation operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecretRotationApplicationService {
    
    private final KeyVaultPort keyVaultPort;
    private final RbacCheckPort rbacCheckPort;
    private final AuditLoggerPort auditLoggerPort;
    private final EventPublisherPort eventPublisherPort;
    
    @Transactional
    public SecretRotationResponse rotateSecret(String scope, String key, SecretRotationRequest request, String userId, String tenantId) {
        // RBAC check
        if (!rbacCheckPort.canRotateSecret(userId, tenantId, scope)) {
            log.warn("Access denied for user {} to rotate secret {}/{}", userId, scope, key);
            auditLoggerPort.log(AuditLog.builder()
                    .scope(scope)
                    .key(key)
                    .action(AuditLog.AuditAction.ACCESS_DENIED)
                    .userId(userId)
                    .tenantId(tenantId)
                    .timestamp(Instant.now())
                    .build());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        boolean dryRun = Boolean.TRUE.equals(request.getDryRun());
        
        SecretRotation rotation = SecretRotation.builder()
                .id(UUID.randomUUID())
                .scope(scope)
                .key(key)
                .status(SecretRotation.RotationStatus.SCHEDULED)
                .scheduledAt(Instant.now())
                .triggeredBy(userId)
                .dryRun(dryRun)
                .build();
        
        try {
            if (!dryRun) {
                rotation = rotation.toBuilder()
                        .status(SecretRotation.RotationStatus.IN_PROGRESS)
                        .build();
                
                keyVaultPort.rotateSecret(scope, key);
                
                rotation = rotation.toBuilder()
                        .status(SecretRotation.RotationStatus.COMPLETED)
                        .completedAt(Instant.now())
                        .build();
                
                eventPublisherPort.publishSecretRotationCompleted(scope, key, true);
            } else {
                log.info("Dry run secret rotation for {}/{}", scope, key);
                rotation = rotation.toBuilder()
                        .status(SecretRotation.RotationStatus.COMPLETED)
                        .completedAt(Instant.now())
                        .build();
            }
            
            // Audit log
            auditLoggerPort.log(AuditLog.builder()
                    .scope(scope)
                    .key(key)
                    .action(AuditLog.AuditAction.SECRET_ROTATE)
                    .userId(userId)
                    .tenantId(tenantId)
                    .timestamp(Instant.now())
                    .details("dry-run=" + dryRun)
                    .build());
            
        } catch (Exception e) {
            log.error("Secret rotation failed for {}/{}", scope, key, e);
            rotation = rotation.toBuilder()
                    .status(SecretRotation.RotationStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .completedAt(Instant.now())
                    .build();
            
            eventPublisherPort.publishSecretRotationCompleted(scope, key, false);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Secret rotation failed: " + e.getMessage());
        }
        
        return SecretRotationResponse.builder()
                .id(rotation.getId())
                .scope(rotation.getScope())
                .key(rotation.getKey())
                .status(rotation.getStatus().name())
                .scheduledAt(rotation.getScheduledAt())
                .completedAt(rotation.getCompletedAt())
                .triggeredBy(rotation.getTriggeredBy())
                .dryRun(rotation.getDryRun())
                .build();
    }
}

