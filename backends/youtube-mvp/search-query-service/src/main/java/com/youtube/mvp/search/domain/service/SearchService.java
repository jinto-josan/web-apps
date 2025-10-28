package com.youtube.mvp.search.domain.service;

import com.youtube.mvp.search.domain.model.SearchDocument;
import com.youtube.mvp.search.domain.model.SearchFilter;
import com.youtube.mvp.search.domain.model.SearchResult;
import com.youtube.mvp.search.domain.model.Suggestion;

import java.util.List;

/**
 * Domain service interface for search operations.
 */
public interface SearchService {
    /**
     * Performs full-text search with filters and pagination.
     */
    SearchResult search(String query, SearchFilter filter, Integer page, Integer pageSize, String sortBy);
    
    /**
     * Provides autocomplete suggestions.
     */
    List<Suggestion> suggest(String prefix, Integer maxResults);
    
    /**
     * Upserts a document into the search index.
     */
    void upsertDocument(SearchDocument document);
    
    /**
     * Deletes a document from the search index.
     */
    void deleteDocument(String videoId);
    
    /**
     * Rebuilds the entire search index.
     */
    void rebuildIndex();
}
