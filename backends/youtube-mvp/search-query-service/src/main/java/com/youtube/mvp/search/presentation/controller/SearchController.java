package com.youtube.mvp.search.presentation.controller;

import com.youtube.mvp.search.application.dto.*;
import com.youtube.mvp.search.application.service.SearchApplicationService;
import com.youtube.mvp.search.presentation.exception.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for search operations.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Search API", description = "Search and autocomplete endpoints")
public class SearchController {
    
    private final SearchApplicationService searchApplicationService;
    
    @GetMapping("/search")
    @Operation(
            summary = "Full-text search",
            description = "Performs full-text search across indexed videos with filters and pagination",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Search successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            },
            security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<SearchResponse> search(
            @Valid @ModelAttribute SearchRequest request,
            @Parameter(description = "ETag for caching") @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
    ) {
        log.info("Search request: query={}, page={}, pageSize={}", 
                request.getQuery(), request.getPage(), request.getPageSize());
        
        SearchResponse response = searchApplicationService.search(request);
        
        return ResponseEntity.ok()
                .eTag(generateETag(response))
                .body(response);
    }
    
    @GetMapping("/suggest")
    @Operation(
            summary = "Autocomplete suggestions",
            description = "Provides autocomplete suggestions for search queries",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Suggestions retrieved"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            },
            security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<SuggestionResponse> suggest(@Valid @ModelAttribute SuggestionRequest request) {
        log.info("Suggestion request: prefix={}, maxResults={}", 
                request.getPrefix(), request.getMaxResults());
        
        SuggestionResponse response = searchApplicationService.suggest(request);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/index/rebuild")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Rebuild search index",
            description = "Rebuilds the entire search index from source data (admin only)",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Rebuild started"),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            },
            security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<Void> rebuildIndex(@Valid @RequestBody(required = false) IndexRebuildRequest request) {
        log.info("Index rebuild requested");
        
        searchApplicationService.rebuildIndex();
        
        return ResponseEntity.accepted().build();
    }
    
    private String generateETag(SearchResponse response) {
        return String.valueOf(response.hashCode());
    }
}
