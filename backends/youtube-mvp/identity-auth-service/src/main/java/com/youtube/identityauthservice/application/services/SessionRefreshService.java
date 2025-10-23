package com.youtube.identityauthservice.application.services;


import com.github.f4b6a3.ulid.UlidCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.identityauthservice.domain.model.OutboxEvent;
import com.youtube.identityauthservice.domain.model.RefreshToken;
import com.youtube.identityauthservice.domain.model.Session;
import com.youtube.identityauthservice.infrastructure.persistence.OutboxRepository;
import com.youtube.identityauthservice.infrastructure.persistence.RefreshTokenRepository;
import com.youtube.identityauthservice.infrastructure.persistence.SessionRepository;
import com.youtube.identityauthservice.infrastructure.util.Hashing;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class SessionRefreshService {

    private final SessionRepository sessionRepo;
    private final RefreshTokenRepository refreshRepo;
    private final OutboxRepository outboxRepo;
    private final int refreshTtlSeconds;
    private final ObjectMapper om = new ObjectMapper();

    public SessionRefreshService(SessionRepository sessionRepo, RefreshTokenRepository refreshRepo,
                                 OutboxRepository outboxRepo, int refreshTtlSeconds) {
        this.sessionRepo = sessionRepo;
        this.refreshRepo = refreshRepo;
        this.outboxRepo = outboxRepo;
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
        Session s = new Session();
        s.setId(sessionId);
        s.setUserId(userId);
        s.setJti(UUID.randomUUID().toString());
        s.setDeviceId(deviceId);
        s.setUserAgent(userAgent);
        s.setIp(ip);
        s = sessionRepo.save(s);

        Map.Entry<String, RefreshToken> rt = mintRefreshForSession(s.getId());
        publish("SessionCreated", "Session", s.getId(), Map.of("userId", userId, "deviceId", deviceId));
        return new SessionWithRefresh(s, rt.getKey(), rt.getValue());
    }

    @Transactional
    public RotationResult rotateRefreshOrThrow(String rawRefreshToken) {
        RefreshToken token = findByRawOrThrow(rawRefreshToken);

        if (token.getRevokedAt() != null || token.getReplacedByTokenId() != null || token.getExpiresAt().isBefore(Instant.now())) {
            revokeWholeSessionChain(token.getSessionId(), "refresh_reuse_or_expired");
            throw new SecurityException("Refresh token reuse/expired");
        }

        Map.Entry<String, RefreshToken> newRt = mintRefreshForSession(token.getSessionId());
        token.setReplacedByTokenId(newRt.getValue().getId());
        refreshRepo.save(token);

        Session s = sessionRepo.findById(token.getSessionId()).orElseThrow();
        s.setJti(UUID.randomUUID().toString());
        sessionRepo.save(s);

        publish("RefreshRotated", "Session", s.getId(), Map.of("old", token.getId(), "new", newRt.getValue().getId()));
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
            s.setRevokedAt(Instant.now());
            s.setRevokeReason(reason);
            sessionRepo.save(s);
        }
        List<RefreshToken> tokens = refreshRepo.findBySessionId(sessionId);
        for (RefreshToken rt : tokens) {
            if (rt.getRevokedAt() == null) {
                rt.setRevokedAt(Instant.now());
                rt.setRevokeReason("chain_revoked:" + reason);
                refreshRepo.save(rt);
            }
        }
        publish("SessionRevoked", "Session", s.getId(), Map.of("reason", reason));
    }

    private Map.Entry<String, RefreshToken> mintRefreshForSession(String sessionId) {
        String tokenId = UlidCreator.getUlid().toString();
        String secret = UUID.randomUUID().toString().replace("-", "");
        String raw = tokenId + "." + secret;
        RefreshToken rt = new RefreshToken();
        rt.setId(tokenId);
        rt.setSessionId(sessionId);
        rt.setTokenHash(Hashing.sha256(raw));
        rt.setCreatedAt(Instant.now());
        rt.setExpiresAt(Instant.now().plusSeconds(refreshTtlSeconds));
        refreshRepo.save(rt);
        return Map.entry(raw, rt);
    }

    private void publish(String type, String aggType, String aggId, Map<String, Object> payload) {
        try {
            OutboxEvent evt = new OutboxEvent();
            evt.setId(UlidCreator.getUlid().toString());
            evt.setEventType(type);
            evt.setAggregateType(aggType);
            evt.setAggregateId(aggId);
            evt.setPayloadJson(om.writeValueAsString(payload));
            outboxRepo.save(evt);
        } catch (Exception e) {
            // Last resort: keep service running, log error
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}
