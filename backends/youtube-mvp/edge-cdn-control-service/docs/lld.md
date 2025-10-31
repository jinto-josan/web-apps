# Low-Level Design: Edge/CDN Control Service

## Overview

The Edge/CDN Control Service manages Azure Front Door and CDN configurations, including routing rules, WAF policies, origin failover, and cache purging operations. It follows Hexagonal Architecture with DDD-lite patterns.

## Architecture Layers

### 1. Domain Layer (`com.youtube.edgecdncontrol.domain`)

#### Core Entities

**CdnRule**
- Represents a CDN/Front Door rule configuration
- States: DRAFT → VALIDATED → APPLIED → (FAILED | ROLLED_BACK | DRIFT_DETECTED)
- Supports optimistic locking via version (ETag)
- Contains match conditions and actions

**PurgeRequest**
- Represents a cache purge operation
- States: PENDING → IN_PROGRESS → COMPLETED | FAILED

#### Value Objects

- **CdnRuleId**: Unique identifier for rules
- **FrontDoorProfileId**: Resource group and profile name
- **RuleStatus**: Rule lifecycle status
- **RuleType**: Type of rule (ROUTING_RULE, WAF_POLICY, etc.)
- **RuleMatchCondition**: Conditions for rule matching
- **RuleAction**: Actions to execute when rule matches
- **OriginConfig**: Origin configuration for failover

#### Domain Services

- **RuleValidationService**: Validates rule configurations
- **DriftDetectionService**: Detects configuration drift
- **AzureFrontDoorPort**: Port for Azure Front Door operations

### 2. Application Layer (`com.youtube.edgecdncontrol.application`)

#### Use Cases

- **CreateCdnRuleUseCase**: Creates new rules in DRAFT status
- **ApplyCdnRuleUseCase**: Applies validated rules to Azure
- **GetCdnRulesUseCase**: Retrieves rules with pagination
- **DetectDriftUseCase**: Detects configuration drift
- **PurgeCacheUseCase**: Executes cache purge operations

#### DTOs

- **CreateCdnRuleRequest**: Input for creating rules
- **CdnRuleResponse**: Rule output with ETag support
- **PurgeRequestDto**: Cache purge request
- **PurgeResponse**: Purge operation result
- **PageResponse**: Paginated results

#### Mappers

- **CdnRuleMapper**: MapStruct mapper for domain ↔ DTO conversion

### 3. Infrastructure Layer (`com.youtube.edgecdncontrol.infrastructure`)

#### Adapters

**Azure Adapters**
- **AzureFrontDoorAdapter**: Implements AzureFrontDoorPort
  - Uses Azure Resource Manager SDK
  - Applies rules to Azure Front Door
  - Purges cache
  - Retrieves current configuration

**Persistence Adapters**
- **CdnRuleRepositoryAdapter**: PostgreSQL JPA adapter
- **PurgeRequestRepositoryAdapter**: PostgreSQL JPA adapter
- Entity mappers for domain ↔ JPA conversion

**REST Controllers**
- **CdnRuleController**: Rule management endpoints
- **PurgeController**: Cache purge endpoints

#### Configuration

- **SecurityConfig**: OAuth2 Resource Server configuration
- **OpenApiConfig**: Swagger/OpenAPI configuration
- **AzureConfig**: Azure SDK client configuration
- **GlobalExceptionHandler**: ProblemDetails error handling

### 4. Interfaces Layer (`com.youtube.edgecdncontrol.interfaces`)

- REST controllers
- Exception handlers
- Filters (correlation ID, idempotency)

## Database Schema

### cdn_rules Table

```sql
CREATE TABLE cdn_rules (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    rule_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    resource_group VARCHAR(255) NOT NULL,
    profile_name VARCHAR(255) NOT NULL,
    priority INTEGER,
    match_conditions TEXT, -- JSON
    action TEXT, -- JSON
    metadata TEXT, -- JSON
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version VARCHAR(255), -- ETag
    rollback_from_rule_id VARCHAR(255),
    INDEX idx_resource_group_profile (resource_group, profile_name),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

### purge_requests Table

```sql
CREATE TABLE purge_requests (
    id UUID PRIMARY KEY,
    resource_group VARCHAR(255) NOT NULL,
    profile_name VARCHAR(255) NOT NULL,
    content_paths TEXT, -- JSON array
    purge_type VARCHAR(50) NOT NULL,
    requested_by VARCHAR(255),
    requested_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    INDEX idx_status (status),
    INDEX idx_requested_at (requested_at)
);
```

## Sequence Diagrams

See `docs/sequences/` for detailed sequence diagrams of key flows.

## Resilience Patterns

- **Circuit Breaker**: Prevents cascading failures to Azure APIs
- **Retry**: Exponential backoff for transient failures
- **Timeouts**: 30s timeout for Azure API calls
- **Rate Limiting**: 10 requests/second limit

## Observability

- **Distributed Tracing**: OpenTelemetry with Azure Monitor
- **Metrics**: Prometheus metrics
- **Logging**: Structured logging with correlation IDs
- **Health Checks**: Kubernetes liveness/readiness probes

