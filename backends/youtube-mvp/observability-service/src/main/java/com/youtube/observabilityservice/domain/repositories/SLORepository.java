package com.youtube.observabilityservice.domain.repositories;

import com.youtube.observabilityservice.domain.entities.SLO;
import com.youtube.observabilityservice.domain.valueobjects.SLOId;

import java.util.List;
import java.util.Optional;

public interface SLORepository {
    SLO save(SLO slo);
    Optional<SLO> findById(SLOId id);
    List<SLO> findByServiceName(String serviceName);
    List<SLO> findAll();
    void deleteById(SLOId id);
}

