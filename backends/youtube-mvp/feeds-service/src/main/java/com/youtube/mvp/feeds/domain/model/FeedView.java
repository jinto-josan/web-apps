package com.youtube.mvp.feeds.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class FeedView {
    String id;
    String videoId;
    String userId;
    FeedType feedType;
    Instant viewedAt;
    Integer position;
    String videoCategory;
    Long videoViewCount;
    String videoChannelId;
}

