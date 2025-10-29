package com.youtube.commentsservice.domain.services;

import com.youtube.commentsservice.domain.events.CommentCreatedEvent;
import com.youtube.commentsservice.domain.events.CommentDeletedEvent;

/**
 * Port for publishing domain events
 * Implemented by infrastructure layer using Service Bus
 */
public interface EventPublisherPort {
    
    void publishCommentCreated(CommentCreatedEvent event);
    
    void publishCommentDeleted(CommentDeletedEvent event);
}

