package com.youtube.mvp.search.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain event representing an index update.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexUpdateEvent {
    private String videoId;
    private String eventType; // PUBLISHED, UPDATED, DELETED
    private Long timestamp;
    private SearchDocument document;
    
    public static IndexUpdateEvent published(String videoId, SearchDocument document) {
        return IndexUpdateEvent.builder()
                .videoId(videoId)
                .eventType("PUBLISHED")
                .timestamp(System.currentTimeMillis())
                .document(document)
                .build();
    }
    
    public static IndexUpdateEvent updated(String videoId, SearchDocument document) {
        return IndexUpdateEvent.builder()
                .videoId(videoId)
                .eventType("UPDATED")
                .timestamp(System.currentTimeMillis())
                .document(document)
                .build();
    }
    
    public static IndexUpdateEvent deleted(String videoId) {
        return IndexUpdateEvent.builder()
                .videoId(videoId)
                .eventType("DELETED")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
