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

### 3. Outbox Dispatcher to Message Brokers

**Components:**
- **OutboxDispatcher** - Background worker that polls and dispatches outbox events
- **MessagePublisher** - Abstraction interface for publishing events to message brokers
- **ServiceBusMessagePublisher** - Azure Service Bus implementation of MessagePublisher
- **DomainEventPublisherConfig** - Spring configuration for message publisher beans
- **DomainEventPublisherProperties** - Configuration properties for broker settings

**Location:** 
- `com.youtube.common.domain.events.outbox.OutboxDispatcher`
- `com.youtube.common.domain.events.outbox.MessagePublisher`
- `com.youtube.common.domain.events.outbox.ServiceBusMessagePublisher`
- `com.youtube.common.domain.events.outbox.DomainEventPublisherConfig`
- `com.youtube.common.domain.events.outbox.DomainEventPublisherProperties`

**Architecture:**
The OutboxDispatcher uses the `MessagePublisher` interface abstraction, allowing it to work with different message brokers (Service Bus, Kafka, RabbitMQ, etc.) without being tightly coupled to a specific implementation. This makes it easy to switch between brokers or support multiple brokers in different environments.

**Current Implementations:**
- **Service Bus** (default) - `ServiceBusMessagePublisher` - Azure Service Bus topic/queue support
- **Kafka** (sample) - `KafkaMessagePublisher` - Sample implementation showing how to add Kafka support

**Configuration:**
```yaml
outbox:
  domain-event-publisher:
    enabled: true              # Enable/disable publisher
    interval: 5000             # Polling interval (ms)
    backend:
      type: servicebus         # Broker type: "servicebus" (default), "kafka", etc.
      # Service Bus specific:
      connection-string: ...    # Or use managed identity
      fully-qualified-namespace: ...
      topic-name: domain-events
      queue-name: ...           # Optional, for queues
      use-topic: true          # true for topics, false for queues
```

**Features:**
- Scheduled polling with configurable interval
- Batch processing with SELECT ... FOR UPDATE SKIP LOCKED
- Generic MessagePublisher abstraction (broker-agnostic)
- Azure Service Bus integration (default)
- Kafka sample implementation
- Distributed tracing support
- Error handling and retry logic

**Adding New Broker Implementations:**

To add support for a new message broker (e.g., RabbitMQ, SQS):

1. Implement the `MessagePublisher` interface:
```java
@Component
@ConditionalOnProperty(name = "outbox.domain-event-publisher.backend.type", havingValue = "rabbitmq")
public class RabbitMqMessagePublisher implements MessagePublisher {
    // Implementation
}
```

2. Configure properties and create broker-specific client beans
3. The OutboxDispatcher will automatically use your implementation when configured

**Kafka Sample:**
See `com.youtube.common.domain.events.outbox.KafkaMessagePublisher` for a complete commented example showing how to implement Kafka support.

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

### 5. HTTP Idempotency Filter

**Components:**
- **IdempotencyFilter** - Servlet filter for HTTP request idempotency
- **HttpIdempotencyRepository** - Unified repository interface
- **JpaHttpIdempotencyRepository** - JPA-based implementation (auto-configured)
- **RedisHttpIdempotencyRepository** - Redis-based implementation (auto-configured)
- **IdempotencyFilterAutoConfiguration** - Auto-configuration for filter

**Location:**
- `com.youtube.common.domain.web.IdempotencyFilter`
- `com.youtube.common.domain.persistence.idempotency.HttpIdempotencyRepository`
- `com.youtube.common.domain.persistence.idempotency.jpa.JpaHttpIdempotencyRepository`
- `com.youtube.common.domain.persistence.idempotency.redis.RedisHttpIdempotencyRepository`
- `com.youtube.common.domain.web.IdempotencyFilterAutoConfiguration`

**Features:**
- Auto-configured servlet filter for HTTP idempotency
- Supports both JPA (database) and Redis storage backends
- Request hash validation (method + URI + body)
- Response caching and replay
- Automatic detection of storage backend
- Works with Idempotency-Key header (RFC draft standard)

