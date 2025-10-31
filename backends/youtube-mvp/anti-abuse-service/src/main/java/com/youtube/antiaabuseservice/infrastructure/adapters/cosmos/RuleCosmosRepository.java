package com.youtube.antiaabuseservice.infrastructure.adapters.cosmos;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RuleCosmosRepository extends CrudRepository<RuleCosmosEntity, String> {
    List<RuleCosmosEntity> findByEnabledTrue();
}

