package com.youtube.mvp.videocatalog.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVideoRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotBlank(message = "Channel ID is required")
    @JsonProperty("channelId")
    private String channelId;
    
    @NotBlank(message = "Owner ID is required")
    @JsonProperty("ownerId")
    private String ownerId;
    
    private String category;
    
    @NotBlank(message = "Language is required")
    private String language;
    
    private String visibility;
    
    private List<String> tags;
    
    private List<LocalizedTextDto> localizedTitles;
    private List<LocalizedTextDto> localizedDescriptions;
}

