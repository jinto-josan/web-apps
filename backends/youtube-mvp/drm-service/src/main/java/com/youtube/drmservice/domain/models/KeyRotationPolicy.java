package com.youtube.drmservice.domain.models;

import lombok.Builder;
import lombok.Value;
import java.time.Duration;

/**
 * Value object for key rotation configuration
 */
@Value
@Builder
public class KeyRotationPolicy {
    Boolean enabled;
    Duration rotationInterval;
    Instant lastRotationAt;
    Instant nextRotationAt;
    String rotationKeyVaultUri;
}

