package com.youtube.mvp.videocatalog.infrastructure.persistence.cosmos;

import com.youtube.mvp.videocatalog.domain.model.Video;
import com.youtube.mvp.videocatalog.domain.model.VideoState;
import com.youtube.mvp.videocatalog.domain.model.VideoVisibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoRepositoryAdapterTest {
    
    @Mock
    private VideoCosmosRepository cosmosRepository;
    
    @Mock
    private VideoCosmosMapper mapper;
    
    @InjectMocks
    private VideoRepositoryAdapter adapter;
    
    private Video testVideo;
    private VideoCosmosEntity testEntity;
    
    @BeforeEach
    void setUp() {
        testVideo = Video.builder()
                .videoId("video-123")
                .title("Test Video")
                .channelId("channel-123")
                .ownerId("owner-123")
                .state(VideoState.DRAFT)
                .visibility(VideoVisibility.PUBLIC)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        testEntity = VideoCosmosEntity.builder()
                .videoId("video-123")
                .title("Test Video")
                .channelId("channel-123")
                .ownerId("owner-123")
                .state("DRAFT")
                .visibility("PUBLIC")
                .build();
    }
    
    @Test
    void save_shouldSaveVideo() {
        // Given
        when(mapper.toEntity(any(Video.class))).thenReturn(testEntity);
        when(cosmosRepository.save(any(VideoCosmosEntity.class))).thenReturn(testEntity);
        when(mapper.toDomain(any(VideoCosmosEntity.class))).thenReturn(testVideo);
        
        // When
        Video saved = adapter.save(testVideo);
        
        // Then
        assertThat(saved).isNotNull();
        verify(cosmosRepository).save(any(VideoCosmosEntity.class));
    }
    
    @Test
    void findById_shouldReturnVideo() {
        // Given
        when(cosmosRepository.findById("video-123")).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testVideo);
        
        // When
        Optional<Video> result = adapter.findById("video-123");
        
        // Then
        assertThat(result).isPresent();
        verify(cosmosRepository).findById("video-123");
    }
    
    @Test
    void deleteById_shouldDeleteVideo() {
        // When
        adapter.deleteById("video-123");
        
        // Then
        verify(cosmosRepository).deleteByVideoId("video-123");
    }
}

