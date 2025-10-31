package com.youtube.contentidservice.infrastructure.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ServiceBusConfig {

    @Bean
    public ServiceBusSenderClient serviceBusSenderClient(
            @Value("${azure.servicebus.connection-string:}") String connectionString,
            @Value("${azure.servicebus.entity-name:content-id-events}") String topicName) {
        
        if (connectionString == null || connectionString.isBlank()) {
            log.warn("Service Bus connection string not configured");
            return null;
        }

        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .topicName(topicName)
                .buildClient();
    }
}

