package com.youtube.identityauthservice.infrastructure.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.identityauthservice.domain.events.*;
import com.youtube.identityauthservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.youtube.identityauthservice.domain.services.EventPublisher;
import com.youtube.identityauthservice.infrastructure.persistence.OutboxRepository;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implementation of EventPublisher using transactional outbox pattern.
 */
@Component
public class EventPublisherImpl implements EventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public EventPublisherImpl(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishUserCreated(UserCreated event) {
        try {
            OutboxEventEntity outboxEvent = new OutboxEventEntity();
            outboxEvent.setId(UlidCreator.getUlid().toString());
            outboxEvent.setEventType("user_created");
            outboxEvent.setAggregateType("User");
            outboxEvent.setAggregateId(event.getUserId());
            outboxEvent.setPayloadJson(objectMapper.writeValueAsString(Map.of(
                    "userId", event.getUserId(),
                    "email", event.getEmail(),
                    "normalizedEmail", event.getNormalizedEmail(),
                    "displayName", event.getDisplayName(),
                    "emailVerified", event.isEmailVerified(),
                    "status", event.getStatus(),
                    "createdAt", event.getCreatedAt().toString()
            )));
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish user_created event", e);
        }
    }

    @Override
    public void publishUserUpdated(UserUpdated event) {
        try {
            OutboxEventEntity outboxEvent = new OutboxEventEntity();
            outboxEvent.setId(UlidCreator.getUlid().toString());
            outboxEvent.setEventType("user_updated");
            outboxEvent.setAggregateType("User");
            outboxEvent.setAggregateId(event.getUserId());
            outboxEvent.setPayloadJson(objectMapper.writeValueAsString(Map.of(
                    "userId", event.getUserId(),
                    "email", event.getEmail(),
                    "displayName", event.getDisplayName(),
                    "emailVerified", event.isEmailVerified()
            )));
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish user_updated event", e);
        }
    }

    @Override
    public void publishSessionCreated(SessionCreated event) {
        try {
            OutboxEventEntity outboxEvent = new OutboxEventEntity();
            outboxEvent.setId(UlidCreator.getUlid().toString());
            outboxEvent.setEventType("session_created");
            outboxEvent.setAggregateType("Session");
            outboxEvent.setAggregateId(event.getSessionId());
            outboxEvent.setPayloadJson(objectMapper.writeValueAsString(Map.of(
                    "sessionId", event.getSessionId(),
                    "userId", event.getUserId(),
                    "deviceId", event.getDeviceId() != null ? event.getDeviceId() : "",
                    "userAgent", event.getUserAgent() != null ? event.getUserAgent() : "",
                    "ip", event.getIp() != null ? event.getIp() : ""
            )));
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish session_created event", e);
        }
    }

    @Override
    public void publishSessionRevoked(SessionRevoked event) {
        try {
            OutboxEventEntity outboxEvent = new OutboxEventEntity();
            outboxEvent.setId(UlidCreator.getUlid().toString());
            outboxEvent.setEventType("session_revoked");
            outboxEvent.setAggregateType("Session");
            outboxEvent.setAggregateId(event.getSessionId());
            outboxEvent.setPayloadJson(objectMapper.writeValueAsString(Map.of(
                    "sessionId", event.getSessionId(),
                    "userId", event.getUserId(),
                    "reason", event.getReason() != null ? event.getReason() : ""
            )));
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish session_revoked event", e);
        }
    }

    @Override
    public void publishRefreshTokenRotated(RefreshTokenRotated event) {
        try {
            OutboxEventEntity outboxEvent = new OutboxEventEntity();
            outboxEvent.setId(UlidCreator.getUlid().toString());
            outboxEvent.setEventType("refresh_token_rotated");
            outboxEvent.setAggregateType("Session");
            outboxEvent.setAggregateId(event.getSessionId());
            outboxEvent.setPayloadJson(objectMapper.writeValueAsString(Map.of(
                    "sessionId", event.getSessionId(),
                    "userId", event.getUserId(),
                    "oldTokenId", event.getOldTokenId() != null ? event.getOldTokenId() : "",
                    "newTokenId", event.getNewTokenId()
            )));
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish refresh_token_rotated event", e);
        }
    }

    @Override
    public void publishRefreshTokenReuseDetected(RefreshTokenReuseDetected event) {
        try {
            OutboxEventEntity outboxEvent = new OutboxEventEntity();
            outboxEvent.setId(UlidCreator.getUlid().toString());
            outboxEvent.setEventType("refresh_token_reuse_detected");
            outboxEvent.setAggregateType("Session");
            outboxEvent.setAggregateId(event.getSessionId());
            outboxEvent.setPayloadJson(objectMapper.writeValueAsString(Map.of(
                    "sessionId", event.getSessionId(),
                    "userId", event.getUserId(),
                    "tokenId", event.getTokenId() != null ? event.getTokenId() : "",
                    "reason", event.getReason()
            )));
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish refresh_token_reuse_detected event", e);
        }
    }
}

