package com.youtube.channelservice.domain.services;

/**
 * Domain service interface for event publishing.
 * Defines the contract for publishing domain events.
 */
public interface EventPublisher {
    
    /**
     * Publishes a channel created event.
     * @param event The channel created event
     */
    void publishChannelCreated(ChannelCreated event);
    
    /**
     * Publishes a channel handle changed event.
     * @param event The channel handle changed event
     */
    void publishChannelHandleChanged(ChannelHandleChanged event);
    
    /**
     * Publishes a channel updated event.
     * @param channelId The channel ID
     * @param changedFields The fields that were changed
     */
    void publishChannelUpdated(String channelId, String changedFields);
    
    /**
     * Publishes a member role changed event.
     * @param event The member role changed event
     */
    void publishMemberRoleChanged(ChannelMemberRoleChanged event);
}
