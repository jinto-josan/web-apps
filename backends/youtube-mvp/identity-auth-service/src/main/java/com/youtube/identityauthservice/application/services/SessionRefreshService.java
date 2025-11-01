package com.youtube.identityauthservice.application.services;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.identityauthservice.domain.entities.RefreshToken;
import com.youtube.identityauthservice.domain.entities.Session;
import com.youtube.identityauthservice.domain.events.RefreshTokenReuseDetected;
import com.youtube.identityauthservice.domain.events.RefreshTokenRotated;
import com.youtube.identityauthservice.domain.events.SessionCreated;
import com.youtube.identityauthservice.domain.events.SessionRevoked;
import com.youtube.identityauthservice.domain.repositories.RefreshTokenRepository;
import com.youtube.identityauthservice.domain.repositories.SessionRepository;
import com.youtube.identityauthservice.domain.services.EventPublisher;
import com.youtube.identityauthservice.infrastructure.util.Hashing;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class SessionRefreshService {

    private final SessionRepository sessionRepo;
    private final RefreshTokenRepository refreshRepo;
    private final EventPublisher eventPublisher;
    private final int refreshTtlSeconds;

    public SessionRefreshService(SessionRepository sessionRepo, RefreshTokenRepository refreshRepo,
                                 EventPublisher eventPublisher, int refreshTtlSeconds) {
        this.sessionRepo = sessionRepo;
        this.refreshRepo = refreshRepo;
        this.eventPublisher = eventPublisher;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public static class SessionWithRefresh {
        public final Session session;
        public final String refreshTokenRaw;
        public final RefreshToken refreshEntity;
        public SessionWithRefresh(Session session, String refreshTokenRaw, RefreshToken refreshEntity) {
            this.session = session; this.refreshTokenRaw = refreshTokenRaw; this.refreshEntity = refreshEntity;
        }
    }

    public static class RotationResult {
        public final Session session;
        public final String refreshTokenRaw;
        public final RefreshToken refreshEntity;
        public RotationResult(Session session, String refreshTokenRaw, RefreshToken refreshEntity) {
            this.session = session; this.refreshTokenRaw = refreshTokenRaw; this.refreshEntity = refreshEntity;
        }
    }

    @Transactional
    public SessionWithRefresh createSessionWithRefresh(String userId, String deviceId, String userAgent, String ip) {
        String sessionId = UlidCreator.getUlid().toString();
        Session s = Session.builder()
                .id(sessionId)
                .userId(userId)
                .jti(UUID.randomUUID().toString())
                .deviceId(deviceId)
                .userAgent(userAgent)
                .ip(ip)
                .build();
        s = sessionRepo.save(s);

        Map.Entry<String, RefreshToken> rt = mintRefreshForSession(s.getId());
        
        // Publish domain event
        SessionCreated event = new SessionCreated(s.getId(), userId, deviceId, userAgent, ip);
        eventPublisher.publishSessionCreated(event);
        
        return new SessionWithRefresh(s, rt.getKey(), rt.getValue());
    }

    @Transactional
    public RotationResult rotateRefreshOrThrow(String rawRefreshToken) {
        RefreshToken token = findByRawOrThrow(rawRefreshToken);

        if (token.getRevokedAt() != null || token.getReplacedByTokenId() != null || token.getExpiresAt().isBefore(Instant.now())) {
            Session s = sessionRepo.findById(token.getSessionId()).orElseThrow();
            RefreshTokenReuseDetected event = new RefreshTokenReuseDetected(
                    token.getSessionId(), s.getUserId(), token.getId(), "refresh_reuse_or_expired");
            eventPublisher.publishRefreshTokenReuseDetected(event);
            revokeWholeSessionChain(token.getSessionId(), "refresh_reuse_or_expired");
            throw new SecurityException("Refresh token reuse/expired");
        }

        Map.Entry<String, RefreshToken> newRt = mintRefreshForSession(token.getSessionId());
        token = token.toBuilder()
                .replacedByTokenId(newRt.getValue().getId())
                .build();
        refreshRepo.save(token);

        Session s = sessionRepo.findById(token.getSessionId()).orElseThrow();
        s = s.toBuilder()
                .jti(UUID.randomUUID().toString())
                .build();
        s = sessionRepo.save(s);

        // Publish domain event
        RefreshTokenRotated event = new RefreshTokenRotated(
                s.getId(), s.getUserId(), token.getId(), newRt.getValue().getId());
        eventPublisher.publishRefreshTokenRotated(event);
        
        return new RotationResult(s, newRt.getKey(), newRt.getValue());
    }

    @Transactional
    public void revokeByRawRefreshToken(String rawRefreshToken) {
        RefreshToken token = findByRawOrThrow(rawRefreshToken);
        revokeWholeSessionChain(token.getSessionId(), "logout");
    }

    private RefreshToken findByRawOrThrow(String rawRefreshToken) {
        byte[] hash = Hashing.sha256(rawRefreshToken.getBytes(StandardCharsets.UTF_8));
        return refreshRepo.findByTokenHash(hash).orElseThrow(() -> new SecurityException("Unknown refresh token"));
    }

    private void revokeWholeSessionChain(String sessionId, String reason) {
        Session s = sessionRepo.findById(sessionId).orElseThrow();
        if (s.getRevokedAt() == null) {
            s = s.toBuilder()
                    .revokedAt(Instant.now())
                    .revokeReason(reason)
                    .build();
            s = sessionRepo.save(s);
        }
        List<RefreshToken> tokens = refreshRepo.findBySessionId(sessionId);
        for (RefreshToken rt : tokens) {
            if (rt.getRevokedAt() == null) {
                rt = rt.toBuilder()
                        .revokedAt(Instant.now())
                        .revokeReason("chain_revoked:" + reason)
                        .build();
                refreshRepo.save(rt);
            }
        }
        
        // Publish domain event
        SessionRevoked event = new SessionRevoked(s.getId(), s.getUserId(), reason);
        eventPublisher.publishSessionRevoked(event);
    }

    private Map.Entry<String, RefreshToken> mintRefreshForSession(String sessionId) {
        String tokenId = UlidCreator.getUlid().toString();
        String secret = UUID.randomUUID().toString().replace("-", "");
        String raw = tokenId + "." + secret;
        RefreshToken rt = RefreshToken.builder()
                .id(tokenId)
                .sessionId(sessionId)
                .tokenHash(Hashing.sha256(raw))
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshTtlSeconds))
                .build();
        rt = refreshRepo.save(rt);
        return Map.entry(raw, rt);
    }
}
