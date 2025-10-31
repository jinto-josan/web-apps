package com.youtube.observabilityservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class RunSyntheticCheckRequest {
    @NotBlank
    private String checkId;
    
    private Map<String, String> variables; // For parameterized checks
}

