# Common-Domain - Shared DDD Building Blocks

This module provides shared Domain-Driven Design (DDD) building blocks and patterns used across all microservices in the YouTube MVP platform. It implements enterprise-grade patterns for reliability, scalability, and maintainability.

## Overview

The common-domain module contains reusable components that implement core DDD patterns and enterprise integration patterns. These building blocks ensure consistency across all microservices while providing robust solutions for distributed systems challenges.

## Architecture Patterns

### Core Domain Building Blocks

- **`Entity<ID>`** - Base class for domain entities with identity
- **`AggregateRoot<ID>`** - Base class for aggregate roots with event sourcing
- **`ValueObject`** - Marker interface for immutable value objects
- **`Identifier`** - Interface for domain identifiers
- **`DomainEvent`** - Base class for domain events

### Event-Driven Architecture

- **Event Publishing** - Reliable event publishing with outbox pattern
- **Event Consumption** - Idempotent event processing with inbox pattern
- **Event Serialization** - Schema-aware event serialization
- **Correlation Tracking** - Distributed tracing and correlation

### Reliability Patterns

- **Transactional Outbox** - Ensures reliable event publishing
- **Inbox Pattern** - Prevents duplicate event processing
- **Optimistic Concurrency** - Handles concurrent modifications
- **HTTP Idempotency** - Auto-configured filter prevents duplicate API calls
- **Request Correlation** - Auto-configured filter tracks requests across services

## Sequence Diagrams

The following sequence diagrams illustrate the key patterns and flows implemented in the common-domain:

### 1. Command Handling with Transactional Outbox

![Command Handling with Transactional Outbox](sequence-diagrams/Common-Domain%20-%20Command%20Handling%20with%20Transactional%20Outbox.png)

**Purpose**: Demonstrates how commands are processed with reliable event publishing using the transactional outbox pattern.

**Key Components**:
- Command Handler/Use Case
- Unit of Work for transaction management
- Repository for aggregate persistence
- Outbox Repository for event storage
- Correlation and tracing context

**Flow**:
1. Client sends command to API Controller
2. Controller starts tracing span and resolves correlation ID
3. Command Handler begins transaction
4. Repository loads or creates aggregate
5. Aggregate processes command and generates domain events
6. Events are stored in outbox within the same transaction
7. Transaction commits ensuring consistency

### 2. Outbox Dispatcher to Azure Service Bus

![Outbox Dispatcher to Azure Service Bus](sequence-diagrams/Common-Domain%20-%20Outbox%20Dispatcher%20to%20Azure%20Service%20Bus.png)

**Purpose**: Shows how outbox messages are reliably dispatched to Azure Service Bus.

**Key Components**:
- Outbox Dispatcher (background worker)
- Outbox Repository
- Azure Service Bus Topic
- Tracing for observability

**Flow**:
1. Dispatcher polls for pending outbox messages
2. Uses `SELECT ... FOR UPDATE SKIP LOCKED` for concurrent processing
3. Publishes each event to Azure Service Bus
4. Updates outbox status with broker message ID
5. Handles failures and retries

### 3. Event Consumption with Inbox Idempotency

![Event Consumption with Inbox Idempotency](sequence-diagrams/Common-Domain%20-%20Event%20Consumption%20with%20Inbox%20Idempotency.png)

**Purpose**: Illustrates idempotent event processing using the inbox pattern.

**Key Components**:
- Event Processor (background worker)
- Inbox Repository for idempotency
- Event Router for handler resolution
- Unit of Work for transaction management

**Flow**:
1. Azure Service Bus delivers message to processor
2. Processor checks inbox for duplicate processing
3. If new, begins transaction and resolves event handler
4. Handler processes event and updates aggregates
5. New domain events are stored in outbox
6. Transaction commits and inbox is marked as processed

### 4. HTTP Idempotency Filter

![HTTP Idempotency](sequence-diagrams/Common-Domain%20-%20HTTP%20Idempotency%20(Redis%20lock%20+%20SQL%20store).png)

**Purpose**: Demonstrates HTTP request idempotency using auto-configured servlet filter.

**Key Components**:
- IdempotencyFilter - Auto-configured servlet filter
- HttpIdempotencyRepository - Unified repository interface
- JpaHttpIdempotencyRepository / RedisHttpIdempotencyRepository - Storage implementations
- Azure SQL Database / Azure Cache for Redis - Storage backends

