package com.youtube.edgecdncontrol.domain.services;

import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleAction;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleMatchCondition;

import java.util.ArrayList;
import java.util.List;

public class RuleValidationService {
    
    public ValidationResult validate(CdnRule rule) {
        List<String> errors = new ArrayList<>();
        
        // Basic validation
        if (rule.getName() == null || rule.getName().isBlank()) {
            errors.add("Rule name is required");
        }
        
        if (rule.getMatchConditions() == null || rule.getMatchConditions().isEmpty()) {
            errors.add("At least one match condition is required");
        }
        
        if (rule.getAction() == null) {
            errors.add("Rule action is required");
        }
        
        // Validate match conditions
        if (rule.getMatchConditions() != null) {
            for (int i = 0; i < rule.getMatchConditions().size(); i++) {
                RuleMatchCondition condition = rule.getMatchConditions().get(i);
                if (condition.getVariable() == null || condition.getVariable().isBlank()) {
                    errors.add(String.format("Match condition %d: variable is required", i));
                }
                if (condition.getValues() == null || condition.getValues().isEmpty()) {
                    errors.add(String.format("Match condition %d: at least one value is required", i));
                }
            }
        }
        
        // Validate action parameters based on action type
        if (rule.getAction() != null) {
            RuleAction.ActionType actionType = rule.getAction().getActionType();
            switch (actionType) {
                case ROUTE_TO_ORIGIN:
                    if (rule.getAction().getParameters() == null || 
                        !rule.getAction().getParameters().containsKey("originName")) {
                        errors.add("ROUTE_TO_ORIGIN action requires 'originName' parameter");
                    }
                    break;
                case REDIRECT:
                    if (rule.getAction().getParameters() == null || 
                        !rule.getAction().getParameters().containsKey("redirectUrl")) {
                        errors.add("REDIRECT action requires 'redirectUrl' parameter");
                    }
                    break;
                case APPLY_WAF_POLICY:
                    if (rule.getAction().getParameters() == null || 
                        !rule.getAction().getParameters().containsKey("wafPolicyId")) {
                        errors.add("APPLY_WAF_POLICY action requires 'wafPolicyId' parameter");
                    }
                    break;
            }
        }
        
        // Validate priority if specified
        if (rule.getPriority() != null && rule.getPriority() < 0) {
            errors.add("Priority must be non-negative");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    public record ValidationResult(boolean isValid, List<String> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}

