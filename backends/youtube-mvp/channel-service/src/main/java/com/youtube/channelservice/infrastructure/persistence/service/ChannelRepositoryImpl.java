                 package com.youtube.channelservice.infrastructure.persistence.service;

import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Branding;
import com.youtube.channelservice.domain.repositories.ChannelRepository;
import com.youtube.channelservice.infrastructure.persistence.entity.ChannelEntity;
import com.youtube.channelservice.infrastructure.persistence.mapper.ChannelMapper;
import com.youtube.channelservice.infrastructure.persistence.repository.ChannelJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Spring Data JPA implementation of ChannelRepository.
 * Provides database operations for channel management using JPA.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChannelRepositoryImpl implements ChannelRepository {
    
    private final ChannelJpaRepository jpaRepository;
    private final ChannelMapper mapper = ChannelMapper.INSTANCE;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Channel> findById(String id) {
        log.debug("Finding channel by ID: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public Channel saveNew(Channel channel) {
        log.debug("Saving new channel: {}", channel.getId());
        ChannelEntity entity = mapper.toEntity(channel);
        ChannelEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public void delete(String id) {
        log.debug("Deleting channel: {}", id);
        jpaRepository.deleteById(id);
    }
    
    @Override
    public Channel updateHandle(String channelId, String oldHandle, String newHandle, 
                                String ifMatchEtag, int newVersion, Instant now) {
        log.debug("Updating handle for channel {}: {} -> {}", channelId, oldHandle, newHandle);
        
        int updatedRows = jpaRepository.updateHandle(channelId, oldHandle, newHandle, 
                newVersion, now, ifMatchEtag);
        
        if (updatedRows == 0) {
            throw new IllegalStateException("Channel handle update failed - no rows affected");
        }
        
        return findById(channelId)
                .orElseThrow(() -> new IllegalStateException("Channel not found after update"));
    }
    
    @Override
    public Channel updateBranding(Channel existing, Branding branding, String ifMatchEtag) {
        log.debug("Updating branding for channel: {}", existing.getId());
        
        int updatedRows = jpaRepository.updateBranding(existing.getId(), 
                mapper.toBrandingEmbeddable(branding), 
                existing.getVersion() + 1, 
                Instant.now(), 
                ifMatchEtag);
        
        if (updatedRows == 0) {
            throw new IllegalStateException("Channel branding update failed - no rows affected");
        }
        
        return findById(existing.getId())
                .orElseThrow(() -> new IllegalStateException("Channel not found after update"));
    }
}
