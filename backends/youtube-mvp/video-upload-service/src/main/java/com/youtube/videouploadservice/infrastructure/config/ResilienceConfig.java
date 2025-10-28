package com.youtube.videouploadservice.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience configuration with retry and circuit breaker patterns.
 * Ensures system stability under failures and high load.
 */
@Configuration
public class ResilienceConfig {
    
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            .retryExceptions(
                java.net.SocketTimeoutException.class,
                java.io.IOException.class,
                org.springframework.web.client.HttpServerErrorException.class
            )
            .build();
        
        return RetryRegistry.of(retryConfig);
    }
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .failureRateThreshold(50)
            .slowCallDurationThreshold(Duration.ofSeconds(5))
            .slowCallRateThreshold(100)
            .build();
        
        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }
    
    @Bean
    public CircuitBreaker blobStorageCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("blobStorage");
    }
    
    @Bean
    public Retry blobStorageRetry(RetryRegistry registry) {
        return registry.retry("blobStorage");
    }
}