### 6. Correlation and Trace Context Propagation

**Components:**
- **CorrelationContext** - Thread-local correlation context
- **TraceProvider** - Distributed tracing integration
- **CorrelationFilter** - Auto-configured servlet filter for correlation/tracing
- **HttpClientConfig** - Auto-configuration for HTTP clients (RestTemplate/WebClient)
- **CorrelationExchangeFilterFunction** - WebClient filter for correlation propagation

**Location:**
- `com.youtube.common.domain.services.correlation.CorrelationContext`
- `com.youtube.common.domain.services.tracing.TraceProvider`
- `com.youtube.common.domain.web.CorrelationFilter`
- `com.youtube.common.domain.web.HttpClientConfig`
- `com.youtube.common.domain.web.CorrelationExchangeFilterFunction`
- `com.youtube.common.domain.web.CommonDomainWebAutoConfiguration`

**Features:**
- Auto-configured servlet filter extracts correlation/trace headers
- W3C traceparent header support
- Correlation ID propagation across services
- Causation ID tracking
- Thread-local context management
- Automatic RestTemplate interceptor configuration
- Automatic WebClient filter configuration
- Zero-configuration setup for services

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

### 9. Azure App Configuration PropertySource Integration

**Components:**
- **AzureAppConfigurationPropertySourceLocator** - EnvironmentPostProcessor that loads properties from Azure App Configuration
- **ConfigurationClient** - Azure App Configuration client bean (auto-configured)

**Location:**
- `com.youtube.common.domain.config.AzureAppConfigurationPropertySourceLocator`
- `com.youtube.common.domain.config.CommonDomainConfiguration`

