# Low-Level Design: Experimentation Service

## Overview

The Experimentation Service provides feature flag and experiment management capabilities with deterministic bucketing, sticky assignments, and Azure App Configuration integration.

## Class Diagram

```
┌─────────────────────────────────────────────────────────┐
│                   Controllers                            │
├─────────────────────────────────────────────────────────┤
│ ExperimentationController                                │
│  + getFlags(userId, context)                             │
│  + getFlag(key, userId, context)                        │
│  + getExperiment(key, userId, context)                  │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                Application Services                       │
├─────────────────────────────────────────────────────────┤
│ FeatureFlagService                                       │
│  + getFlag(key, userId, context): FeatureFlagResponse   │
│  + getAllFlags(userId, context): List<FeatureFlagResponse>│
│  - evaluateFlag(flag, userId, context): FeatureFlag     │
│                                                           │
│ ExperimentService                                        │
│  + getExperiment(key, userId, context): ExperimentResponse│
│  - assignAndPersist(userId, experiment): ExperimentResponse│
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  Domain Layer                            │
├─────────────────────────────────────────────────────────┤
│ FeatureFlag                                              │
│  - key: String                                           │
│  - enabled: boolean                                      │
│  - rolloutPercentage: Double                            │
│  - conditions: Map<String, String>                      │
│                                                           │
│ Experiment                                               │
│  - id: String                                            │
│  - key: String                                           │
│  - status: ExperimentStatus                              │
│  - variants: List<Variant>                               │
│  - assignmentStrategy: AssignmentStrategy                │
│                                                           │
│ UserCohort                                               │
│  - userId: String                                        │
│  - experimentKey: String                                 │
│  - variantId: String                                     │
│                                                           │
│ BucketingService (interface)                             │
│  + computeBucket(userId, experimentKey): int             │
│  + assignVariant(userId, experiment): Variant          │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│            Infrastructure Adapters                       │
├─────────────────────────────────────────────────────────┤
│ FeatureFlagRepositoryAdapter                            │
│  - configurationClient: ConfigurationClient               │
│                                                           │
│ ExperimentRepositoryAdapter                              │
│  - cosmosRepository: ExperimentCosmosRepository          │
│                                                           │
│ CohortRepositoryAdapter                                  │
│  - cosmosRepository: CohortCosmosRepository              │
│                                                           │
│ BucketingServiceImpl                                     │
│  + computeBucket(userId, experimentKey): int             │
│  + assignVariant(userId, experiment): Variant          │
└─────────────────────────────────────────────────────────┘
```

## Key Components

### Feature Flag Resolution Flow

1. Request comes with `userId` and context
2. Service checks Redis cache
3. If miss, fetch from App Configuration
4. Evaluate conditions and rollout percentage
5. Return enabled/disabled flag
6. Cache result in Redis

### Experiment Assignment Flow

1. Check if user already in cohort (sticky assignment)
2. If not, compute bucket using MD5 hash
3. Assign variant based on traffic percentages
4. Persist assignment in Cosmos DB
5. Cache in Redis
6. Return variant configuration

### Bucketing Algorithm

```
bucket = MD5(userId + ":" + experimentKey) % 10000

if bucket < variant1.trafficPercentage * 10000:
    return variant1
elif bucket < (variant1 + variant2).trafficPercentage * 10000:
    return variant2
else:
    return variant3
```

## Data Flow

### Read Path (Optimized)
- Redis Cache → Cosmos DB / App Config
- Cache TTL: 1 hour
- Cache invalidation on App Config events

### Write Path
- Cosmos DB (experiments, cohorts)
- App Configuration (feature flags via Azure Portal)

## Resilience Patterns

- **Circuit Breaker**: Protects App Configuration calls
- **Retry**: Exponential backoff for transient failures
- **Timeout**: 3s timeout prevents hanging requests
- **Fallback**: Returns disabled flag if service unavailable

## Scalability

- **Horizontal Scaling**: Stateless service, scales horizontally
- **Caching**: Reduces load on App Configuration and Cosmos DB
- **Partitioning**: Cosmos DB partitioned by key/userId

