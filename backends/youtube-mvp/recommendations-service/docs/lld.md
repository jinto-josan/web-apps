# Low-Level Design: Recommendations Service

## Overview

This document describes the low-level architecture of the Recommendations Service, including domain models, class structures, and key design patterns.

## Architecture Layers

### 1. Domain Layer

#### Entities

**RecommendedItem**
- Represents a single video recommendation with score, reason, and metadata
- Immutable value object wrapper

**UserFeatures**
- Stores user behavior features for personalization
- Includes embeddings, categorical features, and preference data

**VideoCandidate**
- Represents a video candidate for recommendation
- Contains metadata and feature vectors

#### Value Objects

**UserId, VideoId, RecommendationId**
- Type-safe identifiers
- Prevents primitive obsession

**RecommendationContext**
- Request context (device, location, language, AB test variant)
- Used for contextual ranking

**RecommendationScore**
- Constrained between 0.0 and 1.0
- Represents prediction confidence

**FeatureVector**
- Encapsulates ML features
- Supports embeddings, categorical, and numerical features

#### Repository Interfaces

**UserFeaturesRepository**
```java
Optional<UserFeatures> findByUserId(UserId userId);
void save(UserFeatures userFeatures);
boolean exists(UserId userId);
```

**VideoCandidateRepository**
```java
List<VideoCandidate> findCandidatesForUser(UserId userId, int limit);
List<VideoCandidate> findCandidatesForVideo(VideoId videoId, int limit);
```

#### Domain Services

**CandidateProvider** - Strategy for candidate generation
**RankingService** - Multi-factor ranking algorithm
**DiversityService** - Category-based diversity filtering
**FeatureStore** - Feature caching interface

### 2. Application Layer

#### Use Cases

**GetHomeRecommendationsUseCase**
- Orchestrates two-stage ranking pipeline
- Coordinates candidate providers, ranking, diversity
- Returns RecommendationResponse

**GetNextUpRecommendationsUseCase**
- Similar to home recommendations
- Uses video-specific candidate retrieval

#### DTOs

**RecommendationRequest**
- Input validation via Bean Validation
- Supports pagination and context parameters

**RecommendationResponse**
- Structured output with metadata
- Includes recommendation items and request metadata

#### Mappers

**RecommendationMapper**
- MapStruct-generated mapping
- Domain → DTO transformations

### 3. Infrastructure Layer

#### Persistence Adapters

**UserFeaturesRepositoryAdapter**
- Implements domain repository port
- Uses JPA for PostgreSQL storage
- Handles entity ↔ domain mapping

**VideoCandidateRepositoryAdapter**
- Implements domain repository port
- Uses Cosmos DB for document storage
- Redis caching for performance

#### Service Adapters

**HybridRankingService**
- Implements ranking algorithm
- Factors: recency (30%), relevance (40%), popularity (20%), diversity (10%)
- Exponential decay for recency

**CategoryDiversityService**
- Groups by category
- Limits items per category
- Maintains score-based ordering

**RedisFeatureStore**
- Cache layer for feature vectors
- TTL: 24 hours
- Reduces computation overhead

#### External Integrations

**DefaultCandidateProvider**
- Retrieves candidates from repository
- Can be extended with ML-based providers

## Design Patterns

### 1. Hexagonal Architecture

Clear separation of concerns:
- **Domain**: Pure business logic, no dependencies
- **Application**: Orchestration, use cases
- **Infrastructure**: External adapters (DB, cache, services)

### 2. CQRS (Command Query Separation)

- **Queries**: GET operations (read-only)
- **Commands**: Future POST operations (idempotent)

### 3. Repository Pattern

Domain defines interfaces, infrastructure implements:
```java
// Domain (port)
public interface VideoCandidateRepository { ... }

// Infrastructure (adapter)
@Component
public class VideoCandidateRepositoryAdapter implements VideoCandidateRepository { ... }
```

### 4. Strategy Pattern

Candidate providers use strategy for extensibility:
```java
public interface CandidateProvider {
    List<VideoCandidate> getCandidates(...);
    String getProviderName();
}
```

### 5. Decorator Pattern

Ranking → Diversity → Final selection pipeline:
```
candidates → rank → diversify → limit → response
```

## Class Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      REST Layer                             │
├─────────────────────────────────────────────────────────────┤
│ RecommendationController                                     │
│  + getHomeRecommendations(userId, limit)                   │
│  + getNextUpRecommendations(userId, videoId, limit)        │
└──────────────────────────┬───────────────────────────────────┘
                          │
