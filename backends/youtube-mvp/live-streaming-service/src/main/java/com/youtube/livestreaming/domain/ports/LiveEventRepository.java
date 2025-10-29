package com.youtube.livestreaming.domain.ports;

import com.youtube.livestreaming.domain.entities.LiveEvent;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for Live Event persistence
 */
public interface LiveEventRepository {
    LiveEvent save(LiveEvent liveEvent);
    
    Optional<LiveEvent> findById(String id);
    
    Optional<LiveEvent> findByIdAndUserId(String id, String userId);
    
    List<LiveEvent> findByChannelId(String channelId);
    
    List<LiveEvent> findByUserId(String userId);
    
    List<LiveEvent> findAll();
    
    void delete(String id);
    
    boolean exists(String id);
}

