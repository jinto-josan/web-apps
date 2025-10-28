package com.youtube.mvp.search.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IndexUpdateEventTest {
    
    @Test
    void published_ShouldCreatePublishedEvent() {
        // Given
        SearchDocument document = SearchDocument.builder()
                .videoId("video-1")
                .title("Test")
                .build();
        
        // When
        IndexUpdateEvent event = IndexUpdateEvent.published("video-1", document);
        
        // Then
        assertThat(event.getEventType()).isEqualTo("PUBLISHED");
        assertThat(event.getVideoId()).isEqualTo("video-1");
        assertThat(event.getDocument()).isEqualTo(document);
        assertThat(event.getTimestamp()).isNotNull();
    }
    
    @Test
    void updated_ShouldCreateUpdatedEvent() {
        // Given
        SearchDocument document = SearchDocument.builder()
                .videoId("video-1")
                .title("Test")
                .build();
        
        // When
        IndexUpdateEvent event = IndexUpdateEvent.updated("video-1", document);
        
        // Then
        assertThat(event.getEventType()).isEqualTo("UPDATED");
        assertThat(event.getVideoId()).isEqualTo("video-1");
    }
    
    @Test
    void deleted_ShouldCreateDeletedEvent() {
        // When
        IndexUpdateEvent event = IndexUpdateEvent.deleted("video-1");
        
        // Then
        assertThat(event.getEventType()).isEqualTo("DELETED");
        assertThat(event.getVideoId()).isEqualTo("video-1");
        assertThat(event.getDocument()).isNull();
    }
}
