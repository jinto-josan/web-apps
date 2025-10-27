package com.youtube.channelservice.domain.repositories;

import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Branding;
import java.time.Instant;
import java.util.Optional;

/**
 * Repository interface for Channel aggregate.
 * Defines the contract for channel persistence operations.
 */
public interface ChannelRepository {
    
    /**
     * Finds a channel by its ID.
     * @param id The channel ID
     * @return Optional containing the channel if found
     */
    Optional<Channel> findById(String id);
    
    /**
     * Saves a new channel.
     * @param channel The channel to save
     * @return The saved channel with generated metadata
     */
    Channel saveNew(Channel channel);
    
    /**
     * Deletes a channel by ID.
     * @param id The channel ID to delete
     */
    void delete(String id);
    
    /**
     * Updates a channel's handle.
     * @param channelId The channel ID
     * @param oldHandle The old handle
     * @param newHandle The new handle
     * @param ifMatchEtag The ETag for optimistic concurrency control
     * @param newVersion The new version number
     * @param now The update timestamp
     * @return The updated channel
     */
    Channel updateHandle(String channelId, String oldHandle, String newHandle, 
                        String ifMatchEtag, int newVersion, Instant now);
    
    /**
     * Updates a channel's branding.
     * @param existing The existing channel
     * @param branding The new branding
     * @param ifMatchEtag The ETag for optimistic concurrency control
     * @return The updated channel
     */
    Channel updateBranding(Channel existing, Branding branding, String ifMatchEtag);
}
