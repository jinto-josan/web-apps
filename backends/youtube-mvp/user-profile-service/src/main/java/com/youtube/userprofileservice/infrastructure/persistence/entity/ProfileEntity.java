package com.youtube.userprofileservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * JPA entity for account profiles.
 */
@Entity
@Table(name = "account_profiles", indexes = {
    @Index(name = "idx_account_profiles_account_id", columnList = "account_id")
})
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProfileEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "account_id", unique = true, nullable = false, length = 26)
    private String accountId; // ULID
    
    @Size(max = 100)
    @Column(name = "display_name", length = 100)
    private String displayName;
    
    @Size(max = 500)
    @Column(name = "photo_url", length = 500)
    private String photoUrl;
    
    @Size(max = 10)
    @Column(name = "locale", length = 10)
    private String locale;
    
    @Size(max = 5)
    @Column(name = "country", length = 5)
    private String country;
    
    @Size(max = 100)
    @Column(name = "timezone", length = 100)
    private String timezone;
    
    @Embedded
    private PrivacySettingsEmbeddable privacySettings;
    
    @Embedded
    private NotificationSettingsEmbeddable notificationSettings;
    
    @Embedded
    private AccessibilityPreferencesEmbeddable accessibilityPreferences;
    
    @NotNull
    @Column(name = "version", nullable = false)
    private Integer version;
    
    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "updated_by", length = 26)
    private String updatedBy;
    
    @Column(name = "etag")
    private String etag;
}

