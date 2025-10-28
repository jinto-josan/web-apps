package com.youtube.recommendationsservice.infrastructure.persistence;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoCandidateCosmosRepository extends CosmosRepository<VideoCandidateCosmosEntity, String> {
    VideoCandidateCosmosEntity findByVideoId(String videoId);
}

