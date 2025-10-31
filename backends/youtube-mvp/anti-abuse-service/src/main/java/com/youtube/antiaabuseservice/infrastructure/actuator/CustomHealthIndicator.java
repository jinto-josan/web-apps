package com.youtube.antiaabuseservice.infrastructure.actuator;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;
    private final CosmosTemplate cosmosTemplate;

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

        // Note: ML endpoint health check would require actual endpoint URL
        // For now, we'll skip it to avoid unnecessary calls

        return builder.build();
    }
}

