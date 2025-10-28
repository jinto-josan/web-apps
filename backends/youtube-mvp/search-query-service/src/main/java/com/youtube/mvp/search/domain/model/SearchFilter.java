package com.youtube.mvp.search.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object representing search filter criteria.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilter {
    private String category;
    private String language;
    private Long minDuration;
    private Long maxDuration;
    private Long minPublishedDate;
    private Long maxPublishedDate;
    private Long minViews;
    private Long maxViews;
}
