package com.youtube.analyticstelemetryservice.infrastructure.adapters.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Azure Event Hubs producer client.
 */
@Slf4j
@Configuration
public class EventHubsConfig {
    
    @Value("${azure.eventhubs.connection-string:}")
    private String connectionString;
    
    @Value("${azure.eventhubs.event-hub-name:telemetry-events}")
    private String eventHubName;
    
    @Bean
    public EventHubProducerClient eventHubProducerClient() {
        if (connectionString == null || connectionString.isBlank()) {
            log.warn("Event Hubs connection string not configured. Using mock producer.");
            return null; // Will be handled gracefully in the adapter
        }
        
        return new EventHubClientBuilder()
            .connectionString(connectionString, eventHubName)
            .buildProducerClient();
    }
}

