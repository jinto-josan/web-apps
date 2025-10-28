package com.youtube.drmservice.domain.events;

import lombok.Builder;
import lombok.Value;
import java.time.Instant;

@Value
@Builder
public class DrmPolicyCreated {
    String policyId;
    String videoId;
    String provider;
    String createdBy;
    Instant createdAt;
}

