package com.youtube.configsecretsservice.infrastructure.persistence.adapter;

import com.youtube.configsecretsservice.domain.entity.ConfigurationEntry;
import com.youtube.configsecretsservice.domain.port.ConfigurationRepository;
import com.youtube.configsecretsservice.infrastructure.persistence.entity.ConfigurationEntryEntity;
import com.youtube.configsecretsservice.infrastructure.persistence.repository.JpaConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing ConfigurationRepository port using JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigurationRepositoryAdapter implements ConfigurationRepository {
    
    private final JpaConfigurationRepository jpaRepository;
    
    @Override
    public Optional<ConfigurationEntry> findByScopeAndKey(String scope, String key) {
        return jpaRepository.findByScopeAndKey(scope, key)
                .map(this::toDomain);
    }
    
    @Override
    public ConfigurationEntry save(ConfigurationEntry entry) {
        ConfigurationEntryEntity entity = toEntity(entry);
        ConfigurationEntryEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }
    
    @Override
    public void delete(String scope, String key) {
        jpaRepository.findByScopeAndKey(scope, key)
                .ifPresent(jpaRepository::delete);
    }
    
    @Override
    public List<ConfigurationEntry> findAllByScope(String scope) {
        return jpaRepository.findAllByScope(scope).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ConfigurationEntry> findAllByScopeAndLabel(String scope, String label) {
        return jpaRepository.findByScopeAndLabel(scope, label).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    private ConfigurationEntryEntity toEntity(ConfigurationEntry domain) {
        return ConfigurationEntryEntity.builder()
                .scope(domain.getScope())
                .key(domain.getKey())
                .value(domain.getValue())
                .contentType(domain.getContentType())
                .label(domain.getLabel())
                .etag(domain.getEtag())
                .isSecret(domain.getIsSecret())
                .createdAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now())
                .updatedAt(domain.getUpdatedAt() != null ? domain.getUpdatedAt() : Instant.now())
                .createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .build();
    }
    
    private ConfigurationEntry toDomain(ConfigurationEntryEntity entity) {
        return ConfigurationEntry.builder()
                .scope(entity.getScope())
                .key(entity.getKey())
                .value(entity.getValue())
                .contentType(entity.getContentType())
                .label(entity.getLabel())
                .etag(entity.getEtag())
                .isSecret(entity.getIsSecret())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}

