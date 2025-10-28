package com.youtube.mvp.videocatalog.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVideoRequest {
    
    private String title;
    private String description;
    private String category;
    private String visibility;
    private List<String> tags;
    private String thumbnailUrl;
    private Long durationSeconds;
    
    private List<LocalizedTextDto> localizedTitles;
    private List<LocalizedTextDto> localizedDescriptions;
}

