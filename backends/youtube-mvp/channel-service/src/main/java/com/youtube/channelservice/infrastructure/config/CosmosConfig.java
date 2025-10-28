package com.youtube.channelservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cosmos.config.EnableCosmosRepositories;

@Slf4j
@Configuration
@EnableCosmosRepositories(
    basePackages = "com.youtube.channelservice.infrastructure.persistence.repository"
)
public class CosmosConfig {
    // Cosmos DB configuration is handled by Spring Cloud Azure
}
