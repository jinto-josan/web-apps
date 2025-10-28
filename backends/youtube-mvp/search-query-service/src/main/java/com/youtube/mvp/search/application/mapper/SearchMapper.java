package com.youtube.mvp.search.application.mapper;

import com.youtube.mvp.search.application.dto.SearchRequest;
import com.youtube.mvp.search.application.dto.SearchResponse;
import com.youtube.mvp.search.application.dto.SuggestionResponse;
import com.youtube.mvp.search.domain.model.SearchDocument;
import com.youtube.mvp.search.domain.model.SearchResult;
import com.youtube.mvp.search.domain.model.Suggestion;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SearchMapper {
    
    default com.youtube.mvp.search.domain.model.SearchFilter toFilter(SearchRequest request) {
        return com.youtube.mvp.search.domain.model.SearchFilter.builder()
                .category(request.getCategory())
                .language(request.getLanguage())
                .minDuration(request.getMinDuration())
                .maxDuration(request.getMaxDuration())
                .minPublishedDate(request.getMinPublishedDate())
                .maxPublishedDate(request.getMaxPublishedDate())
                .minViews(request.getMinViews())
                .maxViews(request.getMaxViews())
                .build();
    }
    
    default SearchResponse toResponse(SearchResult result) {
        return SearchResponse.builder()
                .documents(result.getDocuments())
                .totalCount(result.getTotalCount())
                .page(result.getPage())
                .pageSize(result.getPageSize())
                .hasMore(result.getHasMore())
                .build();
    }
    
    default SuggestionResponse toSuggestionResponse(List<Suggestion> suggestions) {
        List<SuggestionResponse.SuggestionItem> items = suggestions.stream()
                .map(s -> SuggestionResponse.SuggestionItem.builder()
                        .text(s.getText())
                        .category(s.getCategory())
                        .score(s.getScore())
                        .build())
                .collect(Collectors.toList());
        
        return SuggestionResponse.builder()
                .suggestions(items)
                .build();
    }
}
