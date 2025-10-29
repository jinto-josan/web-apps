package com.youtube.mvp.feeds.infrastructure.repository.cosmos;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.youtube.mvp.feeds.domain.model.Feed;
import com.youtube.mvp.feeds.domain.model.FeedType;
import com.youtube.mvp.feeds.domain.model.FeedView;
import com.youtube.mvp.feeds.domain.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FeedCosmosRepository implements FeedRepository {
    
    private final CosmosTemplate cosmosTemplate;
    private static final String CONTAINER_NAME = "feeds";
    private static final String FEED_VIEWS_CONTAINER = "feed-views";
    
    @Override
    public Optional<Feed> findByUserIdAndFeedType(String userId, FeedType feedType) {
        try {
            String partitionKey = userId;
            String key = userId + ":" + feedType.name();
            
            FeedDocument feedDoc = cosmosTemplate.findById(key, FeedDocument.class, 
                    com.azure.cosmos.models.PartitionKey.of(partitionKey));
            
            if (feedDoc == null) {
                return Optional.empty();
            }
            
            return Optional.of(mapToDomain(feedDoc));
        } catch (Exception e) {
            log.error("Error finding feed for userId={}, feedType={}", userId, feedType, e);
            return Optional.empty();
        }
    }
    
    @Override
    public void save(Feed feed) {
        try {
            FeedDocument feedDoc = mapToDocument(feed);
            cosmosTemplate.insert(feedDoc);
        } catch (Exception e) {
            log.error("Error saving feed for userId={}, feedType={}", 
                    feed.getUserId(), feed.getFeedType(), e);
            throw new RuntimeException("Failed to save feed", e);
        }
    }
    
    @Override
    public void saveFeedView(FeedView feedView) {
        try {
            FeedViewDocument viewDoc = mapViewToDocument(feedView);
            cosmosTemplate.insert(viewDoc);
        } catch (Exception e) {
            log.error("Error saving feed view for videoId={}, userId={}", 
                    feedView.getVideoId(), feedView.getUserId(), e);
        }
    }
    
    @Override
    public List<FeedView> findRecentViewsByUserId(String userId, int limit) {
        try {
            Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "userId", new Object[]{userId});
            CosmosQuery query = new CosmosQuery(criteria);
            query.setLimit(limit);
            
            List<FeedViewDocument> docs = cosmosTemplate.find(query, FeedViewDocument.class, FEED_VIEWS_CONTAINER);
            return docs.stream().map(this::mapViewToDomain).toList();
        } catch (Exception e) {
            log.error("Error finding feed views for userId={}", userId, e);
            return List.of();
        }
    }
    
    @Override
    public void deleteByUserIdAndFeedType(String userId, FeedType feedType) {
        try {
            String key = userId + ":" + feedType.name();
            cosmosTemplate.deleteById(CONTAINER_NAME, key, 
                    com.azure.cosmos.models.PartitionKey.of(userId));
        } catch (Exception e) {
            log.error("Error deleting feed for userId={}, feedType={}", userId, feedType, e);
        }
    }
    
    @Override
    public void deleteAllByUserId(String userId) {
        // Delete all feed types for user
        for (FeedType type : FeedType.values()) {
            deleteByUserIdAndFeedType(userId, type);
        }
    }
    
    private Feed mapToDomain(FeedDocument doc) {
        return Feed.builder()
                .userId(doc.getUserId())
                .feedType(FeedType.valueOf(doc.getFeedType()))
                .items(mapItemsFromJson(doc))
                .lastUpdated(doc.getLastUpdated())
                .etag(doc.getEtag())
                .totalCount(doc.getTotalCount())
                .pageSize(doc.getPageSize())
                .nextPageToken(doc.getNextPageToken())
                .build();
    }
    
    private FeedDocument mapToDocument(Feed feed) {
        return FeedDocument.builder()
                .id(feed.getUserId() + ":" + feed.getFeedType().name())
                .userId(feed.getUserId())
                .feedType(feed.getFeedType().name())
                .itemsJson(serializeItems(feed.getItems()))
                .lastUpdated(feed.getLastUpdated())
                .etag(feed.getEtag())
                .totalCount(feed.getTotalCount())
                .pageSize(feed.getPageSize())
                .nextPageToken(feed.getNextPageToken())
                .build();
    }
    
    private List<com.youtube.mvp.feeds.domain.model.FeedItem> mapItemsFromJson(FeedDocument doc) {
        // In production, use Jackson to deserialize
        // Simplified for this example
        return doc.getItems() != null ? doc.getItems() : List.of();
    }
    
    private String serializeItems(List<com.youtube.mvp.feeds.domain.model.FeedItem> items) {
        // In production, use Jackson to serialize
        return ""; // Simplified
    }
    
    private FeedView mapViewToDomain(FeedViewDocument doc) {
        return FeedView.builder()
                .id(doc.getId())
                .videoId(doc.getVideoId())
                .userId(doc.getUserId())
                .feedType(com.youtube.mvp.feeds.domain.model.FeedType.valueOf(doc.getFeedType()))
                .viewedAt(doc.getViewedAt())
                .position(doc.getPosition())
                .videoCategory(doc.getVideoCategory())
                .videoViewCount(doc.getVideoViewCount())
                .videoChannelId(doc.getVideoChannelId())
                .build();
    }
    
    private FeedViewDocument mapViewToDocument(FeedView view) {
        return FeedViewDocument.builder()
                .id(view.getId())
                .videoId(view.getVideoId())
                .userId(view.getUserId())
                .feedType(view.getFeedType().name())
                .viewedAt(view.getViewedAt())
                .position(view.getPosition())
                .videoCategory(view.getVideoCategory())
                .videoViewCount(view.getVideoViewCount())
                .videoChannelId(view.getVideoChannelId())
                .build();
    }
}

