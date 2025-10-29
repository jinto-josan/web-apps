package com.youtube.mvp.feeds.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class Feed {
    String userId;
    FeedType feedType;
    List<FeedItem> items;
    Instant lastUpdated;
    String etag;
    Long totalCount;
    Integer pageSize;
    String nextPageToken;
}

