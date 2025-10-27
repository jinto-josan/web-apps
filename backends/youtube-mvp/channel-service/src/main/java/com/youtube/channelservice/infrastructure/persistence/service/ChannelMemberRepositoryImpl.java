package com.youtube.channelservice.infrastructure.persistence.service;

import com.youtube.channelservice.domain.models.Role;
import com.youtube.channelservice.domain.repositories.ChannelMemberRepository;
import com.youtube.channelservice.infrastructure.persistence.entity.ChannelMemberEntity;
import com.youtube.channelservice.infrastructure.persistence.mapper.ChannelMapper;
import com.youtube.channelservice.infrastructure.persistence.repository.ChannelMemberJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Spring Data JPA implementation of ChannelMemberRepository.
 * Provides database operations for channel member management using JPA.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChannelMemberRepositoryImpl implements ChannelMemberRepository {
    
    private final ChannelMemberJpaRepository jpaRepository;
    private final ChannelMapper mapper = ChannelMapper.INSTANCE;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Role> roleOf(String channelId, String userId) {
        log.debug("Finding role for user {} in channel {}", userId, channelId);
        return jpaRepository.findByChannelIdAndUserId(channelId, userId)
                .map(ChannelMemberEntity::getRole);
    }
    
    @Override
    public void add(String channelId, String userId, Role role) {
        log.debug("Adding member {} with role {} to channel {}", userId, role, channelId);
        
        ChannelMemberEntity entity = mapper.toMemberEntity(role, channelId, userId);
        jpaRepository.save(entity);
    }
    
    @Override
    public void remove(String channelId, String userId) {
        log.debug("Removing member {} from channel {}", userId, channelId);
        
        int removedRows = jpaRepository.removeMember(channelId, userId);
        if (removedRows == 0) {
            log.warn("No member found to remove: user {} from channel {}", userId, channelId);
        }
    }
    
    @Override
    public Optional<Role> updateRole(String channelId, String userId, Role newRole) {
        log.debug("Updating role for user {} in channel {} to {}", userId, channelId, newRole);
        
        // Get current role
        Optional<Role> currentRole = roleOf(channelId, userId);
        
        if (currentRole.isPresent()) {
            // Update existing member
            int updatedRows = jpaRepository.updateMemberRole(channelId, userId, newRole, Instant.now());
            if (updatedRows == 0) {
                throw new IllegalStateException("Member role update failed - no rows affected");
            }
        } else {
            // Add new member
            add(channelId, userId, newRole);
        }
        
        return currentRole;
    }
}
