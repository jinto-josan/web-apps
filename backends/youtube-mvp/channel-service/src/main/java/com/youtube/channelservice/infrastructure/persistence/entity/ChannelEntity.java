package com.youtube.channelservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * JPA entity for Channel aggregate.
 * Maps to the channels table in the database.
 */
@Entity
@Table(name = "channels", 
       indexes = {
           @Index(name = "idx_channels_handle", columnList = "handleLower"),
           @Index(name = "idx_channels_owner", columnList = "ownerUserId"),
           @Index(name = "idx_channels_created", columnList = "createdAt")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelEntity {
    
    @Id
    @Column(name = "id", length = 26)
    @NotBlank(message = "Channel ID cannot be blank")
    private String id; // ULID
    
    @Column(name = "owner_user_id", nullable = false, length = 26)
    @NotBlank(message = "Owner user ID cannot be blank")
    private String ownerUserId;
    
    @Column(name = "handle_lower", nullable = false, unique = true, length = 30)
    @NotBlank(message = "Handle cannot be blank")
    @Pattern(regexp = "^[a-z0-9._-]{3,30}$", message = "Handle must be 3-30 characters, lowercase letters, numbers, dots, underscores, or hyphens")
    private String handleLower;
    
    @Column(name = "title", length = 100)
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;
    
    @Column(name = "description", length = 5000)
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
    
    @Column(name = "language", length = 10)
    @Size(max = 10, message = "Language code cannot exceed 10 characters")
    private String language;
    
    @Column(name = "country", length = 10)
    @Size(max = 10, message = "Country code cannot exceed 10 characters")
    private String country;
    
    @Embedded
    private BrandingEmbeddable branding;
    
    @Embedded
    private PolicyEmbeddable policy;
    
    @Column(name = "version", nullable = false)
    @Builder.Default
    private int version = 1;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Version
    @Column(name = "etag")
    private String etag; // For optimistic concurrency control
    
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
