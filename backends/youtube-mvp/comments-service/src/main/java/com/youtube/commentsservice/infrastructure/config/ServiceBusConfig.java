package com.youtube.commentsservice.infrastructure.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ServiceBusConfig {
    
    private final ServiceBusClientBuilder clientBuilder;
    
    @Bean
    public ServiceBusSenderClient serviceBusSenderClient() {
        return clientBuilder
                .sender()
                .topicName("comment-events")
                .buildClient();
    }
}

