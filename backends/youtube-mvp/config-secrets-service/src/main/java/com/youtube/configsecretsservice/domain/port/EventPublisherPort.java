package com.youtube.configsecretsservice.domain.port;

/**
 * Port for publishing domain events.
 */
public interface EventPublisherPort {
    void publishConfigurationUpdated(String scope, String key, String etag);
    void publishSecretRotationCompleted(String scope, String key, boolean success);
}

