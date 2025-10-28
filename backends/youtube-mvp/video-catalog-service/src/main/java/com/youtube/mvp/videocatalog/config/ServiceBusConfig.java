package com.youtube.mvp.videocatalog.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Service Bus configuration.
 */
@Configuration
@Slf4j
public class ServiceBusConfig {
    
    @Value("${azure.servicebus.connection-string}")
    private String connectionString;
    
    @Value("${azure.servicebus.topic-name:video-events}")
    private String topicName;
    
    @Bean
    public ServiceBusSenderClient serviceBusSenderClient() {
        log.info("Configuring Service Bus sender for topic: {}", topicName);
        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .topicName(topicName)
                .buildClient();
    }
}

