package com.youtube.observabilityservice.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Embeddable
@Data
public class SyntheticCheckResultEmbeddable {
    private Instant executedAt;
    
    private Boolean success;
    
    private Integer statusCode;
    
    private Long responseTimeMs;
    
    @Column(length = 1000)
    private String responseBody;
    
    @Column(length = 500)
    private String errorMessage;
    
    @ElementCollection
    @CollectionTable(name = "synthetic_check_result_metadata", joinColumns = @JoinColumn(name = "check_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;
}

