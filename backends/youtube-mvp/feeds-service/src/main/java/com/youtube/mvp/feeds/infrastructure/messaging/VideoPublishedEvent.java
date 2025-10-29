package com.youtube.mvp.feeds.infrastructure.messaging;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class VideoPublishedEvent {
    String videoId;
    String channelId;
    String title;
    Instant publishedAt;
    String category;
}

