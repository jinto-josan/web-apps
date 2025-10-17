package com.youtube.identityauthservice.infrastructure.messaging;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.youtube.identityauthservice.domain.model.OutboxEvent;
import com.youtube.identityauthservice.infrastructure.persistence.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public class ServiceBusOutboxDispatcher {

    private final OutboxEventRepository repo;
    private final ServiceBusSenderClient sender;

    public ServiceBusOutboxDispatcher(OutboxEventRepository repo, ServiceBusProperties props) {
        this.repo = repo;

        ServiceBusClientBuilder builder = new ServiceBusClientBuilder();
        if (props.getConnectionString() != null && !props.getConnectionString().isBlank()) {
            builder = builder.connectionString(props.getConnectionString());
        } else {
            builder = builder.credential(props.getFullyQualifiedNamespace(), new DefaultAzureCredentialBuilder().build());
        }

        if (props.isUseTopic()) {
            this.sender = builder.sender().topicName(props.getTopicName()).buildClient();
        } else {
            this.sender = builder.sender().queueName(props.getQueueName()).buildClient();
        }
    }

    @Scheduled(fixedDelayString = "${app.outbox.dispatch-interval-ms:2000}")
    public void dispatch() {
        List<OutboxEvent> batch = repo.findNextPendingBatch(100);
        for (OutboxEvent evt : batch) {
            try {
                ServiceBusMessage msg = new ServiceBusMessage(evt.getPayload());
                msg.setMessageId(evt.getId()); // enable duplicate detection on entity
                msg.setSubject(evt.getType());
                msg.setContentType("application/json");
                msg.getApplicationProperties().put("aggregateType", evt.getAggregateType());
                msg.getApplicationProperties().put("aggregateId", evt.getAggregateId());
                msg.getApplicationProperties().put("occurredAt", evt.getOccurredAt().toString());
                if (evt.getCorrelationId() != null) msg.setCorrelationId(evt.getCorrelationId());
                if (evt.getPartitionKey() != null) msg.setPartitionKey(evt.getPartitionKey());
                sender.sendMessage(msg);

                repo.markDispatched(evt.getId());
            } catch (Exception e) {
                repo.incrementAttempt(evt.getId(), e.getMessage());
            }
        }
    }
}
