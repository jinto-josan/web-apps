package com.youtube.mvp.videocatalog.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalizedTextDto {
    @NotBlank
    private String language;
    
    @NotBlank
    private String text;
}

