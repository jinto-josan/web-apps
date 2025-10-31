package com.youtube.experimentationservice.infrastructure.actuator;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;
    private final CosmosTemplate cosmosTemplate;
    private final ConfigurationClient configurationClient;

    public CustomHealthIndicator(
            RedisConnectionFactory redisConnectionFactory,
            CosmosTemplate cosmosTemplate,
            ConfigurationClient configurationClient) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.cosmosTemplate = cosmosTemplate;
        this.configurationClient = configurationClient;
    }
    
    // Constructor without ConfigurationClient for cases where it's not available
    public CustomHealthIndicator(
            RedisConnectionFactory redisConnectionFactory,
            CosmosTemplate cosmosTemplate) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.cosmosTemplate = cosmosTemplate;
        this.configurationClient = null;
    }

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();

        try {
            // Check Redis
            redisConnectionFactory.getConnection().ping();
            builder.withDetail("redis", "UP");
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            builder.withDetail("redis", "DOWN").withException(e);
        }

        try {
            // Check Cosmos DB
            cosmosTemplate.getContainerName(null);
            builder.withDetail("cosmos", "UP");
        } catch (Exception e) {
            log.error("Cosmos DB health check failed", e);
            builder.withDetail("cosmos", "DOWN").withException(e);
        }

        try {
            // Check App Configuration (if available)
            if (configurationClient != null) {
                builder.withDetail("appConfiguration", "UP");
            }
        } catch (Exception e) {
            log.error("App Configuration health check failed", e);
            builder.withDetail("appConfiguration", "DOWN").withException(e);
        }

        return builder.build();
    }
}

