package com.youtube.contentidservice.infrastructure.persistence.cosmos;

import com.youtube.contentidservice.domain.repositories.FingerprintIndexRepository;
import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FingerprintIndexRepositoryAdapter implements FingerprintIndexRepository {
    private final FingerprintIndexCosmosRepository cosmosRepository;

    @Override
    public void index(FingerprintId fingerprintId, byte[] hashVector) {
        String partitionKey = generatePartitionKey(hashVector);
        
        FingerprintIndexCosmosEntity entity = FingerprintIndexCosmosEntity.builder()
                .id(fingerprintId.getValue().toString())
                .partitionKey(partitionKey)
                .fingerprintId(fingerprintId.getValue())
                .hashVector(hashVector)
                .build();
        
        cosmosRepository.save(entity);
        log.debug("Indexed fingerprint: {}", fingerprintId.getValue());
    }

    @Override
    public List<FingerprintId> findSimilar(byte[] hashVector, double threshold) {
        // Simplified similarity search
        // In production, use Azure Cognitive Search vector search or cosine similarity
        String partitionKey = generatePartitionKey(hashVector);
        
        // Query all entities in the same partition (simplified)
        Iterable<FingerprintIndexCosmosEntity> entities = cosmosRepository.findAll();
        
        return java.util.stream.StreamSupport.stream(entities.spliterator(), false)
                .filter(entity -> calculateSimilarity(hashVector, entity.getHashVector()) >= threshold)
                .map(entity -> FingerprintId.of(entity.getFingerprintId()))
                .collect(Collectors.toList());
    }

    @Override
    public void remove(FingerprintId fingerprintId) {
        cosmosRepository.deleteById(fingerprintId.getValue().toString());
        log.debug("Removed fingerprint from index: {}", fingerprintId.getValue());
    }

    private String generatePartitionKey(byte[] hash) {
        // Use first byte as partition key for distribution
        if (hash.length > 0) {
            return String.valueOf(hash[0] & 0xFF);
        }
        return "0";
    }

    private double calculateSimilarity(byte[] hash1, byte[] hash2) {
        // Simplified Hamming distance calculation
        // In production, use proper similarity metrics
        if (hash1.length != hash2.length) {
            return 0.0;
        }
        
        int matches = 0;
        for (int i = 0; i < hash1.length; i++) {
            if (hash1[i] == hash2[i]) {
                matches++;
            }
        }
        
        return (double) matches / hash1.length;
    }
}

