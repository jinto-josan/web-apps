package com.youtube.videotranscodeservice.domain.repositories;

import com.youtube.videotranscodeservice.domain.entities.DRMKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DRMKeyRepository {
    void save(DRMKey drmKey);
    void saveAll(List<DRMKey> drmKeys);
    Optional<DRMKey> findById(String contentKeyId);
    List<DRMKey> findByVideoId(UUID videoId);
}

