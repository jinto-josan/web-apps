package com.youtube.observabilityservice.domain.repositories;

import com.youtube.observabilityservice.domain.entities.SyntheticCheck;
import com.youtube.observabilityservice.domain.valueobjects.SyntheticCheckId;

import java.util.List;
import java.util.Optional;

public interface SyntheticCheckRepository {
    SyntheticCheck save(SyntheticCheck check);
    Optional<SyntheticCheck> findById(SyntheticCheckId id);
    List<SyntheticCheck> findAll();
    List<SyntheticCheck> findByEnabled(Boolean enabled);
    void deleteById(SyntheticCheckId id);
}

