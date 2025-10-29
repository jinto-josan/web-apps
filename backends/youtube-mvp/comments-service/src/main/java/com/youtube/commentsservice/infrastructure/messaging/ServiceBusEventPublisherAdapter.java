package com.youtube.commentsservice.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.commentsservice.domain.events.CommentCreatedEvent;
import com.youtube.commentsservice.domain.events.CommentDeletedEvent;
import com.youtube.commentsservice.domain.services.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceBusEventPublisherAdapter implements EventPublisherPort {
    
    private final ServiceBusTopicSender topicSender;
    private final ObjectMapper objectMapper;
    
    @Value("${azure.servicebus.comment-events.topic-name:comment-events}")
    private String topicName;
    
    @Override
    public void publishCommentCreated(CommentCreatedEvent event) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventType", "CommentCreated");
            payload.put("commentId", event.getCommentId());
            payload.put("videoId", event.getVideoId());
            payload.put("authorId", event.getAuthorId());
            payload.put("parentId", event.getParentId());
            payload.put("text", event.getText());
            payload.put("timestamp", event.getTimestamp());
            
            String messageBody = objectMapper.writeValueAsString(payload);
            topicSender.send(topicName, messageBody, createMessageProperties(event.getCommentId()));
            
            log.info("Published CommentCreated event for comment: {}", event.getCommentId());
        } catch (Exception e) {
            log.error("Failed to publish CommentCreated event", e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
    
    @Override
    public void publishCommentDeleted(CommentDeletedEvent event) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventType", "CommentDeleted");
            payload.put("commentId", event.getCommentId());
            payload.put("videoId", event.getVideoId());
            payload.put("authorId", event.getAuthorId());
            payload.put("parentId", event.getParentId());
            payload.put("timestamp", event.getTimestamp());
            
            String messageBody = objectMapper.writeValueAsString(payload);
            topicSender.send(topicName, messageBody, createMessageProperties(event.getCommentId()));
            
            log.info("Published CommentDeleted event for comment: {}", event.getCommentId());
        } catch (Exception e) {
            log.error("Failed to publish CommentDeleted event", e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
    
    private Map<String, String> createMessageProperties(String commentId) {
        Map<String, String> properties = new HashMap<>();
        properties.put("messageId", commentId);
        return properties;
    }
}

