package com.youtube.contentidservice.infrastructure.persistence.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Container(containerName = "fingerprint-index")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FingerprintIndexCosmosEntity {
    @Id
    private String id;
    
    @PartitionKey
    private String partitionKey; // Partition by hash prefix for distribution
    
    private UUID fingerprintId;
    
    private byte[] hashVector; // Stored as binary
}