**Features:**
- Early property loading via EnvironmentPostProcessor (runs before beans are created)
- Automatic property source registration in Spring Environment
- Properties available for injection via `@Value` or `@ConfigurationProperties`
- Key normalization (converts Azure App Config colon-separated keys to Spring dot-notation)
- Label filtering support (load specific labels or all settings)
- Multi-label support (comma-separated labels for environment-specific configurations)
- Sensitive value masking in logs
- Graceful error handling (doesn't fail startup if App Configuration is unavailable)
- High priority property source (can override application.yml values)

**Configuration:**
```yaml
azure:
  appconfig:
    enabled: true                              # Enable/disable property loading
    connection-string: ${AZURE_APP_CONFIG_CONNECTION_STRING:}  # Optional: connection string
    endpoint: ${AZURE_APP_CONFIG_ENDPOINT:}   # Optional: endpoint (uses managed identity)
    labels: ${AZURE_APP_CONFIG_LABELS:}       # Optional: comma-separated labels (e.g., "dev,common" or "prod")
```

**Label Support:**
Azure App Configuration supports labels to organize configuration settings by environment, region, or any other dimension. The PropertySourceLocator supports:

- **No label specified**: Loads all configuration settings regardless of label
- **Single label**: Loads only settings with the specified label
- **Multiple labels**: Loads settings from multiple labels, with later labels overriding earlier ones if keys conflict

**Label Format Support:**
The labels configuration supports multiple formats for flexibility:

1. **YAML List Format** (recommended for multiple labels):
```yaml
azure:
  appconfig:
    labels:
      - common
      - dev
```

2. **YAML Inline List**:
```yaml
azure:
  appconfig:
    labels: [common, dev]
```

3. **Comma-Separated String**:
```yaml
azure:
  appconfig:
    labels: "common,dev"
```

4. **Single Label**:
```yaml
azure:
  appconfig:
    labels: "dev"
```

**Label Examples:**
```yaml
# Load all settings (no label filter)
azure:
  appconfig:
    enabled: true
    endpoint: https://your-appconfig.azconfig.io
    # labels not specified - loads all

# Load only production settings (single label)
azure:
  appconfig:
    enabled: true
    endpoint: https://your-appconfig.azconfig.io
    labels: "prod"

# Load development settings, with common settings as fallback (YAML list format)
# Common settings are loaded first, then dev settings override if keys conflict
azure:
  appconfig:
    enabled: true
    endpoint: https://your-appconfig.azconfig.io
    labels:
      - common
      - dev

# Same as above, using comma-separated string
azure:
  appconfig:
    enabled: true
    endpoint: https://your-appconfig.azconfig.io
    labels: "common,dev"
```

**How Labels Are Read:**
The implementation reads labels from the Spring Environment and automatically detects the format:
- If configured as a YAML list (`labels: [dev, common]`), Spring Boot converts it to a `List` object
- If configured as a string (`labels: "dev,common"`), it's parsed as a comma-separated string
- The parser handles both formats seamlessly, so you can use whichever format is most convenient

**Usage:**
Properties loaded from Azure App Configuration are automatically available in the Spring Environment and can be used in:

1. **application.yml** - Reference properties from App Configuration:
```yaml
spring:
  datasource:
    url: ${database.url}  # Loaded from Azure App Configuration
    username: ${database.username}
```

2. **@Value annotation**:
```java
@Service
public class DatabaseService {
    @Value("${database.url}")
    private String databaseUrl;
}
```

3. **@ConfigurationProperties**:
```java
@ConfigurationProperties(prefix = "database")
public class DatabaseProperties {
    private String url;
    private String username;
    // getters/setters
}
```

**Property Key Normalization:**
Azure App Configuration uses colon-separated keys (e.g., `my-service:database:url`), which are automatically converted to Spring's dot-notation format (e.g., `my-service.database.url`) for compatibility.

**Registration:**
The PropertySourceLocator is automatically registered via `META-INF/spring.factories`:
```
org.springframework.boot.env.EnvironmentPostProcessor=com.youtube.common.domain.config.AzureAppConfigurationPropertySourceLocator
```


## Configuration

### Auto-Configuration

Common-domain uses Spring Boot auto-configuration for web components:

**Web Components Auto-Configured:**
- `CorrelationFilter` - Automatically registered as servlet filter (runs first)
- `IdempotencyFilter` - Automatically registered when repository bean exists
- `RestTemplate` - Auto-configured with correlation ID interceptor
- `WebClient.Builder` - Auto-configured with correlation ID filter function

**Repository Auto-Configuration:**
- `JpaHttpIdempotencyRepository` - Auto-configured when JPA adapter bean exists
- `RedisHttpIdempotencyRepository` - Auto-configured when Redis available and no JPA adapter

**Configuration Properties:**
- `outbox.domain-event-publisher.enabled` - Enable/disable publisher (default: true)
- `outbox.domain-event-publisher.interval` - Outbox polling interval in ms (default: 5000)
- `outbox.domain-event-publisher.backend.type` - Broker type: "servicebus" (default), "kafka", etc.
- `outbox.domain-event-publisher.backend.connection-string` - Service Bus connection string (optional)
- `outbox.domain-event-publisher.backend.fully-qualified-namespace` - Service Bus namespace for managed identity (optional)
- `outbox.domain-event-publisher.backend.topic-name` - Topic name for publishing events
- `outbox.domain-event-publisher.backend.queue-name` - Queue name (Service Bus only, optional)
- `outbox.domain-event-publisher.backend.use-topic` - Use topic (true) or queue (false) for Service Bus (default: true)
- `azure.appconfig.enabled` - Enable/disable Azure App Configuration property loading (default: false)
- `azure.appconfig.connection-string` - Azure App Configuration connection string (optional, alternative to endpoint)
- `azure.appconfig.endpoint` - Azure App Configuration endpoint URL (optional, uses managed identity if provided)
- `azure.appconfig.labels` - Comma-separated list of labels to filter configuration settings (optional, loads all if not specified)

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

### HTTP Idempotency (Auto-Configured Filter)

The idempotency filter is automatically configured and doesn't require manual handling in controllers.

**For JPA-based services:**

1. Extend HttpIdempotency entity:
```java
@Entity
@Table(name = "http_idempotency", schema = "auth")
public class HttpIdempotencyEntity extends HttpIdempotency {}
```

2. Create JPA repository:
```java
@Repository
public interface HttpIdempotencyJpaRepository 
    extends JpaRepository<HttpIdempotencyEntity, Long> {
    Optional<HttpIdempotencyEntity> findByIdempotencyKeyAndRequestHash(
        String key, byte[] hash);
}
```

3. Create adapter bean:
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
                    .map(entity -> (HttpIdempotency) entity);
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

**For Redis-based services:**
- Just include `spring-boot-starter-data-redis`
- Redis implementation auto-configures automatically
- No additional configuration needed

**Controller usage:**
```java
@RestController
public class PaymentController {
    
    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> createPayment(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody PaymentRequest request
    ) {
        // Filter automatically handles idempotency
        // Same request with same key returns cached response
        PaymentResponse response = paymentService.create(request);
        return ResponseEntity.status(201).body(response);
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

**For HTTP Idempotency (JPA):**
1. Extend `HttpIdempotency` with service-specific entity
2. Create JPA repository interface with `findByIdempotencyKeyAndRequestHash` method
3. Create adapter bean in configuration class
4. Filter automatically activates and handles idempotency

**For HTTP Idempotency (Redis):**
1. Include `spring-boot-starter-data-redis` dependency
2. Redis implementation auto-configures automatically
3. Filter automatically activates

**For Correlation/Tracing:**
1. No configuration needed - filters auto-configure
2. HTTP clients (RestTemplate/WebClient) automatically propagate correlation IDs
3. Ensure `TraceProvider` bean exists (auto-configured if Micrometer Tracing is present)

**For Azure App Configuration:**
1. Set `azure.appconfig.enabled=true` in application.yml or environment variable
2. Configure either `azure.appconfig.connection-string` or `azure.appconfig.endpoint`
3. Optionally specify `azure.appconfig.labels` to filter by label(s) (e.g., "dev", "prod", "dev,common")
4. Properties from Azure App Configuration are automatically loaded and available for injection
5. Use properties in application.yml with `${property.key}` syntax or inject via `@Value` or `@ConfigurationProperties`
6. When multiple labels are specified, later labels override earlier ones if keys conflict

**General Setup:**
1. Extend entity classes (OutboxEvent, InboxMessage, HttpIdempotency)
2. Implement repository interfaces
3. Register event handlers with EventRouter
4. Configure Azure resources (Service Bus)
5. Set up database schemas for outbox/inbox/idempotency tables
6. Import classes from new package structure:
   - Core DDD: `com.youtube.common.domain.core.*`
   - Events: `com.youtube.common.domain.events.*`
   - Services: `com.youtube.common.domain.services.*`
   - Web: `com.youtube.common.domain.web.*`
   - Persistence: `com.youtube.common.domain.persistence.*`
   - Utils: `com.youtube.common.domain.utils.*`

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
│   ├── idempotency/       # HTTP idempotency (legacy service)
│   └── tenant/            # Multi-tenancy
├── persistence/            # Persistence layer
│   ├── entity/            # JPA entity base classes
│   ├── repository/        # Repository implementations
│   └── idempotency/       # HTTP idempotency repositories
│       ├── jpa/           # JPA implementation
│       └── redis/         # Redis implementation
├── web/                    # Web components
│   ├── CorrelationFilter              # Correlation/tracing filter
│   ├── IdempotencyFilter              # HTTP idempotency filter
│   ├── HttpClientConfig               # HTTP client auto-config
│   ├── CorrelationExchangeFilterFunction  # WebClient filter
│   └── *AutoConfiguration            # Auto-configuration classes
├── utils/                  # Utility classes
│   └── Hashing            # Cryptographic hashing utilities
├── repository/             # Repository interfaces (ports)
└── config/                 # Configuration
    ├── CommonDomainConfiguration              # Main configuration class
    └── AzureAppConfigurationPropertySourceLocator  # App Config property loader
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
