package com.youtube.identityauthservice.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain entity representing a user session.
 * This is a pure domain entity without JPA annotations.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    
    private String id;
    private String userId;
    private String jti;
    private String deviceId;
    private String userAgent;
    private String ip;
    private Instant revokedAt;
    private String revokeReason;
}

