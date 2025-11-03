package com.youtube.common.domain.config;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring configuration for common-domain infrastructure components.
 */
@Configuration
@EnableScheduling
public class CommonDomainConfiguration {
    @Bean
    @ConditionalOnProperty(name = "azure.appconfig.enabled", havingValue = "true", matchIfMissing = true)
    public ConfigurationClient configurationClient(
        @Value("${azure.appconfig.connection-string:}") String connectionString,
        @Value("${azure.appconfig.endpoint:}") String endpoint
    ) {
        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        
        if (connectionString != null && !connectionString.isEmpty()) {
            builder.connectionString(connectionString);
        } else if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build());
        } else {
            throw new IllegalArgumentException(
                "Either azure.appconfig.connection-string or azure.appconfig.endpoint must be configured"
            );
        }
        
        return builder.buildClient();
    }
    
    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
