package com.youtube.identityauthservice.infrastructure.config;

import com.youtube.identityauthservice.application.services.DeviceFlowService;
import com.youtube.identityauthservice.application.services.OidcIdTokenVerifier;
import com.youtube.identityauthservice.application.services.SessionRefreshService;
import com.youtube.identityauthservice.application.services.TokenService;
import com.youtube.identityauthservice.infrastructure.jwt.JwkProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AppConfig {

    @Bean
    public TokenService tokenService(
            @Value("${app.issuer}") String issuer,
            @Value("${app.audience}") String audience,
            @Value("${app.access-token-ttl-seconds}") int accessTtl,
            JwkProvider jwkProvider) {
        return new TokenService(jwkProvider,issuer, audience, accessTtl);
    }

    @Bean
    public SessionRefreshService sessionRefreshService(
            com.youtube.identityauthservice.domain.repositories.SessionRepository sessionRepo,
            com.youtube.identityauthservice.domain.repositories.RefreshTokenRepository refreshRepo,
            com.youtube.identityauthservice.domain.services.EventPublisher eventPublisher,
            @Value("${app.refresh-token-ttl-seconds}") int refreshTtl) {
        return new SessionRefreshService(sessionRepo, refreshRepo, eventPublisher, refreshTtl);
    }

    @Bean
    public DeviceFlowService deviceFlowService(
            StringRedisTemplate redis,
            @Value("${app.device.user-code-length}") int userCodeLen,
            @Value("${app.device.device-code-ttl-seconds}") long ttl,
            @Value("${app.device.poll-interval-seconds}") long interval) {
        return new DeviceFlowService(redis, userCodeLen, ttl, interval);
    }

    @Bean
    public OidcIdTokenVerifier oidcVerifier(OidcProperties properties) {
        return new OidcIdTokenVerifier(properties);
    }

    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }
}
