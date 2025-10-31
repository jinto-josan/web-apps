# Sequence Diagram: Rule Create and Apply Flow

```plantuml
@startuml
actor User
participant "API Gateway" as Gateway
participant "CdnRuleController" as Controller
participant "CreateCdnRuleUseCase" as CreateUC
participant "ApplyCdnRuleUseCase" as ApplyUC
participant "RuleValidationService" as Validation
participant "CdnRuleRepository" as Repository
participant "AzureFrontDoorAdapter" as Azure
database PostgreSQL
database "Azure Front Door" as FrontDoor

User -> Gateway: POST /api/v1/cdn/rules
Gateway -> Controller: Create rule request
Controller -> CreateUC: execute(request, user)
CreateUC -> Validation: validate(rule)
Validation --> CreateUC: ValidationResult
alt Validation fails
    CreateUC --> Controller: IllegalArgumentException
    Controller --> Gateway: 400 Bad Request
    Gateway --> User: Error response
else Validation succeeds
    CreateUC -> Repository: save(rule)
    Repository -> PostgreSQL: INSERT cdn_rules
    PostgreSQL --> Repository: Saved entity
    Repository --> CreateUC: CdnRule (DRAFT)
    CreateUC --> Controller: CdnRuleResponse
    Controller --> Gateway: 201 Created + ETag
    Gateway --> User: Rule created (DRAFT)
end

User -> Gateway: POST /api/v1/cdn/rules/{id}/apply
Gateway -> Controller: Apply rule request
Controller -> ApplyUC: execute(ruleId, dryRun=false)
ApplyUC -> Repository: findById(ruleId)
Repository -> PostgreSQL: SELECT by id
PostgreSQL --> Repository: Rule entity
Repository --> ApplyUC: CdnRule
alt Dry-run mode
    ApplyUC -> Validation: validate(rule)
    Validation --> ApplyUC: ValidationResult
    ApplyUC -> Repository: save(rule) [VALIDATED]
else Apply mode
    ApplyUC -> Azure: applyRule(rule)
    Azure -> FrontDoor: ARM API: Create/Update Rule
    FrontDoor --> Azure: Success
    Azure --> ApplyUC: Rule applied
    ApplyUC -> Repository: save(rule) [APPLIED]
end
Repository -> PostgreSQL: UPDATE cdn_rules
PostgreSQL --> Repository: Updated entity
Repository --> ApplyUC: Updated CdnRule
ApplyUC --> Controller: CdnRuleResponse
Controller --> Gateway: 200 OK + ETag
Gateway --> User: Rule applied
@enduml
```

## Description

1. **Create Rule**: User creates a new rule via REST API
   - Rule is validated using domain validation service
   - If valid, rule is saved in DRAFT status
   - Returns rule with ETag for optimistic locking

2. **Apply Rule**: User applies a validated rule
   - Rule is retrieved from repository
   - In dry-run mode, only validation is performed
   - In apply mode, rule is sent to Azure Front Door via ARM API
   - Rule status is updated to APPLIED on success
   - ETag is updated for version control

