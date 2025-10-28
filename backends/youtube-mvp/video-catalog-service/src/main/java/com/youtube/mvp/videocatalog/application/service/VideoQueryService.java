package com.youtube.mvp.videocatalog.application.service;

import com.youtube.mvp.videocatalog.application.dto.PagedResponse;
import com.youtube.mvp.videocatalog.application.dto.VideoResponse;
import com.youtube.mvp.videocatalog.application.mapper.VideoMapper;
import com.youtube.mvp.videocatalog.domain.model.Video;
import com.youtube.mvp.videocatalog.domain.model.VideoState;
import com.youtube.mvp.videocatalog.domain.model.VideoVisibility;
import com.youtube.mvp.videocatalog.domain.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query service for video operations (CQRS read side).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoQueryService {
    
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;
    
    /**
     * Gets a video by ID.
     */
    public VideoResponse getVideo(String videoId) {
        log.debug("Getting video: {}", videoId);
        
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + videoId));
        
        return videoMapper.toResponse(video);
    }
    
    /**
     * Gets videos by channel.
     */
    public PagedResponse<VideoResponse> getVideosByChannel(String channelId, int page, int size) {
        log.debug("Getting videos for channel: {}, page: {}, size: {}", channelId, page, size);
        
        List<Video> videos = videoRepository.findByChannelId(channelId, page, size);
        List<VideoResponse> content = videos.stream()
                .map(videoMapper::toResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<VideoResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(videos.size())
                .totalPages((int) Math.ceil((double) videos.size() / size))
                .hasNext(videos.size() > (page + 1) * size)
                .hasPrevious(page > 0)
                .build();
    }
    
    /**
     * Gets videos by state.
     */
    public PagedResponse<VideoResponse> getVideosByState(String state, int page, int size) {
        log.debug("Getting videos by state: {}, page: {}, size: {}", state, page, size);
        
        VideoState videoState = VideoState.valueOf(state.toUpperCase());
        List<Video> videos = videoRepository.findByState(videoState, page, size);
        
        List<VideoResponse> content = videos.stream()
                .map(videoMapper::toResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<VideoResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(videos.size())
                .totalPages((int) Math.ceil((double) videos.size() / size))
                .hasNext(videos.size() > (page + 1) * size)
                .hasPrevious(page > 0)
                .build();
    }
    
    /**
     * Gets videos by visibility.
     */
    public PagedResponse<VideoResponse> getVideosByVisibility(String visibility, int page, int size) {
        log.debug("Getting videos by visibility: {}, page: {}, size: {}", visibility, page, size);
        
        VideoVisibility videoVisibility = VideoVisibility.valueOf(visibility.toUpperCase());
        List<Video> videos = videoRepository.findByVisibility(videoVisibility, page, size);
        
        List<VideoResponse> content = videos.stream()
                .map(videoMapper::toResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<VideoResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(videos.size())
                .totalPages((int) Math.ceil((double) videos.size() / size))
                .hasNext(videos.size() > (page + 1) * size)
                .hasPrevious(page > 0)
                .build();
    }
}

