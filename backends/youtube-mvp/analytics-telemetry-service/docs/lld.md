# Low-Level Design: Analytics & Telemetry Service

## Overview

This document describes the low-level design of the Analytics & Telemetry Service, a production-grade microservice for collecting and forwarding telemetry events to Azure Event Hubs.

## Architecture

### Hexagonal Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│              Interfaces Layer (REST)                      │
│  TelemetryController, GlobalExceptionHandler             │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Application Layer                           │
│  TelemetryApplicationService, DTOs, Mappers             │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Domain Layer                                │
│  TelemetryEvent, Value Objects, Repository Ports        │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Infrastructure Layer                        │
│  EventHubsPublisher, BlobDLQ, RedisIdempotency          │
└──────────────────────────────────────────────────────────┘
```

## Domain Model

### Entities

**TelemetryEvent**
- Core domain entity representing a telemetry event
- Immutable after creation
- Contains event ID, type, source, schema, timestamp, and properties

### Value Objects

- **EventId**: Unique identifier (UUID)
- **EventType**: Type-safe event type (e.g., "video.view")
- **EventSource**: Source identifier (client/server)
- **EventSchema**: Schema version and name

### Domain Services (Ports)

- **EventPublisher**: Interface for publishing events to Event Hubs
- **SchemaValidator**: Interface for schema validation
- **DeadLetterQueue**: Interface for DLQ operations
- **IdempotencyService**: Interface for idempotency checking

## Application Layer

### Services

**TelemetryApplicationService**
- Orchestrates event collection and processing
- Validates events
- Checks idempotency
- Publishes to Event Hubs
- Handles errors and DLQ

**TelemetryStatsService**
- Tracks statistics (events processed, errors, etc.)
- Thread-safe counters

### DTOs

- **BatchEventRequest**: Incoming batch request
- **BatchEventResponse**: Response with acceptance/rejection status
- **TelemetryEventRequest**: Individual event request
- **StatsResponse**: Service statistics

## Infrastructure Layer

### Adapters

**EventHubsEventPublisher**
- Publishes events to Azure Event Hubs
- Implements batching (100 events per batch)
- Circuit breaker and retry via Resilience4j
- Async publishing with CompletableFuture

**BlobDeadLetterQueue**
- Stores failed events in Azure Blob Storage
- Organizes by date (yyyy/MM/dd)
- Includes error details and exception info

**RedisIdempotencyService**
- Tracks processed idempotency keys in Redis
- TTL: 24 hours (configurable)
- Thread-safe operations

**JsonSchemaValidator**
- Validates events against JSON Schema
- Loads schemas from classpath
- Supports multiple schema versions

**InMemoryTelemetryEventRepository**
- In-memory storage for local development
- Production can use Cosmos DB or PostgreSQL

## Flow Diagram

```
Client Request
    │
    ▼
TelemetryController.collectEvents()
    │
    ▼
TelemetryApplicationService.processBatch()
    │
    ├─► IdempotencyService (check)
    ├─► SchemaValidator (validate)
    ├─► TelemetryEventRepository (save)
    └─► EventPublisher.publishBatch() (async)
            │
            ├─► Success → StatsService.recordProcessed()
            └─► Failure → DeadLetterQueue.sendToDlq()
```

## Resilience Patterns

1. **Retry**: 3 attempts with 1s delay
2. **Circuit Breaker**: Opens at 50% failure rate
3. **Rate Limiter**: 10,000 events/second
4. **Timeout**: 5 seconds for Event Hubs operations

## Security

- OAuth2 Resource Server (JWT validation)
- Scope-based authorization: `telemetry.write`, `telemetry.read`
- Non-root container execution

## Performance Considerations

- **Batching**: Reduces Event Hubs API calls
- **Async Processing**: Non-blocking event publishing
- **Connection Pooling**: Redis and Event Hubs connection pools
- **Backpressure**: Circuit breaker prevents overload

## Error Handling

- **Validation Errors**: Returned immediately with ProblemDetails
- **Publish Failures**: Sent to DLQ (Blob Storage)
- **Schema Errors**: Rejected with validation message
- **Duplicate Events**: Rejected based on idempotency key

## Monitoring & Observability

- **Metrics**: Prometheus metrics via Actuator
- **Traces**: OpenTelemetry to Azure Monitor
- **Logs**: Structured logging with correlation IDs
- **Health**: Liveness and readiness probes

