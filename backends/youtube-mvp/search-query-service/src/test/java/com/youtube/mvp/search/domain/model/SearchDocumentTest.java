package com.youtube.mvp.search.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchDocumentTest {
    
    @Test
    void getDocumentId_WithDocumentId_ShouldReturnDocumentId() {
        // Given
        SearchDocument document = SearchDocument.builder()
                .documentId("doc-1")
                .videoId("video-1")
                .build();
        
        // When
        String documentId = document.getDocumentId();
        
        // Then
        assertThat(documentId).isEqualTo("doc-1");
    }
    
    @Test
    void getDocumentId_WithoutDocumentId_ShouldReturnVideoId() {
        // Given
        SearchDocument document = SearchDocument.builder()
                .videoId("video-1")
                .build();
        
        // When
        String documentId = document.getDocumentId();
        
        // Then
        assertThat(documentId).isEqualTo("video-1");
    }
}
