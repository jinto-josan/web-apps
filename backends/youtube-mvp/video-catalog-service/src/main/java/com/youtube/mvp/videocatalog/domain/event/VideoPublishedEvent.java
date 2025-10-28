package com.youtube.mvp.videocatalog.domain.event;

import lombok.*;
import java.time.Instant;

/**
 * Domain event raised when a video is published.
 */
@Getter
@Builder
@ToString
public class VideoPublishedEvent {
    private final String videoId;
    private final String channelId;
    private final String ownerId;
    private final String title;
    private final String description;
    private final String category;
    private final String visibility;
    private final Instant publishedAt;
    private final Instant occurredAt;
    
    public VideoPublishedEvent(
            String videoId,
            String channelId,
            String ownerId,
            String title,
            String description,
            String category,
            String visibility,
            Instant publishedAt,
            Instant occurredAt) {
        this.videoId = videoId;
        this.channelId = channelId;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.visibility = visibility;
        this.publishedAt = publishedAt;
        this.occurredAt = occurredAt;
    }
}

