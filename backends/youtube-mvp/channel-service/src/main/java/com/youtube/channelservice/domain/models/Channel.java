package com.youtube.channelservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * Represents a YouTube channel aggregate.
 * Immutable domain model with validation constraints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Channel {
    
    @NotBlank(message = "Channel ID cannot be blank")
    private String id; // ULID
    
    @NotBlank(message = "Owner user ID cannot be blank")
    private String ownerUserId;
    
    @NotBlank(message = "Handle cannot be blank")
    @Pattern(regexp = "^[a-z0-9._-]{3,30}$", message = "Handle must be 3-30 characters, lowercase letters, numbers, dots, underscores, or hyphens")
    private String handleLower;
    
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;
    
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
    
    @Size(max = 10, message = "Language code cannot exceed 10 characters")
    private String language;
    
    @Size(max = 10, message = "Country code cannot exceed 10 characters")
    private String country;
    
    private Branding branding;
    private Policy policy;
    
    @Builder.Default
    private int version = 1;
    
    @NotNull(message = "Created at timestamp cannot be null")
    private Instant createdAt;
    
    @NotNull(message = "Updated at timestamp cannot be null")
    private Instant updatedAt;
    
    private String etag; // For optimistic concurrency control
    
    /**
     * Creates a new channel with updated handle information.
     */
    public Channel withHandle(String newHandleLower, int newVersion, Instant updatedAt, String newEtag) {
        return this.toBuilder()
                .handleLower(newHandleLower)
                .version(newVersion)
                .updatedAt(updatedAt)
                .etag(newEtag)
                .build();
    }
    
    /**
     * Creates a new channel with updated branding information.
     */
    public Channel withBranding(Branding branding, int newVersion, Instant updatedAt, String newEtag) {
        return this.toBuilder()
                .branding(branding)
                .version(newVersion)
                .updatedAt(updatedAt)
                .etag(newEtag)
                .build();
    }
}