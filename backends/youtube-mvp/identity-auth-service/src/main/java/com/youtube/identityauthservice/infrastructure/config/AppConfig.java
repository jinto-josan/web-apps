package com.youtube.identityauthservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.youtube.common.domain.events.outbox.JpaOutboxRepository;
import com.youtube.common.domain.persistence.entity.OutboxEvent;
import com.youtube.identityauthservice.application.services.DeviceFlowService;
import com.youtube.identityauthservice.application.services.OidcIdTokenVerifier;
import com.youtube.identityauthservice.application.services.SessionRefreshService;
import com.youtube.identityauthservice.application.services.TokenService;
import com.youtube.identityauthservice.infrastructure.jwt.JwkProvider;
import com.youtube.identityauthservice.infrastructure.persistence.entity.OutboxEventEntity;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AppConfig {

    @Bean
    public TokenService tokenService(
            @Value("${identity-auth.issuer}") String issuer,
            @Value("${identity-auth.audience}") String audience,
            @Value("${identity-auth.access-token-ttl-seconds}") int accessTtl,
            JwkProvider jwkProvider) {
        return new TokenService(jwkProvider,issuer, audience, accessTtl);
    }

    @Bean
    public SessionRefreshService sessionRefreshService(
            com.youtube.identityauthservice.domain.repositories.SessionRepository sessionRepo,
            com.youtube.identityauthservice.domain.repositories.RefreshTokenRepository refreshRepo,
            com.youtube.identityauthservice.domain.services.EventPublisher eventPublisher,
            @Value("${identity-auth.refresh-token-ttl-seconds}") int refreshTtl) {
        return new SessionRefreshService(sessionRepo, refreshRepo, eventPublisher, refreshTtl);
    }

    @Bean
    public DeviceFlowService deviceFlowService(
            StringRedisTemplate redis,
            @Value("${identity-auth.device.user-code-length}") int userCodeLen,
            @Value("${identity-auth.device.device-code-ttl-seconds}") long ttl,
            @Value("${identity-auth.device.poll-interval-seconds}") long interval) {
        return new DeviceFlowService(redis, userCodeLen, ttl, interval);
    }

    @Bean
    public OidcIdTokenVerifier oidcVerifier(OidcProperties properties) {
        return new OidcIdTokenVerifier(properties);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Creates a JpaOutboxRepository bean for common-domain EventPublisher.
     * This bean implements the OutboxRepository interface required by EventPublisher.
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
}
