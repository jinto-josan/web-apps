package com.youtube.antiaabuseservice.infrastructure.adapters.cosmos;

import com.youtube.antiaabuseservice.domain.model.Rule;
import com.youtube.antiaabuseservice.domain.repositories.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class RuleRepositoryAdapter implements RuleRepository {
    private final RuleCosmosRepository cosmosRepository;

    @Override
    public List<Rule> findAllEnabled() {
        return cosmosRepository.findByEnabledTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Rule> findById(String id) {
        return cosmosRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Rule save(Rule rule) {
        RuleCosmosEntity entity = toEntity(rule);
        RuleCosmosEntity saved = cosmosRepository.save(entity);
        return toDomain(saved);
    }

    private Rule toDomain(RuleCosmosEntity entity) {
        return Rule.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .condition(Rule.RuleCondition.builder()
                        .operator(entity.getCondition().getOperator())
                        .predicates(entity.getCondition().getPredicates().stream()
                                .map(p -> Rule.RulePredicate.builder()
                                        .field(p.getField())
                                        .operator(p.getOperator())
                                        .value(p.getValue())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .action(Rule.EnforcementAction.valueOf(entity.getAction()))
                .priority(entity.getPriority())
                .enabled(entity.isEnabled())
                .build();
    }

    private RuleCosmosEntity toEntity(Rule rule) {
        return RuleCosmosEntity.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .condition(RuleCosmosEntity.RuleConditionEntity.builder()
                        .operator(rule.getCondition().getOperator())
                        .predicates(rule.getCondition().getPredicates().stream()
                                .map(p -> RuleCosmosEntity.RulePredicateEntity.builder()
                                        .field(p.getField())
                                        .operator(p.getOperator())
                                        .value(p.getValue())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .action(rule.getAction().name())
                .priority(rule.getPriority())
                .enabled(rule.isEnabled())
                .build();
    }
}

