package com.youtube.drmservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DRM Service
 * 
 * Features:
 * - DRM policy management (Widevine, PlayReady, FairPlay)
 * - Azure Media Services integration for license configuration
 * - Key rotation scheduler
 * - Audit trail for policy changes
 * - Idempotent policy updates
 * - Policy caching with Redis
 * - Service Bus integration for key.rotation events
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaRepositories
@EnableScheduling
public class DrmServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DrmServiceApplication.class, args);
    }
}

