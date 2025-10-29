package com.youtube.moderationservice.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class Strike {
    String userId;
    String reason;
    int weight; // 1..3
    Instant at;
}


