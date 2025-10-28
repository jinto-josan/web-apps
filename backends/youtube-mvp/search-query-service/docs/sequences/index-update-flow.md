# Index Update Flow Sequence Diagram

```plantuml
@startuml
participant "Video Catalog" as Catalog
participant "Service Bus" as Bus
participant "Index Consumer" as Consumer
participant "Application Service" as App
participant "Search Service" as Domain
participant "Azure Search" as Search

Catalog -> Bus: Publish video.published event
activate Bus

Bus --> Consumer: Receive message (video.published)
activate Consumer

Consumer -> Consumer: Parse message body
Consumer -> Consumer: Extract eventType & videoId

alt EventType == DELETED
    Consumer -> App: handleIndexUpdate(document, "DELETED")
    activate App
    App -> Domain: deleteDocument(videoId)
    activate Domain
    Domain -> Search: Delete document
    activate Search
    Search --> Domain: Success
    deactivate Search
    Domain --> App: Done
    deactivate Domain
    App --> Consumer: Complete
    deactivate App
else EventType == PUBLISHED | UPDATED
    Consumer -> Consumer: Build SearchDocument from event data
    Consumer -> App: handleIndexUpdate(document, eventType)
    activate App
    App -> Domain: upsertDocument(document)
    activate Domain
    Domain -> Domain: Convert to Azure Search document
    Domain -> Search: Upload document
    activate Search
    Search --> Domain: Success
    deactivate Search
    Domain --> App: Done
    deactivate Domain
    App --> Consumer: Complete
    deactivate App
end

Consumer -> Bus: Complete message
deactivate Consumer
deactivate Bus

@enduml
```

## Description

1. **Event Publishing**: Video Catalog Service publishes events to Service Bus
2. **Message Consumption**: Index Consumer receives message with idempotency key
3. **Message Processing**:
   - Parse event type and video data
   - Build SearchDocument from event payload
   - Route to appropriate operation (upsert or delete)
4. **Index Update**: Update Azure Cognitive Search index
5. **Message Completion**: Mark message as complete (or abandon on error)

## Idempotency

- Messages include idempotency key (videoId + timestamp)
- Redis check before processing
- Already processed → skip
- Not processed → process and mark in Redis

## Error Handling

- Retry on transient failures (up to 3 times)
- Dead-letter queue for permanent failures
- Circuit breaker for Azure Search failures
