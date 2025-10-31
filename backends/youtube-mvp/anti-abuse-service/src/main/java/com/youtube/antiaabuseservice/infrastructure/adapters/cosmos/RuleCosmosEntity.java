package com.youtube.antiaabuseservice.infrastructure.adapters.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Container(containerName = "rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCosmosEntity {
    @org.springframework.data.annotation.Id
    private String id;
    @PartitionKey
    private String name;
    private String description;
    private RuleConditionEntity condition;
    private String action;
    private Integer priority;
    private boolean enabled;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleConditionEntity {
        private String operator;
        private List<RulePredicateEntity> predicates;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RulePredicateEntity {
        private String field;
        private String operator;
        private Object value;
    }
}

