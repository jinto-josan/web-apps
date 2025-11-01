package com.youtube.identityauthservice.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain entity representing a refresh token.
 * This is a pure domain entity without JPA annotations.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    
    private String id;
    private String sessionId;
    private byte[] tokenHash;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant revokedAt;
    private String revokeReason;
    private String replacedByTokenId;
}

