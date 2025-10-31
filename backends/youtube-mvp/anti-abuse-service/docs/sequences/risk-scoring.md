# Sequence Diagram: Risk Scoring Flow

```plantuml
@startuml Risk Scoring Flow
actor Client
participant "AntiAbuseController" as Controller
participant "RiskScoringService" as Service
participant "FeatureEnrichmentService" as Enrichment
participant "RiskEngine" as Engine
participant "RuleEvaluator" as RuleEval
participant "MlEndpointClient" as ML
participant "FeatureStoreRepository" as FeatureRepo
participant "RuleRepository" as RuleRepo
participant "Redis Cache" as Cache
participant "Cosmos DB" as Cosmos
participant "Event Hubs" as Events

Client -> Controller: POST /api/v1/risk/score
Controller -> Service: calculateRiskScore(request)

Service -> FeatureRepo: findByUserIdAndFeatureSet(userId, "risk-features")
FeatureRepo -> Cache: Check cache
alt Cache Hit
    Cache -> FeatureRepo: Return cached features
else Cache Miss
    FeatureRepo -> Cosmos: Query feature-store container
    Cosmos -> FeatureRepo: Return feature store document
    FeatureRepo -> Cache: Store in cache
end
FeatureRepo -> Service: FeatureStore

Service -> Enrichment: enrichFeatures(event, featureStore)
Enrichment -> Enrichment: Add event context
Enrichment -> Enrichment: Add time-based features
Enrichment -> Service: Enriched features

Service -> RuleRepo: findAllEnabled()
RuleRepo -> Cosmos: Query rules container
Cosmos -> RuleRepo: Return enabled rules
RuleRepo -> Service: List<Rule>

Service -> Engine: calculateRisk(event, features, rules)

Engine -> RuleEval: evaluateRules(rules, features)
RuleEval -> Engine: triggeredRules: ["rule-1", "rule-3"]

Engine -> ML: predict(features)
alt ML Available
    ML -> Engine: { "risk_score": 0.75 }
else ML Unavailable (Circuit Open)
    ML -> Engine: Fallback: { "risk_score": 0.0 }
end

Engine -> Engine: Combine scores (70% ML, 30% rules)
Engine -> Engine: Determine risk level: HIGH
Engine -> Engine: Determine action: REVIEW
Engine -> Service: RiskScore(score: 0.75, riskLevel: HIGH)

Service -> FeatureRepo: save(updatedFeatureStore)
Service -> Events: Publish risk score event
Service -> Controller: RiskScoreResponse
Controller -> Client: 200 OK
@enduml
```

## Steps

1. Client sends risk score request with event details
2. Service loads feature store from Cosmos DB (cached in Redis)
3. Features are enriched with event context and time-based features
4. Enabled rules are loaded from Cosmos DB
5. Rules are evaluated against enriched features
6. ML endpoint is called for predictive risk score (with circuit breaker)
7. Scores are combined (70% ML, 30% rules)
8. Risk level and action are determined
9. Risk score is returned and optionally published to Event Hubs

## Error Handling

- **ML Timeout**: Falls back to rule-based score only
- **Circuit Open**: Returns 0.0 ML score, continues with rules
- **Feature Store Missing**: Creates empty feature store
- **Rule Evaluation Error**: Logs warning, continues with ML only

