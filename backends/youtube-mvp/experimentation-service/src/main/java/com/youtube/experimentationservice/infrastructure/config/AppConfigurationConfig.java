package com.youtube.experimentationservice.infrastructure.config;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AppConfigurationConfig {

    @Value("${spring.cloud.azure.appconfiguration.endpoint:}")
    private String endpoint;

    @Value("${spring.cloud.azure.appconfiguration.connection-string:}")
    private String connectionString;

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
            name = "spring.cloud.azure.appconfiguration.enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    public ConfigurationClient configurationClient() {
        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        
        if (connectionString != null && !connectionString.isEmpty()) {
            builder.connectionString(connectionString);
        } else if (endpoint != null && !endpoint.isEmpty()) {
            TokenCredential credential = new DefaultAzureCredentialBuilder().build();
            builder.endpoint(endpoint).credential(credential);
        } else {
            log.warn("App Configuration endpoint or connection string not provided");
            throw new IllegalStateException("App Configuration must be configured with either endpoint or connection-string");
        }
        
        return builder.buildClient();
    }
}

