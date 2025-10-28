package com.youtube.recommendationsservice.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "user_features", indexes = {@Index(name = "idx_user_id", columnList = "userId")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFeaturesJpaRepository {
    
    @Id
    private String userId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_feature_embeddings", 
                     joinColumns = @JoinColumn(name = "userId"))
    @Column(name = "embedding")
    private List<Double> embeddings;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_feature_categorical",
                     joinColumns = @JoinColumn(name = "userId"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> categoricalFeatures;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_feature_numerical",
                     joinColumns = @JoinColumn(name = "userId"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, Double> numericalFeatures;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_categories",
                     joinColumns = @JoinColumn(name = "userId"))
    @Column(name = "category")
    private List<String> recentlyViewedCategories;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_languages",
                     joinColumns = @JoinColumn(name = "userId"))
    @Column(name = "language")
    private List<String> preferredLanguages;
    
    private Instant lastUpdated;
    
    @Version
    private Long version;
}

