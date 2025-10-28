package com.youtube.mvp.search.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object representing search suggestions/autocomplete results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Suggestion {
    private String text;
    private String category;
    private Integer score;
}
