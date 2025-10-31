# Low-Level Design: Configuration and Secrets Service

## Overview

The Configuration and Secrets Service implements a hexagonal architecture with domain, application, and infrastructure layers. It provides a unified interface for managing application configurations and secrets, with read-through caching to Azure App Configuration and Azure Key Vault.

## Architecture Layers

### 1. Domain Layer (`domain/`)

**Entities:**
- `ConfigurationEntry`: Core configuration entity with scope, key, value, metadata
- `SecretRotation`: Secret rotation record with status tracking
- `AuditLog`: Audit log entry for compliance and security

**Value Objects:**
- `ConfigScope`: Scope validation (tenant/environment)
- `ConfigKey`: Key validation and normalization

**Ports (Interfaces):**
- `ConfigurationRepository`: Local repository for configuration metadata
- `AppConfigurationPort`: Azure App Configuration operations
- `KeyVaultPort`: Azure Key Vault secret operations
- `CachePort`: Caching operations
- `EventPublisherPort`: Event publishing
- `AuditLoggerPort`: Audit logging
- `RbacCheckPort`: RBAC validation

### 2. Application Layer (`application/`)

**DTOs:**
- `ConfigRequest`: Configuration update request
- `ConfigResponse`: Configuration response with ETag
- `SecretRotationRequest`: Secret rotation request
- `SecretRotationResponse`: Secret rotation response

**Services:**
- `ConfigurationApplicationService`: Orchestrates configuration operations
  - Read-through caching
  - ETag handling
  - RBAC enforcement
  - Audit logging
- `SecretRotationApplicationService`: Orchestrates secret rotation
  - Dry-run support
  - Error handling
  - Event publishing

**Mappers:**
- `ConfigMapper`: MapStruct mapper for DTOs/entities

### 3. Infrastructure Layer (`infrastructure/`)

**Adapters:**
- `ConfigurationRepositoryAdapter`: JPA implementation
- `AppConfigurationAdapter`: Azure App Configuration SDK
- `KeyVaultAdapter`: Azure Key Vault SDK
- `RedisCacheAdapter`: Redis caching
- `ServiceBusEventPublisherAdapter`: Azure Service Bus messaging
- `JpaAuditLoggerAdapter`: JPA audit logging
- `JwtRbacCheckAdapter`: JWT-based RBAC

**Configuration:**
- `SecurityConfig`: Spring Security OAuth2 Resource Server
- `AzureConfig`: Azure service clients
- `RedisConfig`: Redis template configuration
- `OpenApiConfig`: Swagger/OpenAPI documentation

### 4. Interface Layer (`interfaces/rest/`)

**Controllers:**
- `ConfigController`: REST endpoints for configuration operations
  - `GET /api/v1/config/{scope}/{key}`: Get configuration
  - `PUT /api/v1/config/{scope}/{key}`: Update configuration
- `SecretController`: REST endpoints for secret operations
  - `POST /api/v1/secrets/{scope}/{key}/rotate`: Rotate secret

**Exception Handling:**
- `GlobalExceptionHandler`: RFC 7807 Problem Details

## Class Diagram

```
┌─────────────────────────────────────────────────────────┐
│                  ConfigController                        │
│                  SecretController                        │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│         ConfigurationApplicationService                  │
│         SecretRotationApplicationService                 │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼──────┐ ┌──▼──────┐ ┌──▼──────────────┐
│Configuration │ │AppConfig│ │   KeyVaultPort   │
│Repository    │ │Port     │ │                  │
└──────────────┘ └─────────┘ └──────────────────┘
        │            │            │
        └────────────┼────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│  Adapters (JPA, Azure SDK, Redis, Service Bus)          │
└─────────────────────────────────────────────────────────┘
```

## Data Flow

### Configuration Read Flow

1. Client requests configuration via REST API
2. RBAC check validates user permissions
3. Check Redis cache (cache hit → return immediately)
4. Cache miss → Read from App Configuration
5. Store in local PostgreSQL repository
6. Update cache
7. Return configuration with ETag

### Configuration Write Flow

1. Client sends PUT request with value and optional ETag
2. RBAC check validates write permissions
3. ETag validation (if provided)
4. Write to App Configuration or Key Vault (based on isSecret)
5. Save to local repository
6. Invalidate cache
7. Publish event to Service Bus
8. Audit log
9. Return updated configuration with new ETag

### Secret Rotation Flow

1. Client sends POST request to rotate endpoint
2. RBAC check validates rotation permissions
3. Create rotation record (status: SCHEDULED)
4. If not dry-run: Call Key Vault rotate API
5. Update rotation record (status: COMPLETED/FAILED)
6. Publish event to Service Bus
7. Audit log
8. Return rotation response

## Resilience Patterns

### Circuit Breaker

- Applied to: App Configuration and Key Vault calls
- Configuration: 10 requests sliding window, 50% failure threshold
- Fallback: Return cached value or error

### Retry

- Applied to: All Azure service calls
- Configuration: 3 attempts with exponential backoff
- Strategy: Retry on transient errors (5xx, timeouts)

### Timeout

- Applied to: All external calls
- Configuration: 3-5 seconds depending on service
- Behavior: Fail fast on slow downstream services

### Bulkhead

- Applied to: Thread pool isolation
- Configuration: 25 concurrent calls max
- Purpose: Prevent cascading failures

## Security

### Authentication

- OAuth2 Resource Server with JWT validation
- JWT issuer validation via JWK Set URI
- Token expiration and signature verification

### Authorization

- RBAC via JWT claims (scopes, roles)
- Tenant isolation (users can only access their tenant's configs)
- Permission hierarchy: `config.admin` > scope-specific > global

### Secrets Handling

- Secrets never stored in local database
- Secrets stored only in Azure Key Vault
- Local repository stores metadata only (with masked value)

## Caching Strategy

### Cache Key Format

```
config:{scope}:{key}
```

### Cache TTL

- Default: 5 minutes (300 seconds)
- Configurable via `app.config.cache-ttl-seconds`

### Cache Invalidation

- On write: Immediate eviction
- Pattern-based eviction for scope changes
- Manual eviction via cache management endpoint (future)

## Event Publishing

### Configuration Updated Event

```json
{
  "scope": "tenant1",
  "key": "app.setting",
  "etag": "etag123",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Secret Rotation Completed Event

```json
{
  "scope": "tenant1",
  "key": "db.password",
  "success": true,
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Database Schema

### configuration_entries

- Primary key: `id` (UUID)
- Unique constraint: `(scope, key)`
- Indexes: `(scope, key)`

### audit_logs

- Primary key: `id` (UUID)
- Indexes: `(scope, key)`, `(user_id)`, `(tenant_id)`, `(timestamp)`

## Performance Considerations

- **Read Performance**: Cache-first strategy reduces App Config calls
- **Write Performance**: Async event publishing, synchronous database writes
- **Connection Pooling**: HikariCP with 20 max connections
- **Batch Operations**: Future enhancement for bulk reads/writes

## Scalability

- **Horizontal Scaling**: Stateless service design
- **Database**: PostgreSQL read replicas (future)
- **Cache**: Redis cluster for high availability
- **Load Balancing**: Round-robin via Kubernetes service

## Observability

- **Metrics**: Prometheus metrics via Actuator
- **Tracing**: OpenTelemetry to Azure Monitor
- **Logging**: Structured JSON logs with correlation IDs
- **Health Checks**: Liveness and readiness probes

