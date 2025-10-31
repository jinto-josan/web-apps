package com.youtube.configsecretsservice.infrastructure.persistence.repository;

import com.youtube.configsecretsservice.infrastructure.persistence.entity.ConfigurationEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for configuration entries.
 */
@Repository
public interface JpaConfigurationRepository extends JpaRepository<ConfigurationEntryEntity, String> {
    Optional<ConfigurationEntryEntity> findByScopeAndKey(String scope, String key);
    
    List<ConfigurationEntryEntity> findAllByScope(String scope);
    
    @Query("SELECT e FROM ConfigurationEntryEntity e WHERE e.scope = :scope AND e.label = :label")
    List<ConfigurationEntryEntity> findByScopeAndLabel(@Param("scope") String scope, @Param("label") String label);
}

