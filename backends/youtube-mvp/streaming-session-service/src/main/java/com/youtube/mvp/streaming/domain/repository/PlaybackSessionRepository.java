package com.youtube.mvp.streaming.domain.repository;

import com.youtube.mvp.streaming.domain.model.PlaybackSession;
import java.util.Optional;

/**
 * Playback session repository interface.
 */
public interface PlaybackSessionRepository {
    
    PlaybackSession save(PlaybackSession session);
    
    Optional<PlaybackSession> findById(String sessionId);
    
    Optional<PlaybackSession> findByVideoIdAndUserId(String videoId, String userId);
    
    boolean existsById(String sessionId);
    
    void deleteById(String sessionId);
}

