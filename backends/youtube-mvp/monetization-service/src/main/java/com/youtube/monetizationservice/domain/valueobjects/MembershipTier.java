package com.youtube.monetizationservice.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Value object representing membership tier.
 */
@Getter
@AllArgsConstructor
public enum MembershipTier {
    BASIC("Basic", "Basic membership tier"),
    PREMIUM("Premium", "Premium membership tier"),
    ELITE("Elite", "Elite membership tier");

    private final String name;
    private final String description;
}

