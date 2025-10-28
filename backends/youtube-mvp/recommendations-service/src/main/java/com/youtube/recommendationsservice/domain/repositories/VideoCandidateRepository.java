package com.youtube.recommendationsservice.domain.repositories;

import com.youtube.recommendationsservice.domain.entities.VideoCandidate;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;
import com.youtube.recommendationsservice.domain.valueobjects.VideoId;

import java.util.List;
import java.util.Optional;

public interface VideoCandidateRepository {
    List<VideoCandidate> findCandidatesForUser(UserId userId, int limit);
    List<VideoCandidate> findCandidatesForVideo(VideoId videoId, int limit);
    Optional<VideoCandidate> findById(VideoId videoId);
    List<VideoCandidate> findByIds(List<VideoId> videoIds);
}

