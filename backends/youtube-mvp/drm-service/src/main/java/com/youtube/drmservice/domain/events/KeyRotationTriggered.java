package com.youtube.drmservice.domain.events;

import lombok.Builder;
import lombok.Value;
import java.time.Instant;
import java.util.List;

@Value
@Builder
public class KeyRotationTriggered {
    List<String> policyIds;
    Instant rotatedAt;
    String rotatedBy;
}

