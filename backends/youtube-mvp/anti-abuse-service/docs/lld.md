# Low-Level Design: Anti-Abuse Service

## Overview

The Anti-Abuse Service provides real-time risk scoring and fraud detection using ML models and rule-based evaluation.

## Class Diagram

```
┌─────────────────────────────────────────────────────────┐
│                   Controllers                            │
├─────────────────────────────────────────────────────────┤
│ AntiAbuseController                                      │
│  + calculateRiskScore(request): RiskScoreResponse       │
│  + evaluateRules(request): RuleEvaluationResponse       │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                Application Services                       │
├─────────────────────────────────────────────────────────┤
│ RiskScoringService                                       │
│  + calculateRiskScore(request): RiskScoreResponse       │
│                                                           │
│ RuleEvaluationService                                    │
│  + evaluateRules(request): RuleEvaluationResponse        │
│  - determineAction(rules, triggeredRules): String        │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  Domain Layer                            │
├─────────────────────────────────────────────────────────┤
│ RiskEvent                                                │
│  - id: String                                            │
│  - eventType: EventType                                  │
│  - userId: String                                        │
│  - context: Map<String, Object>                         │
│                                                           │
│ RiskScore                                                │
│  - score: Double                                          │
│  - riskLevel: RiskLevel                                   │
│  - triggeredRules: List<String>                          │
│  - recommendedAction: EnforcementAction                  │
│                                                           │
│ Rule                                                     │
│  - id: String                                            │
│  - condition: RuleCondition                              │
│  - action: EnforcementAction                             │
│  - priority: Integer                                     │
│                                                           │
│ RiskEngine (interface)                                   │
│  + calculateRisk(event, features, rules): RiskScore     │
│                                                           │
│ RuleEvaluator (interface)                                │
│  + evaluateRules(rules, features): List<String>         │
│                                                           │
│ FeatureEnrichmentService (interface)                     │
│  + enrichFeatures(event, featureStore): Map<String, Object>│
│                                                           │
│ MlEndpointClient (interface)                             │
│  + predict(features): Map<String, Object>               │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│            Infrastructure Adapters                       │
├─────────────────────────────────────────────────────────┤
│ RiskEngineImpl                                           │
│  - mlEndpointClient: MlEndpointClient                    │
│  - ruleEvaluator: RuleEvaluator                          │
│                                                           │
│ RuleEvaluatorImpl                                        │
│  + evaluateRules(rules, features): List<String>         │
│                                                           │
│ FeatureEnrichmentServiceImpl                            │
│  + enrichFeatures(event, featureStore): Map<String, Object>│
│                                                           │
│ AzureMlEndpointClient                                    │
│  - endpointUrl: String                                   │
│  - apiKey: String                                        │
│  + predict(features): Map<String, Object>               │
│                                                           │
│ RuleRepositoryAdapter                                    │
│  - cosmosRepository: RuleCosmosRepository                │
│                                                           │
│ FeatureStoreRepositoryAdapter                            │
│  - cosmosRepository: FeatureStoreCosmosRepository        │
└─────────────────────────────────────────────────────────┘
```

## Key Components

### Risk Scoring Flow

1. Receive risk score request with event details
2. Load feature store for user
3. Enrich features with event context and historical data
4. Evaluate rules against enriched features
5. Call ML endpoint for predictive score
6. Combine ML and rule-based scores (70% ML, 30% rules)
7. Determine risk level and enforcement action
8. Return risk score response

### Rule Evaluation

- Supports AND/OR operators
- Predicates: GT, LT, EQ, NOT_EQ, IN, NOT_IN
- Priority-based action determination
- Enabled/disabled flag support

### Feature Enrichment

- Combines real-time event context
- Historical features from feature store
- Time-based features (hour of day, day of week)
- Default values for missing features

## Data Flow

### Read Path
- Redis Cache → Cosmos DB
- Feature store cached for 1 hour
- Rules loaded at startup, refreshed periodically

### Write Path
- Cosmos DB (rules, feature store)
- Event Hubs (risk scores for analytics)

## Resilience Patterns

- **Circuit Breaker**: Protects ML endpoint calls
- **Retry**: Exponential backoff for transient failures
- **Timeout**: 2s timeout prevents hanging requests
- **Fallback**: Returns 0.0 score if ML unavailable

## Scalability

- **Horizontal Scaling**: Stateless service, scales horizontally
- **Caching**: Redis caching for feature store
- **Async Processing**: Event Hubs for score streaming
- **Shadow Evaluation**: Run evaluations without enforcement

