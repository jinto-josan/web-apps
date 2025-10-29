# Live Streaming Service - Sequence Diagrams

This directory contains PlantUML sequence diagrams for key operations in the Live Streaming Service.

## Diagrams

### 1. create-live-event.puml
Sequence diagram for creating a new live event, including:
- Idempotency key processing
- Domain event publishing
- Azure Media Services integration
- State transitions

### 2. start-live-event.puml
Sequence diagram for starting a live event, including:
- State machine transitions
- AMS callback handling
- Event publishing

### 3. lifecycle.puml
Complete lifecycle flow from creation to archiving:
- Create → Start → Run → Stop → Archive
- AMS callbacks
- Event publishing
- State persistence

## Viewing Diagrams

### Option 1: PlantUML CLI
```bash
# Install PlantUML
brew install plantuml

# Generate PNG
plantuml create-live-event.puml

# Generate SVG
plantuml -tsvg create-live-event.puml
```

### Option 2: Online Editor
Visit http://www.plantuml.com/plantuml/uml/ and paste the content.

### Option 3: VS Code Extension
Install the PlantUML extension in VS Code for live preview.

## Key Patterns Shown

1. **Idempotency**: Deduplication via Redis using `Idempotency-Key` header
2. **State Machine**: Enforced state transitions for live events
3. **Domain Events**: Event publishing for downstream services
4. **Circuit Breaker**: Resilience patterns for AMS integration
5. **Callbacks**: AMS state change callbacks processed asynchronously

## Integration Points

- **Azure Media Services**: Live event management, encoding, streaming
- **PostgreSQL**: Event persistence with optimistic locking (version field)
- **Redis**: Idempotency tracking with 24-hour TTL
- **Service Bus**: Domain event publishing for event-driven architecture

