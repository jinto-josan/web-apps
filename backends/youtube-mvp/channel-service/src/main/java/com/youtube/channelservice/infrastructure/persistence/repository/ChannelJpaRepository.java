package com.youtube.channelservice.infrastructure.persistence.repository;

import com.youtube.channelservice.infrastructure.persistence.entity.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;

/**
 * Spring Data JPA repository for Channel entities.
 * Provides database operations for channel management.
 */
@Repository
public interface ChannelJpaRepository extends JpaRepository<ChannelEntity, String> {
    
    /**
     * Finds a channel by its handle (case-insensitive).
     * @param handleLower The handle in lowercase
     * @return Optional containing the channel if found
     */
    Optional<ChannelEntity> findByHandleLower(String handleLower);
    
    /**
     * Finds channels by owner user ID.
     * @param ownerUserId The owner user ID
     * @return List of channels owned by the user
     */
    @Query("SELECT c FROM ChannelEntity c WHERE c.ownerUserId = :ownerUserId ORDER BY c.createdAt DESC")
    java.util.List<ChannelEntity> findByOwnerUserId(@Param("ownerUserId") String ownerUserId);
    
    /**
     * Checks if a handle exists.
     * @param handleLower The handle in lowercase
     * @return true if handle exists, false otherwise
     */
    boolean existsByHandleLower(String handleLower);
    
    /**
     * Updates a channel's handle with optimistic locking.
     * @param channelId The channel ID
     * @param oldHandle The old handle
     * @param newHandle The new handle
     * @param newVersion The new version number
     * @param updatedAt The update timestamp
     * @param etag The ETag for optimistic concurrency control
     * @return Number of affected rows
     */
    @Modifying
    @Query("UPDATE ChannelEntity c SET c.handleLower = :newHandle, c.version = :newVersion, c.updatedAt = :updatedAt " +
           "WHERE c.id = :channelId AND c.handleLower = :oldHandle AND c.etag = :etag")
    int updateHandle(@Param("channelId") String channelId, 
                    @Param("oldHandle") String oldHandle, 
                    @Param("newHandle") String newHandle, 
                    @Param("newVersion") int newVersion, 
                    @Param("updatedAt") Instant updatedAt, 
                    @Param("etag") String etag);
    
    /**
     * Updates a channel's branding with optimistic locking.
     * @param channelId The channel ID
     * @param branding The new branding
     * @param newVersion The new version number
     * @param updatedAt The update timestamp
     * @param etag The ETag for optimistic concurrency control
     * @return Number of affected rows
     */
    @Modifying
    @Query("UPDATE ChannelEntity c SET c.branding = :branding, c.version = :newVersion, c.updatedAt = :updatedAt " +
           "WHERE c.id = :channelId AND c.etag = :etag")
    int updateBranding(@Param("channelId") String channelId, 
                      @Param("branding") com.youtube.channelservice.infrastructure.persistence.entity.BrandingEmbeddable branding, 
                      @Param("newVersion") int newVersion, 
                      @Param("updatedAt") Instant updatedAt, 
                      @Param("etag") String etag);
    
    /**
     * Finds a channel with pessimistic locking for critical operations.
     * @param channelId The channel ID
     * @return Optional containing the channel if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChannelEntity c WHERE c.id = :channelId")
    Optional<ChannelEntity> findByIdWithLock(@Param("channelId") String channelId);
}
