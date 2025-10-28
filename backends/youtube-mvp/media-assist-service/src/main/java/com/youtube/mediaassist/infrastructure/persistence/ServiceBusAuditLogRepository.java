package com.youtube.mediaassist.infrastructure.persistence;

import com.youtube.mediaassist.domain.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.azure.messaging.servicebus.ServiceBusMessageBuilder;
import org.springframework.cloud.azure.messaging.servicebus.ServiceBusTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Service Bus-based audit logging repository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ServiceBusAuditLogRepository implements AuditLogRepository {
    
    @Value("${azure.servicebus.audit-topic:audit-logs}")
    private String auditTopic;
    
    private final ServiceBusTemplate serviceBusTemplate;
    
    @Override
    public void log(AuditEvent event) {
        try {
            String messageBody = serializeEvent(event);
            serviceBusTemplate.sendAsync(auditTopic, 
                ServiceBusMessageBuilder
                    .withBody(messageBody)
                    .setHeader("eventType", "BlobAccess")
                    .setHeader("userId", event.userId())
                    .build()
            );
            log.debug("Sent audit log event: {}", event);
        } catch (Exception e) {
            log.error("Failed to send audit log event", e);
        }
    }
    
    private String serializeEvent(AuditEvent event) {
        // Simple JSON serialization
        return String.format(
            "{\"userId\":\"%s\",\"operation\":\"%s\",\"resourcePath\":\"%s\"," +
            "\"status\":\"%s\",\"details\":\"%s\",\"ipAddress\":\"%s\",\"timestamp\":\"%s\"}",
            event.userId(),
            event.operation(),
            event.resourcePath(),
            event.status(),
            event.details(),
            event.ipAddress(),
            event.timestamp()
        );
    }
}

