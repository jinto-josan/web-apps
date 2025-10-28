package com.youtube.videotranscodeservice.domain.repositories;

import com.youtube.videotranscodeservice.domain.entities.MediaProcessingJob;
import com.youtube.videotranscodeservice.domain.entities.ProcessingStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaProcessingRepository {
    void save(MediaProcessingJob job);
    Optional<MediaProcessingJob> findById(String jobId);
    List<MediaProcessingJob> findByVideoId(UUID videoId);
    void updateStatus(String jobId, ProcessingStatus status);
    void update(MediaProcessingJob job);
}

