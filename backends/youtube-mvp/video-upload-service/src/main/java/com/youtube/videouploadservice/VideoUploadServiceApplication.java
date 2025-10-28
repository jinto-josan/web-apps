package com.youtube.videouploadservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Video Upload Service.
 * 
 * Features:
 * - Pre-signed URLs for direct client uploads to Azure Blob Storage
 * - Resumable chunked uploads for large files
 * - Saga pattern for distributed transaction orchestration
 * - Resilience patterns (retry, circuit breaker, timeout)
 * - Upload validation and quota management
 * 
 * Architecture:
 * - Clean Architecture with DDD patterns
 * - Saga orchestration for complex workflows
 * - Azure Blob Storage for object storage
 * - Azure Service Bus for saga coordination
 * - Azure Event Grid for blob notifications
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableAsync
public class VideoUploadServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(VideoUploadServiceApplication.class, args);
    }
}

