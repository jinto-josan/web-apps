package com.youtube.edgecdncontrol.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PurgeRequestDto {
    @NotBlank(message = "Resource group is required")
    private String resourceGroup;
    
    @NotBlank(message = "Front Door profile name is required")
    private String frontDoorProfileName;
    
    @NotEmpty(message = "At least one content path is required")
    private List<String> contentPaths;
    
    @NotNull(message = "Purge type is required")
    private PurgeType purgeType;
    
    public enum PurgeType {
        SINGLE_PATH,
        WILDCARD,
        ALL
    }
}

