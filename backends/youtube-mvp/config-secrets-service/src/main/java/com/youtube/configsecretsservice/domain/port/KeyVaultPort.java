package com.youtube.configsecretsservice.domain.port;

import java.util.Optional;

/**
 * Port for Azure Key Vault secret operations.
 */
public interface KeyVaultPort {
    Optional<String> getSecret(String scope, String key);
    void setSecret(String scope, String key, String value);
    void rotateSecret(String scope, String key);
    void deleteSecret(String scope, String key);
}

