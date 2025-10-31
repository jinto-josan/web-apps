package com.youtube.edgecdncontrol.application.usecases;

import com.youtube.edgecdncontrol.application.dto.CreateCdnRuleRequest;
import com.youtube.edgecdncontrol.application.dto.CdnRuleResponse;
import com.youtube.edgecdncontrol.application.mappers.CdnRuleMapper;
import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.repositories.CdnRuleRepository;
import com.youtube.edgecdncontrol.domain.services.RuleValidationService;
import com.youtube.edgecdncontrol.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCdnRuleUseCaseTest {
    
    @Mock
    private CdnRuleRepository ruleRepository;
    
    @Mock
    private CdnRuleMapper mapper;
    
    @Mock
    private RuleValidationService validationService;
    
    @InjectMocks
    private CreateCdnRuleUseCase useCase;
    
    @Test
    void shouldCreateRuleSuccessfully() {
        // Given
        CreateCdnRuleRequest request = createValidRequest();
        CdnRule domainRule = createDomainRule();
        CdnRule savedRule = createDomainRule();
        CdnRuleResponse response = CdnRuleResponse.builder()
                .id(savedRule.getId().getValue())
                .name(savedRule.getName())
                .build();
        
        when(mapper.toDomain(request)).thenReturn(domainRule);
        when(validationService.validate(any(CdnRule.class)))
                .thenReturn(new RuleValidationService.ValidationResult(true, List.of()));
        when(ruleRepository.save(any(CdnRule.class))).thenReturn(savedRule);
        when(mapper.toResponse(savedRule)).thenReturn(response);
        
        // When
        CdnRuleResponse result = useCase.execute(request, "test-user");
        
        // Then
        assertNotNull(result);
        assertEquals(savedRule.getId().getValue(), result.getId());
        verify(ruleRepository).save(any(CdnRule.class));
        verify(validationService).validate(any(CdnRule.class));
    }
    
    @Test
    void shouldThrowExceptionWhenValidationFails() {
        // Given
        CreateCdnRuleRequest request = createValidRequest();
        CdnRule domainRule = createDomainRule();
        
        when(mapper.toDomain(request)).thenReturn(domainRule);
        when(validationService.validate(any(CdnRule.class)))
                .thenReturn(new RuleValidationService.ValidationResult(false, List.of("Validation error")));
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request, "test-user"));
        verify(ruleRepository, never()).save(any(CdnRule.class));
    }
    
    private CreateCdnRuleRequest createValidRequest() {
        CreateCdnRuleRequest request = new CreateCdnRuleRequest();
        request.setName("Test Rule");
        request.setDescription("Test description");
        request.setRuleType(RuleType.ROUTING_RULE);
        request.setResourceGroup("rg-test");
        request.setFrontDoorProfileName("fd-test");
        request.setPriority(1);
        
        CreateCdnRuleRequest.MatchConditionDto condition = new CreateCdnRuleRequest.MatchConditionDto();
        condition.setMatchType(CreateCdnRuleRequest.MatchConditionDto.RuleMatchConditionType.REQUEST_URI);
        condition.setVariable("requestUri");
        condition.setOperator("Contains");
        condition.setValues(List.of("/api/"));
        request.setMatchConditions(List.of(condition));
        
        CreateCdnRuleRequest.RuleActionDto action = new CreateCdnRuleRequest.RuleActionDto();
        action.setActionType(CreateCdnRuleRequest.RuleActionDto.ActionTypeDto.ROUTE_TO_ORIGIN);
        action.setParameters(Map.of("originName", "primary-origin"));
        request.setAction(action);
        
        return request;
    }
    
    private CdnRule createDomainRule() {
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
                .description("Test description")
                .ruleType(RuleType.ROUTING_RULE)
                .status(RuleStatus.DRAFT)
                .frontDoorProfile(new FrontDoorProfileId("rg-test", "fd-test"))
                .priority(1)
                .matchConditions(List.of(condition))
                .action(action)
                .metadata(Map.of())
                .createdBy("test-user")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version("v1")
                .rollbackFromRuleId(Optional.empty())
                .build();
    }
}

