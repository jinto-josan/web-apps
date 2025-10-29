# Low-Level Design - Feeds Service

## Overview

The Feeds Service provides personalized video feeds with ad slot injection for YouTube MVP. It implements hexagonal architecture with DDD-lite patterns and integrates with Azure services for persistence and messaging.

## Architecture Layers

### Domain Layer

**Entities:**
- `Feed` - Aggregates feed items with metadata
- `FeedView` - Tracks user views for analytics

**Value Objects:**
- `FeedItem` - Individual video entry in feed
- `FeedType` - Enum (HOME, SUBSCRIPTIONS, TRENDING)

**Domain Services:**
- Feed generation logic
- Ad injection algorithm

**Repositories (Interfaces):**
- `FeedRepository` - Feed persistence
- `VideoRepository` - Video catalog access
- `SubscriptionRepository` - Subscription status

### Application Layer

**Use Cases:**
- `GetFeedUseCase` - Orchestrates feed retrieval
- `FeedCacheService` - Cache management

**DTOs:**
- `FeedDto` - API response representation
- `FeedItemDto` - Individual item DTO
- `FeedViewDto` - View tracking DTO

**Mappers:**
- `FeedMapper` - MapStruct mappers

### Infrastructure Layer

**Repositories:**
- `FeedCosmosRepository` - Cosmos DB adapter
- `VideoRepositoryAdapter` - Video catalog adapter
- `SubscriptionRepositoryAdapter` - Subscription adapter

**Messaging:**
- `VideoPublishedConsumer` - Service Bus consumer
- `VideoPublishedEvent` - Event DTO

**Configuration:**
- `SecurityConfig` - OAuth2 Resource Server
- `RedisConfig` - Redis cache configuration
- `ResilienceConfig` - Resilience4j configuration

## Class Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         FEEDS SERVICE                           │
└─────────────────────────────────────────────────────────────────┘

┌────────────────────────────┐
│      Domain Layer          │
├────────────────────────────┤
│ Feed                       │
│ FeedType                   │
│ FeedItem                   │
│ FeedView                   │
│                            │
│ FeedRepository (interface)│
│ VideoRepository (interface)│
│ SubscriptionRepository     │
└────────────────────────────┘
           ▲
           │
┌────────────────────────────┐
│   Application Layer        │
├────────────────────────────┤
│ GetFeedUseCase             │
│ FeedCacheService           │
│ AdSlotService              │
│                            │
│ FeedDto                    │
│ FeedItemDto                │
│ FeedMapper                 │
└────────────────────────────┘
           ▲
           │
┌────────────────────────────┐
│   Infrastructure Layer     │
├────────────────────────────┤
│ FeedController (REST API)   │
│                            │
│ FeedCosmosRepository       │
│ VideoRepositoryAdapter     │
│ SubscriptionRepositoryAdpt │
│                            │
│ VideoPublishedConsumer     │
│ Redis (Cache)              │
│ Service Bus (Messaging)    │
└────────────────────────────┘
```

## Components

### 1. Feed Controller

REST endpoints:
- `GET /api/v1/feeds/home`
- `GET /api/v1/feeds/subscriptions`
- `GET /api/v1/feeds/trending`

Features:
- ETag support for caching
- OpenAPI documentation
- OAuth2 security
- Validation

### 2. GetFeedUseCase

Orchestrates feed retrieval:
1. Check cache (Redis)
2. If miss, generate feed
3. Inject ads via AdSlotService
4. Cache result
5. Return DTO

### 3. AdSlotService

Ad injection algorithm:
- Injects ads every 10 items
- Random ad selection
- Maintains ad balance

### 4. VideoPublishedConsumer

Event-driven feed updates:
1. Receive video.published event
2. Find subscribers
3. Fan-out cache invalidation
4. Async feed regeneration

## Database Schema

### Cosmos DB

**Container: feeds**
```json
{
  "id": "user123:HOME",
  "userId": "user123",
  "feedType": "HOME",
  "items": [...],
  "lastUpdated": "2024-01-01T00:00:00Z",
  "etag": "etag-123",
  "totalCount": 50,
  "pageSize": 50
}
```

**Container: feed-views**
```json
{
  "id": "view-123",
  "userId": "user123",
  "videoId": "video-456",
  "feedType": "HOME",
  "viewedAt": "2024-01-01T00:00:00Z",
  "position": 5
}
```

## Cache Strategy

**Redis:**
- Key format: `{userId}:{feedType}`
- TTL: 30 minutes
- Cache-aside pattern

## Resilience Patterns

1. **Retry**: Feed repository calls (3 attempts)
2. **Circuit Breaker**: External service calls
3. **Timeout**: Configurable timeouts
4. **Idempotency**: Event processing via idempotency keys

## Security

- OAuth2 Resource Server (Entra ID/External ID)
- JWT validation
- Workload identity for Azure services
- Network policies for K8s

## Observability

- OpenTelemetry instrumentation
- Azure Monitor metrics
- Structured logging
- Correlation IDs

## Performance

- Horizontal pod autoscaling
- Redis caching (30 min TTL)
- Cosmos DB RU: 400
- Async event processing

