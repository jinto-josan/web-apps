package com.youtube.adsdecisionservice.domain.valueobjects;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CapState {
    private String userId;
    private String campaignId;
    private long impressionsToday;
    private long impressionsTotal;
}


