package com.youtube.commentsservice.infrastructure.config;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebPubSubConfig {
    
    @Bean
    public WebPubSubServiceClient webPubSubClient(
            @Value("${azure.webpubsub.connection-string}") String connectionString) {
        return new WebPubSubServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }
}

