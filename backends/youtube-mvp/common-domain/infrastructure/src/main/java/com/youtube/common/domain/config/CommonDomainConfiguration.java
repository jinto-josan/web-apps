package com.youtube.common.domain.config;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.youtube.common.domain.core.Clock;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CommonDomainConfiguration {
    @Bean
    @ConditionalOnProperty(name = "azure.appconfig.enabled", havingValue = "true", matchIfMissing = false)
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
        log.info("Azure App Configuration configured successfully");
        
        return builder.buildClient();
    }
    
    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
    
    /**
     * Provides a default SystemClock implementation for Clock interface.
     * Services can override this bean if they need a custom clock implementation.
     * 
     * <p>This uses the inner SystemClock class from Clock interface which provides
     * a simple system clock implementation using {@link java.time.Instant#now()}.</p>
     * 
     * @return SystemClock instance
     */
    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock systemClock() {
        return new Clock.SystemClock();
    }
}
