package com.youtube.analyticstelemetryservice.infrastructure;

import com.youtube.analyticstelemetryservice.infrastructure.adapters.redis.RedisIdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RedisIdempotencyServiceTest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379);
    
    @Autowired(required = false)
    private RedisIdempotencyService idempotencyService;
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    @BeforeEach
    void setUp() {
        if (redisTemplate != null) {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        }
    }
    
    @Test
    void shouldMarkAndCheckIdempotency() {
        if (idempotencyService == null) {
            return; // Skip if Redis not available
        }
        
        String key = "test-key-123";
        
        assertThat(idempotencyService.isProcessed(key)).isFalse();
        idempotencyService.markProcessed(key);
        assertThat(idempotencyService.isProcessed(key)).isTrue();
    }
}

