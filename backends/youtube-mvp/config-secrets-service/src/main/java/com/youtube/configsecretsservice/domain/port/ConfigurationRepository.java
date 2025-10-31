package com.youtube.configsecretsservice.domain.port;

import com.youtube.configsecretsservice.domain.entity.ConfigurationEntry;

import java.util.List;
import java.util.Optional;

/**
 * Port for configuration repository operations.
 */
public interface ConfigurationRepository {
    Optional<ConfigurationEntry> findByScopeAndKey(String scope, String key);
    ConfigurationEntry save(ConfigurationEntry entry);
    void delete(String scope, String key);
    List<ConfigurationEntry> findAllByScope(String scope);
    List<ConfigurationEntry> findAllByScopeAndLabel(String scope, String label);
}

