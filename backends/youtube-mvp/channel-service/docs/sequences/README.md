# Sequence Diagrams

This directory contains PlantUML sequence diagrams for key flows in the Channel Service.

## Diagrams

### subscribe-flow.puml
Shows the subscription flow with idempotency handling, anti-supernode sharding, and event publishing.

**Key Steps:**
1. Extract userId from JWT
2. Check idempotency key cache
3. If cache hit, return cached response
4. If cache miss, check for existing subscription
5. Create new subscription with shard suffix
6. Save to Cosmos DB
7. Update stats
8. Cache idempotency
9. Publish event to Service Bus

**Anti-Supernode Strategy:**
- Calculate shard suffix from last 2 chars of userId
- Partition subscriptions across 256 shards
- Prevents hotspot on popular channels

**Idempotency:**
- Redis cache with 24-hour TTL
- Duplicate requests return cached response
- CAS operations for atomic updates

### Generating Images

```bash
# Install PlantUML
brew install plantuml  # macOS
sudo apt-get install plantuml  # Ubuntu

# Generate PNG
plantuml -tpng docs/sequences/subscribe-flow.puml

# Generate SVG
plantuml -tsvg docs/sequences/subscribe-flow.puml
```

## Flow Descriptions

### Subscribe Flow
1. User sends POST with Idempotency-Key header
2. Controller validates JWT and extracts userId
3. Use case checks Redis for idempotency
4. If new, creates subscription with shard suffix
5. Saves to Cosmos DB
6. Updates stats atomically
7. Caches idempotency response
8. Publishes subscription.created event
9. Returns 201 Created with ETag

### Unsubscribe Flow
1. User sends DELETE with Idempotency-Key
2. Check idempotency cache
3. Find active subscription
4. Mark as inactive
5. Save to Cosmos DB
6. Decrement stats
7. Cache idempotency
8. Publish subscription.deleted event
9. Return 204 No Content

## Performance Targets

- Subscribe: P95 < 100ms
- Read: P95 < 80ms
- Idempotency check: < 5ms
- Redis cache hit: < 2ms
