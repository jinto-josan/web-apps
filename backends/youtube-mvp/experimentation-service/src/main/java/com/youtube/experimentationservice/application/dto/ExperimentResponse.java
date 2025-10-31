package com.youtube.experimentationservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperimentResponse {
    private String key;
    private String variantId;
    private String variantName;
    private Map<String, Object> configuration;
    private Map<String, String> metadata;
}

