package com.youtube.adsdecisionservice.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Creative {
    private String id;
    private String assetUrl;
    private int durationSeconds;
    private String mimeType;
}


