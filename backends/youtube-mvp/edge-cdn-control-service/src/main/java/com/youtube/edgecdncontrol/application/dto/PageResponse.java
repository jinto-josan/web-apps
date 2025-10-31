package com.youtube.edgecdncontrol.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    
    @JsonProperty("hasNext")
    public boolean hasNext() {
        return page < totalPages - 1;
    }
    
    @JsonProperty("hasPrevious")
    public boolean hasPrevious() {
        return page > 0;
    }
}

