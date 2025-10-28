package com.youtube.mvp.videocatalog.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.*;

/**
 * Video aggregate root entity.
 * Manages video metadata, state transitions, and localization.
 */
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Video {
    
    @EqualsAndHashCode.Include
    private final String videoId;
    
    private String title;
    private String description;
    private String channelId;
    private String ownerId;
    
    private VideoState state;
    private VideoVisibility visibility;
    
    private List<LocalizedText> titles;
    private List<LocalizedText> descriptions;
    private List<String> tags;
    private String category;
    private String language;
    private String thumbnailUrl;
    private String contentUrl;
    private Duration duration;
    
    @Builder.Default
    private long viewCount = 0;
    @Builder.Default
    private long likeCount = 0;
    @Builder.Default
    private long commentCount = 0;
    
    private String version;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant publishedAt;
    
    // DDD: Business logic methods
    
    /**
     * Transitions video to published state.
     * @throws IllegalStateException if transition is not allowed
     */
    public void publish() {
        if (state == VideoState.PUBLISHED) {
            throw new IllegalStateException("Video is already published");
        }
        if (state != VideoState.DRAFT) {
            throw new IllegalStateException("Only draft videos can be published. Current state: " + state);
        }
        if (visibility == null) {
            throw new IllegalStateException("Visibility must be set before publishing");
        }
        
        this.state = VideoState.PUBLISHED;
        this.publishedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    /**
     * Updates video metadata.
     */
    public void updateMetadata(String title, String description, List<String> tags, String category) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (tags != null) {
            this.tags = tags;
        }
        if (category != null) {
            this.category = category;
        }
        this.updatedAt = Instant.now();
    }
    
    /**
     * Adds localized content.
     */
    public void addLocalizedTitle(LocalizedText localizedTitle) {
        if (titles == null) {
            titles = new ArrayList<>();
        }
        titles.removeIf(lt -> lt.getLanguage().equals(localizedTitle.getLanguage()));
        titles.add(localizedTitle);
    }
    
    /**
     * Adds localized description.
     */
    public void addLocalizedDescription(LocalizedText localizedDescription) {
        if (descriptions == null) {
            descriptions = new ArrayList<>();
        }
        descriptions.removeIf(ld -> ld.getLanguage().equals(localizedDescription.getLanguage()));
        descriptions.add(localizedDescription);
    }
    
    /**
     * Checks if video is viewable by user.
     */
    public boolean isViewableBy(String userId) {
        if (ownerId.equals(userId)) {
            return true; // Owner can always view
        }
        
        switch (visibility) {
            case PUBLIC:
                return state == VideoState.PUBLISHED;
            case UNLISTED:
                return state == VideoState.PUBLISHED;
            case PRIVATE:
                return false;
            default:
                return false;
        }
    }
    
    /**
     * Increments view count.
     */
    public void incrementViewCount() {
        this.viewCount++;
        this.updatedAt = Instant.now();
    }
}

