package com.youtube.videouploadservice.infrastructure.persistence;

import com.youtube.videouploadservice.domain.entities.VideoUpload;
import com.youtube.videouploadservice.domain.repositories.VideoUploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of VideoUploadRepository.
 */
@Repository
@RequiredArgsConstructor
public class VideoUploadRepositoryJPA implements VideoUploadRepository {
    
    private final VideoUploadEntityRepository jpaRepo;
    
    @Override
    public VideoUpload save(VideoUpload upload) {
        VideoUploadEntity entity = toEntity(upload);
        VideoUploadEntity saved = jpaRepo.save(entity);
        return toDomain(saved);
    }
    
    @Override
    public Optional<VideoUpload> findById(String uploadId) {
        return jpaRepo.findById(uploadId).map(this::toDomain);
    }
    
    @Override
    public List<VideoUpload> findByUserId(String userId) {
        return jpaRepo.findByUserId(userId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VideoUpload> findActiveUploads(String userId) {
        return jpaRepo.findByUserIdAndStatusIn(
            userId, 
            List.of(VideoUploadEntity.UploadStatus.INITIALIZING, VideoUploadEntity.UploadStatus.UPLOADING)
        ).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VideoUpload> findByStatus(VideoUpload.UploadStatus status) {
        VideoUploadEntity.UploadStatus entityStatus = mapStatus(status);
        return jpaRepo.findByStatus(entityStatus).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void updateStatus(String uploadId, VideoUpload.UploadStatus status) {
        jpaRepo.findById(uploadId).ifPresent(entity -> {
            entity.setStatus(mapStatus(status));
            entity.setUpdatedAt(Instant.now());
            jpaRepo.save(entity);
        });
    }
    
    @Override
    public void updateProgress(String uploadId, long uploadedSizeBytes) {
        jpaRepo.findById(uploadId).ifPresent(entity -> {
            entity.setUploadedSizeBytes(uploadedSizeBytes);
            entity.setUpdatedAt(Instant.now());
            jpaRepo.save(entity);
        });
    }
    
    @Override
    public void delete(String uploadId) {
        jpaRepo.deleteById(uploadId);
    }
    
    @Override
    public boolean exists(String uploadId) {
        return jpaRepo.existsById(uploadId);
    }
    
    @Override
    public List<VideoUpload> findExpiredUploads() {
        return jpaRepo.findByExpiresAtBefore(Instant.now()).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    private VideoUploadEntity toEntity(VideoUpload upload) {
        VideoUploadEntity entity = new VideoUploadEntity();
        entity.setId(upload.getId());
        entity.setUserId(upload.getUserId());
        entity.setChannelId(upload.getChannelId());
        entity.setVideoTitle(upload.getVideoTitle());
        entity.setVideoDescription(upload.getVideoDescription());
        entity.setStatus(mapStatus(upload.getStatus()));
        entity.setTotalSizeBytes(upload.getTotalSizeBytes());
        entity.setUploadedSizeBytes(upload.getUploadedSizeBytes());
        entity.setBlobName(upload.getBlobName());
        entity.setBlobContainer(upload.getBlobContainer());
        entity.setContentType(upload.getContentType());
        entity.setEtag(upload.getEtag());
        entity.setCreatedAt(upload.getCreatedAt());
        entity.setUpdatedAt(upload.getUpdatedAt());
        entity.setExpiresAt(upload.getExpiresAt());
        entity.setExpirationMinutes(upload.getExpirationMinutes());
        entity.setErrorMessage(upload.getErrorMessage());
        entity.setRetryCount(upload.getRetryCount());
        entity.setMaxRetries(upload.getMaxRetries());
        return entity;
    }
    
    private VideoUpload toDomain(VideoUploadEntity entity) {
        return VideoUpload.builder()
            .id(entity.getId())
            .userId(entity.getUserId())
            .channelId(entity.getChannelId())
            .videoTitle(entity.getVideoTitle())
            .videoDescription(entity.getVideoDescription())
            .status(mapStatus(entity.getStatus()))
            .totalSizeBytes(entity.getTotalSizeBytes())
            .uploadedSizeBytes(entity.getUploadedSizeBytes())
            .blobName(entity.getBlobName())
            .blobContainer(entity.getBlobContainer())
            .contentType(entity.getContentType())
            .etag(entity.getEtag())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .expiresAt(entity.getExpiresAt())
            .expirationMinutes(entity.getExpirationMinutes())
            .errorMessage(entity.getErrorMessage())
            .retryCount(entity.getRetryCount())
            .maxRetries(entity.getMaxRetries())
            .build();
    }
    
    private VideoUploadEntity.UploadStatus mapStatus(VideoUpload.UploadStatus status) {
        return VideoUploadEntity.UploadStatus.valueOf(status.name());
    }
    
    private VideoUpload.UploadStatus mapStatus(VideoUploadEntity.UploadStatus status) {
        return VideoUpload.UploadStatus.valueOf(status.name());
    }
}

