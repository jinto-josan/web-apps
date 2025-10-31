package com.youtube.configsecretsservice.application.service;

import com.youtube.configsecretsservice.application.dto.ConfigRequest;
import com.youtube.configsecretsservice.application.dto.ConfigResponse;
import com.youtube.configsecretsservice.application.mapper.ConfigMapper;
import com.youtube.configsecretsservice.domain.entity.AuditLog;
import com.youtube.configsecretsservice.domain.entity.ConfigurationEntry;
import com.youtube.configsecretsservice.domain.port.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Application service for configuration operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationApplicationService {
    
    private final ConfigurationRepository configurationRepository;
    private final AppConfigurationPort appConfigurationPort;
    private final KeyVaultPort keyVaultPort;
    private final CachePort cachePort;
    private final RbacCheckPort rbacCheckPort;
    private final AuditLoggerPort auditLoggerPort;
    private final ConfigMapper configMapper;
    
    private static final long CACHE_TTL_SECONDS = 300; // 5 minutes
    
    @Transactional(readOnly = true)
    public ConfigResponse getConfiguration(String scope, String key, String userId, String tenantId, String ifNoneMatch) {
        // RBAC check
        if (!rbacCheckPort.canRead(userId, tenantId, scope)) {
            log.warn("Access denied for user {} to read config {}/{}", userId, scope, key);
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
        
        // Try cache first
        String cacheKey = buildCacheKey(scope, key);
        Optional<String> cached = cachePort.get(cacheKey);
        if (cached.isPresent()) {
            log.debug("Cache hit for {}/{}", scope, key);
            Optional<ConfigurationEntry> entry = configurationRepository.findByScopeAndKey(scope, key);
            if (entry.isPresent() && ifNoneMatch != null && entry.get().getEtag().equals(ifNoneMatch)) {
                throw new ResponseStatusException(HttpStatus.NOT_MODIFIED);
            }
            return configMapper.toResponse(entry.get());
        }
        
        // Read from App Configuration
        Optional<ConfigurationEntry> entry = appConfigurationPort.getConfiguration(scope, key, null);
        if (entry.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuration not found");
        }
        
        // Store in local repository and cache
        configurationRepository.save(entry.get());
        cachePort.put(cacheKey, "1", CACHE_TTL_SECONDS);
        
        // Audit log
        auditLoggerPort.log(AuditLog.builder()
                .scope(scope)
                .key(key)
                .action(AuditLog.AuditAction.CONFIG_READ)
                .userId(userId)
                .tenantId(tenantId)
                .timestamp(Instant.now())
                .build());
        
        // ETag check
        if (ifNoneMatch != null && entry.get().getEtag().equals(ifNoneMatch)) {
            throw new ResponseStatusException(HttpStatus.NOT_MODIFIED);
        }
        
        return configMapper.toResponse(entry.get());
    }
    
    @Transactional
    public ConfigResponse updateConfiguration(String scope, String key, ConfigRequest request, String etag, String userId, String tenantId) {
        // RBAC check
        if (!rbacCheckPort.canWrite(userId, tenantId, scope)) {
            log.warn("Access denied for user {} to write config {}/{}", userId, scope, key);
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
        
        // Get existing entry for ETag check
        Optional<ConfigurationEntry> existing = configurationRepository.findByScopeAndKey(scope, key);
        if (etag != null && existing.isPresent() && !existing.get().getEtag().equals(etag)) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "ETag mismatch");
        }
        
        // Update in App Configuration or Key Vault based on isSecret
        ConfigurationEntry updated;
        if (Boolean.TRUE.equals(request.getIsSecret())) {
            keyVaultPort.setSecret(scope, key, request.getValue());
            // For secrets, create a minimal entry for tracking
            updated = ConfigurationEntry.builder()
                    .scope(scope)
                    .key(key)
                    .value("***") // Don't store actual secret value
                    .contentType(request.getContentType())
                    .label(request.getLabel())
                    .etag(generateEtag())
                    .isSecret(true)
                    .createdAt(existing.map(ConfigurationEntry::getCreatedAt).orElse(Instant.now()))
                    .updatedAt(Instant.now())
                    .createdBy(existing.map(ConfigurationEntry::getCreatedBy).orElse(userId))
                    .updatedBy(userId)
                    .build();
        } else {
            updated = appConfigurationPort.setConfiguration(
                    scope, key, request.getValue(), 
                    request.getContentType() != null ? request.getContentType() : "text/plain",
                    request.getLabel(), etag);
            updated = updated.toBuilder()
                    .updatedAt(Instant.now())
                    .updatedBy(userId)
                    .build();
        }
        
        // Save to local repository
        ConfigurationEntry saved = configurationRepository.save(updated);
        
        // Invalidate cache
        cachePort.evict(buildCacheKey(scope, key));
        
        // Audit log
        auditLoggerPort.log(AuditLog.builder()
                .scope(scope)
                .key(key)
                .action(AuditLog.AuditAction.CONFIG_UPDATE)
                .userId(userId)
                .tenantId(tenantId)
                .timestamp(Instant.now())
                .build());
        
        return configMapper.toResponse(saved);
    }
    
    private String buildCacheKey(String scope, String key) {
        return String.format("config:%s:%s", scope, key);
    }
    
    private String generateEtag() {
        return String.valueOf(Instant.now().toEpochMilli());
    }
}

