package com.youtube.observabilityservice.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "slos")
@Data
public class SLOEntity {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String serviceName;
    
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "slis", joinColumns = @JoinColumn(name = "slo_id"))
    private List<SLIEmbeddable> slis;
    
    @Column(nullable = false)
    private Double targetPercent;
    
    @Column(nullable = false)
    private String timeWindowDuration;
    
    @Column(nullable = false)
    private String timeWindowType;
    
    private Double errorBudget;
    
    private Double errorBudgetRemaining;
    
    @ElementCollection
    @CollectionTable(name = "slo_labels", joinColumns = @JoinColumn(name = "slo_id"))
    @MapKeyColumn(name = "label_key")
    @Column(name = "label_value")
    private Map<String, String> labels;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
}

