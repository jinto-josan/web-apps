# Low-Level Design: Media Assist Service

## Overview

The Media Assist Service provides secure access to Azure Blob Storage assets through SAS URL generation, path validation, and audit logging.

## Architecture

### Hexagonal Architecture Layers

```
┌────────────────────────────────────────────────────┐
│              Interface Layer (REST)                │
│  - MediaController: REST endpoints                │
└────────────────────────────────────────────────────┘
                        ↓
┌────────────────────────────────────────────────────┐
│          Application Layer (Use Cases)             │
│  - MediaAccessUseCase: Business logic             │
│  - GenerateSignedUrlRequest/Response: DTOs        │
└────────────────────────────────────────────────────┘
                        ↓
┌────────────────────────────────────────────────────┐
│            Domain Layer (Core Business)            │
│  - BlobPath: Path normalization VO                │
│  - SignedUrl: Signed URL VO                       │
│  - SasPolicy: SAS policy configuration            │
│  - BlobStorageService: Storage port               │
│  - Repositories: Idempotency, Audit, Metadata     │
└────────────────────────────────────────────────────┘
                        ↓
┌────────────────────────────────────────────────────┐
│       Infrastructure Layer (Adapters)              │
│  - AzureBlobStorageService: Blob storage adapter  │
│  - RedisIdempotencyRepository: Redis adapter      │
│  - ServiceBusAuditLogRepository: Service Bus      │
│  - Configuration: Security, Redis, etc.          │
└────────────────────────────────────────────────────┘
```

## Key Components

### Domain Objects

#### BlobPath (Value Object)
- Normalizes blob paths
- Prevents directory traversal attacks
- Maps to Azure containers (renditions, thumbnails, etc.)

#### SignedUrl (Value Object)
- Encapsulates signed URL with expiry
- Type-safe (READ, WRITE, PLAYBACK)
- Immutable

#### SasPolicy (Value Object)
- Configures SAS permissions
- Enforces HTTPS
- Sets cache-control headers

### Use Cases

#### MediaAccessUseCase
1. **Generate SAS URL**
   - Validate idempotency key
   - Normalize and validate path
   - Check blob existence
   - Generate SAS signature
   - Cache result
   - Log audit event

### Infrastructure Adapters

#### AzureBlobStorageService
- Implements BlobStorageService port
- Resilient with Circuit Breaker + Retry
- Generates SAS signatures via Azure SDK
- Optimizes cache-control for playback

#### RedisIdempotencyRepository
- Stores idempotency results (TTL 24h)
- Prevents duplicate operations
- Key format: `idempotency:{key}`

#### ServiceBusAuditLogRepository
- Publishes audit events to Service Bus
- Event schema includes: userId, operation, resourcePath, status, timestamp

## Sequence Flows

### Generate Signed URL

```
Client → MediaController → MediaAccessUseCase → IdempotencyRepo
                                                    ↓ (cache miss)
                                                AzureBlobService → Azure Blob Storage
                                                    ↓
                                                AuditLogRepo → Service Bus
                                                    ↓
                                                Response → Client
```

### Path Validation

```java
BlobPath.fromString(path, container)
  → Normalize path (remove .., /, etc.)
  → Check for traversal attempts
  → Validate container name
  → Return BlobPath VO
```

## Security

### Path Traversal Protection
- All paths normalized via `Paths.normalize()`
- Reject paths containing `..`
- Reject absolute paths starting with `/`
- Whitelist allowed containers

### HTTPS Enforcement
- SAS policies enforce HTTPS in production
- Service-level configuration

### Audit Logging
- All access events logged
- Includes: user, resource, timestamp, IP, status
- Published to Service Bus for downstream processing

## Resilience Patterns

### Circuit Breaker
- `blob-storage-operation` circuit
- Config: 50% failure rate, 10 calls window
- Half-open state with 3 permitted calls

### Retry
- Exponential backoff
- Max 3 attempts
- Wait duration: 500ms → 1s → 2s

### Timeout
- Blob operations: 5s timeout
- Controlled via Resilience4j TimeLimiter

## Observability

### Metrics
- HTTP request metrics (p50, p90, p95, p99)
- Circuit breaker state changes
- Blob operation counts

### Tracing
- OpenTelemetry instrumentation
- Export to Azure Monitor
- Correlation IDs propagated

### Logging
- Structured logging (JSON)
- Levels: INFO (default), DEBUG (development)
- Security events logged separately

## Deployment

### Container Structure
- Multistage Docker build
- Distroless/alpine runtime
- Java 17 (Eclipse Temurin)

### Kubernetes
- Deployment: 3 replicas (HPA: 3-10)
- Resources: 512Mi-1Gi memory, 250m-1000m CPU
- Health checks: liveness + readiness
- Secrets: Azure Storage credentials

### Local Development
- Azurite (Blob Storage emulator)
- Redis (Docker)
- Local profile: `application-local.yml`

## Configuration

### Key Properties

```yaml
media-assist:
  service:
    sas:
      default-validity-duration: PT1H
      playback-validity-duration: PT4H
      max-validity-duration: PT24H
    idempotency:
      ttl: PT24H
    audit:
      enabled: true
```

### Environment Variables
- Azure Storage: account name, key, endpoint
- Redis: host, port, password
- OAuth2: issuer URI, JWK set URI
- Azure App Configuration, Key Vault endpoints

## Testing Strategy

### Unit Tests
- Value objects (immutability, validation)
- Use cases (business logic)
- Adapters (mocking external deps)

### Integration Tests
- Testcontainers: Azurite + Redis
- Real Azure SDK interactions
- End-to-end API tests

### Contract Tests
- WireMock for service dependencies
- Pact for consumer contracts (if applicable)

## Performance Considerations

### Caching
- Idempotency results cached (24h TTL)
- Blob metadata cached (1h TTL)
- CDN cache-control headers set

### Async Operations
- Audit logging async (non-blocking)
- Service Bus send async

### Connection Pooling
- Redis: Lettuce pooled connection
- Azure SDK: HTTP client reuse

