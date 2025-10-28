package com.youtube.videouploadservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * JPA Repository for VideoUploadEntity.
 */
@Repository
public interface VideoUploadEntityRepository extends JpaRepository<VideoUploadEntity, String> {
    
    List<VideoUploadEntity> findByUserId(String userId);
    
    List<VideoUploadEntity> findByUserIdAndStatusIn(String userId, List<VideoUploadEntity.UploadStatus> statuses);
    
    List<VideoUploadEntity> findByStatus(VideoUploadEntity.UploadStatus status);
    
    List<VideoUploadEntity> findByExpiresAtBefore(Instant expiresAt);
}

