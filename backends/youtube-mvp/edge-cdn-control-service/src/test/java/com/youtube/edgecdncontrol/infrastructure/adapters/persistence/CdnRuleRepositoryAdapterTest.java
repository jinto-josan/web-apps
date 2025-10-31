package com.youtube.edgecdncontrol.infrastructure.adapters.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.valueobjects.*;
import com.youtube.edgecdncontrol.infrastructure.adapters.persistence.entity.CdnRuleEntity;
import com.youtube.edgecdncontrol.infrastructure.adapters.persistence.jpa.CdnRuleJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CdnRuleRepositoryAdapterTest {
    
    @Mock
    private CdnRuleJpaRepository jpaRepository;
    
    @Mock
    private CdnRuleEntityMapper mapper;
    
    @InjectMocks
    private CdnRuleRepositoryAdapter adapter;
    
    @Test
    void shouldSaveRule() {
        // Given
        CdnRule rule = createTestRule();
        CdnRuleEntity entity = createTestEntity();
        CdnRuleEntity savedEntity = createTestEntity();
        
        when(mapper.toEntity(rule)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(rule);
        
        // When
        CdnRule result = adapter.save(rule);
        
        // Then
        assertNotNull(result);
        verify(jpaRepository).save(entity);
    }
    
    @Test
    void shouldFindById() {
        // Given
        CdnRuleId id = CdnRuleId.generate();
        CdnRuleEntity entity = createTestEntity();
        CdnRule rule = createTestRule();
        
        when(jpaRepository.findById(id.getValue())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(rule);
        
        // When
        Optional<CdnRule> result = adapter.findById(id);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(rule.getId(), result.get().getId());
    }
    
    private CdnRule createTestRule() {
        RuleMatchCondition condition = RuleMatchCondition.builder()
                .matchType(RuleMatchCondition.MatchType.REQUEST_URI)
                .variable("requestUri")
                .operator("Contains")
                .values(List.of("/api/"))
                .caseSensitive(false)
                .build();
        
        RuleAction action = RuleAction.builder()
                .actionType(RuleAction.ActionType.ROUTE_TO_ORIGIN)
                .parameters(Map.of("originName", "primary-origin"))
                .build();
        
        return CdnRule.builder()
                .id(CdnRuleId.generate())
                .name("Test Rule")
                .ruleType(RuleType.ROUTING_RULE)
                .status(RuleStatus.DRAFT)
                .frontDoorProfile(new FrontDoorProfileId("rg-test", "fd-test"))
                .matchConditions(List.of(condition))
                .action(action)
                .createdBy("test-user")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version("v1")
                .rollbackFromRuleId(Optional.empty())
                .build();
    }
    
    private CdnRuleEntity createTestEntity() {
        return CdnRuleEntity.builder()
                .id("test-id")
                .name("Test Rule")
                .ruleType(RuleType.ROUTING_RULE)
                .status(RuleStatus.DRAFT)
                .resourceGroup("rg-test")
                .profileName("fd-test")
                .createdBy("test-user")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version("v1")
                .build();
    }
}

