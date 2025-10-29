# Video Published Fan-out Sequence

```plantuml
@startuml
actor User
participant "Video Catalog Service" as VideoService
participant "Service Bus" as ServiceBus
participant "Feeds Service" as FeedsService
participant "Feed Cache (Redis)" as Redis
participant "Cosmos DB" as Cosmos
database "Subscription DB" as SubDB

User -> VideoService: Publish video
VideoService -> VideoService: Save video
VideoService -> ServiceBus: Publish event (video.published)

ServiceBus -> FeedsService: video.published event\n(idempotency-key: xxx)
activate FeedsService

FeedsService -> SubDB: Get subscribers for channel
SubDB --> FeedsService: List of user IDs

loop For each subscriber
    FeedsService -> Redis: Evict cache\nkey: {userId}:SUBSCRIPTIONS
    
    opt Cache miss or regeneration needed
        FeedsService -> Cosmos: Generate new feed
        Cosmos --> FeedsService: Feed items
        FeedsService -> Redis: Cache feed (30 min TTL)
    end
    
    FeedsService -> Cosmos: Save feed view metrics
end

FeedsService --> ServiceBus: Processed

note right of FeedsService
  Idempotent processing:
  - Same event (idempotency-key)
  - Multiple deliveries
  - Exactly-once semantics
end note

@enduml
```

## Description

When a video is published, the feeds service:

1. Receives `video.published` event from Service Bus
2. Retrieves all users subscribed to the video's channel
3. For each subscriber:
   - Evicts their subscriptions feed cache
   - Optionally regenerates feed (async)
   - Tracks view metrics
4. Process is idempotent using `idempotency-key` header

## Resilience

- **Retry**: Failed event processing retries up to 3 times
- **Circuit Breaker**: Opens if external services fail
- **Timeout**: 30 second timeout for fan-out operations

