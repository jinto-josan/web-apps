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
@Table(name = "users",
        indexes = {
                @Index(name = "ix_users_status", columnList = "status"),
                @Index(name = "ix_users_updated_at", columnList = "updated_at"),
                @Index(name = "ix_users_service_principal_id", columnList = "service_principal_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_users_normalized_email", columnNames = "normalized_email"),
                @UniqueConstraint(name = "ux_users_service_principal_id", columnNames = "service_principal_id")
        }
)
@Getter
@Setter
public class UserEntity {

    @Id
    @Column(length = 26, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private com.youtube.identityauthservice.domain.entities.UserType userType = com.youtube.identityauthservice.domain.entities.UserType.USER;

    @Column(length = 320)
    private String email;

    @Column(name = "normalized_email", length = 320)
    private String normalizedEmail;

    @Column(name = "service_principal_id", length = 255)
    private String servicePrincipalId;

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
                .userType(userType)
                .email(email)
                .normalizedEmail(normalizedEmail)
                .servicePrincipalId(servicePrincipalId)
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
        entity.setUserType(user.getUserType());
        entity.setEmail(user.getEmail());
        entity.setNormalizedEmail(user.getNormalizedEmail());
        entity.setServicePrincipalId(user.getServicePrincipalId());
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

