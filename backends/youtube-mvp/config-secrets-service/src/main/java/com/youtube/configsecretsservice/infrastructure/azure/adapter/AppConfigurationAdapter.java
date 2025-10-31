package com.youtube.configsecretsservice.infrastructure.azure.adapter;

import com.azure.core.exception.HttpResponseException;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.youtube.configsecretsservice.domain.entity.ConfigurationEntry;
import com.youtube.configsecretsservice.domain.port.AppConfigurationPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing AppConfigurationPort using Azure App Configuration SDK.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppConfigurationAdapter implements AppConfigurationPort {
    
    private final ConfigurationClient configurationClient;
    
    @Override
    @Retry(name = "appConfig")
    @CircuitBreaker(name = "appConfig")
    @TimeLimiter(name = "appConfig")
    public Optional<ConfigurationEntry> getConfiguration(String scope, String key, String label) {
        try {
            String fullKey = buildKey(scope, key);
            ConfigurationSetting setting = configurationClient.getConfigurationSetting(fullKey, label);
            return Optional.of(toDomain(setting, scope, key));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return Optional.empty();
            }
            log.error("Error getting configuration from App Config: {}/{}", scope, key, e);
            throw new RuntimeException("Failed to get configuration from App Config", e);
        }
    }
    
    @Override
    @Retry(name = "appConfig")
    @CircuitBreaker(name = "appConfig")
    @TimeLimiter(name = "appConfig")
    public ConfigurationEntry setConfiguration(String scope, String key, String value, String contentType, String label, String etag) {
        try {
            String fullKey = buildKey(scope, key);
            ConfigurationSetting setting = new ConfigurationSetting(fullKey, value);
            if (label != null && !label.isEmpty()) {
                setting.setLabel(label);
            }
            if (etag != null && !etag.isEmpty()) {
                setting.setETag(etag);
            }
            
            ConfigurationSetting saved = configurationClient.setConfigurationSetting(setting);
            return toDomain(saved, scope, key);
        } catch (HttpResponseException e) {
            log.error("Error setting configuration in App Config: {}/{}", scope, key, e);
            throw new RuntimeException("Failed to set configuration in App Config", e);
        }
    }
    
    @Override
    @Retry(name = "appConfig")
    @CircuitBreaker(name = "appConfig")
    @TimeLimiter(name = "appConfig")
    public void deleteConfiguration(String scope, String key, String label) {
        try {
            String fullKey = buildKey(scope, key);
            configurationClient.deleteConfigurationSetting(fullKey, label);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() != 404) {
                log.error("Error deleting configuration from App Config: {}/{}", scope, key, e);
                throw new RuntimeException("Failed to delete configuration from App Config", e);
            }
        }
    }
    
    @Override
    @Retry(name = "appConfig")
    @CircuitBreaker(name = "appConfig")
    @TimeLimiter(name = "appConfig")
    public List<ConfigurationEntry> listConfigurations(String scope, String labelFilter) {
        try {
            String keyFilter = buildKey(scope, "*");
            SettingSelector selector = new SettingSelector()
                    .setKeyFilter(keyFilter);
            if (labelFilter != null && !labelFilter.isEmpty()) {
                selector.setLabelFilter(labelFilter);
            }
            
            return configurationClient.listConfigurationSettings(selector).stream()
                    .map(setting -> toDomain(setting, scope, extractKey(setting.getKey(), scope)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error listing configurations from App Config: scope={}", scope, e);
            throw new RuntimeException("Failed to list configurations from App Config", e);
        }
    }
    
    private String buildKey(String scope, String key) {
        return String.format("%s:%s", scope, key);
    }
    
    private String extractKey(String fullKey, String scope) {
        String prefix = scope + ":";
        if (fullKey.startsWith(prefix)) {
            return fullKey.substring(prefix.length());
        }
        return fullKey;
    }
    
    private ConfigurationEntry toDomain(ConfigurationSetting setting, String scope, String key) {
        return ConfigurationEntry.builder()
                .scope(scope)
                .key(key)
                .value(setting.getValue())
                .contentType(setting.getContentType())
                .label(setting.getLabel() != null ? setting.getLabel() : "")
                .etag(setting.getETag())
                .isSecret(false)
                .createdAt(setting.getLastModified() != null ? setting.getLastModified().toInstant() : Instant.now())
                .updatedAt(setting.getLastModified() != null ? setting.getLastModified().toInstant() : Instant.now())
                .build();
    }
}

