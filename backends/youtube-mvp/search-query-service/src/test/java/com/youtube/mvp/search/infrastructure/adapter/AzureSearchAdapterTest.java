package com.youtube.mvp.search.infrastructure.adapter;

import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResults;
import com.azure.search.documents.models.SuggestResult;
import com.youtube.mvp.search.domain.model.SearchDocument;
import com.youtube.mvp.search.domain.model.SearchFilter;
import com.youtube.mvp.search.infrastructure.client.AzureSearchClient;
import com.youtube.mvp.search.infrastructure.repository.CosmosVideoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureSearchAdapterTest {
    
    @Mock
    private AzureSearchClient azureSearchClient;
    
    @Mock
    private CosmosVideoRepository cosmosVideoRepository;
    
    @InjectMocks
    private AzureSearchAdapter adapter;
    
    @Test
    void search_WithFilters_ShouldBuildFilterExpression() {
        // Given
        String query = "java";
        SearchFilter filter = SearchFilter.builder()
                .category("programming")
                .language("en")
                .minDuration(60000L)
                .build();
        
        SearchResults<Map> mockResults = mock(SearchResults.class);
        when(mockResults.getResults()).thenReturn(new ArrayList<>());
        when(mockResults.getTotalCount()).thenReturn(0L);
        
        when(azureSearchClient.search(anyString(), any(SearchOptions.class))).thenReturn(mockResults);
        
        // When
        var result = adapter.search(query, filter, 1, 20, "relevance");
        
        // Then
        assertThat(result).isNotNull();
        verify(azureSearchClient).search(eq(query), any(SearchOptions.class));
    }
    
    @Test
    void suggest_ShouldReturnSuggestions() {
        // Given
        String prefix = "java";
        int maxResults = 10;
        
        SuggestResult mockSuggestResult = mock(SuggestResult.class);
        when(azureSearchClient.suggest(anyString(), any())).thenReturn(mockSuggestResult);
        when(mockSuggestResult.getResults()).thenReturn(new ArrayList<>());
        
        // When
        var suggestions = adapter.suggest(prefix, maxResults);
        
        // Then
        assertThat(suggestions).isNotNull();
        verify(azureSearchClient).suggest(eq(prefix), any());
    }
    
    @Test
    void upsertDocument_ShouldCallAzureSearch() {
        // Given
        SearchDocument document = SearchDocument.builder()
                .videoId("video-1")
                .title("Test Video")
                .build();
        
        // When
        adapter.upsertDocument(document);
        
        // Then
        verify(azureSearchClient).uploadDocuments(anyList());
    }
    
    @Test
    void deleteDocument_ShouldCallAzureSearch() {
        // Given
        String videoId = "video-1";
        
        // When
        adapter.deleteDocument(videoId);
        
        // Then
        verify(azureSearchClient).deleteDocuments(eq(Collections.singletonList(videoId)));
    }
    
    @Test
    void rebuildIndex_ShouldFetchAllVideosAndIndex() {
        // Given
        List<Map<String, Object>> videos = Arrays.asList(
                createVideoMap("video-1"),
                createVideoMap("video-2")
        );
        
        when(cosmosVideoRepository.findAllVideos()).thenReturn(videos);
        
        // When
        adapter.rebuildIndex();
        
        // Then
        verify(cosmosVideoRepository).findAllVideos();
        verify(azureSearchClient, atLeastOnce()).uploadDocuments(anyList());
    }
    
    private Map<String, Object> createVideoMap(String videoId) {
        Map<String, Object> video = new HashMap<>();
        video.put("videoId", videoId);
        video.put("title", "Test Video");
        video.put("description", "Test Description");
        return video;
    }
}
