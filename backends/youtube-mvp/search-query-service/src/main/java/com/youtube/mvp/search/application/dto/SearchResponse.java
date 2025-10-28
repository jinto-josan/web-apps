package com.youtube.mvp.search.application.dto;

import com.youtube.mvp.search.domain.model.SearchDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<SearchDocument> documents;
    private Long totalCount;
    private Integer page;
    private Integer pageSize;
    private Boolean hasMore;
}
