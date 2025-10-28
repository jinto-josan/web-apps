# Search Query Service - Architecture

## Overview

Production-grade microservice implementing hexagonal architecture with domain-driven design principles, providing full-text search and autocomplete capabilities for video content.

## Tech Stack

- **Runtime**: Java 17, Spring Boot 3.3.0
- **Build**: Maven, Lombok, MapStruct
- **Cloud**: Azure Cognitive Search, Cosmos DB, Service Bus
- **Security**: Spring Security OAuth2 Resource Server (OIDC)
- **Resilience**: Resilience4j (retry, circuit breaker, bulkhead, rate limiter)
- **Observability**: OpenTelemetry, Azure Monitor
- **Documentation**: Springdoc OpenAPI (Swagger)

## Architecture Pattern: Hexagonal Architecture

```
┌─────────────────────────────────────────────────────┐
│              Presentation Layer                      │
│    Controllers: SearchController                     │
│    Exception Handlers: GlobalExceptionHandler       │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│              Application Layer                       │
│    Application Services: SearchApplicationService    │
│    DTOs: SearchRequest, SearchResponse, etc.        │
│    Mappers: SearchMapper (MapStruct)                 │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│               Domain Layer                            │
│    Entities: SearchDocument                          │
│    Value Objects: SearchFilter, SearchResult         │
│    Domain Services: SearchService interface          │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│            Infrastructure Layer                      │
│    Adapters: AzureSearchAdapter                     │
│    Clients: AzureSearchClient                        │
│    Repositories: CosmosVideoRepository               │
│    Messaging: IndexUpdateConsumer                   │
└─────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### Domain Layer
- **Pure business logic** with no external dependencies
- Entities and value objects
- Domain service interfaces
- Business rules and validations

### Application Layer
- Orchestrates domain operations
- DTOs for external communication
- Mappers between layers
- Transaction management

### Infrastructure Layer
- External service adapters (Azure Search, Cosmos, Service Bus)
- Technical implementations
- Configuration and security
- Observability integration

### Presentation Layer
- REST API controllers
- Request/response handling
- Security enforcement
- Exception handling (RFC 7807)

## Key Features

### 1. Full-Text Search
- Multi-field search (title, description, tags)
- Filtering (category, language, duration, views, date range)
- Sorting (relevance, date, views)
- Pagination
- ETag caching support

### 2. Autocomplete
- Fuzzy matching
- Multi-field suggestions
- Configurable result count

### 3. Index Management
- Eventually consistent updates via Service Bus
- Event-driven index updates (PUBLISHED, UPDATED, DELETED)
- Admin-triggered full rebuild
- Batch indexing (100 docs/batch)

## Resilience Patterns

### Retry (Resilience4j)
- Max attempts: 3
- Exponential backoff
- Retries: TimeoutException, IOException

### Circuit Breaker
- Sliding window: 10
- Failure threshold: 50%
- Half-open calls: 3

### Rate Limiting
- Azure Search 429 handling
- Exponential backoff on retry

## Security

- OAuth2 Resource Server (OIDC with Entra External ID/B2C)
- JWT validation
- Role-based access (ADMIN for rebuild)
- Managed Identity for service-to-service

## Observability

- OpenTelemetry auto-instrumentation
- Correlation IDs in logs
- Azure Monitor integration
- Health checks (liveness, readiness)
- Prometheus metrics

## Deployment

### Local Development
```bash
make local-start  # Start emulators
make run-local    # Run service
```

### Docker
```bash
make docker-build
make docker-run
```

### Kubernetes (Helm)
```bash
helm install search-query-service ./charts/search-query-service
```

## Testing

- **Unit Tests**: Domain, application, infrastructure
- **Slice Tests**: Controllers, services
- **Integration Tests**: With Testcontainers (Cosmos emulator)
- **Mocking**: WireMock for Azure Search API
- **Coverage**: JaCoCo reporting

## Performance

- Azure Cognitive Search (managed search engine)
- Read replicas for scale
- Connection pooling
- Async processing for index updates
- Caching with ETags
