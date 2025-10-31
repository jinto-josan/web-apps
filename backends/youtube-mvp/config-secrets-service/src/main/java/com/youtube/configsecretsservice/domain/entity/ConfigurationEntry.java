package com.youtube.configsecretsservice.domain.entity;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Domain entity representing a configuration entry.
 */
@Value
@Builder(toBuilder = true)
public class ConfigurationEntry {
    String key;
    String scope;
    String value;
    String contentType;
    String label;
    String etag;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    Boolean isSecret;
}

