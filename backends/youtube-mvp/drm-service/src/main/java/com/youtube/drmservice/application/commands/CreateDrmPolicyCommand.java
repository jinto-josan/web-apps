package com.youtube.drmservice.application.commands;

import com.youtube.drmservice.domain.models.DrmPolicy;
import com.youtube.drmservice.domain.models.PolicyConfiguration;
import com.youtube.drmservice.domain.models.KeyRotationPolicy;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateDrmPolicyCommand {
    String videoId;
    DrmPolicy.DrmProvider provider;
    PolicyConfiguration configuration;
    KeyRotationPolicy rotationPolicy;
    String createdBy;
    String idempotencyKey;
}

