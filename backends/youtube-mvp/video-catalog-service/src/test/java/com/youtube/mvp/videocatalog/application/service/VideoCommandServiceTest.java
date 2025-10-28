package com.youtube.mvp.videocatalog.application.service;

import com.youtube.mvp.videocatalog.application.dto.*;
import com.youtube.mvp.videocatalog.application.mapper.VideoMapper;
import com.youtube.mvp.videocatalog.domain.model.Video;
import com.youtube.mvp.videocatalog.domain.model.VideoState;
import com.youtube.mvp.videocatalog.domain.model.VideoVisibility;
import com.youtube.mvp.videocatalog.domain.repository.VideoRepository;
import com.youtube.mvp.videocatalog.domain.service.VideoDomainService;
import com.youtube.mvp.videocatalog.infrastructure.messaging.VideoEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoCommandServiceTest {
    
    @Mock
    private VideoRepository videoRepository;
    
    @Mock
    private VideoMapper videoMapper;
    
    @Mock
    private VideoDomainService domainService;
    
    @Mock
    private VideoEventPublisher eventPublisher;
    
    @InjectMocks
    private VideoCommandService commandService;
    
    private Video testVideo;
    
    @BeforeEach
    void setUp() {
        testVideo = Video.builder()
                .videoId("video-123")
                .title("Test Video")
                .description("Test Description")
                .channelId("channel-123")
                .ownerId("owner-123")
                .state(VideoState.DRAFT)
                .visibility(VideoVisibility.PUBLIC)
                .version("v1")
                .build();
    }
    
    @Test
    void createVideo_shouldCreateSuccessfully() {
        // Given
        CreateVideoRequest request = CreateVideoRequest.builder()
                .title("New Video")
                .description("Description")
                .channelId("channel-123")
                .ownerId("owner-123")
                .language("en")
                .visibility("PUBLIC")
                .build();
        
        VideoResponse response = VideoResponse.builder()
                .videoId("video-456")
                .title("New Video")
                .build();
        
        when(domainService.generateVersion()).thenReturn("v1");
        when(videoRepository.save(any(Video.class))).thenReturn(testVideo);
        when(videoMapper.toResponse(any(Video.class))).thenReturn(response);
        
        // When
        VideoResponse result = commandService.createVideo(request);
        
        // Then
        assertThat(result).isNotNull();
        verify(videoRepository).save(any(Video.class));
        verify(domainService).generateVersion();
    }
    
    @Test
    void publishVideo_shouldPublishSuccessfully() {
        // Given
        when(videoRepository.findById("video-123")).thenReturn(Optional.of(testVideo));
        when(domainService.generateVersion()).thenReturn("v2");
        when(videoRepository.save(any(Video.class))).thenReturn(testVideo);
        when(videoMapper.toResponse(any(Video.class))).thenReturn(VideoResponse.builder().build());
        
        // When
        commandService.publishVideo("video-123");
        
        // Then
        verify(eventPublisher).publishVideoPublishedEvent(any());
        verify(videoRepository, times(2)).save(any(Video.class));
    }
    
    @Test
    void publishVideo_shouldThrowWhenVideoNotFound() {
        // Given
        when(videoRepository.findById("video-123")).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> commandService.publishVideo("video-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}

