package com.youtube.adsdecisionservice.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdDecisionRequest {
    @NotBlank private String userId;
    @NotBlank private String videoId;
    @NotNull private Map<String, Object> context;
}


