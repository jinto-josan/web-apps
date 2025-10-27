package com.youtube.channelservice.domain.repositories;

import com.youtube.channelservice.domain.models.Role;
import java.util.Optional;

/**
 * Repository interface for Channel Member aggregate.
 * Defines the contract for channel member persistence operations.
 */
public interface ChannelMemberRepository {
    
    /**
     * Gets the role of a user in a channel.
     * @param channelId The channel ID
     * @param userId The user ID
     * @return Optional containing the role if the user is a member
     */
    Optional<Role> roleOf(String channelId, String userId);
    
    /**
     * Adds a member to a channel with the specified role.
     * @param channelId The channel ID
     * @param userId The user ID
     * @param role The role to assign
     */
    void add(String channelId, String userId, Role role);
    
    /**
     * Removes a member from a channel.
     * @param channelId The channel ID
     * @param userId The user ID
     */
    void remove(String channelId, String userId);
    
    /**
     * Updates the role of a member in a channel.
     * @param channelId The channel ID
     * @param userId The user ID
     * @param newRole The new role
     * @return Optional containing the old role if the user was already a member
     */
    Optional<Role> updateRole(String channelId, String userId, Role newRole);
}
