package com.youtube.mvp.feeds.application.mapper;

import com.youtube.mvp.feeds.application.dto.FeedDto;
import com.youtube.mvp.feeds.application.dto.FeedItemDto;
import com.youtube.mvp.feeds.application.dto.FeedViewDto;
import com.youtube.mvp.feeds.domain.model.Feed;
import com.youtube.mvp.feeds.domain.model.FeedItem;
import com.youtube.mvp.feeds.domain.model.FeedView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeedMapper {
    
    @Mapping(target = "items", source = "items")
    @Mapping(target = "lastUpdated", source = "lastUpdated")
    @Mapping(target = "etag", source = "etag")
    @Mapping(target = "totalCount", source = "totalCount")
    @Mapping(target = "pageSize", source = "pageSize")
    @Mapping(target = "nextPageToken", source = "nextPageToken")
    FeedDto toDto(Feed feed);
    
    FeedItemDto toDto(FeedItem feedItem);
    
    List<FeedItemDto> toDto(List<FeedItem> feedItems);
    
    FeedViewDto toDto(FeedView feedView);
    
    List<FeedViewDto> toDto(List<FeedView> feedViews);
}

