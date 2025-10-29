# Live Streaming Service - Low-Level Design

## Overview

The Live Streaming Service manages the lifecycle of live streaming events, integrating with Azure Media Services (AMS) for video encoding and delivery. The service implements hexagonal architecture with DDD patterns, CQRS, and comprehensive resilience mechanisms.

## Domain Model

### Core Entity: LiveEvent

```
┌─────────────────────────────────────────────────────────────────┐
│                          LiveEvent                              │
├─────────────────────────────────────────────────────────────────┤
│ - id: String                                                    │
│ - userId: String                                                │
│ - channelId: String                                             │
│ - configuration: LiveEventConfiguration                         │
│ - state: LiveEventState                                         │
│ - amsReference: AmsLiveEventReference                           │
│ - endpoints: List<StreamingEndpoint>                           │
│ - ingestUrl: String                                             │
│ - previewUrl: String                                            │
│ - createdAt: Instant                                            │
│ - startedAt: Instant                                            │
│ - stoppedAt: Instant                                            │
│ - archivedAt: Instant                                           │
│ - failureReason: String                                          │
│ - domainEvents: List<Object>                                     │
├─────────────────────────────────────────────────────────────────┤
│ + start()                                                       │
│ + stop()                                                        │
│ + archive()                                                     │
│ + confirmStarted()                                              │
│ + confirmStopped()                                              │
│ + markFailed()                                                  │
│ + assignAmsReference()                                          │
└─────────────────────────────────────────────────────────────────┘
```

### Value Objects

#### LiveEventConfiguration
```
┌─────────────────────────────────────────────────────────────┐
│                  LiveEventConfiguration                     │
├─────────────────────────────────────────────────────────────┤
│ - name: String                                              │
│ - description: String                                       │
│ - channelId: String                                         │
│ - userId: String                                            │
│ - region: String                                            │
│ - dvrEnabled: Boolean                                       │
│ - dvrWindowInMinutes: Integer                               │
│ - lowLatencyEnabled: Boolean                                │
│ - autoStart: Boolean                                        │
│ - maxConcurrentViewers: Integer                             │
│ - allowedCountries: String[]                                │
│ - blockedCountries: String[]                                │
└─────────────────────────────────────────────────────────────┘
```

#### LiveEventState (Enum)

```
CREATED → STARTING → RUNNING → STOPPING → STOPPED → ARCHIVING → ARCHIVED
   ↓         ↓          ↓
FAILED → DELETED     DELETED
```

Valid transitions are enforced at the domain level.

#### AmsLiveEventReference
```
┌─────────────────────────────────────────────────────────────┐
│              AmsLiveEventReference                          │
├─────────────────────────────────────────────────────────────┤
│ - liveEventId: String                                       │
│ - liveEventName: String                                     │
│ - resourceGroupName: String                                 │
│ - accountName: String                                       │
│ - resourceId: String                                        │
│ - ingestUrl: String                                         │
│ - previewUrl: String                                        │
│ - state: String                                             │
└─────────────────────────────────────────────────────────────┘
```

## Architecture Layers

### 1. Interface Layer (REST)

**Responsibilities:**
- HTTP request/response handling
- Validation
- Authentication/Authorization
- ETag support
- API versioning

**Controllers:**
- `LiveEventController`: CRUD operations for live events
- `AmsCallbackController`: Handles AMS state change callbacks

### 2. Application Layer

**Responsibilities:**
- Orchestrates domain operations
- Coordinates between domain and infrastructure
- Transaction management
- Resilience patterns application

**Key Components:**
- `LiveEventOrchestrationService`: Coordinates live event lifecycle
- DTOs: Request/Response objects
- MapStruct Mappers: Object transformations

### 3. Domain Layer

**Responsibilities:**
- Business logic
- State management
- Domain events
- Entities and value objects

**Key Components:**
- `LiveEvent`: Aggregate root
- Value Objects: `LiveEventConfiguration`, `AmsLiveEventReference`, `StreamingEndpoint`
- Domain Events: `LiveEventCreated`, `LiveEventStarted`, `LiveEventStopped`
- Ports: `LiveEventRepository`, `AmsClient`, `EventPublisher`, `IdempotencyService`

### 4. Infrastructure Layer

**Responsibilities:**
- External service integration
- Data persistence
- Messaging
- Caching

**Adapters:**
- `AmsClientAdapter`: Azure Media Services integration
- `JpaLiveEventRepository`: PostgreSQL persistence
- `RedisIdempotencyService`: Redis-based idempotency
- `ServiceBusEventPublisher`: Event publishing to Service Bus

## State Machine

### Live Event Lifecycle States

```
┌─────────┐
│ CREATED │
└────┬────┘
     │
     ├──────────┬─────────────────┐
     │          │                 │
     ▼          ▼                 ▼
┌─────────┐  ┌─────────┐     ┌────────┐
│STARTING │  │ FAILED  │     │DELETED │
└────┬────┘  └─────────┘     └────────┘
     │
     ▼
┌─────────┐
│RUNNING │
└────┬────┘
     │
     ▼
┌─────────┐
│STOPPING │
└────┬────┘
     │
     ▼
┌─────────┐
│ STOPPED │
└────┬────┘
     │
     ▼
┌───────────┐
│ARCHIVING │
└────┬──────┘
     │
     ▼
┌──────────┐
│ARCHIVED │
└────┬─────┘
     │
     ▼
┌─────────┐
│DELETED │
└────────┘
```

