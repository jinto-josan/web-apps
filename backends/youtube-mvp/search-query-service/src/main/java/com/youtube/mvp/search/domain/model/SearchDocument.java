package com.youtube.mvp.search.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing a document in the search index.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDocument {
    private String documentId;
    private String videoId;
    private String title;
    private String description;
    private String channelName;
    private String channelId;
    private String category;
    private String tags;
    private Long viewCount;
    private Long likeCount;
    private Long duration;
    private String thumbnailUrl;
    private Long publishedAt;
    private String language;
    private Integer quality;
    private Double relevanceScore;
    
    public String getDocumentId() {
        return documentId != null ? documentId : videoId;
    }
}
