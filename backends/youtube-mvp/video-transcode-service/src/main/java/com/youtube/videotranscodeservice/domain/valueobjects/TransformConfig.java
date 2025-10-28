package com.youtube.videotranscodeservice.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransformConfig {
    private String name;
    private String description;
    private Map<String, String> encodingPresets;
    private ThumbnailGenerationConfig thumbnailConfig;
    private DRMConfig drmConfig;
}

