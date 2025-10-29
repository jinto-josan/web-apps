# Create Comment Sequence Diagram

## Overview
This sequence diagram shows the flow for creating a comment with idempotency, profanity filtering, and real-time broadcasting.

## Sequence Flow

```plantuml
@startuml CreateCommentSequence
participant Client
participant "Comment Controller" as Controller
participant "Comment Application Service" as Service
participant "Idempotency Checker" as Idempotency
participant "Profanity Filter" as Filter
participant "Comment Repository" as Repository
participant "Cosmos DB" as CosmosDB
participant "Event Publisher" as Publisher
participant "Service Bus" as ServiceBus
participant "Broadcast Adapter" as Broadcast
participant "Web PubSub" as WebPubSub

Client -> Controller: POST /api/v1/videos/{videoId}/comments\n{text, parentId}\nIdempotency-Key: key-123
activate Controller

Controller -> Service: createComment(command)
activate Service

Service -> Idempotency: checkDuplicate(idempotencyKey)
activate Idempotency
Idempotency -> Idempotency: Get from Redis
Idempotency -> Service: Optional<String> result
deactivate Idempotency

alt Idempotency hit
    Service -> Repository: findById(existingId)
    activate Repository
    Repository -> CosmosDB: Query by id
    CosmosDB -> Repository: Comment
    Repository -> Service: Comment
    deactivate Repository
    Service -> Controller: CommentDto
    Controller -> Client: 200 OK with existing comment
else New comment
    Service -> Filter: filterProfanity(text)
    activate Filter
    Filter -> Filter: Check profanity list
    Filter -> Service: filteredText
    deactivate Filter
    
    Service -> Service: create Comment aggregate
    note right: Domain validation
    
    Service -> Repository: save(comment)
    activate Repository
    Repository -> CosmosDB: Upsert document
    CosmosDB -> Repository: Saved Comment
    Repository -> Service: Comment
    deactivate Repository
    
    Service -> Idempotency: storeResult(key, commentId, 3600s)
    activate Idempotency
    Idempotency -> Idempotency: Store in Redis with TTL
    deactivate Idempotency
    
    Service -> Publisher: publishCommentCreated(event)
    activate Publisher
    Publisher -> ServiceBus: Send message
    activate ServiceBus
    ServiceBus -> ServiceBus: Publish to topic
    ServiceBus -> Publisher: ACK
    deactivate ServiceBus
    deactivate Publisher
    
    Service -> Broadcast: broadcastCommentCreated(videoId, json)
    activate Broadcast
    Broadcast -> WebPubSub: Send to group
    activate WebPubSub
    WebPubSub -> WebPubSub: Broadcast to connected clients
    WebPubSub -> Broadcast: Success
    deactivate WebPubSub
    deactivate Broadcast
    
    Service -> Controller: CommentDto
    Controller -> Client: 201 Created with ETag
end

deactivate Service
deactivate Controller

@enduml
```

## Key Points

1. **Idempotency Check**: First step checks Redis for existing request
2. **Profanity Filtering**: Text is filtered before persistence
3. **Domain Validation**: Comment aggregate enforces business rules
4. **Atomic Persistence**: Saves to Cosmos DB with partition key
5. **Event Publishing**: Publishes domain event to Service Bus
6. **Real-time Broadcast**: Broadcasts to Web PubSub for live updates

## Error Handling

- Idempotency: Returns existing comment if key exists
- Profanity: Filters and logs violation
- Repository: Throws IllegalArgumentException on invalid data
- Broadcasting: Non-blocking, best-effort (doesn't fail request)

## Performance Considerations

- Redis lookup: O(1) for idempotency check
- Cosmos DB write: ~20ms (partitioned by videoId)
- Service Bus: Async with retry
- Web PubSub: Non-blocking, fire-and-forget

## Resilience Patterns Applied

1. **Circuit Breaker**: On Cosmos DB and Service Bus
2. **Retry**: On transient failures
3. **Bulkhead**: Isolates event publishing
4. **Rate Limiter**: Prevents spam (10/60s per user)

