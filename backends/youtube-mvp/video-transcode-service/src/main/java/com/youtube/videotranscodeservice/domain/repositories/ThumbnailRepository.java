package com.youtube.videotranscodeservice.domain.repositories;

import com.youtube.videotranscodeservice.domain.entities.Thumbnail;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThumbnailRepository {
    void save(Thumbnail thumbnail);
    void saveAll(List<Thumbnail> thumbnails);
    List<Thumbnail> findByVideoId(UUID videoId);
    Optional<Thumbnail> findById(UUID thumbnailId);
    void setSelected(UUID videoId, UUID thumbnailId);
    Optional<Thumbnail> findSelected(UUID videoId);
}

