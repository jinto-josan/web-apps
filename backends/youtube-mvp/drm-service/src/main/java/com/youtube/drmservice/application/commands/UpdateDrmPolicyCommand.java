package com.youtube.drmservice.application.commands;

import com.youtube.drmservice.domain.models.PolicyConfiguration;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateDrmPolicyCommand {
    String policyId;
    PolicyConfiguration configuration;
    String updatedBy;
    String etag;
    String idempotencyKey;
}

