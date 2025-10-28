# DRM Service - Low-Level Design

## Overview

The DRM Service is responsible for managing DRM (Digital Rights Management) policies for video content, integrating with Azure Media Services for content key management and license configuration.

## Architecture

### System Context

```
┌─────────────┐
│   APIM      │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│       DRM Service                   │
│  ┌───────────────────────────────┐ │
│  │    REST Controllers           │ │
│  └───────────┬───────────────────┘ │
│              │                      │
│  ┌───────────▼───────────────────┐ │
│  │    Application Layer          │ │
│  │    (Use Cases, Commands)      │ │
│  └───────────┬───────────────────┘ │
│              │                      │
│  ┌───────────▼───────────────────┐ │
│  │    Domain Layer               │ │
│  │    (Entities, Services)       │ │
│  └───────────┬───────────────────┘ │
│              │                      │
│  ┌───────────▼───────────────────┐ │
│  │    Infrastructure Layer       │ │
│  │    (Adapters, Repositories)   │ │
│  └───────────────────────────────┘ │
└─────────────────────────────────────┘
              │
    ┌─────────┼─────────┬──────────┬──────────┐
    ▼         ▼         ▼          ▼          ▼
 ┌────┐  ┌─────┐  ┌────────┐  ┌──────┐  ┌───────┐
 │PostgreSQL │Redis│ │Service Bus│ │AMS│ │Key Vault│
 └────┘  └─────┘  └────────┘  └──────┘  └───────┘
```

## Class Diagrams

### Domain Layer

```
┌─────────────────────────────────────┐
│         DrmPolicy                   │
│  (Aggregate Root)                   │
├─────────────────────────────────────┤
│ - id: String                        │
│ - videoId: String                   │
│ - provider: DrmProvider             │
│ - configuration: PolicyConfiguration│
│ - rotationPolicy: KeyRotationPolicy │
│ - createdAt: Instant                │
│ - updatedAt: Instant                │
│ - createdBy: String                 │
│ - updatedBy: String                 │
│ - version: Long                     │
└─────────────────────────────────────┘
            │
            │ contains
            ▼
┌─────────────────────────────────────┐
│      PolicyConfiguration            │
│      (Value Object)                 │
├─────────────────────────────────────┤
│ - contentKeyPolicyName: String      │
│ - licenseConfiguration: Map         │
│ - allowedApplications: List         │
│ - persistentLicenseAllowed: Boolean │
│ - ... (provider-specific fields)    │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│       KeyRotationPolicy             │
│      (Value Object)                 │
├─────────────────────────────────────┤
│ - enabled: Boolean                  │
│ - rotationInterval: Duration        │
│ - lastRotationAt: Instant           │
│ - nextRotationAt: Instant           │
│ - rotationKeyVaultUri: String       │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│          AuditLog                   │
│      (Value Object)                 │
├─────────────────────────────────────┤
│ - id: String                        │
│ - policyId: String                  │
│ - action: String                    │
│ - changedBy: String                 │
│ - changedAt: Instant                │
│ - oldValues: Map                    │
│ - newValues: Map                    │
│ - correlationId: String             │
└─────────────────────────────────────┘
```

### Application Layer

```
┌─────────────────────────────────────┐
│    DrmPolicyUseCase                 │
│    (Interface)                      │
├─────────────────────────────────────┤
│ + createPolicy(CreateDrmPolicyCmd)  │
│ + updatePolicy(UpdateDrmPolicyCmd)  │
│ + rotateKeys(RotateKeysCmd)         │
│ + getPolicy(GetDrmPolicyQuery)      │
│ + getPolicyByVideoId(Query)         │
└─────────────────────────────────────┘
            ▲
            │ implements
            │
┌───────────┴───────────────────────────┐
│   DrmPolicyUseCaseImpl                │
│   (Use Case Implementation)           │
├───────────────────────────────────────┤
│ - policyRepository: DrmPolicyRepo     │
│ - auditLogRepository: AuditLogRepo    │
│ - cacheService: CacheService          │
│ - eventPublisher: EventPublisher      │
│ - amsAdapter: AmsAdapter              │
│ - idempotencyRepository: Idempotency  │
└───────────────────────────────────────┘
```

### Infrastructure Layer

```
┌─────────────────────────────────────┐
│    DrmPolicyRepository              │
│    (Port)                           │
├─────────────────────────────────────┤
│ + findById(String)                  │
│ + findByVideoId(String)             │
│ + save(DrmPolicy)                   │
│ + delete(String)                    │
│ + existsByVideoId(String)           │
└─────────────────────────────────────┘
            ▲
            │ implements
            │
┌───────────┴───────────────────────────┐
│   DrmPolicyRepositoryImpl             │
│   (Adapter)                           │
├───────────────────────────────────────┤
│ - jpaRepository: DrmPolicyJpaRepo     │
│ - mapper: DrmPolicyMapper             │
└───────────────────────────────────────┘

┌─────────────────────────────────────┐
│       AmsAdapter                    │
│       (Port)                        │
├─────────────────────────────────────┤
│ + createOrUpdateContentKeyPolicy()  │
│ + rotateContentKey()                │
│ + deleteContentKeyPolicy()          │
└─────────────────────────────────────┘
            ▲
            │ implements
            │
┌───────────┴───────────────────────────┐
│   AmsAdapterImpl                      │
│   (Adapter)                           │
├───────────────────────────────────────┤
│ - amsEndpoint: String                 │
│ - tenantId: String                    │
└───────────────────────────────────────┘
```

## Sequence Diagrams

### Create DRM Policy

See `docs/sequences/policy-creation.md` for detailed sequence diagram.

### Key Rotation

See `docs/sequences/key-rotation.md` for detailed sequence diagram.

## Key Design Decisions

1. **Hexagonal Architecture**: Separation of concerns with clear ports and adapters
2. **CQRS Pattern**: Read and write models separated for optimal performance
3. **Idempotency**: Support for idempotency keys to prevent duplicate operations
4. **Optimistic Locking**: Version-based concurrency control for updates
5. **Cache-Aside**: Redis caching with automatic invalidation
6. **Scheduled Jobs**: Automatic key rotation based on policy configuration
7. **Audit Trail**: Complete audit logging for compliance and debugging
8. **Resilience4j**: Circuit breaker and retry patterns for external calls

## Technology Stack

- **Framework**: Spring Boot 3.3.x
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL (primary), Redis (cache)
- **Messaging**: Azure Service Bus
- **External Integrations**: Azure Media Services, Key Vault
- **Observability**: OpenTelemetry, Azure Monitor
- **Testing**: JUnit, Testcontainers, WireMock

## Deployment

- **Container**: Docker with distroless base image
- **Orchestration**: Kubernetes with Helm charts
- **Scaling**: Horizontal Pod Autoscaler (3-10 replicas)
- **Networking**: Network policies for security
- **High Availability**: Pod disruption budget

## Security

- OAuth2/JWT authentication via Spring Security
- Managed identity for Azure services
- Key Vault integration for secrets
- Non-root container execution
- Network policies for pod-to-pod communication

