package com.youtube.experimentationservice.infrastructure.adapters.appconfig;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.youtube.experimentationservice.domain.model.FeatureFlag;
import com.youtube.experimentationservice.domain.repositories.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cloud.azure.data.appconfiguration.config.AppConfigurationPropertySourceLocator;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnBean(ConfigurationClient.class)
public class FeatureFlagRepositoryAdapter implements FeatureFlagRepository {
    private final ConfigurationClient configurationClient;
    private static final String FEATURE_FLAG_PREFIX = ".appconfig.featureflag/";

    @Override
    @CacheEvict(value = "featureFlags", key = "#flag.key")
    public FeatureFlag save(FeatureFlag flag) {
        // In a real implementation, this would update App Configuration
        // For now, we'll log it
        log.info("Feature flag saved: {}", flag.getKey());
        return flag;
    }

    @Override
    public Optional<FeatureFlag> findByKey(String key) {
        try {
            String settingKey = FEATURE_FLAG_PREFIX + key;
            ConfigurationSetting setting = configurationClient.getConfigurationSetting(settingKey);
            
            // Parse the feature flag from App Configuration
            // This is a simplified version - in production, parse JSON
            boolean enabled = setting.getValue() != null && 
                             setting.getValue().contains("\"enabled\":true");
            
            return Optional.of(FeatureFlag.builder()
                    .key(key)
                    .enabled(enabled)
                    .rolloutPercentage(1.0)
                    .conditions(new HashMap<>())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());
        } catch (Exception e) {
            log.debug("Feature flag not found: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public List<FeatureFlag> findAll() {
        try {
            SettingSelector selector = new SettingSelector()
                    .setKeyFilter(FEATURE_FLAG_PREFIX + "*");
            
            return configurationClient.listConfigurationSettings(selector)
                    .stream()
                    .map(this::toFeatureFlag)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching feature flags", e);
            return new ArrayList<>();
        }
    }

    @Override
    @CacheEvict(value = "featureFlags", key = "#key")
    public void deleteByKey(String key) {
        try {
            String settingKey = FEATURE_FLAG_PREFIX + key;
            configurationClient.deleteConfigurationSetting(settingKey);
        } catch (Exception e) {
            log.error("Error deleting feature flag: {}", key, e);
        }
    }

    private FeatureFlag toFeatureFlag(ConfigurationSetting setting) {
        String key = setting.getKey().replace(FEATURE_FLAG_PREFIX, "");
        boolean enabled = setting.getValue() != null && 
                         setting.getValue().contains("\"enabled\":true");
        
        return FeatureFlag.builder()
                .key(key)
                .enabled(enabled)
                .rolloutPercentage(1.0)
                .conditions(new HashMap<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

