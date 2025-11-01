package com.youtube.identityauthservice.infrastructure.persistence.entity;

import com.youtube.identityauthservice.domain.entities.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity for User.
 * Maps domain entity to database table.
 */
@Entity
@Table(name = "users", schema = "auth",
        indexes = {
                @Index(name = "ix_auth_users_status", columnList = "status"),
                @Index(name = "ix_auth_users_updated_at", columnList = "updated_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_auth_users_normalized_email", columnNames = "normalized_email")
        }
)
@Getter
@Setter
public class UserEntity {

    @Id
    @Column(length = 26, nullable = false)
    private String id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "normalized_email", nullable = false, length = 320)
    private String normalizedEmail;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(nullable = false)
    private short status = 1;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "password_alg")
    private Short passwordAlg;

    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled;

    @Column(name = "terms_version")
    private String termsVersion;

    @Column(name = "terms_accepted_at")
    private Instant termsAcceptedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
    
    /**
     * Converts JPA entity to domain entity.
     */
    public User toDomain() {
        return User.builder()
                .id(com.youtube.common.domain.shared.valueobjects.UserId.from(id))
                .email(email)
                .normalizedEmail(normalizedEmail)
                .displayName(displayName)
                .status(status)
                .emailVerified(emailVerified)
                .passwordHash(passwordHash)
                .passwordAlg(passwordAlg)
                .mfaEnabled(mfaEnabled)
                .termsVersion(termsVersion)
                .termsAcceptedAt(termsAcceptedAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .version(version != null ? version.longValue() : 0L)
                .build();
    }
    
    /**
     * Creates JPA entity from domain entity.
     */
    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId().asString());
        entity.setEmail(user.getEmail());
        entity.setNormalizedEmail(user.getNormalizedEmail());
        entity.setDisplayName(user.getDisplayName());
        entity.setStatus(user.getStatus());
        entity.setEmailVerified(user.isEmailVerified());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setPasswordAlg(user.getPasswordAlg());
        entity.setMfaEnabled(user.isMfaEnabled());
        entity.setTermsVersion(user.getTermsVersion());
        entity.setTermsAcceptedAt(user.getTermsAcceptedAt());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setVersion((int) user.getVersion());
        return entity;
    }
}

