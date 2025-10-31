package com.youtube.configsecretsservice.infrastructure.azure.adapter;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.youtube.configsecretsservice.domain.port.KeyVaultPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter implementing KeyVaultPort using Azure Key Vault SDK.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeyVaultAdapter implements KeyVaultPort {
    
    private final SecretClient secretClient;
    
    @Value("${azure.keyvault.secret-scope-prefix:config}")
    private String scopePrefix;
    
    @Override
    @Retry(name = "keyVault")
    @CircuitBreaker(name = "keyVault")
    @TimeLimiter(name = "keyVault")
    public Optional<String> getSecret(String scope, String key) {
        try {
            String secretName = buildSecretName(scope, key);
            KeyVaultSecret secret = secretClient.getSecret(secretName);
            return Optional.of(secret.getValue());
        } catch (ResourceNotFoundException e) {
            log.debug("Secret not found: {}/{}", scope, key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting secret from Key Vault: {}/{}", scope, key, e);
            throw new RuntimeException("Failed to get secret from Key Vault", e);
        }
    }
    
    @Override
    @Retry(name = "keyVault")
    @CircuitBreaker(name = "keyVault")
    @TimeLimiter(name = "keyVault")
    public void setSecret(String scope, String key, String value) {
        try {
            String secretName = buildSecretName(scope, key);
            secretClient.setSecret(secretName, value);
            log.info("Secret set successfully: {}/{}", scope, key);
        } catch (Exception e) {
            log.error("Error setting secret in Key Vault: {}/{}", scope, key, e);
            throw new RuntimeException("Failed to set secret in Key Vault", e);
        }
    }
    
    @Override
    @Retry(name = "keyVault")
    @CircuitBreaker(name = "keyVault")
    @TimeLimiter(name = "keyVault")
    public void rotateSecret(String scope, String key) {
        try {
            String secretName = buildSecretName(scope, key);
            // Get current secret
            KeyVaultSecret currentSecret = secretClient.getSecret(secretName);
            
            // Rotate by setting a new version (Key Vault automatically creates new versions)
            // For actual rotation, you might want to generate a new secret value
            // This is a simplified version - in production, you'd generate a new secret
            String newValue = generateNewSecretValue(currentSecret.getValue());
            secretClient.setSecret(secretName, newValue);
            
            log.info("Secret rotated successfully: {}/{}", scope, key);
        } catch (Exception e) {
            log.error("Error rotating secret in Key Vault: {}/{}", scope, key, e);
            throw new RuntimeException("Failed to rotate secret in Key Vault", e);
        }
    }
    
    @Override
    @Retry(name = "keyVault")
    @CircuitBreaker(name = "keyVault")
    @TimeLimiter(name = "keyVault")
    public void deleteSecret(String scope, String key) {
        try {
            String secretName = buildSecretName(scope, key);
            secretClient.beginDeleteSecret(secretName);
            log.info("Secret deletion initiated: {}/{}", scope, key);
        } catch (Exception e) {
            log.error("Error deleting secret from Key Vault: {}/{}", scope, key, e);
            throw new RuntimeException("Failed to delete secret from Key Vault", e);
        }
    }
    
    private String buildSecretName(String scope, String key) {
        return String.format("%s-%s-%s", scopePrefix, scope, key).toLowerCase();
    }
    
    private String generateNewSecretValue(String currentValue) {
        // In production, implement proper secret generation logic
        // This is a placeholder - you might want to use a secure random generator
        return currentValue + "-rotated-" + System.currentTimeMillis();
    }
}

