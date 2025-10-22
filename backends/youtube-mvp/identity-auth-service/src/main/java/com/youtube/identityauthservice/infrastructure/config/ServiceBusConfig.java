package com.youtube.identityauthservice.infrastructure.config;

import com.youtube.identityauthservice.infrastructure.messaging.ServiceBusOutboxDispatcher;
import com.youtube.identityauthservice.infrastructure.persistence.OutboxRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({ServiceBusProperties.class})
public class ServiceBusConfig {


    @Bean
    public ServiceBusOutboxDispatcher outboxDispatcher(OutboxRepository repo,
                                                       ServiceBusProperties props) {
        if (!props.isEnabled()) return null;
        return new ServiceBusOutboxDispatcher(repo, props);
    }
}
