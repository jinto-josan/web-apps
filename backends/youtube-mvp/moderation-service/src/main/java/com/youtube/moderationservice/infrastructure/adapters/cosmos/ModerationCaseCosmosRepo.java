package com.youtube.moderationservice.infrastructure.adapters.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import lombok.Data;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface ModerationCaseCosmosRepo extends CosmosRepository<ModerationCaseCosmosRepo.ModerationCaseEntity, UUID> {
}

@Container(containerName = "moderation_cases")
@Data
class ModerationCaseEntity {
    private UUID id;
    private String contentId;
    private String reporterUserId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}


