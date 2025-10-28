package com.youtube.mvp.search.infrastructure.adapter;

import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResults;
import com.azure.search.documents.models.SuggestOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.mvp.search.domain.model.SearchDocument;
import com.youtube.mvp.search.domain.model.SearchFilter;
import com.youtube.mvp.search.domain.model.SearchResult;
import com.youtube.mvp.search.domain.model.Suggestion;
import com.youtube.mvp.search.domain.service.SearchService;
import com.youtube.mvp.search.infrastructure.client.AzureSearchClient;
import com.youtube.mvp.search.infrastructure.repository.CosmosVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter implementing SearchService using Azure Cognitive Search.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AzureSearchAdapter implements SearchService {
    
    private final AzureSearchClient azureSearchClient;
    private final CosmosVideoRepository cosmosVideoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public SearchResult search(String query, SearchFilter filter, Integer page, Integer pageSize, String sortBy) {
        SearchOptions searchOptions = new SearchOptions();
        
        // Pagination
        int skip = (page - 1) * pageSize;
        searchOptions.setSkip(skip);
        searchOptions.setTop(pageSize);
        
        // Build filter expression
        String filterExpression = buildFilterExpression(filter);
        if (filterExpression != null) {
            searchOptions.setFilter(filterExpression);
        }
        
        // Sorting
        if ("date".equalsIgnoreCase(sortBy)) {
            searchOptions.setOrderBy(Arrays.asList("publishedAt desc"));
        } else if ("views".equalsIgnoreCase(sortBy)) {
            searchOptions.setOrderBy(Arrays.asList("viewCount desc"));
        }
        
        // Search
        SearchResults<Map> results = azureSearchClient.search(query, searchOptions);
        
        List<SearchDocument> documents = results.getResults().stream()
                .map(r -> azureSearchClient.documentToMap(r.getDocument()))
                .map(this::mapToSearchDocument)
                .collect(Collectors.toList());
        
        Long totalCount = results.getTotalCount();
        boolean hasMore = (skip + documents.size()) < totalCount;
        
        return SearchResult.builder()
                .documents(documents)
                .totalCount(totalCount)
                .page(page)
                .pageSize(pageSize)
                .hasMore(hasMore)
                .build();
    }
    
    @Override
    public List<Suggestion> suggest(String prefix, Integer maxResults) {
        SuggestOptions suggestOptions = new SuggestOptions();
        suggestOptions.setUseFuzzyMatching(true);
        suggestOptions.setTop(maxResults);
        
        var suggestResult = azureSearchClient.suggest(prefix, suggestOptions);
        
        return suggestResult.getResults().stream()
                .map(result -> Suggestion.builder()
                        .text(result.getText())
                        .score((int) result.getScore())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    public void upsertDocument(SearchDocument document) {
        Map<String, Object> docMap = documentToMap(document);
        azureSearchClient.uploadDocuments(Collections.singletonList(docMap));
        log.info("Upserted document: {}", document.getDocumentId());
    }
    
    @Override
    public void deleteDocument(String videoId) {
        azureSearchClient.deleteDocuments(Collections.singletonList(videoId));
        log.info("Deleted document: {}", videoId);
    }
    
    @Override
    public void rebuildIndex() {
        log.info("Starting index rebuild...");
        // This would typically fetch all videos from Cosmos and re-index
        // For production, this should be done in batches with pagination
        List<Map<String, Object>> allVideos = cosmosVideoRepository.findAllVideos();
        
        int batchSize = 100;
        for (int i = 0; i < allVideos.size(); i += batchSize) {
            List<Map<String, Object>> batch = allVideos.subList(i, 
                    Math.min(i + batchSize, allVideos.size()));
            azureSearchClient.uploadDocuments(batch);
            log.info("Indexed batch: {}/{}", i + batch.size(), allVideos.size());
        }
        
        log.info("Index rebuild completed");
    }
    
    private String buildFilterExpression(SearchFilter filter) {
        if (filter == null) {
            return null;
        }
        
        List<String> filters = new ArrayList<>();
        
        if (filter.getCategory() != null) {
            filters.add(String.format("category eq '%s'", filter.getCategory()));
        }
        if (filter.getLanguage() != null) {
            filters.add(String.format("language eq '%s'", filter.getLanguage()));
        }
        if (filter.getMinDuration() != null) {
            filters.add(String.format("duration ge %d", filter.getMinDuration()));
        }
        if (filter.getMaxDuration() != null) {
            filters.add(String.format("duration le %d", filter.getMaxDuration()));
        }
        if (filter.getMinViews() != null) {
            filters.add(String.format("viewCount ge %d", filter.getMinViews()));
        }
        if (filter.getMaxViews() != null) {
            filters.add(String.format("viewCount le %d", filter.getMaxViews()));
        }
        if (filter.getMinPublishedDate() != null) {
            filters.add(String.format("publishedAt ge %d", filter.getMinPublishedDate()));
        }
        if (filter.getMaxPublishedDate() != null) {
            filters.add(String.format("publishedAt le %d", filter.getMaxPublishedDate()));
        }
        
        return filters.isEmpty() ? null : String.join(" and ", filters);
    }
    
    private SearchDocument mapToSearchDocument(Map<String, Object> docMap) {
        return SearchDocument.builder()
                .documentId((String) docMap.get("documentId"))
                .videoId((String) docMap.get("videoId"))
                .title((String) docMap.get("title"))
                .description((String) docMap.get("description"))
                .channelName((String) docMap.get("channelName"))
                .channelId((String) docMap.get("channelId"))
                .category((String) docMap.get("category"))
                .tags((String) docMap.get("tags"))
                .viewCount(getLongValue(docMap.get("viewCount")))
                .likeCount(getLongValue(docMap.get("likeCount")))
                .duration(getLongValue(docMap.get("duration")))
                .thumbnailUrl((String) docMap.get("thumbnailUrl"))
                .publishedAt(getLongValue(docMap.get("publishedAt")))
                .language((String) docMap.get("language"))
                .quality((Integer) docMap.get("quality"))
                .relevanceScore((Double) docMap.get("relevanceScore"))
                .build();
    }
    
    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }
    
    private Map<String, Object> documentToMap(SearchDocument document) {
        Map<String, Object> map = new HashMap<>();
        map.put("documentId", document.getDocumentId());
        map.put("videoId", document.getVideoId());
        map.put("title", document.getTitle());
        map.put("description", document.getDescription());
        map.put("channelName", document.getChannelName());
        map.put("channelId", document.getChannelId());
        map.put("category", document.getCategory());
        map.put("tags", document.getTags());
        map.put("viewCount", document.getViewCount());
        map.put("likeCount", document.getLikeCount());
        map.put("duration", document.getDuration());
        map.put("thumbnailUrl", document.getThumbnailUrl());
        map.put("publishedAt", document.getPublishedAt());
        map.put("language", document.getLanguage());
        map.put("quality", document.getQuality());
        map.put("relevanceScore", document.getRelevanceScore());
        return map;
    }
}
