package com.youtube.moderationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PolicyThreshold {
    private String policyCode; // e.g. HATE_SPEECH, SEXUAL, VIOLENCE
    private double autoRejectScore; // >= triggers automatic case/strike
    private double autoApproveBelow; // < auto approve threshold
}


