package com.youtube.identityauthservice.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain entity representing a user in the system.
 * This is a pure domain entity without JPA annotations.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private String id;
    private String email;
    private String normalizedEmail;
    private String displayName;
    private short status;
    private boolean emailVerified;
    private String passwordHash;
    private Short passwordAlg;
    private boolean mfaEnabled;
    private String termsVersion;
    private Instant termsAcceptedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer version;
}

