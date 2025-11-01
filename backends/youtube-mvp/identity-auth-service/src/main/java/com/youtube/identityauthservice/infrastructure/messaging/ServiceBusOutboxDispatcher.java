package com.youtube.identityauthservice.infrastructure.messaging;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.*;
import com.youtube.identityauthservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.youtube.identityauthservice.infrastructure.config.ServiceBusProperties;
import com.youtube.identityauthservice.infrastructure.persistence.OutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
public class ServiceBusOutboxDispatcher {

    private final OutboxRepository repo;
    private final ServiceBusSenderClient sender;

    public ServiceBusOutboxDispatcher(OutboxRepository repo, ServiceBusProperties props) {
        this.repo = repo;


        ServiceBusClientBuilder builder = new ServiceBusClientBuilder();
        if (props.getConnectionString() != null && !props.getConnectionString().isBlank()) {
            builder = builder.connectionString(props.getConnectionString());
        } else {
            builder = builder.credential(props.getFullyQualifiedNamespace(), new DefaultAzureCredentialBuilder().build());
        }

        this.sender = props.isUseTopic()
                ? builder.sender().topicName(props.getTopicName()).buildClient()
                : builder.sender().queueName(props.getQueueName()).buildClient();
    }

    @Scheduled(fixedDelayString = "${app.outbox.dispatch-interval-ms:2000}")
    @Transactional
    public void dispatch() {
        List<OutboxEventEntity> batch = repo.findTop100ByDispatchedAtIsNullOrderByCreatedAtAsc();
        for (OutboxEventEntity evt : batch) {
            try {
                ServiceBusMessage msg = new ServiceBusMessage(evt.getPayload());
                // Use outbox id for duplicate detection
                msg.setMessageId(evt.getId());
                msg.setSubject(evt.getEventType());
                msg.setContentType("application/json");

                // Enrich with useful metadata
                if (evt.getAggregateType() != null) msg.getApplicationProperties().put("aggregateType", evt.getAggregateType());
                if (evt.getAggregateId() != null) msg.getApplicationProperties().put("aggregateId", evt.getAggregateId());
                if (evt.getOccurredAt() != null) msg.getApplicationProperties().put("occurredAt", evt.getOccurredAt().toString());
                if (evt.getCausationId() != null) msg.getApplicationProperties().put("causationId", evt.getCausationId());
                if (evt.getTraceparent() != null) msg.getApplicationProperties().put("traceparent", evt.getTraceparent());

                if (evt.getCorrelationId() != null) msg.setCorrelationId(evt.getCorrelationId());
                if (evt.getPartitionKey() != null) msg.setPartitionKey(evt.getPartitionKey());

                sender.sendMessage(msg);

                // Persist dispatch info (we set messageId above; you can also keep SB sequence number if you read it elsewhere)
                repo.markDispatched(evt.getId(), msg.getMessageId(), Instant.now());
            } catch (Exception e) {
                repo.markFailed(evt.getId(), truncate(e.getMessage(), 3900));
            }
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}