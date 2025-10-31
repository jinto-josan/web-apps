package com.youtube.configsecretsservice.infrastructure.persistence.adapter;

import com.youtube.configsecretsservice.domain.entity.ConfigurationEntry;
import com.youtube.configsecretsservice.infrastructure.persistence.entity.ConfigurationEntryEntity;
import com.youtube.configsecretsservice.infrastructure.persistence.repository.JpaConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationRepositoryAdapterTest {
    
    @Mock
    private JpaConfigurationRepository jpaRepository;
    
    @InjectMocks
    private ConfigurationRepositoryAdapter adapter;
    
    private ConfigurationEntryEntity entity;
    private ConfigurationEntry domain;
    
    @BeforeEach
    void setUp() {
        entity = ConfigurationEntryEntity.builder()
                .id("1")
                .scope("tenant1")
                .key("app.setting")
                .value("value")
                .contentType("text/plain")
                .label("")
                .etag("etag123")
                .isSecret(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("user1")
                .updatedBy("user1")
                .build();
        
        domain = ConfigurationEntry.builder()
                .scope("tenant1")
                .key("app.setting")
                .value("value")
                .contentType("text/plain")
                .label("")
                .etag("etag123")
                .isSecret(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("user1")
                .updatedBy("user1")
                .build();
    }
    
    @Test
    void findByScopeAndKey_Success() {
        // Given
        when(jpaRepository.findByScopeAndKey("tenant1", "app.setting"))
                .thenReturn(Optional.of(entity));
        
        // When
        Optional<ConfigurationEntry> result = adapter.findByScopeAndKey("tenant1", "app.setting");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("tenant1", result.get().getScope());
        assertEquals("app.setting", result.get().getKey());
    }
    
    @Test
    void save_Success() {
        // Given
        when(jpaRepository.save(any(ConfigurationEntryEntity.class))).thenReturn(entity);
        
        // When
        ConfigurationEntry saved = adapter.save(domain);
        
        // Then
        assertNotNull(saved);
        assertEquals("tenant1", saved.getScope());
        verify(jpaRepository).save(any(ConfigurationEntryEntity.class));
    }
}

