package com.youtube.mvp.search.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    @NotBlank(message = "Query is required")
    private String query;
    
    private String category;
    private String language;
    private Long minDuration;
    private Long maxDuration;
    private Long minPublishedDate;
    private Long maxPublishedDate;
    private Long minViews;
    private Long maxViews;
    
    @Min(value = 1, message = "Page must be at least 1")
    private Integer page = 1;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    private Integer pageSize = 20;
    
    private String sortBy = "relevance"; // relevance, date, views
}
