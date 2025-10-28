# Video Catalog Service - Implementation Summary

## Overview

This document provides a high-level summary of the Video Catalog Service implementation for the YouTube-scale platform.

## Architecture

### Layers

1. **Domain Layer**: Core business logic, aggregates, value objects
2. **Application Layer**: Use cases, DTOs, mappers (MapStruct)
3. **Infrastructure Layer**: Cosmos DB, Service Bus, Outbox
4. **Presentation Layer**: REST controllers with API versioning

### Key Patterns

- **Hexagonal Architecture**: Clean separation of concerns
- **CQRS**: Separate read/write models
- **DDD-lite**: Aggregates, repositories, domain services
- **Outbox Pattern**: Reliable event publishing
- **ETag Support**: Optimistic locking

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.3.4 |
| Cloud | Spring Cloud Azure | 5.15.0 |
| Database | Azure Cosmos DB | - |
| Messaging | Azure Service Bus | - |
| Mapper | MapStruct | 1.5.5 |
| Testing | Testcontainers | 1.20.0 |
| Resilience | Resilience4j | 2.1.0 |

## Domain Model

### Video Aggregate
- **ID**: `video-{uuid}`
- **State**: DRAFT → PUBLISHING → PUBLISHED
- **Visibility**: PUBLIC, UNLISTED, PRIVATE
- **Metadata**: Title, description, tags, category
- **Localization**: Multi-language support
- **Metrics**: Views, likes, comments
- **Version**: For ETag/optimistic locking

### Value Objects
- `LocalizedText`: Language + text
- `Duration`: Time in seconds
- `VideoState`: Lifecycle enum
- `VideoVisibility`: Access control enum

## Key Flows

### 1. Create Video
```
POST /api/v1/videos
→ Generate ID
→ Build Video aggregate
→ Save to Cosmos DB
→ Return 201 Created + ETag
```

### 2. Publish Video
```
POST /api/v1/videos/{id}/publish
→ Validate state (must be DRAFT)
→ Transition to PUBLISHED
→ Generate domain event
→ Save to Outbox
→ Return 200 OK
→ Background: Publish to Service Bus
```

### 3. Update Video
```
PATCH /api/v1/videos/{id}
If-Match: "v123"
→ Validate ETag
→ Update metadata
→ Generate new version
→ Save to Cosmos
→ Return 200 OK + new ETag
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/videos` | Create video |
| GET | `/api/v1/videos/{id}` | Get video |
| PATCH | `/api/v1/videos/{id}` | Update video |
| DELETE | `/api/v1/videos/{id}` | Delete video |
| POST | `/api/v1/videos/{id}/publish` | Publish video |
| GET | `/api/v1/videos` | List videos |

## Database Schema

### Cosmos DB Container: `videos`
- Partition Key: `channelId`
- Indexes: `videoId`, `title`, `category`, `tags[]`, `state`, `visibility`

### Cosmos DB Container: `outbox`
- Partition Key: `partitionKey` (date-based)
- Status: PENDING → PROCESSED
- Retry logic: 3 attempts

## Messaging

### Service Bus Topic: `video-events`

**VideoPublishedEvent**:
- Payload: Video metadata
- Consumed by: Search indexer, notifications, recommendations

**Outbox Pattern**:
1. Save event to `outbox` table in same transaction
2. Background processor picks up PENDING events
3. Publishes to Service Bus
4. Marks as PROCESSED

## Resilience

- **Retry**: 3 attempts with exponential backoff
- **Circuit Breaker**: 50% failure threshold
- **Bulkhead**: Max 10 concurrent calls
- **Rate Limiter**: 100 requests/second

## Security

- **Authentication**: OIDC (Entra External ID/B2C)
- **Authorization**: Check owner ID
- **Network Policies**: Kubernetes network isolation

## Observability

- **Metrics**: Prometheus endpoint
- **Traces**: OpenTelemetry
- **Logs**: Structured JSON with correlation IDs

## Deployment

### Kubernetes
- **Replicas**: 3-10 (HPA)
- **Resources**: 500m CPU, 1Gi RAM
- **Health Checks**: Liveness, readiness, startup probes
- **PDB**: Min 2 available

### Docker
- **Base**: `eclipse-temurin:17-jre-alpine`
- **Size**: ~200MB
- **Non-root user**: spring

## Testing

- **Unit Tests**: Domain logic
- **Integration Tests**: Repository adapter
- **Testcontainers**: Cosmos emulator

## Performance

- **P50**: < 50ms
- **P95**: < 200ms
- **P99**: < 500ms
- **Throughput**: 1000 req/s per instance

## Scalability

- **Horizontal**: Multiple instances
- **Database**: Cosmos DB autoscale
- **Messaging**: Partitioned topics

## Security Checklist

- [x] OIDC authentication
- [x] JWT validation
- [x] ETag support (optimistic locking)
- [x] Input validation
- [x] Error handling (RFC 7807)
- [x] Secrets in Key Vault
- [x] Network policies
- [x] Non-root container
- [x] Security scanning

## Monitoring

### Metrics to Watch
- Request rate
- Error rate
- Latency (p50, p95, p99)
- Database RU/s
- Service Bus throughput

### Alerts
- Error rate > 5%
- Latency p95 > 1s
- Database throttling
- Service Bus lag

## Next Steps

1. Add Azure Cognitive Search integration
2. Implement change feed processor for real-time updates
3. Add more domain validations
4. Implement soft delete
5. Add version history

## References

- [README.md](./README.md) - Full documentation
- [docs/lld.md](./docs/lld.md) - Low-level design
- Sequence diagrams in `sequence-diagrams/`

