package com.youtube.channelservice.infrastructure.persistence.repository;

import com.youtube.channelservice.infrastructure.persistence.entity.HandleEntity;
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
 * Spring Data JPA repository for Handle entities.
 * Provides database operations for handle reservation and management.
 */
@Repository
public interface HandleJpaRepository extends JpaRepository<HandleEntity, String> {
    
    /**
     * Finds a handle by its ID with pessimistic locking for critical operations.
     * @param handleId The handle ID
     * @return Optional containing the handle if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HandleEntity h WHERE h.id = :handleId")
    Optional<HandleEntity> findByIdWithLock(@Param("handleId") String handleId);
    
    /**
     * Checks if a handle exists and is in the specified status.
     * @param handleId The handle ID
     * @param status The handle status
     * @return true if handle exists with the status, false otherwise
     */
    boolean existsByIdAndStatus(String handleId, HandleEntity.HandleStatus status);
    
    /**
     * Finds handles by status.
     * @param status The handle status
     * @return List of handles with the specified status
     */
    java.util.List<HandleEntity> findByStatus(HandleEntity.HandleStatus status);
    
    /**
     * Finds handles by bucket for partitioning.
     * @param bucket The bucket number
     * @return List of handles in the specified bucket
     */
    java.util.List<HandleEntity> findByBucket(int bucket);
    
    /**
     * Commits a reserved handle to a channel.
     * @param handleId The handle ID
     * @param channelId The channel ID
     * @param committedAt The commit timestamp
     * @return Number of affected rows
     */
    @Modifying
    @Query("UPDATE HandleEntity h SET h.status = 'COMMITTED', h.channelId = :channelId, h.committedAt = :committedAt " +
           "WHERE h.id = :handleId AND h.status = 'RESERVED'")
    int commitHandle(@Param("handleId") String handleId, 
                    @Param("channelId") String channelId, 
                    @Param("committedAt") Instant committedAt);
    
    /**
     * Releases a handle (deletes the record).
     * @param handleId The handle ID
     * @return Number of affected rows
     */
    @Modifying
    @Query("DELETE FROM HandleEntity h WHERE h.id = :handleId")
    int releaseHandle(@Param("handleId") String handleId);
    
    /**
     * Finds handles that are expired (past their TTL).
     * @param now Current timestamp
     * @return List of expired handles
     */
    @Query("SELECT h FROM HandleEntity h WHERE h.status = 'RESERVED' AND h.reservedAt + h.ttlSeconds < :now")
    java.util.List<HandleEntity> findExpiredHandles(@Param("now") Instant now);
    
    /**
     * Cleans up expired handles.
     * @param now Current timestamp
     * @return Number of deleted handles
     */
    @Modifying
    @Query("DELETE FROM HandleEntity h WHERE h.status = 'RESERVED' AND h.reservedAt + h.ttlSeconds < :now")
    int cleanupExpiredHandles(@Param("now") Instant now);
}
