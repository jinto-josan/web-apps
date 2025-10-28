package com.youtube.mvp.videocatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Video Catalog Service Application.
 * 
 * Features:
 * - Hexagonal architecture (domain, application, infrastructure)
 * - DDD-lite with aggregates, value objects, repositories
 * - CQRS for command/query separation
 * - Cosmos DB for persistence
 * - Service Bus with Outbox pattern for reliable messaging
 * - API versioning, ETag support, pagination
 */
@SpringBootApplication
public class VideoCatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoCatalogServiceApplication.class, args);
    }
}

