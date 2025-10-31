package com.youtube.observabilityservice.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "synthetic_checks")
@Data
public class SyntheticCheckEntity {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private String endpoint;
    
    @Column(nullable = false)
    private String method;
    
    @ElementCollection
    @CollectionTable(name = "synthetic_check_headers", joinColumns = @JoinColumn(name = "check_id"))
    @MapKeyColumn(name = "header_key")
    @Column(name = "header_value")
    private Map<String, String> headers;
    
    @Column(length = 5000)
    private String body;
    
    @Column(nullable = false)
    private Integer expectedStatusCode;
    
    private String expectedBodyPattern;
    
    private Integer timeoutSeconds;
    
    @Column(nullable = false)
    private Integer intervalSeconds;
    
    @Column(nullable = false)
    private Boolean enabled;
    
    private Instant lastRunAt;
    
    @Embedded
    private SyntheticCheckResultEmbeddable lastResult;
    
    @ElementCollection
    @CollectionTable(name = "synthetic_check_labels", joinColumns = @JoinColumn(name = "check_id"))
    @MapKeyColumn(name = "label_key")
    @Column(name = "label_value")
    private Map<String, String> labels;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
}