┌──────────────────────────▼───────────────────────────────────┐
│                  Application Layer                           │
├─────────────────────────────────────────────────────────────┤
│ GetHomeRecommendationsUseCase                               │
│ GetNextUpRecommendationsUseCase                             │
│   └─→ CandidateProvider[]                                  │
│   └─→ RankingService                                       │
│   └─→ DiversityService                                     │
└──────────────────────────┬───────────────────────────────────┘
                          │
┌──────────────────────────▼───────────────────────────────────┐
│                    Domain Layer                              │
├─────────────────────────────────────────────────────────────┤
│ Entities:                                                    │
│  - RecommendedItem                                          │
│  - UserFeatures                                             │
│  - VideoCandidate                                           │
│                                                              │
│ Value Objects:                                              │
│  - UserId, VideoId, RecommendationId                        │
│  - RecommendationContext                                    │
│  - RecommendationScore                                      │
│  - FeatureVector                                            │
│                                                              │
│ Repository Ports:                                           │
│  - UserFeaturesRepository                                   │
│  - VideoCandidateRepository                                 │
│                                                              │
│ Domain Services (Ports):                                    │
│  - CandidateProvider                                        │
│  - RankingService                                           │
│  - DiversityService                                         │
│  - FeatureStore                                             │
└──────────────────────────┬───────────────────────────────────┘
                          │
┌──────────────────────────▼───────────────────────────────────┐
│                Infrastructure Layer                          │
├─────────────────────────────────────────────────────────────┤
│ Adapters:                                                    │
│  - UserFeaturesRepositoryAdapter                             │
│  - VideoCandidateRepositoryAdapter                          │
│                                                              │
│ Implementations:                                            │
│  - HybridRankingService                                     │
│  - CategoryDiversityService                                 │
│  - DefaultCandidateProvider                                 │
│  - RedisFeatureStore                                        │
│                                                              │
│ Persistence:                                                │
│  - UserFeaturesJpaRepository (JPA)                         │
│  - VideoCandidateCosmosRepository (Cosmos)                  │
│  - RedisTemplate (cache)                                     │
└─────────────────────────────────────────────────────────────┘
```

## Ranking Algorithm

### Multi-Factor Scoring

```java
score = (recency * 0.3) + 
        (relevance * 0.4) + 
        (popularity * 0.2) + 
        (diversity * 0.1)
```

**Recency** (30%):
- Exponential decay: `exp(-daysOld / 30.0)`
- Recent videos scored higher

**Relevance** (40%):
- Context matching (language, device)
- User preference alignment

**Popularity** (20%):
- View count normalization
- Engagement metrics

**Diversity** (10%):
- Category spread
- Reduces filter bubbles

### Diversity Constraints

- Max items per category: `limit / 3`
- Shuffle then select top-K
- Preserves score ordering

## Performance Optimizations

### Caching Strategy

1. **Redis**: Feature vectors (24h TTL)
2. **Application**: Candidate lists (5min TTL)
3. **CDN**: Static responses (2min cache-control)

### Query Optimization

- Indexes on `userId`, `publishedAt`
- Pagination with keyset-based cursor
- Batch reads for multiple IDs

## Resilience Patterns

### Circuit Breaker

- Window size: 10 requests
- Failure threshold: 50%
- Half-open retries: 3

### Retry

- Max attempts: 3
- Exponential backoff: 500ms → 1s → 2s
- Only on transient failures

### Timeout

- External calls: 5s
- Database queries: 3s
- Ranking computation: 2s

### Bulkhead

- Thread pool size: 25
- Max queue depth: 100
- Queue timeout: 5s

## Security Considerations

### Authentication

- OAuth2 Resource Server
- JWT validation via Entra ID
- Claims extraction for user context

### Authorization

- Role-based access (future)
- Rate limiting per user
- Input validation on all endpoints

## Observability

### Metrics

- `recommendations.request.count` (counter)
- `recommendations.latency.seconds` (histogram)
- `recommendations.cache.hit.rate` (gauge)

### Traces

- OpenTelemetry auto-instrumentation
- Custom spans for ranking pipeline
- Correlation ID propagation

### Logs

- Structured JSON logging
- Correlation IDs per request
- MDC for contextual data

## Future Enhancements

1. **ML Integration**: Real-time prediction via Azure ML
2. **A/B Testing**: Feature flags via App Configuration
3. **Event Sourcing**: User interaction events → recommendation updates
4. **GraphQL**: Flexible querying for mobile clients
5. **Real-time**: WebSocket support for live recommendations

