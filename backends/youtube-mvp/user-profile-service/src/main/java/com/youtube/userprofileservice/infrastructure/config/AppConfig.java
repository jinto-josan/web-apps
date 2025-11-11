package com.youtube.userprofileservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.youtube.common.domain.events.outbox.JpaOutboxRepository;
import com.youtube.common.domain.persistence.entity.OutboxEvent;
import com.youtube.userprofileservice.infrastructure.persistence.entity.OutboxEventEntity;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration for user-profile-service.
 * Configures beans required by the common-domain infrastructure.
 */
@Configuration
public class AppConfig {

    /**
     * Creates a JpaOutboxRepository bean for common-domain EventPublisher.
     * This bean implements the OutboxRepository interface required by EventPublisher.
     * 
     * <p>The transactional outbox pattern ensures reliable event publishing by storing
     * events in the database within the same transaction as domain changes.</p>
     * 
     * @param entityManager the JPA EntityManager
     * @return configured JpaOutboxRepository instance
     */
    @Bean
    public JpaOutboxRepository commonDomainOutboxRepository(EntityManager entityManager) {
        return new JpaOutboxRepository(entityManager, OutboxEventEntity.class) {
            @Override
            protected OutboxEvent createOutboxEvent() {
                return new OutboxEventEntity();
            }
        };
    }
    
    /**
     * Creates an ObjectMapper bean for JSON serialization/deserialization.
     * Used by photo processing queue messages and other JSON operations.
     * 
     * @return configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Register subtypes for polymorphic deserialization
        mapper.registerSubtypes(
                com.youtube.userprofileservice.domain.events.PhotoUploadedEvent.class
        );
        return mapper;
    }
}

