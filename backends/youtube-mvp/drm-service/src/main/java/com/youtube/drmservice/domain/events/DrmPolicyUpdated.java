package com.youtube.drmservice.domain.events;

import lombok.Builder;
import lombok.Value;
import java.time.Instant;

@Value
@Builder
public class DrmPolicyUpdated {
    String policyId;
    String videoId;
    String updatedBy;
    Instant updatedAt;
}

