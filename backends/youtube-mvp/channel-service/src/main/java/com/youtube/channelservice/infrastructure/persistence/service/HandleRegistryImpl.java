package com.youtube.channelservice.infrastructure.persistence.service;

import com.youtube.channelservice.domain.repositories.HandleRegistry;
import com.youtube.channelservice.infrastructure.persistence.entity.HandleEntity;
import com.youtube.channelservice.infrastructure.persistence.repository.HandleJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Spring Data JPA implementation of HandleRegistry.
 * Provides database operations for handle reservation and management using JPA.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HandleRegistryImpl implements HandleRegistry {
    
    private final HandleJpaRepository jpaRepository;
    
    @Override
    public boolean reserve(String handleLower, String userId, Duration ttl) {
        log.debug("Reserving handle: {} for user: {}", handleLower, userId);
        
        // Check if handle already exists
        if (jpaRepository.existsByIdAndStatus(handleLower, HandleEntity.HandleStatus.COMMITTED)) {
            log.debug("Handle {} is already committed", handleLower);
            return false;
        }
        
        // Try to create new reservation
        try {
            HandleEntity entity = HandleEntity.builder()
                    .id(handleLower)
                    .bucket(bucket(handleLower))
                    .status(HandleEntity.HandleStatus.RESERVED)
                    .reservedByUserId(userId)
                    .reservedAt(Instant.now())
                    .ttlSeconds((int) ttl.getSeconds())
                    .build();
            
            jpaRepository.save(entity);
            log.debug("Successfully reserved handle: {}", handleLower);
            return true;
            
        } catch (Exception e) {
            log.debug("Failed to reserve handle {}: {}", handleLower, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean commit(String handleLower, String channelId) {
        log.debug("Committing handle: {} to channel: {}", handleLower, channelId);
        
        int updatedRows = jpaRepository.commitHandle(handleLower, channelId, Instant.now());
        
        if (updatedRows > 0) {
            log.debug("Successfully committed handle: {}", handleLower);
            return true;
        } else {
            log.debug("Failed to commit handle: {} - not reserved or expired", handleLower);
            return false;
        }
    }
    
    @Override
    public boolean release(String handleLower) {
        log.debug("Releasing handle: {}", handleLower);
        
        int deletedRows = jpaRepository.releaseHandle(handleLower);
        
        if (deletedRows > 0) {
            log.debug("Successfully released handle: {}", handleLower);
            return true;
        } else {
            log.debug("Handle {} was not found to release", handleLower);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<String> lookupChannelId(String handleLower) {
        log.debug("Looking up channel ID for handle: {}", handleLower);
        
        return jpaRepository.findById(handleLower)
                .filter(entity -> entity.getStatus() == HandleEntity.HandleStatus.COMMITTED)
                .map(HandleEntity::getChannelId);
    }
    
    /**
     * Calculates the bucket for a handle to distribute load.
     * @param handleLower The handle in lowercase
     * @return The bucket number
     */
    private int bucket(String handleLower) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(handleLower.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            int v = ByteBuffer.wrap(hash, 0, 4).getInt() & 0x7FFFFFFF;
            return v % 128;
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate bucket for handle: " + handleLower, e);
        }
    }
}
