# Sequence Diagram: Experiment Assignment

```plantuml
@startuml Experiment Assignment Flow
actor User
participant "ExperimentationController" as Controller
participant "ExperimentService" as Service
participant "BucketingService" as Bucketing
participant "CohortRepository" as CohortRepo
participant "Redis Cache" as Cache
participant "Cosmos DB" as Cosmos

User -> Controller: GET /api/v1/experiments/recommendation-algo?userId=user123
Controller -> Service: getExperiment("recommendation-algo", "user123", context)

alt Sticky Assignment Strategy
    Service -> CohortRepo: findByUserIdAndExperimentKey("user123", "recommendation-algo")
    CohortRepo -> Cache: Check cache
    alt Cache Hit
        Cache -> CohortRepo: Return cached cohort
        CohortRepo -> Service: UserCohort(variantId: "variant-2")
    else Cache Miss
        CohortRepo -> Cosmos: Query cohorts container
        Cosmos -> CohortRepo: Return cohort document
        CohortRepo -> Cache: Store in cache
        CohortRepo -> Service: UserCohort(variantId: "variant-2")
    end
    
    Service -> Service: Load variant configuration
else Deterministic Assignment
    Service -> Bucketing: computeBucket("user123", "recommendation-algo")
    Bucketing -> Bucketing: MD5 hash computation
    Bucketing -> Service: bucket: 5234
    
    Service -> Bucketing: assignVariant("user123", experiment)
    Bucketing -> Service: Variant("variant-2")
    
    Service -> CohortRepo: save(UserCohort)
    CohortRepo -> Cosmos: Persist assignment
    CohortRepo -> Cache: Cache assignment
end

Service -> Controller: ExperimentResponse(variantId: "variant-2", config: {...})
Controller -> User: 200 OK
@enduml
```

## Steps

1. User requests experiment variant
2. Service checks experiment assignment strategy
3. **Sticky**: Look up existing assignment in cohort repository
4. **Deterministic**: Compute bucket using MD5 hash of userId + experimentKey
5. Assign variant based on cumulative traffic percentages
6. Persist assignment in Cosmos DB (for sticky/audit)
7. Cache in Redis for fast lookups
8. Return variant configuration

## Bucketing Details

- Hash input: `userId + ":" + experimentKey`
- Bucket range: 0-9999
- Variant assignment: Cumulative percentage check
- Deterministic: Same user always gets same bucket