**Flow**:
1. Client sends request with Idempotency-Key header
2. Filter extracts header and computes request hash (method + URI + body)
3. Filter checks repository for cached response
4. If cached, returns stored response immediately
5. If not cached, processes request and stores response
6. Subsequent requests with same key return cached response

### 5. Correlation and Trace Context Propagation

![Correlation and Trace Context Propagation](sequence-diagrams/Common-Domain%20-%20Correlation%20and%20Trace%20Context%20Propagation.png)

**Purpose**: Shows how correlation IDs and trace context propagate through distributed systems.

**Key Components**:
- CorrelationFilter - Auto-configured servlet filter for incoming requests
- HttpClientConfig - Auto-configuration for outbound HTTP clients
- CorrelationExchangeFilterFunction - WebClient filter for correlation propagation
- TraceProvider - Distributed tracing integration
- CorrelationContext - Thread-local correlation context
- Outbox Repository
- Azure Service Bus

**Flow**:
1. HTTP request arrives with traceparent and correlation headers
2. CorrelationFilter extracts headers and sets thread-local context
3. TraceProvider starts/continues trace span
4. Outbound HTTP requests (via RestTemplate/WebClient) automatically include correlation headers
5. Events are published with correlation metadata
6. Consumer receives events and continues trace
7. Correlation context is maintained across services automatically

### 6. Optimistic Concurrency Control

![Optimistic Concurrency Control](sequence-diagrams/Common-Domain%20-%20Optimistic%20Concurrency%20Control.png)

**Purpose**: Illustrates optimistic concurrency control for aggregate updates.

**Key Components**:
- Command Handler
- Repository
- Azure SQL Database

**Flow**:
1. Handler loads aggregate with current version
2. Aggregate is modified
3. Repository attempts to save with expected version
4. Database checks version match in WHERE clause
5. Success if version matches, conflict if not

### 7. Tenant Resolution (Multi-tenant Request Context)

![Tenant Resolution](sequence-diagrams/Common-Domain%20-%20Tenant%20Resolution%20(Multi-tenant%20Request%20Context).png)

**Purpose**: Shows how tenant context is resolved and propagated in multi-tenant systems.

**Key Components**:
- API Controller
- Token Validator
- Tenant Resolver
- Tenant Repository

**Flow**:
1. Client sends request with Authorization header
2. JWT token is validated and claims extracted
3. Tenant is resolved from claims and host
4. Request context is set with tenant ID
5. Use case executes with tenant context

### 8. Feature Flags via Azure App Configuration

![Feature Flags](sequence-diagrams/Common-Domain%20-%20Feature%20Flags%20via%20Azure%20App%20Configuration.png)

**Purpose**: Demonstrates feature flag evaluation using Azure App Configuration.

**Key Components**:
- Use Case
- Feature Flag Service
- In-memory Cache
- Azure App Configuration
- Azure Key Vault

**Flow**:
1. Use case requests feature flag evaluation
2. Service checks in-memory cache
3. If cache miss, fetches from Azure App Configuration
4. Resolves Key Vault references if needed
5. Caches result and returns decision

## Key Features

### Reliability & Consistency
- **Transactional Outbox** - Ensures events are published exactly once
- **Inbox Pattern** - Prevents duplicate event processing
- **Optimistic Concurrency** - Handles concurrent modifications safely
- **HTTP Idempotency** - Prevents duplicate API operations

### Observability & Tracing
- **Distributed Tracing** - End-to-end request tracing (auto-configured)
- **Correlation IDs** - Automatic request correlation across services (auto-configured)
- **HTTP Client Propagation** - Automatic correlation/trace header propagation (auto-configured)
- **Event Metadata** - Rich context for event processing
- **Structured Logging** - Consistent logging patterns

### Scalability & Performance
- **Event-Driven Architecture** - Loose coupling and scalability
- **Caching Strategies** - Redis and in-memory caching
- **Batch Processing** - Efficient outbox message processing
- **Connection Pooling** - Optimized database connections

### Security & Multi-tenancy
- **Tenant Isolation** - Secure multi-tenant data access
- **JWT Validation** - Secure token-based authentication
- **Feature Flags** - Safe feature rollouts
- **Rate Limiting** - Protection against abuse

## Usage

### Adding Common-Domain to Your Service

```xml
<dependency>
    <groupId>com.youtube</groupId>
    <artifactId>common-domain</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Aggregate Implementation

```java
public class UserAggregate extends AggregateRoot<UserId> {
    
    public UserAggregate(UserId id) {
        super(id);
    }
    
    public void createUser(CreateUserCommand command) {
        // Business logic
        record(new UserCreatedEvent(id, command.getEmail()));
    }
    
