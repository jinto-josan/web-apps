# Common-Domain Implementation Summary

This document summarizes the implementation of all sequence diagrams in the common-domain module.

## Implementation Status

All sequence diagrams have been fully implemented in the `common-domain-infrastructure` module.

## Implemented Components

### 1. Core Domain Building Blocks

- **Identifier<T>** - Interface for domain identifiers
- **ValueObject** - Marker interface for value objects
- **Entity<ID>** - Base class for domain entities
- **AggregateRoot<ID>** - Base class for aggregate roots with event sourcing
- **DomainEvent** - Base class for domain events
- **Clock** - Time abstraction interface
- **IdGenerator** - ULID-based identifier generation
- **UnitOfWork** - Transaction management
- **ConcurrencyException** - Exception for version conflicts

**Location:** `com.youtube.common.domain.core`

### 2. Command Handling with Transactional Outbox

**Components:**
- **CorrelationContext** - Manages correlation IDs across requests
- **TraceProvider** - Wraps Micrometer Tracing for distributed tracing
- **UnitOfWork** - Manages transaction boundaries
- **EventPublisher** - Publishes events to outbox within transactions
- **OutboxRepository** - Interface and JPA implementation for outbox storage
- **IdGenerator** - ULID-based identifier generation
- **Clock** - Time abstraction interface

**Location:** 
- `com.youtube.common.domain.services.correlation.CorrelationContext`
- `com.youtube.common.domain.services.tracing.TraceProvider`
- `com.youtube.common.domain.core.UnitOfWork`
- `com.youtube.common.domain.events.EventPublisher`
- `com.youtube.common.domain.events.outbox.OutboxRepository`
- `com.youtube.common.domain.events.outbox.JpaOutboxRepository`
- `com.youtube.common.domain.core.IdGenerator`
- `com.youtube.common.domain.core.Clock`

### 3. Outbox Dispatcher to Azure Service Bus

**Components:**
- **OutboxDispatcher** - Background worker that polls and dispatches outbox events

**Location:** `com.youtube.common.domain.events.outbox.OutboxDispatcher`

**Features:**
- Scheduled polling with configurable interval
- Batch processing with SELECT ... FOR UPDATE SKIP LOCKED
- Azure Service Bus integration
- Distributed tracing support
- Error handling and retry logic

### 4. Event Consumption with Inbox Idempotency

**Components:**
- **EventProcessor** - Processes events from Azure Service Bus
- **InboxRepository** - Interface and JPA implementation for inbox storage
- **EventRouter** - Routes events to appropriate handlers

**Location:**
- `com.youtube.common.domain.events.EventProcessor`
- `com.youtube.common.domain.events.inbox.InboxRepository`
- `com.youtube.common.domain.events.inbox.JpaInboxRepository`
- `com.youtube.common.domain.events.EventRouter`

**Features:**
- Idempotent event processing using inbox pattern
- Transaction management
- Handler routing
- Correlation context propagation

### 5. HTTP Idempotency (Redis lock + SQL store)

**Components:**
- **IdempotencyService** - Handles HTTP request idempotency

**Location:** `com.youtube.common.domain.services.idempotency.IdempotencyService`

**Features:**
- Redis-based distributed locking
- SQL-based result storage
- Request hash validation
- Response caching

### 6. Correlation and Trace Context Propagation

**Components:**
- **CorrelationContext** - Thread-local correlation context
- **TraceProvider** - Distributed tracing integration

**Location:**
- `com.youtube.common.domain.services.correlation.CorrelationContext`
- `com.youtube.common.domain.services.tracing.TraceProvider`

**Features:**
- W3C traceparent header support
- Correlation ID propagation
- Causation ID tracking
- Thread-local context management

### 7. Optimistic Concurrency Control

**Components:**
- **Repository** - Base repository interface with version support
- **JpaRepositoryBase** - JPA implementation with optimistic locking
- **ConcurrencyException** - Exception for version conflicts

**Location:**
- `com.youtube.common.domain.repository.Repository`
- `com.youtube.common.domain.persistence.repository.JpaRepositoryBase`
- `com.youtube.common.domain.core.ConcurrencyException`

**Features:**
- Version-based optimistic locking
- Automatic version increment
- Conflict detection and handling

### 8. Tenant Resolution (Multi-tenant Request Context)

**Components:**
- **TenantResolver** - Resolves tenant from JWT claims and host
- **RequestContext** - Thread-local request context
- **TokenValidator** - Interface for JWT token validation

**Location:**
- `com.youtube.common.domain.services.tenant.TenantResolver`
- `com.youtube.common.domain.services.tenant.RequestContext`

**Features:**
- JWT token validation
- Tenant resolution from claims or host
- Thread-local context storage
- Multi-tenant support


## Configuration

All services are configured via Spring Boot configuration in `CommonDomainConfiguration`.

**Configuration Properties:**
- `azure.servicebus.connection-string` - Service Bus connection string
- `azure.servicebus.topic-name` - Topic name (default: domain-events)
- `outbox.dispatcher.interval` - Outbox polling interval in ms (default: 5000)

## Usage Examples

### Command Handling with Outbox

