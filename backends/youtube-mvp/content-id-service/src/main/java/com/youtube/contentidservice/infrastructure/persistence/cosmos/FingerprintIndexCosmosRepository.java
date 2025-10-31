package com.youtube.contentidservice.infrastructure.persistence.cosmos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FingerprintIndexCosmosRepository extends CrudRepository<FingerprintIndexCosmosEntity, String> {
    // Custom queries for similarity search would be implemented here
    // In production, use Azure Cognitive Search or vector similarity search
}

