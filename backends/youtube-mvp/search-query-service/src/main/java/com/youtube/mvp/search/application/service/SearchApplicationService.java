package com.youtube.mvp.search.application.service;

import com.youtube.mvp.search.application.dto.SearchRequest;
import com.youtube.mvp.search.application.dto.SearchResponse;
import com.youtube.mvp.search.application.dto.SuggestionRequest;
import com.youtube.mvp.search.application.dto.SuggestionResponse;
import com.youtube.mvp.search.domain.model.SearchDocument;
import com.youtube.mvp.search.domain.model.SearchFilter;
import com.youtube.mvp.search.domain.model.SearchResult;
import com.youtube.mvp.search.domain.model.Suggestion;
import com.youtube.mvp.search.domain.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service orchestrating search domain operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchApplicationService {
    
    private final SearchService searchService;
    private final SearchMapper searchMapper;
    
    /**
     * Executes search query with filters and pagination.
     */
    @Transactional(readOnly = true)
    public SearchResponse search(SearchRequest request) {
        log.info("Searching for query: {}, page: {}, pageSize: {}", 
                request.getQuery(), request.getPage(), request.getPageSize());
        
        SearchFilter filter = searchMapper.toFilter(request);
        SearchResult result = searchService.search(
                request.getQuery(), 
                filter, 
                request.getPage(), 
                request.getPageSize(),
                request.getSortBy()
        );
        
        return searchMapper.toResponse(result);
    }
    
    /**
     * Provides autocomplete suggestions.
     */
    @Transactional(readOnly = true)
    public SuggestionResponse suggest(SuggestionRequest request) {
        log.info("Getting suggestions for prefix: {}", request.getPrefix());
        
        List<Suggestion> suggestions = searchService.suggest(
                request.getPrefix(), 
                request.getMaxResults()
        );
        
        return searchMapper.toSuggestionResponse(suggestions);
    }
    
    /**
     * Handles index update events from messaging.
     */
    @Transactional
    public void handleIndexUpdate(SearchDocument document, String eventType) {
        log.info("Handling index update for videoId: {}, eventType: {}", 
                document.getVideoId(), eventType);
        
        if ("DELETED".equals(eventType)) {
            searchService.deleteDocument(document.getVideoId());
        } else {
            searchService.upsertDocument(document);
        }
    }
    
    /**
     * Triggers index rebuild.
     */
    @Transactional
    public void rebuildIndex() {
        log.info("Starting index rebuild");
        searchService.rebuildIndex();
        log.info("Index rebuild completed");
    }
}
