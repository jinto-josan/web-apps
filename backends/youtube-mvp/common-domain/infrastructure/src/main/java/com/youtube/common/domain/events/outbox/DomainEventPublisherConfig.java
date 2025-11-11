package com.youtube.common.domain.events.outbox;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Domain Event Publisher background service.
 * 
 * <p>Configures MessagePublisher beans for common-domain OutboxDispatcher.
 * This is a background service that polls the outbox table and publishes domain
 * events to various message brokers (Service Bus, Kafka, etc.).</p>
 */
@Configuration
@EnableConfigurationProperties({DomainEventPublisherProperties.class})
public class DomainEventPublisherConfig {

    /**
     * Creates ServiceBusMessagePublisher bean for Azure Service Bus.
     * 
     * <p>This is the default implementation when Service Bus is configured.
     * The MessagePublisher abstraction allows switching to Kafka or other
     * brokers without changing OutboxDispatcher code.</p>
     */
    @Bean
    @ConditionalOnProperty(name = "outbox.domain-event-publisher.enabled", havingValue = "true")
    @ConditionalOnProperty(name = "outbox.domain-event-publisher.backend.type", havingValue = "servicebus", matchIfMissing = true)
    public MessagePublisher serviceBusMessagePublisher(ServiceBusSenderClient serviceBusSender) {
        return new ServiceBusMessagePublisher(serviceBusSender);
    }

    /**
     * Creates ServiceBusSenderClient bean for Azure Service Bus.
     * 
     * <p>The client is configured using DomainEventPublisherProperties which supports:
     * - Connection string authentication
     * - Managed identity authentication (with or without namespace)
     * - Both topics and queues</p>
     */
    @Bean
    @ConditionalOnProperty(name = "outbox.domain-event-publisher.enabled", havingValue = "true")
    @ConditionalOnProperty(name = "outbox.domain-event-publisher.backend.type", havingValue = "servicebus", matchIfMissing = true)
    public ServiceBusSenderClient serviceBusSenderClient(DomainEventPublisherProperties props) {
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder();
        
        if (props.getConnectionString() != null && !props.getConnectionString().isBlank()) {
            builder.connectionString(props.getConnectionString());
        } else if (props.getFullyQualifiedNamespace() != null && !props.getFullyQualifiedNamespace().isBlank()) {
            // Use managed identity with namespace
            builder.credential(
                props.getFullyQualifiedNamespace(),
                new DefaultAzureCredentialBuilder().build()
            );
        } else {
            // Fallback to managed identity (default credential)
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        
        // Build sender client for topic or queue
        if (props.isUseTopic() && props.getTopicName() != null) {
            return builder.sender()
                .topicName(props.getTopicName())
                .buildClient();
        } else if (!props.isUseTopic() && props.getQueueName() != null) {
            return builder.sender()
                .queueName(props.getQueueName())
                .buildClient();
        } else {
            throw new IllegalArgumentException(
                "Either topic-name or queue-name must be configured in outbox.domain-event-publisher.backend.* properties"
            );
        }
    }
}