```java
import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.common.domain.services.tracing.TraceProvider;
import com.youtube.common.domain.core.AggregateRoot;
import com.youtube.common.domain.events.EventPublisher;
import com.youtube.common.domain.repository.Repository;

@Transactional
public void handleCommand(CreateUserCommand command) {
    // Set correlation context
    CorrelationContext.setCorrelationId(command.getCorrelationId());
    
    // Start tracing
    Span span = traceProvider.startSpan("createUser");
    
    try {
        // Load or create aggregate
        UserAggregate user = repository.findById(command.getUserId())
            .orElseGet(() -> UserAggregate.create(command));
        
        // Process command
        user.createUser(command);
        
        // Save aggregate
        repository.save(user);
        
        // Publish events (stored in outbox)
        eventPublisher.publishAll(user.pendingDomainEvents());
        
        // Mark events as committed
        user.markEventsCommitted();
    } finally {
        traceProvider.endSpan(span);
        CorrelationContext.clear();
    }
}
```

### Event Processing with Inbox

```java
import com.youtube.common.domain.events.EventRouter;
import com.youtube.common.domain.services.correlation.CorrelationContext;

@Service
public class UserEventHandler implements EventRouter.EventHandler<UserCreatedEvent> {
    
    @Override
    @Transactional
    public void handle(UserCreatedEvent event, String correlationId) {
        // Set correlation context
        CorrelationContext.setCorrelationId(correlationId);
        
        // Process event
        // ... business logic ...
    }
}
```

### HTTP Idempotency

```java
import com.youtube.common.domain.services.idempotency.IdempotencyService;

@RestController
public class PaymentController {
    
    @PostMapping("/payments")
    public ResponseEntity<?> createPayment(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody PaymentRequest request
    ) {
        // Compute request hash
        byte[] requestHash = IdempotencyService.computeRequestHash(
            "POST", "/payments", request.getBytes()
        );
        
        // Check for cached result
        Optional<IdempotencyService.StoredResponse> cached = idempotencyService.getStoredResult(
            idempotencyKey, requestHash
        );
        
        if (cached.isPresent()) {
            return ResponseEntity.status(cached.get().status())
                .body(cached.get().body());
        }
        
        // Acquire lock
        if (!idempotencyService.acquireLock(idempotencyKey)) {
            return ResponseEntity.status(409).build(); // Conflict
        }
        
        try {
            // Process request
            PaymentResponse response = paymentService.create(request);
            
            // Store result
            idempotencyService.storeResult(
                idempotencyKey,
                requestHash,
                201,
                serializeResponse(response)
            );
            
            return ResponseEntity.status(201).body(response);
        } finally {
            idempotencyService.releaseLock(idempotencyKey);
        }
    }
}
```

### Tenant Resolution

```java
import com.youtube.common.domain.services.tenant.TenantResolver;
import com.youtube.common.domain.services.tenant.RequestContext;

@RestController
public class UserController {
    
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(HttpServletRequest request) {
        // Resolve tenant
        String tenantId = tenantResolver.resolve(
            request.getHeader("Authorization"),
            request.getServerName(),
            extractHeaders(request)
        );
        
        // Set in request context
        RequestContext.setTenantId(tenantId);
        
        // Use in service
        List<User> users = userService.findByTenant(tenantId);
        return ResponseEntity.ok(users);
    }
}
```


## Dependencies

All required dependencies are configured in `pom.xml`:
- Spring Boot (Data JPA, Redis, etc.)
- Azure SDK (Service Bus, Identity)
- Micrometer Tracing
- Jackson for JSON serialization
- ULID Creator for ID generation

## Testing

Components are designed to be testable with:
- Mock implementations for repositories
- Test doubles for external services
- In-memory databases for integration tests
- Embedded Redis for testing

## Next Steps

Services should:
1. Extend entity classes (OutboxEvent, InboxMessage, HttpIdempotency)
2. Implement repository interfaces
3. Register event handlers with EventRouter
4. Configure Azure resources (Service Bus)
5. Set up database schemas for outbox/inbox/idempotency tables
6. Import classes from new package structure:
   - Core DDD: `com.youtube.common.domain.core.*`
   - Events: `com.youtube.common.domain.events.*`
   - Services: `com.youtube.common.domain.services.*`
   - Repository: `com.youtube.common.domain.repository.*`
   - Persistence: `com.youtube.common.domain.persistence.*`

## Package Structure

The infrastructure module follows a clean package organization:

```
com.youtube.common.domain/
├── core/                    # Core DDD building blocks
├── events/                  # Event-driven infrastructure
│   ├── outbox/             # Transactional outbox pattern
│   └── inbox/              # Inbox pattern for idempotency
├── services/                # Infrastructure services
│   ├── correlation/        # Request correlation
│   ├── tracing/            # Distributed tracing
│   ├── idempotency/       # HTTP idempotency
│   └── tenant/            # Multi-tenancy
├── persistence/            # Persistence layer
│   ├── entity/            # JPA entity base classes
│   └── repository/        # Repository implementations
├── repository/             # Repository interfaces (ports)
└── config/                 # Configuration
```

## Error Handling

Common error handling is provided by the `error` module:

**Components:**
- **DomainException** - Base exception for domain errors
- **ValidationException** - Validation failures with field-level errors
- **NotFoundException** - Resource not found errors
- **ConflictException** - Conflict errors
- **UnauthorizedException** - Authentication required
- **ForbiddenException** - Authorization failed
- **ErrorCodes** - Common error code constants
- **ProblemDetailBuilder** - RFC 7807 Problem Details builder
- **GlobalExceptionHandler** - Base exception handler for REST controllers
- **Result<T>** - Functional result type for error handling

**Location:** `com.youtube.common.domain.error`

## Notes

- Event deserialization in EventProcessor is simplified - services should implement proper type resolution
- TokenValidator interface must be implemented by each service
- TenantRepository interface must be implemented by each service
- All repository interfaces have JPA implementations that can be extended
- Feature flags are handled by a separate service, not in common-domain
- Use common error handling from `error` module for consistent error responses
