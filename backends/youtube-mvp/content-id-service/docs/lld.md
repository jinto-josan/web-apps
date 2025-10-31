# Content ID Service - Low-Level Design

## Overview

The Content ID Service implements fingerprinting, matching, and claims/disputes management using hexagonal architecture with clear separation of concerns.

## Domain Model

### Entities

**Fingerprint**
- Unique identifier
- Video ID reference
- Fingerprint data (hash, algorithm, duration, blob URI)
- Status (PENDING, PROCESSED, FAILED)
- Timestamps

**Match**
- Unique identifier
- Source and matched fingerprint IDs
- Source and matched video IDs
- Match score (0.0 to 1.0)
- Detection timestamp
- Processed flag

**Claim**
- Unique identifier
- Claimed video ID
- Owner ID
- List of matches
- Status (PENDING, REVIEWING, RESOLVED, WITHDRAWN)
- Dispute status (PENDING, UNDER_REVIEW, APPROVED, REJECTED, WITHDRAWN)
- Resolution details

### Value Objects

- **VideoId**: UUID wrapper
- **FingerprintId**: UUID wrapper
- **ClaimId**: UUID wrapper
- **MatchScore**: Double (0.0-1.0) with validation
- **FingerprintData**: Hash, algorithm, duration, blob URI
- **DisputeStatus**: Enum (PENDING, UNDER_REVIEW, APPROVED, REJECTED, WITHDRAWN)

## Architecture Layers

### Domain Layer

**Entities**: `Fingerprint`, `Match`, `Claim`
**Value Objects**: `VideoId`, `FingerprintId`, `ClaimId`, `MatchScore`, `FingerprintData`, `DisputeStatus`
**Domain Events**: `FingerprintCreatedEvent`, `MatchDetectedEvent`, `ClaimCreatedEvent`, `ClaimResolvedEvent`
**Repository Interfaces** (Ports):
- `FingerprintRepository`
- `MatchRepository`
- `ClaimRepository`
- `FingerprintIndexRepository`
- `EventPublisher`

**Domain Services** (Ports):
- `FingerprintEngine` - Fingerprint generation
- `MatchEngine` - Similarity matching

### Application Layer

**Commands**: `CreateFingerprintCommand`, `CreateMatchCommand`, `CreateClaimCommand`, `ResolveClaimCommand`
**DTOs**: `FingerprintResponse`, `MatchResponse`, `ClaimResponse`, `MatchRequest`
**Mappers**: `ContentIdMapper` (MapStruct)
**Services**: `FingerprintService`, `MatchService`, `ClaimService`

### Infrastructure Layer

**Persistence**:
- `FingerprintRepositoryAdapter` (JPA)
- `MatchRepositoryAdapter` (JPA)
- `ClaimRepositoryAdapter` (JPA)
- `FingerprintIndexRepositoryAdapter` (Cosmos DB)
- JPA Entities: `FingerprintJpaEntity`, `MatchJpaEntity`, `ClaimJpaEntity`, `OutboxEventJpaEntity`

**External Services**:
- `BlobStorageService` - Azure Blob Storage integration
- `FingerprintEngineImpl` - Fingerprint generation implementation
- `MatchEngineImpl` - Similarity matching implementation

**Messaging**:
- `OutboxEventPublisherImpl` - Transactional outbox publisher
- `OutboxDispatcher` - Scheduled dispatcher for outbox events
- Event Hubs integration for match events
- Service Bus integration for case workflow

**Config**:
- `SecurityConfig` - OAuth2 Resource Server
- `OpenApiConfig` - Swagger/OpenAPI documentation

## Data Flow

### Fingerprint Creation Flow

1. REST API receives `POST /api/v1/fingerprint/{videoId}`
2. `FingerprintController` → `FingerprintService.createFingerprint()`
3. `FingerprintEngine.generateFingerprint()` creates fingerprint data
4. `BlobStorageService.uploadFingerprint()` stores hash in blob
5. `FingerprintRepository.save()` persists to PostgreSQL
6. `EventPublisher.publish()` creates outbox event
7. `OutboxDispatcher` publishes to Service Bus
8. Response returned to client

### Match Detection Flow

1. `MatchService.findMatches()` invoked
2. `MatchEngine.findMatches()` queries fingerprint index (Cosmos DB)
3. Similar fingerprints found above threshold
4. Match entities created and persisted
5. `MatchDetectedEvent` published via Event Hubs
6. Matches returned to client

### Claim Creation Flow

1. REST API receives `POST /api/v1/claims`
2. `ClaimController` → `ClaimService.createClaim()`
3. Matches fetched from repository
4. Claim entity created
5. `ClaimCreatedEvent` published via Service Bus (case workflow)
6. Response returned to client

## Database Schema

### PostgreSQL Tables

**fingerprints**
- Primary key: `id` (UUID)
- Unique constraint: `video_id`
- Indexes: `status`, `created_at`

**matches**
- Primary key: `id` (UUID)
- Unique constraint: `(source_fingerprint_id, matched_fingerprint_id)` (idempotency)
- Indexes: `source_video_id`, `matched_video_id`, `processed`

**claims**
- Primary key: `id` (UUID)
- Indexes: `claimed_video_id`, `owner_id`, `status`

**outbox_events**
- Primary key: `id` (UUID)
- Index: `(status, created_at)` for pending events

### Cosmos DB

**fingerprint-index** container
- Partition key: Hash prefix
- Document ID: Fingerprint ID
- Hash vector stored for similarity search

## Patterns

### Outbox Pattern
- Domain events saved to `outbox_events` table in same transaction
- `OutboxDispatcher` polls and publishes to Service Bus/Event Hubs
- Events marked as DISPATCHED after successful publish

### Idempotency
- Redis-based deduplication via `Idempotency-Key` header
- TTL: 24 hours
- Returns 409 Conflict for duplicates

### CQRS
- Separate read/write models (future enhancement)
- Write model: Domain entities
- Read model: DTOs with MapStruct mappers

## Resilience

- **Circuit Breaker**: Blob storage, match engine
- **Retry**: Transient failures
- **Timeout**: 10s for blob operations
- **Bulkhead**: Isolated thread pools

## Observability

- OpenTelemetry instrumentation
- Azure Monitor integration
- Correlation IDs for request tracing
- Structured logging

