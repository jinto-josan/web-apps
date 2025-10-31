package com.youtube.edgecdncontrol.domain.valueobjects;

import lombok.Value;

import java.util.UUID;

@Value
public class CdnRuleId {
    String value;

    public CdnRuleId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CDN Rule ID cannot be null or blank");
        }
        this.value = value;
    }

    public static CdnRuleId generate() {
        return new CdnRuleId(UUID.randomUUID().toString());
    }

    public static CdnRuleId of(String value) {
        return new CdnRuleId(value);
    }
}

