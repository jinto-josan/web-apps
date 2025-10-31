package com.youtube.observabilityservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSLIRequest {
    @NotBlank
    private String name;
    
    @NotBlank
    private String type; // AVAILABILITY, LATENCY, ERROR_RATE, etc.
    
    @NotBlank
    private String query; // KQL query
}

