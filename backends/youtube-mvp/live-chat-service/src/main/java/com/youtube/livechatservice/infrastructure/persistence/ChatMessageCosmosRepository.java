package com.youtube.livechatservice.infrastructure.persistence;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ChatMessageCosmosRepository extends CosmosRepository<ChatMessageDocument, String> {
    List<ChatMessageDocument> findTop200ByLiveIdOrderByCreatedAtDesc(String liveId);
    List<ChatMessageDocument> findTop200ByLiveIdAndCreatedAtAfterOrderByCreatedAtAsc(String liveId, Instant createdAt);
}


