package com.youtube.mvp.videocatalog.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Generic paged response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    
    private List<T> content;
    
    private int page;
    private int size;
    
    @JsonProperty("totalElements")
    private long totalElements;
    
    @JsonProperty("totalPages")
    private int totalPages;
    
    private boolean hasNext;
    private boolean hasPrevious;
}

