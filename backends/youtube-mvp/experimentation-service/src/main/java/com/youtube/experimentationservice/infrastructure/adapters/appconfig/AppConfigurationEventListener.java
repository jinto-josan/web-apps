package com.youtube.experimentationservice.infrastructure.adapters.appconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cloud.azure.data.appconfiguration.config.AppConfigurationRefreshEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppConfigurationEventListener {
    
    @EventListener
    @CacheEvict(value = "featureFlags", allEntries = true)
    public void handleAppConfigurationRefresh(AppConfigurationRefreshEvent event) {
        log.info("App Configuration refreshed, clearing feature flag cache");
    }
}

