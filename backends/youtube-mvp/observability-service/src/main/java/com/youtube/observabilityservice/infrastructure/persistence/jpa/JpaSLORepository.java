package com.youtube.observabilityservice.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaSLORepository extends JpaRepository<SLOEntity, UUID> {
    List<SLOEntity> findByServiceName(String serviceName);
}

