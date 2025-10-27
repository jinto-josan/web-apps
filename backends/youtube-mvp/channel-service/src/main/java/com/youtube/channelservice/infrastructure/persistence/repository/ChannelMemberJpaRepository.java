package com.youtube.channelservice.infrastructure.persistence.repository;

import com.youtube.channelservice.infrastructure.persistence.entity.ChannelMemberEntity;
import com.youtube.channelservice.domain.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Channel Member entities.
 * Provides database operations for channel member management.
 */
@Repository
public interface ChannelMemberJpaRepository extends JpaRepository<ChannelMemberEntity, Long> {
    
    /**
     * Finds a member by channel ID and user ID.
     * @param channelId The channel ID
     * @param userId The user ID
     * @return Optional containing the member if found
     */
    Optional<ChannelMemberEntity> findByChannelIdAndUserId(String channelId, String userId);
    
    /**
     * Finds all members of a channel.
     * @param channelId The channel ID
     * @return List of channel members
     */
    List<ChannelMemberEntity> findByChannelIdOrderByCreatedAtAsc(String channelId);
    
    /**
     * Finds all channels where a user is a member.
     * @param userId The user ID
     * @return List of channel memberships
     */
    List<ChannelMemberEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Finds members by channel ID and role.
     * @param channelId The channel ID
     * @param role The role
     * @return List of members with the specified role
     */
    List<ChannelMemberEntity> findByChannelIdAndRole(String channelId, Role role);
    
    /**
     * Checks if a user is a member of a channel.
     * @param channelId The channel ID
     * @param userId The user ID
     * @return true if user is a member, false otherwise
     */
    boolean existsByChannelIdAndUserId(String channelId, String userId);
    
    /**
     * Updates a member's role.
     * @param channelId The channel ID
     * @param userId The user ID
     * @param newRole The new role
     * @param updatedAt The update timestamp
     * @return Number of affected rows
     */
    @Modifying
    @Query("UPDATE ChannelMemberEntity cm SET cm.role = :newRole, cm.updatedAt = :updatedAt " +
           "WHERE cm.channelId = :channelId AND cm.userId = :userId")
    int updateMemberRole(@Param("channelId") String channelId, 
                        @Param("userId") String userId, 
                        @Param("newRole") Role newRole, 
                        @Param("updatedAt") Instant updatedAt);
    
    /**
     * Removes a member from a channel.
     * @param channelId The channel ID
     * @param userId The user ID
     * @return Number of affected rows
     */
    @Modifying
    @Query("DELETE FROM ChannelMemberEntity cm WHERE cm.channelId = :channelId AND cm.userId = :userId")
    int removeMember(@Param("channelId") String channelId, @Param("userId") String userId);
    
    /**
     * Counts members in a channel by role.
     * @param channelId The channel ID
     * @param role The role
     * @return Number of members with the specified role
     */
    long countByChannelIdAndRole(String channelId, Role role);
    
    /**
     * Finds the owner of a channel.
     * @param channelId The channel ID
     * @return Optional containing the owner member
     */
    @Query("SELECT cm FROM ChannelMemberEntity cm WHERE cm.channelId = :channelId AND cm.role = 'OWNER'")
    Optional<ChannelMemberEntity> findOwnerByChannelId(@Param("channelId") String channelId);
}
