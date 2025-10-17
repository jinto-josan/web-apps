package com.youtube.identityauthservice.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.servicebus")
public class ServiceBusProperties {
    private String connectionString; // or use managed identity with namespace
    private String fullyQualifiedNamespace; // e.g. my-ns.servicebus.windows.net
    private String topicName; // or queueName
    private String queueName;
    private boolean useTopic = true;
    private boolean enabled = true;

    public String getConnectionString() { return connectionString; }
    public void setConnectionString(String connectionString) { this.connectionString = connectionString; }
    public String getFullyQualifiedNamespace() { return fullyQualifiedNamespace; }
    public void setFullyQualifiedNamespace(String fullyQualifiedNamespace) { this.fullyQualifiedNamespace = fullyQualifiedNamespace; }
    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public boolean isUseTopic() { return useTopic; }
    public void setUseTopic(boolean useTopic) { this.useTopic = useTopic; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
