package com.youtube.identityauthservice.application.services;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.identityauthservice.domain.entities.RefreshToken;
import com.youtube.identityauthservice.domain.entities.Session;
import com.youtube.identityauthservice.domain.events.RefreshTokenReuseDetected;
import com.youtube.identityauthservice.domain.events.RefreshTokenRotated;
import com.youtube.identityauthservice.domain.events.SessionCreated;
import com.youtube.identityauthservice.domain.events.SessionRevoked;
import com.youtube.identityauthservice.domain.repositories.RefreshTokenRepository;
import com.youtube.identityauthservice.domain.repositories.SessionRepository;
import com.youtube.identityauthservice.domain.services.EventPublisher;
import com.youtube.identityauthservice.domain.valueobjects.RefreshTokenId;
import com.youtube.identityauthservice.domain.valueobjects.SessionId;
import com.youtube.common.domain.utils.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class SessionRefreshService {

    private static final Logger log = LoggerFactory.getLogger(SessionRefreshService.class);

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
    public SessionWithRefresh createSessionWithRefresh(UserId userId, String deviceId, String userAgent, String ip) {
        log.debug("Creating session with refresh token - userId: {}, deviceId: {}", userId.asString(), deviceId);
        
        SessionId sessionId = SessionId.from(UlidCreator.getUlid().toString());
        Session s = Session.builder()
                .id(sessionId)
                .userId(userId)
                .jti(UUID.randomUUID().toString())
                .deviceId(deviceId)
                .userAgent(userAgent)
                .ip(ip)
                .build();
        s = sessionRepo.save(s);
        log.debug("Session created - sessionId: {}, userId: {}", s.getId().asString(), userId.asString());

        Map.Entry<String, RefreshToken> rt = mintRefreshForSession(s.getId());
        log.debug("Refresh token minted - sessionId: {}, tokenId: {}", s.getId().asString(), rt.getValue().getId().asString());
        
        // Publish domain event
        SessionCreated event = new SessionCreated(s.getId().asString(), userId.asString(), deviceId, userAgent, ip);
        eventPublisher.publishSessionCreated(event);
        
        log.info("Session with refresh token created successfully - sessionId: {}, userId: {}", 
                s.getId().asString(), userId.asString());
        
        return new SessionWithRefresh(s, rt.getKey(), rt.getValue());
    }

    @Transactional
    public RotationResult rotateRefreshOrThrow(String rawRefreshToken) {
        log.debug("Rotating refresh token");
        
        RefreshToken token = findByRawOrThrow(rawRefreshToken);
        log.debug("Refresh token found - tokenId: {}, sessionId: {}", token.getId().asString(), token.getSessionId().asString());

        if (token.getRevokedAt() != null || token.getReplacedByTokenId() != null || token.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Refresh token reuse or expired detected - tokenId: {}, revokedAt: {}, replacedBy: {}, expiresAt: {}", 
                    token.getId().asString(), token.getRevokedAt(), token.getReplacedByTokenId(), token.getExpiresAt());
            
            Session s = sessionRepo.findById(token.getSessionId()).orElseThrow();
            RefreshTokenReuseDetected event = new RefreshTokenReuseDetected(
                    token.getSessionId().asString(), s.getUserId().asString(), token.getId().asString(), "refresh_reuse_or_expired");
            eventPublisher.publishRefreshTokenReuseDetected(event);
            revokeWholeSessionChain(token.getSessionId(), "refresh_reuse_or_expired");
            throw new SecurityException("Refresh token reuse/expired");
        }

        Map.Entry<String, RefreshToken> newRt = mintRefreshForSession(token.getSessionId());
        log.debug("New refresh token minted - oldTokenId: {}, newTokenId: {}", token.getId().asString(), newRt.getValue().getId().asString());
        
        token = token.replaceWith(newRt.getValue().getId());
        refreshRepo.save(token);

        Session s = sessionRepo.findById(token.getSessionId()).orElseThrow();
        s = Session.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .jti(UUID.randomUUID().toString())
                .deviceId(s.getDeviceId())
                .userAgent(s.getUserAgent())
                .ip(s.getIp())
                .revokedAt(s.getRevokedAt())
                .revokeReason(s.getRevokeReason())
                .build();
        s = sessionRepo.save(s);
        log.debug("Session updated with new JTI - sessionId: {}", s.getId().asString());

        // Publish domain event
        RefreshTokenRotated event = new RefreshTokenRotated(
                s.getId().asString(), s.getUserId().asString(), token.getId().asString(), newRt.getValue().getId().asString());
        eventPublisher.publishRefreshTokenRotated(event);
        
        log.info("Refresh token rotated successfully - sessionId: {}, userId: {}, oldTokenId: {}, newTokenId: {}", 
                s.getId().asString(), s.getUserId().asString(), token.getId().asString(), newRt.getValue().getId().asString());
        
        return new RotationResult(s, newRt.getKey(), newRt.getValue());
    }

    @Transactional
    public void revokeByRawRefreshToken(String rawRefreshToken) {
        log.debug("Revoking session by refresh token");
        
        RefreshToken token = findByRawOrThrow(rawRefreshToken);
        log.debug("Refresh token found for revocation - tokenId: {}, sessionId: {}", 
                token.getId().asString(), token.getSessionId().asString());
        
        revokeWholeSessionChain(token.getSessionId(), "logout");
        log.info("Session revoked successfully - sessionId: {}", token.getSessionId().asString());
    }

    private RefreshToken findByRawOrThrow(String rawRefreshToken) {
        byte[] hash = Hashing.sha256(rawRefreshToken.getBytes(StandardCharsets.UTF_8));
        return refreshRepo.findByTokenHash(hash).orElseThrow(() -> {
            log.warn("Refresh token not found - hash: {}", java.util.Base64.getEncoder().encodeToString(hash));
            return new SecurityException("Unknown refresh token");
        });
    }

    private void revokeWholeSessionChain(SessionId sessionId, String reason) {
        log.debug("Revoking session chain - sessionId: {}, reason: {}", sessionId.asString(), reason);
        
        Session s = sessionRepo.findById(sessionId).orElseThrow();
        if (s.getRevokedAt() == null) {
            s = s.revoke(reason);
            s = sessionRepo.save(s);
            log.debug("Session revoked - sessionId: {}", s.getId().asString());
        }
        
        List<RefreshToken> tokens = refreshRepo.findBySessionId(sessionId);
        log.debug("Found {} refresh tokens to revoke for session - sessionId: {}", tokens.size(), sessionId.asString());
        
        for (RefreshToken rt : tokens) {
            if (rt.getRevokedAt() == null) {
                rt = rt.revoke("chain_revoked:" + reason);
                refreshRepo.save(rt);
                log.debug("Refresh token revoked - tokenId: {}", rt.getId().asString());
            }
        }
        
        // Publish domain event
        SessionRevoked event = new SessionRevoked(s.getId().asString(), s.getUserId().asString(), reason);
        eventPublisher.publishSessionRevoked(event);
        
        log.info("Session chain revoked successfully - sessionId: {}, userId: {}, reason: {}", 
                s.getId().asString(), s.getUserId().asString(), reason);
    }

    private Map.Entry<String, RefreshToken> mintRefreshForSession(SessionId sessionId) {
        log.debug("Minting refresh token for session - sessionId: {}", sessionId.asString());
        
        RefreshTokenId tokenId = RefreshTokenId.from(UlidCreator.getUlid().toString());
        String secret = UUID.randomUUID().toString().replace("-", "");
        String raw = tokenId.asString() + "." + secret;
        RefreshToken rt = RefreshToken.builder()
                .id(tokenId)
                .sessionId(sessionId)
                .tokenHash(Hashing.sha256(raw.getBytes(StandardCharsets.UTF_8)))
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshTtlSeconds))
                .revokedAt(null)
                .revokeReason(null)
                .replacedByTokenId(null)
                .build();
        rt = refreshRepo.save(rt);
        
        log.debug("Refresh token minted - tokenId: {}, sessionId: {}, expiresAt: {}", 
                rt.getId().asString(), sessionId.asString(), rt.getExpiresAt());
        
        return Map.entry(raw, rt);
    }
}
