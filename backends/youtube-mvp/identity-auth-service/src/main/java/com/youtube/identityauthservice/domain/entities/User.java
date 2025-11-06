package com.youtube.identityauthservice.domain.entities;

import com.youtube.common.domain.core.AggregateRoot;
import com.youtube.common.domain.shared.valueobjects.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Domain aggregate root representing a user in the system.
 * Extends AggregateRoot for event sourcing and optimistic concurrency control.
 */
@Getter
public class User extends AggregateRoot<UserId> {
    
    private UserType userType;
    private String email;
    private String normalizedEmail;
    private String servicePrincipalId;
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
    
    protected User() {
        // For JPA and framework deserialization
        super();
    }
    
    @Builder
    protected User(
            UserId id,
            UserType userType,
            String email,
            String normalizedEmail,
            String servicePrincipalId,
            String displayName,
            short status,
            boolean emailVerified,
            String passwordHash,
            Short passwordAlg,
            boolean mfaEnabled,
            String termsVersion,
            Instant termsAcceptedAt,
            Instant createdAt,
            Instant updatedAt,
            long version) {
        super(id, version);
        this.userType = userType != null ? userType : UserType.USER;
        this.email = email;
        this.normalizedEmail = normalizedEmail;
        this.servicePrincipalId = servicePrincipalId;
        this.displayName = displayName;
        this.status = status;
        this.emailVerified = emailVerified;
        this.passwordHash = passwordHash;
        this.passwordAlg = passwordAlg;
        this.mfaEnabled = mfaEnabled;
        this.termsVersion = termsVersion;
        this.termsAcceptedAt = termsAcceptedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public User withEmail(String email) {
        return new User(
            getId(), userType, email, normalizedEmail, servicePrincipalId, displayName, status,
            emailVerified, passwordHash, passwordAlg, mfaEnabled,
            termsVersion, termsAcceptedAt, createdAt, Instant.now(), getVersion()
        );
    }
    
    public User withDisplayName(String displayName) {
        return new User(
            getId(), userType, email, normalizedEmail, servicePrincipalId, displayName, status,
            emailVerified, passwordHash, passwordAlg, mfaEnabled,
            termsVersion, termsAcceptedAt, createdAt, Instant.now(), getVersion()
        );
    }
    
    public User withEmailVerified(boolean verified) {
        return new User(
            getId(), userType, email, normalizedEmail, servicePrincipalId, displayName, status,
            verified, passwordHash, passwordAlg, mfaEnabled,
            termsVersion, termsAcceptedAt, createdAt, Instant.now(), getVersion()
        );
    }
    
    public User markUpdated() {
        incrementVersion();
        return new User(
            getId(), userType, email, normalizedEmail, servicePrincipalId, displayName, status,
            emailVerified, passwordHash, passwordAlg, mfaEnabled,
            termsVersion, termsAcceptedAt, createdAt, Instant.now(), getVersion()
        );
    }
}

