package com.youtube.common.domain.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.youtube.common.domain.core.DomainEvent;
import com.youtube.common.domain.shared.valueobjects.ChannelId;
import com.youtube.common.domain.shared.valueobjects.VideoId;

import java.time.Instant;

/**
 * Domain event published when a video is published and becomes available for viewing.
 * 
 * <p>Published by: video-catalog-service
 * <p>Consumed by:
 * <ul>
 *   <li>search-indexer-service - Index video for search</li>
 *   <li>recommendations-service - Add to recommendation pool</li>
 *   <li>notifications-service - Notify subscribers</li>
 *   <li>analytics-telemetry-service - Initialize tracking</li>
 * </ul>
 */
public final class VideoPublishedEvent extends DomainEvent {
    
    private final VideoId videoId;
    private final ChannelId channelId;
    private final String title;
    private final String description;

    @JsonCreator
    public VideoPublishedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("videoId") VideoId videoId,
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description) {
        super(eventId, occurredAt);
        this.videoId = videoId;
        this.channelId = channelId;
        this.title = title;
        this.description = description;
    }

    public VideoPublishedEvent(VideoId videoId, ChannelId channelId, String title, String description) {
        this.videoId = videoId;
        this.channelId = channelId;
        this.title = title;
        this.description = description;
    }

    public VideoId getVideoId() {
        return videoId;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getEventType() {
        return "video.published";
    }
}

