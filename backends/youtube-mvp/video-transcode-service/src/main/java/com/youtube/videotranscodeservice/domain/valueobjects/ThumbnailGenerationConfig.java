package com.youtube.videotranscodeservice.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThumbnailGenerationConfig {
    private List<String> timeCodes;
    private int width;
    private int height;
    private String format;
}

