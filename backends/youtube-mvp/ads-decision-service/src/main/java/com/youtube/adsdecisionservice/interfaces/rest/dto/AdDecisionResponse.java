package com.youtube.adsdecisionservice.interfaces.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdDecisionResponse {
    private String campaignId;
    private String creativeId;
    private boolean fill;
    private String reason; // e.g., capped, no-match, success
}