    @Override
    protected void apply(DomainEvent event) {
        // Event sourcing logic
    }
}
```

### Command Handler Implementation

```java
@Component
public class CreateUserHandler {
    
    @Transactional
    public Result<User> handle(CreateUserCommand command) {
        try {
            var user = UserAggregate.create(command);
            repository.save(user);
            eventPublisher.publishAll(user.pendingDomainEvents());
            return Result.ok(user);
        } catch (Exception e) {
            return Result.err(new DomainException("USER_CREATION_FAILED", e.getMessage()));
        }
    }
}
```

### HTTP Idempotency (Auto-Configured)

For JPA-based services, create an adapter configuration:

```java
@Configuration
public class IdempotencyConfig {
    @Bean
    public JpaHttpIdempotencyRepository.JpaIdempotencyRepositoryAdapter 
            httpIdempotencyAdapter(HttpIdempotencyJpaRepository jpaRepository) {
        return new JpaHttpIdempotencyRepository.JpaIdempotencyRepositoryAdapter() {
            @Override
            public Optional<HttpIdempotency> find(String key, byte[] hash) {
                return jpaRepository.findByIdempotencyKeyAndRequestHash(key, hash)
                    .map(e -> (HttpIdempotency) e);
            }
            
            @Override
            public HttpIdempotency save(HttpIdempotency entity) {
                return jpaRepository.save((HttpIdempotencyEntity) entity);
            }
            
            @Override
            public HttpIdempotency create() {
                return new HttpIdempotencyEntity();
            }
        };
    }
}
```

For Redis-based services, no configuration needed - just include `spring-boot-starter-data-redis`.

### HTTP Client Usage (Auto-Configured)

RestTemplate and WebClient automatically propagate correlation IDs:

```java
@Service
public class ExternalServiceClient {
    
    private final RestTemplate restTemplate; // Auto-configured with correlation interceptor
    private final WebClient webClient; // Built from auto-configured builder
    
    public void callExternalService() {
        // Correlation ID and traceparent automatically added to headers
        restTemplate.postForEntity("https://api.example.com/data", request, String.class);
        
        // WebClient also automatically includes correlation headers
        webClient.post()
            .uri("https://api.example.com/data")
            .bodyValue(request)
            .retrieve()
            .toEntity(String.class)
            .block();
    }
}
```

## Configuration

### Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://your-server.database.windows.net:1433;database=your-db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### Redis Configuration

```yaml
spring:
  redis:
    host: your-redis.redis.cache.windows.net
    port: 6380
    ssl: true
    password: ${REDIS_PASSWORD}
```

### Azure Service Bus Configuration

```yaml
azure:
  servicebus:
    connection-string: ${SERVICEBUS_CONNECTION_STRING}
    topic-name: domain-events
```

## Testing

### Unit Testing

```java
@Test
public void shouldCreateUserSuccessfully() {
    // Given
    var command = new CreateUserCommand("user@example.com");
    
    // When
    var result = handler.handle(command);
    
    // Then
    assertThat(result.isSuccess()).isTrue();
    verify(repository).save(any(UserAggregate.class));
    verify(eventPublisher).publishAll(anyList());
}
```

### Integration Testing

```java
@SpringBootTest
@Testcontainers
class UserIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
    
    @Test
    void shouldProcessUserCreationEndToEnd() {
        // Integration test implementation
    }
}
```

## Monitoring & Observability

### Metrics

- **Event Processing Latency** - Time to process domain events
- **Outbox Message Count** - Number of pending outbox messages
- **Concurrency Conflicts** - Rate of optimistic concurrency failures
- **Idempotency Hit Rate** - Percentage of duplicate requests (filter statistics)
- **Correlation ID Propagation** - Success rate of correlation header propagation
- **Trace Span Duration** - Distributed trace span durations across services

### Alerts

- **Outbox Backlog** - Alert when outbox messages accumulate
- **Event Processing Failures** - Alert on event processing errors
- **Database Connection Issues** - Alert on connection pool exhaustion
- **Redis Connectivity** - Alert on cache connectivity issues
- **Correlation Filter Errors** - Alert on correlation/trace context setup failures
- **Idempotency Filter Errors** - Alert on idempotency processing failures

## Contributing

1. Follow DDD principles and patterns
2. Ensure all new components are properly tested
3. Update documentation and sequence diagrams
4. Follow the established coding standards
5. Add appropriate logging and monitoring

## License

This project is licensed under the MIT License - see the LICENSE file for details.