## Key Design Patterns

### 1. Hexagonal Architecture (Ports & Adapters)

```
Domain (Core)
   ↕
Ports (Interfaces)
   ↕
Adapters (Implementations)
```

- **Domain**: Pure business logic, no external dependencies
- **Ports**: Interface definitions for external integrations
- **Adapters**: Concrete implementations (JPA, AMS, Redis, Service Bus)

### 2. CQRS (Command Query Responsibility Segregation)

- **Commands**: `createLiveEvent`, `startLiveEvent`, `stopLiveEvent`
- **Queries**: `getLiveEvent`, `getLiveEventsByChannel`

### 3. Domain Events

Events are published for important state changes:
- `LiveEventCreated`: When an event is created
- `LiveEventStarted`: When an event starts streaming
- `LiveEventStopped`: When an event stops

### 4. Resilience Patterns

- **Circuit Breaker**: Prevents cascading failures when AMS is down
- **Retry**: Automatically retries transient failures
- **Timeout**: Sets maximum execution time for AMS calls
- **Idempotency**: Ensures idempotent operations via Redis

### 5. Outbox Pattern

For reliable event publishing, events are stored in database first, then published to Service Bus (TODO: implement).

## Database Schema

### live_events Table

```sql
CREATE TABLE live_events (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    channel_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    description VARCHAR(2000),
    state VARCHAR(50) NOT NULL,
    ams_live_event_id VARCHAR(255),
    ams_live_event_name VARCHAR(255),
    ingest_url VARCHAR(1000),
    preview_url VARCHAR(1000),
    region VARCHAR(100),
    dvr_enabled BOOLEAN DEFAULT true,
    dvr_window_in_minutes INTEGER DEFAULT 120,
    low_latency_enabled BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    stopped_at TIMESTAMP,
    archived_at TIMESTAMP,
    failure_reason VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_live_events_user_id ON live_events(user_id);
CREATE INDEX idx_live_events_channel_id ON live_events(channel_id);
CREATE INDEX idx_live_events_state ON live_events(state);
CREATE INDEX idx_live_events_user_state ON live_events(user_id, state);
```

## External Integrations

### 1. Azure Media Services

**Purpose**: Live event management, encoding, streaming

**Operations**:
- `createLiveEvent()`: Creates AMS Live Event
- `startLiveEvent()`: Starts streaming
- `stopLiveEvent()`: Stops streaming
- `deleteLiveEvent()`: Cleanup
- `getLiveEventStatus()`: Health check

**Callback Handling**: AMS sends state change events to `/api/v1/events/ams-live-callback`

### 2. Redis

**Purpose**: Idempotency tracking and caching

**Key Patterns**:
- `idempotency:{key}`: Stores request ID for idempotency
- TTL: 24 hours

### 3. Azure Service Bus

**Purpose**: Event publishing for downstream services

**Topics**:
- `live-events-created`
- `live-events-started`
- `live-events-stopped`

## Security

### Authentication/Authorization

- **OAuth2 Resource Server**: JWT token validation
- **User Context**: Extracted from JWT claims
- **Authorization**: Users can only access their own live events

### Endpoint Security

- **Public**: `/api/v1/events/ams-live-callback` (requires callback secret validation)
- **Protected**: All other endpoints require valid JWT

## Testing Strategy

### Unit Tests

- Domain logic: State transitions, business rules
- Service logic: Orchestration scenarios
- Mappers: DTO conversions

### Integration Tests

- **Testcontainers**: PostgreSQL and Redis
- **WireMock**: External service mocking
- **Test Scenarios**:
  - Create live event flow
  - Start/stop transitions
  - AMS callback handling
  - Idempotency

### Contract Tests

- API contracts: OpenAPI schema validation
- Service Bus events: Message format validation

## Deployment

### Container

- **Base Image**: `eclipse-temurin:17-jre-alpine`
- **Multi-stage Build**: Optimized for size
- **Non-root User**: Security best practice
- **Health Checks**: Liveness, readiness, startup probes

### Kubernetes

- **Replicas**: 3 (min), 20 (max) via HPA
- **Resources**: 512Mi-2Gi memory, 250m-1000m CPU
- **Pod Disruption Budget**: Min 2 available
- **Network Policy**: Restricted ingress/egress

## Observability

### Metrics

- Live events created/started/stopped counts
- AMS API latency
- Error rates
- Circuit breaker state

### Traces

- Request correlation IDs
- Distributed tracing via OpenTelemetry
- Azure Monitor integration

### Logs

- Structured logging with correlation IDs
- Log levels: DEBUG (local), INFO (prod)

## Failover & Disaster Recovery

### Failover Plan

1. **AMS Failure**: Circuit breaker opens, events stay in CREATED state
2. **PostgreSQL Failure**: Read replicas for queries
3. **Redis Failure**: Idempotency lost, but events continue

### Health Checks

- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`
- **Startup**: `/actuator/health`

## Future Enhancements

- [ ] Outbox pattern for reliable event publishing
- [ ] Full AMS SDK integration (currently stubbed)
- [ ] Real-time viewer count via WebSocket
- [ ] DVR playback support
- [ ] Multi-region deployment
- [ ] Chaos engineering tests

