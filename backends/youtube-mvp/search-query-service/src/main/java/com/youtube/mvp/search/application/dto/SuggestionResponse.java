package com.youtube.mvp.search.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionResponse {
    private List<SuggestionItem> suggestions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestionItem {
        private String text;
        private String category;
        private Integer score;
    }
}
