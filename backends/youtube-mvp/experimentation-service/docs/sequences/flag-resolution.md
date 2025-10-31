# Sequence Diagram: Feature Flag Resolution

```plantuml
@startuml Flag Resolution Flow
actor User
participant "API Gateway" as Gateway
participant "ExperimentationController" as Controller
participant "FeatureFlagService" as Service
participant "Redis Cache" as Cache
participant "App Configuration" as AppConfig

User -> Gateway: GET /api/v1/flags/new-ui?userId=user123
Gateway -> Controller: Forward request

Controller -> Service: getFlag("new-ui", "user123", context)

Service -> Cache: Get flag from cache
alt Cache Hit
    Cache -> Service: Return cached flag
else Cache Miss
    Service -> AppConfig: Fetch flag configuration
    AppConfig -> Service: Return flag config
    Service -> Cache: Store in cache (TTL: 1h)
end

Service -> Service: Evaluate conditions
Service -> Service: Check rollout percentage
Service -> Controller: FeatureFlagResponse(enabled: true)
Controller -> Gateway: 200 OK
Gateway -> User: { "key": "new-ui", "enabled": true }
@enduml
```

## Steps

1. User requests feature flag with userId and context
2. Service checks Redis cache first
3. On cache miss, fetches from Azure App Configuration
4. Evaluates conditions (region, user segment, etc.)
5. Checks rollout percentage using deterministic bucketing
6. Returns enabled/disabled flag
7. Caches result for 1 hour

