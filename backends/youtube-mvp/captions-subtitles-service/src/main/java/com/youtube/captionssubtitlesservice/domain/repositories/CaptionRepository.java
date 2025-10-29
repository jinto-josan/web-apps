package com.youtube.captionssubtitlesservice.domain.repositories;

import com.youtube.captionssubtitlesservice.domain.entities.Caption;
import com.youtube.captionssubtitlesservice.domain.valueobjects.CaptionStatus;
import com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode;
import com.youtube.captucentinasservice.domain.valueobjects.SourceType;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Caption entity
 */
public interface CaptionRepository {
    
    Caption save(Caption caption);
    
    Optional<Caption> findById(String id);
    
    List<Caption> findByVideoId(String videoId);
    
    Optional<Caption> findByVideoIdAndLanguage(String videoId, LanguageCode language);
    
    List<Caption> findByVideoIdAndStatus(String videoId, CaptionStatus status);
    
    void delete(String id);
    
    boolean existsByVideoIdAndLanguage(String videoId, LanguageCode language);
    
    List<Caption> findByStatus(CaptionStatus status);
}
