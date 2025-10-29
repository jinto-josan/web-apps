package com.youtube.livestreaming.domain.valueobjects;

import lombok.Builder;
import lombok.Value;

/**
 * Value object for live event configuration
 */
@Value
@Builder
public class LiveEventConfiguration {
    String name;
    String description;
    String channelId;
    String userId;
    String region;
    Boolean dvrEnabled;
    Integer dvrWindowInMinutes;
    Boolean lowLatencyEnabled;
    Boolean autoStart;
    Integer maxConcurrentViewers;
    String[] allowedCountries;
    String[] blockedCountries;
    
    public static LiveEventConfiguration withDefaults() {
        return LiveEventConfiguration.builder()
            .dvrEnabled(true)
            .dvrWindowInMinutes(120)
            .lowLatencyEnabled(false)
            .autoStart(false)
            .maxConcurrentViewers(10000)
            .build();
    }
}

