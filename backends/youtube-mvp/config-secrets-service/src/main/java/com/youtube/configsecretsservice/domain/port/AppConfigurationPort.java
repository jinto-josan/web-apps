package com.youtube.configsecretsservice.domain.port;

import com.youtube.configsecretsservice.domain.entity.ConfigurationEntry;

import java.util.List;
import java.util.Optional;

/**
 * Port for Azure App Configuration operations.
 */
public interface AppConfigurationPort {
    Optional<ConfigurationEntry> getConfiguration(String scope, String key, String label);
    ConfigurationEntry setConfiguration(String scope, String key, String value, String contentType, String label, String etag);
    void deleteConfiguration(String scope, String key, String label);
    List<ConfigurationEntry> listConfigurations(String scope, String labelFilter);
}

