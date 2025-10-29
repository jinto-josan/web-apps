package com.youtube.commentsservice.domain.services;

/**
 * Port for broadcasting real-time updates via Web PubSub
 * Implemented by infrastructure layer
 */
public interface BroadcastPort {
    
    /**
     * Broadcast comment created event to connected clients
     * @param videoId the video ID
     * @param commentJson JSON representation of the comment
     */
    void broadcastCommentCreated(String videoId, String commentJson);
    
    /**
     * Broadcast comment deleted event to connected clients
     * @param videoId the video ID
     * @param commentId the deleted comment ID
     */
    void broadcastCommentDeleted(String videoId, String commentId);
}

