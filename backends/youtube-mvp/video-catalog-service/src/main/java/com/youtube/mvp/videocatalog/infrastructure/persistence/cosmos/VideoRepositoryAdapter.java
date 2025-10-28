package com.youtube.mvp.videocatalog.infrastructure.persistence.cosmos;

import com.youtube.mvp.videocatalog.domain.model.*;
import com.youtube.mvp.videocatalog.domain.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing VideoRepository using Cosmos DB.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VideoRepositoryAdapter implements VideoRepository {
    
    private final VideoCosmosRepository cosmosRepository;
    private final VideoCosmosMapper mapper;
    
    @Override
    public Video save(Video video) {
        log.debug("Saving video: {}", video.getVideoId());
        
        VideoCosmosEntity entity = mapper.toEntity(video);
        VideoCosmosEntity saved = cosmosRepository.save(entity);
        
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Video> findById(String videoId) {
        log.debug("Finding video by ID: {}", videoId);
        
        return cosmosRepository.findById(videoId)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<Video> findByChannelId(String channelId, int page, int size) {
        log.debug("Finding videos by channel: {}, page: {}, size: {}", channelId, page, size);
        
        List<VideoCosmosEntity> entities = cosmosRepository.findByChannelId(channelId);
        
        // Manual pagination (Cosmos DB doesn't support native pagination in simple queries)
        int start = page * size;
        int end = Math.min(start + size, entities.size());
        
        if (start >= entities.size()) {
            return List.of();
        }
        
        return entities.subList(start, end).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Video> findByState(VideoState state, int page, int size) {
        log.debug("Finding videos by state: {}, page: {}, size: {}", state, page, size);
        
        List<VideoCosmosEntity> entities = cosmosRepository.findByState(state.name());
        
        int start = page * size;
        int end = Math.min(start + size, entities.size());
        
        if (start >= entities.size()) {
            return List.of();
        }
        
        return entities.subList(start, end).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Video> findByVisibility(VideoVisibility visibility, int page, int size) {
        log.debug("Finding videos by visibility: {}, page: {}, size: {}", visibility, page, size);
        
        List<VideoCosmosEntity> entities = cosmosRepository.findByVisibility(visibility.name());
        
        int start = page * size;
        int end = Math.min(start + size, entities.size());
        
        if (start >= entities.size()) {
            return List.of();
        }
        
        return entities.subList(start, end).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsById(String videoId) {
        return cosmosRepository.existsByVideoId(videoId);
    }
    
    @Override
    public void deleteById(String videoId) {
        log.debug("Deleting video: {}", videoId);
        cosmosRepository.deleteByVideoId(videoId);
    }
    
    @Override
    public void updateVersion(String videoId, String version) {
        log.debug("Updating version for video: {}, version: {}", videoId, version);
        
        Optional<Video> optional = findById(videoId);
        if (optional.isPresent()) {
            Video video = optional.get();
            Video updated = video.toBuilder()
                    .version(version)
                    .build();
            save(updated);
        }
    }
}

