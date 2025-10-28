# Low-Level Design: Channel Service with Subscriptions

## Overview

The Channel Service is responsible for managing YouTube channels and handling subscription operations. It implements a production-grade architecture with hexagonal patterns, CQRS, and anti-supernode strategies.

## Domain Models

### Channel
```java
Channel {
  id: ULID
  ownerUserId: ULID
  handleLower: String (unique)
  title: String
  description: String
  language: String
  country: String
  branding: Branding
  policy: Policy
  createdAt: Instant
  updatedAt: Instant
  etag: String
}
```

### Subscription
```java
Subscription {
  id: ULID
  userId: ULID
  channelId: ULID
  shardSuffix: String (last 2 chars of userId)
  createdAt: Instant
  isActive: Boolean
  notificationPreference: {
    notifyOnUpload: Boolean
    notifyOnLive: Boolean
    notifyOnCommunityPost: Boolean
    notifyOnShorts: Boolean
  }
}
```

### ChannelSubscriptionStats (Read Model)
```java
ChannelSubscriptionStats {
  channelId: ULID
  subscriberCount: Long
  activeSubscriberCount: Long
  lastUpdatedAt: Instant
  lastSubscriberAddedAt: Instant
}
```

## Repository Interfaces

### SubscriptionRepository (Port)
```java
- findById(subscriptionId)
- findByUserIdWithShard(userId, shardSuffix, offset, limit)
- findByUserIdAndChannelId(userId, channelId)
- countByUserId(userId)
- countByChannelId(channelId)
- save(subscription)
- delete(subscription)
```

### IdempotencyRepository (Port)
```java
- get(idempotencyKey)
- put(idempotencyKey, response, ttl)
- checkAndSet(idempotencyKey, response, ttl)
```

## Use Cases

### Subscribe Use Case
1. Extract user ID from JWT
2. Check idempotency key in Redis
3. If cached, return cached response
4. Check if already subscribed
5. If already active, throw ConflictException
6. Create subscription with ULID
7. Calculate shard suffix (last 2 chars of userId)
8. Save to Cosmos DB
9. Increment stats atomically
10. Cache idempotency response
11. Publish subscription.created event

### Unsubscribe Use Case
1. Extract user ID from JWT
2. Check idempotency key
3. Find subscription
4. If not found or already inactive, return (idempotent)
5. Mark inactive
6. Save to Cosmos DB
7. Decrement stats atomically
8. Cache idempotency
9. Publish subscription.deleted event

## Infrastructure Adapters

### CosmosSubscriptionRepository (Adapter)
- Partition key: `shardSuffix`
- Container: `subscriptions`
- Indexes on: userId, channelId
- Optimized for read-heavy workloads

### IdempotencyRepositoryImpl (Adapter)
- Redis with 24-hour TTL
- Key format: `idempotency:{idempotencyKey}`
- CAS operations for atomic updates

## Anti-Supernode Strategy

### Problem
Popular channels (supernodes) would create hotspots if all subscriptions were stored together.

### Solution
- Shard subscriptions by user ID suffix (last 2 hex characters)
- 256 possible shards (00-FF)
- Distributes subscriptions across partitions
- Example: User `01HZ...AB` → shard `AB`

### Query Strategy
```sql
SELECT * FROM subscriptions 
WHERE userId = '01HZ...AB' 
  AND shardSuffix = 'AB'
```

## CQRS Read Models

### Purpose
Optimize read performance by pre-aggregating statistics.

### ChannelSubscriptionStats
- Stored in separate container: `channel-subscription-stats`
- Updated via outbox pattern
- Atomic increment/decrement operations
- Refresh every N updates or T seconds

## Event Flow

### Subscribe Event
```
POST /api/v1/channels/{id}/subscriptions
  ↓
SubscriptionUseCase.subscribeToChannel()
  ↓
SubscriptionRepository.save()
  ↓
StatsRepository.incrementSubscriberCount()
  ↓
IdempotencyRepository.put()
  ↓
EventPublisher.publish("subscription.created")
  ↓
Service Bus Topic: subscription.created
```

### Fan-Out Pattern
When subscription.created event is published:
1. Feed Service receives event
2. Adds user to channel feed
3. Prepares notification (if enabled)

## Error Handling

### Idempotency
- All write operations require `Idempotency-Key` header
- Redis deduplication with 24-hour window
- Returns cached response if duplicate

### Conflict Scenarios
- Already subscribed: 409 Conflict
- Not found: 404 Not Found
- Invalid request: 400 Bad Request

## Performance Optimization

### Caching Strategy
- Cache-aside pattern
- TTL: 1 hour
- Invalidate on write
- Redis for hot data

### Query Optimization
- Use shard suffix in all user queries
- Materialized views for stats
- Pagination for lists (default: 20, max: 100)

## Security

### Authentication
- OAuth2 Resource Server
- JWT validation via Spring Security
- Claims: `uid`, `sub`, `roles`

### Authorization
- User can only subscribe for themselves
- Channel owners can view stats

## Observability

### Metrics
- `subscriptions.count` - Counter of subscriptions
- `subscription.latency` - Histogram of operation latency
- `idempotency.cache.hits` - Cache hit rate

### Traces
- OpenTelemetry integration
- Correlation IDs per request
- Span hierarchy: HTTP → Service → Repository

## Testing Strategy

### Unit Tests
- Domain logic (use cases)
- Value object validation
- Repository mocks

### Integration Tests
- Testcontainers for Cosmos DB
- Testcontainers for Redis
- WireMock for Service Bus

### Idempotency Tests
- Duplicate request handling
- Cache expiration
- CAS contention

## Deployment

### Kubernetes
- Deployment with 3 replicas
- HPA: 3-10 replicas (CPU 70%, Memory 80%)
- PDB: min 2 available
- Network policies for isolation

### Health Checks
- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`

### Resource Requirements
- CPU: 500m requests, 2000m limits
- Memory: 1Gi requests, 2Gi limits
