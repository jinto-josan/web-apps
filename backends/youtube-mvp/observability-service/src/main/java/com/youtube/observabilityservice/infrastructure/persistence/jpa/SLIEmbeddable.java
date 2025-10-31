package com.youtube.observabilityservice.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.Instant;

@Embeddable
@Data
public class SLIEmbeddable {
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false, length = 2000)
    private String query;
    
    private Instant lastCalculatedAt;
    
    private Double lastValue;
}

