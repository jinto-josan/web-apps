package com.youtube.configsecretsservice.infrastructure.config;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Azure services.
 */
@Configuration
@Slf4j
public class AzureConfig {
    
    @Value("${azure.appconfiguration.endpoint:}")
    private String appConfigEndpoint;
    
    @Value("${azure.keyvault.uri:}")
    private String keyVaultUri;
    
    @Value("${azure.servicebus.connection-string:}")
    private String serviceBusConnectionString;
    
    @Value("${azure.servicebus.topic:config-updates}")
    private String serviceBusTopic;
    
    @Bean
    public TokenCredential tokenCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }
    
    @Bean
    public ConfigurationClient configurationClient(TokenCredential tokenCredential) {
        if (appConfigEndpoint == null || appConfigEndpoint.isEmpty()) {
            log.warn("Azure App Configuration endpoint not configured");
            return null;
        }
        return new ConfigurationClientBuilder()
                .endpoint(appConfigEndpoint)
                .credential(tokenCredential)
                .buildClient();
    }
    
    @Bean
    public SecretClient secretClient(TokenCredential tokenCredential) {
        if (keyVaultUri == null || keyVaultUri.isEmpty()) {
            log.warn("Azure Key Vault URI not configured");
            return null;
        }
        return new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(tokenCredential)
                .buildClient();
    }
    
    @Bean
    public ServiceBusSenderClient serviceBusSenderClient() {
        if (serviceBusConnectionString == null || serviceBusConnectionString.isEmpty()) {
            log.warn("Azure Service Bus connection string not configured");
            return null;
        }
        return new ServiceBusClientBuilder()
                .connectionString(serviceBusConnectionString)
                .sender()
                .topicName(serviceBusTopic)
                .buildClient();
    }
}

