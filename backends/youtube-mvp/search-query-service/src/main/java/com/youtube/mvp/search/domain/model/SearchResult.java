package com.youtube.mvp.search.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Value object representing search results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private List<SearchDocument> documents;
    private Long totalCount;
    private Integer page;
    private Integer pageSize;
    private Boolean hasMore;
}
