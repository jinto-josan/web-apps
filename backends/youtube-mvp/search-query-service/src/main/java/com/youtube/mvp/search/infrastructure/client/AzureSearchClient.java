package com.youtube.mvp.search.infrastructure.client;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exceptions.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.models.IndexingResult;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResults;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.indexes.models.*;
import com.azure.search.documents.models.IndexingBatch;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Azure Cognitive Search client wrapper.
 */
@Component
@Slf4j
public class AzureSearchClient {
    
    @Value("${azure.search.endpoint}")
    private String searchEndpoint;
    
    @Value("${azure.search.api-key}")
    private String apiKey;
    
    @Value("${azure.search.index-name}")
    private String indexName;
    
    private SearchClient searchClient;
    private SearchIndexClient indexClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void init() {
        AzureKeyCredential credential = new AzureKeyCredential(apiKey);
        
        this.searchClient = new SearchClientBuilder()
                .endpoint(searchEndpoint)
                .credential(credential)
                .indexName(indexName)
                .buildClient();
        
        this.indexClient = new SearchIndexClientBuilder()
                .endpoint(searchEndpoint)
                .credential(credential)
                .buildClient();
        
        ensureIndexExists();
    }
    
    public void ensureIndexExists() {
        try {
            SearchIndex index = indexClient.getIndex(indexName);
            log.info("Search index '{}' already exists", indexName);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                createSearchIndex();
            } else {
                throw e;
            }
        }
    }
    
    public void createSearchIndex() {
        log.info("Creating search index: {}", indexName);
        
        SearchIndex index = new SearchIndex(indexName)
                .setFields(Arrays.asList(
                        new SearchField("documentId", SearchFieldDataType.STRING)
                                .setKey(true)
                                .setSearchable(false),
                        new SearchField("videoId", SearchFieldDataType.STRING)
                                .setKey(false)
                                .setSearchable(false),
                        new SearchField("title", SearchFieldDataType.STRING)
                                .setSearchable(true)
                                .setFilterable(true)
                                .setSortable(true),
                        new SearchField("description", SearchFieldDataType.STRING)
                                .setSearchable(true),
                        new SearchField("channelName", SearchFieldDataType.STRING)
                                .setSearchable(true)
                                .setFilterable(true)
                                .setSortable(true),
                        new SearchField("channelId", SearchFieldDataType.STRING)
                                .setFilterable(true),
                        new SearchField("category", SearchFieldDataType.STRING)
                                .setSearchable(false)
                                .setFilterable(true)
                                .setSortable(true),
                        new SearchField("tags", SearchFieldDataType.STRING)
                                .setSearchable(true)
                                .setFilterable(false),
                        new SearchField("viewCount", SearchFieldDataType.INT64)
                                .setFilterable(true)
                                .setSortable(true),
                        new SearchField("likeCount", SearchFieldDataType.INT64)
                                .setFilterable(true)
                                .setSortable(true),
                        new SearchField("duration", SearchFieldDataType.INT64)
                                .setFilterable(true)
                                .setSortable(true),
                        new SearchField("thumbnailUrl", SearchFieldDataType.STRING)
                                .setSearchable(false),
                        new SearchField("publishedAt", SearchFieldDataType.INT64)
                                .setFilterable(true)
                                .setSortable(true),
                        new SearchField("language", SearchFieldDataType.STRING)
                                .setFilterable(true),
                        new SearchField("quality", SearchFieldDataType.INT32)
                                .setFilterable(true),
                        new SearchField("relevanceScore", SearchFieldDataType.DOUBLE)
                                .setFilterable(true)
                                .setSortable(true)
                ))
                .setSuggesters(Collections.singletonList(
                        new SearchSuggester("sg", Arrays.asList("title", "description", "tags"))
                ))
                .setScoringProfiles(Arrays.asList(
                        new ScoringProfile("views")
                                .setFunctionAggregation(ScoringFunctionAggregation.SUM)
                                .setFunctions(Arrays.asList(
                                        new MagnitudeScoringFunction(
                                                "viewCount",
                                                new MagnitudeScoringParameters()
                                                        .setBoostingRangeStart(0)
                                                        .setBoostingRangeEnd(10000000)
                                                        .setShouldBoostBeyondRangeByConstant(false),
                                                2.0
                                        )
                                )),
                        new ScoringProfile("recent")
                                .setFunctionAggregation(ScoringFunctionAggregation.SUM)
                                .setFunctions(Arrays.asList(
                                        new FreshnessScoringFunction(
                                                "publishedAt",
                                                new FreshnessScoringParameters().setBoostingDuration(Duration.parse("P365D")),
                                                2.0
                                        )
                                ))
                ))
                .setDefaultScoringProfile("views");
        
        indexClient.createOrUpdateIndex(index);
        log.info("Search index created: {}", indexName);
    }
    
    public SearchResults<Map> search(String query, SearchOptions options) {
        try {
            return searchClient.search(query, options, Context.NONE);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 429) {
                log.warn("Rate limit exceeded, retrying after delay");
                try {
                    Thread.sleep(2000);
                    return searchClient.search(query, options, Context.NONE);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
            throw new RuntimeException("Search failed", e);
        }
    }
    
    public SuggestResult suggest(String query, SuggestOptions options) {
        return searchClient.suggest(query, options, String.class);
    }
    
    public void uploadDocuments(List<Map<String, Object>> documents) {
        try {
            IndexingBatch<Map<String, Object>> batch = new IndexingBatch<>();
            documents.forEach(batch::addUploadActions);
            
            List<IndexingResult> results = searchClient.indexDocuments(batch, null, Context.NONE).getResults();
            
            for (IndexingResult result : results) {
                if (!result.isSucceeded()) {
                    log.error("Failed to index document: {} - {}", result.getKey(), result.getErrorMessage());
                }
            }
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 429) {
                log.warn("Rate limit exceeded during document upload");
                throw new RuntimeException("Rate limit exceeded", e);
            }
            throw new RuntimeException("Document upload failed", e);
        }
    }
    
    public void deleteDocuments(List<String> documentIds) {
        List<Map<String, Object>> documents = documentIds.stream()
                .map(id -> {
                    Map<String, Object> doc = new HashMap<>();
                    doc.put("documentId", id);
                    return doc;
                })
                .toList();
        
        IndexingBatch<Map<String, Object>> batch = new IndexingBatch<>();
        documents.forEach(batch::addDeleteActions);
        
        searchClient.indexDocuments(batch, null, Context.NONE);
    }
    
    public Map<String, Object> documentToMap(Object doc) {
        return objectMapper.convertValue(doc, new TypeReference<Map<String, Object>>() {});
    }
}
