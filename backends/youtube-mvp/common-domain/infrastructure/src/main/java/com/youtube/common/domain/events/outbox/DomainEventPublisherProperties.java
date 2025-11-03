package com.youtube.common.domain.events.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Domain Event Publisher background service.
 * 
 * <p>Configures the connection to message brokers (Service Bus, Kafka, etc.) for publishing domain events.
 * Supports multiple broker types via the {@code type} property.</p>
 */
@ConfigurationProperties(prefix = "outbox.domain-event-publisher.backend")
public class DomainEventPublisherProperties {
    
    /**
     * Broker type: "servicebus", "kafka", etc.
     * Default: "servicebus"
     */
    private String type = "servicebus";
    
    /**
     * Connection string for Azure Service Bus.
     * Format: Endpoint=sb://...;SharedAccessKeyName=...;SharedAccessKey=...
     */
    private String connectionString;
    
    /**
     * Fully qualified namespace for managed identity authentication (Service Bus).
     * Example: my-namespace.servicebus.windows.net
     */
    private String fullyQualifiedNamespace;
    
    /**
     * Topic name for publishing events (when using topics).
     */
    private String topicName;
    
    /**
     * Queue name for publishing events (when using queues - Service Bus only).
     */
    private String queueName;
    
    /**
     * Whether to use topic (true) or queue (false) - Service Bus only.
     * Default: true
     */
    private boolean useTopic = true;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    public void setFullyQualifiedNamespace(String fullyQualifiedNamespace) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public boolean isUseTopic() {
        return useTopic;
    }

    public void setUseTopic(boolean useTopic) {
        this.useTopic = useTopic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

