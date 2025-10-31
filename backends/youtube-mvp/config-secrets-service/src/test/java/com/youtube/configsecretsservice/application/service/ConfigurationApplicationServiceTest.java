package com.youtube.configsecretsservice.application.service;

import com.youtube.configsecretsservice.application.dto.ConfigRequest;
import com.youtube.configsecretsservice.application.dto.ConfigResponse;
import com.youtube.configsecretsservice.domain.entity.ConfigurationEntry;
import com.youtube.configsecretsservice.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationApplicationServiceTest {
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Mock
    private AppConfigurationPort appConfigurationPort;
    
    @Mock
    private KeyVaultPort keyVaultPort;
    
    @Mock
    private CachePort cachePort;
    
    @Mock
    private RbacCheckPort rbacCheckPort;
    
    @Mock
    private AuditLoggerPort auditLoggerPort;
    
    @InjectMocks
    private ConfigurationApplicationService service;
    
    private String scope;
    private String key;
    private String userId;
    private String tenantId;
    
    @BeforeEach
    void setUp() {
        scope = "tenant1";
        key = "app.setting";
        userId = "user123";
        tenantId = "tenant1";
    }
    
    @Test
    void getConfiguration_Success() {
        // Given
        when(rbacCheckPort.canRead(userId, tenantId, scope)).thenReturn(true);
        when(cachePort.get(anyString())).thenReturn(Optional.empty());
        
        ConfigurationEntry entry = ConfigurationEntry.builder()
                .scope(scope)
                .key(key)
                .value("test-value")
                .contentType("text/plain")
                .label("")
                .etag("etag123")
                .isSecret(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(userId)
                .updatedBy(userId)
                .build();
        
        when(appConfigurationPort.getConfiguration(scope, key, null))
                .thenReturn(Optional.of(entry));
        when(configurationRepository.save(any())).thenReturn(entry);
        
        // When
        ConfigResponse response = service.getConfiguration(scope, key, userId, tenantId, null);
        
        // Then
        assertNotNull(response);
        assertEquals(key, response.getKey());
        assertEquals(scope, response.getScope());
        assertEquals("test-value", response.getValue());
        verify(rbacCheckPort).canRead(userId, tenantId, scope);
        verify(auditLoggerPort).log(any());
    }
    
    @Test
    void getConfiguration_AccessDenied() {
        // Given
        when(rbacCheckPort.canRead(userId, tenantId, scope)).thenReturn(false);
        
        // When/Then
        assertThrows(ResponseStatusException.class, () -> {
            service.getConfiguration(scope, key, userId, tenantId, null);
        });
        
        verify(auditLoggerPort).log(argThat(log -> 
            log.getAction().equals(com.youtube.configsecretsservice.domain.entity.AuditLog.AuditAction.ACCESS_DENIED)
        ));
    }
    
    @Test
    void updateConfiguration_Success() {
        // Given
        when(rbacCheckPort.canWrite(userId, tenantId, scope)).thenReturn(true);
        when(configurationRepository.findByScopeAndKey(scope, key))
                .thenReturn(Optional.empty());
        
        ConfigRequest request = ConfigRequest.builder()
                .value("new-value")
                .contentType("text/plain")
                .isSecret(false)
                .build();
        
        ConfigurationEntry updated = ConfigurationEntry.builder()
                .scope(scope)
                .key(key)
                .value("new-value")
                .contentType("text/plain")
                .label("")
                .etag("etag456")
                .isSecret(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(userId)
                .updatedBy(userId)
                .build();
        
        when(appConfigurationPort.setConfiguration(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(updated);
        when(configurationRepository.save(any())).thenReturn(updated);
        
        // When
        ConfigResponse response = service.updateConfiguration(scope, key, request, null, userId, tenantId);
        
        // Then
        assertNotNull(response);
        assertEquals("new-value", response.getValue());
        verify(rbacCheckPort).canWrite(userId, tenantId, scope);
        verify(auditLoggerPort).log(any());
    }
}

