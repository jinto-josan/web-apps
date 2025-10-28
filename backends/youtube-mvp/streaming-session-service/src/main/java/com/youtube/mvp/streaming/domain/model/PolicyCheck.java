package com.youtube.mvp.streaming.domain.model;

import lombok.*;
import java.time.Instant;

/**
 * Policy check result.
 */
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class PolicyCheck {
    private String policyName;
    private boolean allowed;
    private String reason;
    private Instant checkedAt;
}

