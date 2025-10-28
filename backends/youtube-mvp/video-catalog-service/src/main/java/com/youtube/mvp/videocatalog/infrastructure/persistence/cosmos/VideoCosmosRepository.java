package com.youtube.mvp.videocatalog.infrastructure.persistence.cosmos;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Cosmos DB repository for videos.
 */
@Repository
public interface VideoCosmosRepository extends org.springframework.data.repository.CrudRepository<VideoCosmosEntity, String> {
    
    List<VideoCosmosEntity> findByChannelId(String channelId);
    
    List<VideoCosmosEntity> findByState(String state);
    
    List<VideoCosmosEntity> findByVisibility(String visibility);
    
    boolean existsByVideoId(String videoId);
    
    void deleteByVideoId(String videoId);
}

