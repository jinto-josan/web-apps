package com.youtube.experimentationservice.infrastructure.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CosmosConfig {

    @Value("${spring.cloud.azure.cosmos.endpoint:}")
    private String endpoint;

    @Value("${spring.cloud.azure.cosmos.key:}")
    private String key;

    @Bean
    public CosmosClient cosmosClient() {
        if (endpoint == null || endpoint.isEmpty() || key == null || key.isEmpty()) {
            log.warn("Cosmos DB not configured - endpoint or key missing");
            return null;
        }
        
        return new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .buildClient();
    }
}

