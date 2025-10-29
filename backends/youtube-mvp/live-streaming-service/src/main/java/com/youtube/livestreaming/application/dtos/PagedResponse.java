package com.youtube.livestreaming.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PagedResponse<T> {
    @JsonProperty("items")
    private List<T> items;
    
    @JsonProperty("total")
    private Long total;
    
    @JsonProperty("page")
    private Integer page;
    
    @JsonProperty("size")
    private Integer size;
    
    @JsonProperty("hasNext")
    private Boolean hasNext;
    
    @JsonProperty("hasPrevious")
    private Boolean hasPrevious;
    
    public PagedResponse(List<T> items, Long total, Integer page, Integer size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
        this.hasNext = (long) page * size < total;
        this.hasPrevious = page > 1;
    }
}

