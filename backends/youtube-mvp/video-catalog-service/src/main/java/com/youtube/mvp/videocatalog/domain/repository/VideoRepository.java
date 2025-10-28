package com.youtube.mvp.videocatalog.domain.repository;

import com.youtube.mvp.videocatalog.domain.model.Video;
import com.youtube.mvp.videocatalog.domain.model.VideoState;
import com.youtube.mvp.videocatalog.domain.model.VideoVisibility;
import java.util.List;
import java.util.Optional;

/**
 * Video repository interface (port).
 * Define what we need, not how to implement it.
 */
public interface VideoRepository {
    
    /**
     * Saves video (create or update).
     */
    Video save(Video video);
    
    /**
     * Finds video by ID.
     */
    Optional<Video> findById(String videoId);
    
    /**
     * Finds videos by channel.
     */
    List<Video> findByChannelId(String channelId, int page, int size);
    
    /**
     * Finds videos by state.
     */
    List<Video> findByState(VideoState state, int page, int size);
    
    /**
     * Finds videos by visibility.
     */
    List<Video> findByVisibility(VideoVisibility visibility, int page, int size);
    
    /**
     * Checks if video exists.
     */
    boolean existsById(String videoId);
    
    /**
     * Deletes video.
     */
    void deleteById(String videoId);
    
    /**
     * Updates video version (ETag).
     */
    void updateVersion(String videoId, String version);
}

