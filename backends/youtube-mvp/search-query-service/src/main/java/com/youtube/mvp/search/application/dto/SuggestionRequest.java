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
public class SuggestionRequest {
    @NotBlank(message = "Prefix is required")
    private String prefix;
    
    @Min(value = 1, message = "Max results must be at least 1")
    @Max(value = 50, message = "Max results must not exceed 50")
    private Integer maxResults = 10;
}
