package com.youtube.observabilityservice.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CreateSLORequest {
    @NotBlank
    private String name;
    
    @NotBlank
    private String serviceName;
    
    private String description;
    
    @Valid
    @NotNull
    private List<CreateSLIRequest> slis;
    
    @Positive
    @NotNull
    private Double targetPercent; // e.g., 99.9
    
    @NotBlank
    private String timeWindow; // e.g., "30d" for 30 days
    
    private Map<String, String> labels;
}

