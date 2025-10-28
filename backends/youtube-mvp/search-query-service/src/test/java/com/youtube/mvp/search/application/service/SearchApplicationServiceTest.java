package com.youtube.mvp.search.application.service;

import com.youtube.mvp.search.application.dto.SearchRequest;
import com.youtube.mvp.search.application.dto.SearchResponse;
import com.youtube.mvp.search.application.dto.SuggestionRequest;
import com.youtube.mvp.search.application.dto.SuggestionResponse;
import com.youtube.mvp.search.application.mapper.SearchMapper;
import com.youtube.mvp.search.domain.model.SearchDocument;
import com.youtube.mvp.search.domain.model.SearchFilter;
import com.youtube.mvp.search.domain.model.SearchResult;
import com.youtube.mvp.search.domain.model.Suggestion;
import com.youtube.mvp.search.domain.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchApplicationServiceTest {
    
    @Mock
    private SearchService searchService;
    
    @Mock
    private SearchMapper searchMapper;
    
    @InjectMocks
    private SearchApplicationService service;
    
    @Test
    void search_ShouldReturnSearchResponse() {
        // Given
        SearchRequest request = SearchRequest.builder()
                .query("java tutorial")
                .page(1)
                .pageSize(20)
                .build();
        
        SearchResult searchResult = SearchResult.builder()
                .documents(Arrays.asList(createTestDocument()))
                .totalCount(100L)
                .page(1)
                .pageSize(20)
                .hasMore(true)
                .build();
        
        SearchResponse expectedResponse = SearchResponse.builder()
                .documents(searchResult.getDocuments())
                .totalCount(searchResult.getTotalCount())
                .page(searchResult.getPage())
                .pageSize(searchResult.getPageSize())
                .hasMore(searchResult.getHasMore())
                .build();
        
        when(searchMapper.toFilter(request)).thenReturn(new SearchFilter());
        when(searchService.search(anyString(), any(SearchFilter.class), anyInt(), anyInt(), anyString()))
                .thenReturn(searchResult);
        when(searchMapper.toResponse(searchResult)).thenReturn(expectedResponse);
        
        // When
        SearchResponse response = service.search(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(100L);
        verify(searchService).search(request.getQuery(), any(), anyInt(), anyInt(), anyString());
    }
    
    @Test
    void suggest_ShouldReturnSuggestions() {
        // Given
        SuggestionRequest request = SuggestionRequest.builder()
                .prefix("java")
                .maxResults(10)
                .build();
        
        List<Suggestion> suggestions = Arrays.asList(
                new Suggestion("java tutorial", "programming", 100)
        );
        
        SuggestionResponse expectedResponse = SuggestionResponse.builder()
                .suggestions(Arrays.asList(
                        SuggestionResponse.SuggestionItem.builder()
                                .text("java tutorial")
                                .category("programming")
                                .score(100)
                                .build()
                ))
                .build();
        
        when(searchService.suggest(anyString(), anyInt())).thenReturn(suggestions);
        when(searchMapper.toSuggestionResponse(suggestions)).thenReturn(expectedResponse);
        
        // When
        SuggestionResponse response = service.suggest(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSuggestions()).hasSize(1);
        verify(searchService).suggest(request.getPrefix(), request.getMaxResults());
    }
    
    @Test
    void handleIndexUpdate_ShouldCallService() {
        // Given
        SearchDocument document = createTestDocument();
        String eventType = "PUBLISHED";
        
        // When
        service.handleIndexUpdate(document, eventType);
        
        // Then
        verify(searchService).upsertDocument(document);
    }
    
    @Test
    void handleIndexUpdate_DeletedEvent_ShouldCallDelete() {
        // Given
        SearchDocument document = createTestDocument();
        String eventType = "DELETED";
        
        // When
        service.handleIndexUpdate(document, eventType);
        
        // Then
        verify(searchService).deleteDocument(document.getVideoId());
    }
    
    @Test
    void rebuildIndex_ShouldCallService() {
        // When
        service.rebuildIndex();
        
        // Then
        verify(searchService).rebuildIndex();
    }
    
    private SearchDocument createTestDocument() {
        return SearchDocument.builder()
                .documentId("doc-1")
                .videoId("video-1")
                .title("Java Tutorial")
                .description("Learn Java programming")
                .channelName("Tech Channel")
                .build();
    }
}
