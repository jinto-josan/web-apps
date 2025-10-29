package com.youtube.captionssubtitlesservice.infrastructure.persistence;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.repository.support.SimpleCosmosRepository;
import com.youtube.captionssubtitlesservice.domain.entities.Caption;
import com.youtube.captionssubtitlesservice.domain.repositories.CaptionRepository;
import com.youtube.captionssubtitlesservice.domain.valueobjects.CaptionStatus;
import com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Cosmos DB implementation of CaptionRepository
 */
@Slf4j
@Repository
public class CaptionRepositoryImpl extends SimpleCosmosRepository<Caption, String> implements CaptionRepository {
    
    private final CosmosTemplate cosmosTemplate;
    
    public CaptionRepositoryImpl(CosmosEntityInformation<Caption, String> metadata, CosmosTemplate cosmosTemplate) {
        super(metadata, cosmosTemplate);
        this.cosmosTemplate = cosmosTemplate;
    }
    
    @Override
    public List<Caption> findByVideoId(String videoId) {
        String query = NL + "SELECT * FROM c WHERE c.videoId = @videoId" + NL;
        return cosmosTemplate.runQuery(query, Caption.class, videoId);
    }
    
    @Override
    public Optional<Caption> findByVideoIdAndLanguage(String videoId, LanguageCode language) {
        String query = 
                "SELECT TOP 1 * FROM c WHERE c.videoId = @videoId AND c.language = @language ORDER BY c.createdAt DESC";
        List<Caption> results = cosmosTemplate.runQuery(query, Caption.class, videoId, language.name());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public List<Caption> findByVideoIdAndStatus(String videoId, CaptionStatus status) {
        String query = "SELECT * FROM c WHERE c.videoId = @videoId AND c.status = @status";
        return cosmosTemplate.runQuery(query, Caption.class, videoId, status.name());
    }
    
    @Override
    public void delete(String id) {
        super.deleteById(id);
    }
    
    @Override
    public boolean existsByVideoIdAndLanguage(String videoId, LanguageCode language) {
        String query = 
                "SELECT TOP 1 * FROM c WHERE c.videoId = @videoId AND c.language = @language";
        List<Caption> results = cosmosTemplate.runQuery(query, Caption.class, videoId, language.name());
        return !results.isEmpty();
    }
    
    @Override
    public List<Caption> findByStatus(CaptionStatus status) {
        String query = "SELECT * FROM c WHERE c.status = @status";
        return cosmosTemplate.runQuery(query, Caption.class, status.name());
    }
}
