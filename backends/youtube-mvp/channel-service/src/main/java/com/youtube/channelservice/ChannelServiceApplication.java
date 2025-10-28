package com.youtube.channelservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Channel and Subscription Service
 * 
 * Features:
 * - Channels CRUD operations
 * - Subscribe/unsubscribe functionality
 * - Anti-supernode sharding strategy
 * - Idempotent writes with Idempotency-Key header
 * - CQRS read models
 * - Cache-aside pattern with Redis
 * - Service Bus event publishing
 * - Cosmos DB persistence
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaRepositories
public class ChannelServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChannelServiceApplication.class, args);
    }
}
