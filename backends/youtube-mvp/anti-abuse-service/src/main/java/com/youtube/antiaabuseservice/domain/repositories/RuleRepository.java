package com.youtube.antiaabuseservice.domain.repositories;

import com.youtube.antiaabuseservice.domain.model.Rule;

import java.util.List;
import java.util.Optional;

public interface RuleRepository {
    List<Rule> findAllEnabled();
    Optional<Rule> findById(String id);
    Rule save(Rule rule);
}

