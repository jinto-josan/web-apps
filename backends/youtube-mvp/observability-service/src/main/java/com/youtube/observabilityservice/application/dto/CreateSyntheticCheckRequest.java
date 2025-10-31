package com.youtube.observabilityservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Map;

@Data
public class CreateSyntheticCheckRequest {
    @NotBlank
    private String name;
    
    private String description;
    
    @NotBlank
    private String type; // HTTP, HTTPS, TCP, DNS, etc.
    
    @NotBlank
    private String endpoint;
    
    @NotBlank
    private String method; // GET, POST, etc.
    
    private Map<String, String> headers;
    
    private String body; // For POST requests
    
    @Positive
    @NotNull
    private Integer expectedStatusCode;
    
    private String expectedBodyPattern; // Regex
    
    @Positive
    private Integer timeoutSeconds;
    
    @Positive
    @NotNull
    private Integer intervalSeconds;
    
    private Boolean enabled;
    
    private Map<String, String> labels;
}

