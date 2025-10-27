# Low-Level Design (LLD) - Channel Service

## 📋 **Document Information**
- **Service**: YouTube Channel Service
- **Version**: 2.0
- **Architecture**: Clean Architecture with Command & Saga Patterns
- **Date**: 2024
- **Author**: System Architect

---

## 🎯 **Overview**

The Channel Service is a microservice responsible for managing YouTube channels, handles, branding, and member roles. It implements Clean Architecture principles with Command Pattern for operations and Saga Pattern for distributed transactions.

## 🏗️ **Architecture Overview**

### **Clean Architecture Layers**

```
┌─────────────────────────────────────────────────────────────┐
│                    INTERFACE LAYER                          │
│  ┌─────────────────┐  ┌─────────────────┐                 │
│  │ ChannelController│  │ CommandFactory   │                 │
│  │ (REST API)      │  │ (Command Creation)│                 │
│  └─────────────────┘  └─────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                        │
│  ┌─────────────────┐  ┌─────────────────┐                 │
│  │ Use Cases        │  │ Commands         │                 │
│  │ - ChannelUseCase │  │ - CreateChannel  │                 │
│  │ - ChannelUseCaseImpl│ - ChangeHandle   │                 │
│  └─────────────────┘  │ - UpdateBranding │                 │
│  ┌─────────────────┐  │ - SetMemberRole  │                 │
│  │ Command Handlers │  └─────────────────┘                 │
│  │ - ChannelCommandHandler│ ┌─────────────────┐             │
│  │ - ChannelCommandHandlerImpl│ Sagas         │             │
│  └─────────────────┘  │ - CreateChannelSaga│               │
│                       │ - ChangeHandleSaga│               │
│                       │ - UpdateBrandingSaga│              │
│                       │ - SetMemberRoleSaga│               │
│                       └─────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                            │
│  ┌─────────────────┐  ┌─────────────────┐                 │
│  │ Entities         │  │ Domain Events    │                 │
│  │ - Channel        │  │ - ChannelCreated │                 │
│  │ - Branding       │  │ - HandleChanged  │                 │
│  │ - Policy         │  │ - RoleChanged    │                 │
│  │ - Role           │  └─────────────────┘                 │
│  └─────────────────┘  ┌─────────────────┐                 │
│  ┌─────────────────┐  │ Repository      │                 │
│  │ Domain Services  │  │ Interfaces      │                 │
│  │ - EventPublisher │  │ - ChannelRepo  │                 │
│  │ - CacheService   │  │ - HandleRegistry│                 │
│  │ - BlobValidator  │  │ - MemberRepo    │                 │
│  │ - ReservedWords  │  └─────────────────┘                 │
│  └─────────────────┘                                      │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
┌─────────────────────────────────────────────────────────────┐
│                  INFRASTRUCTURE LAYER                      │
│  ┌─────────────────┐  ┌─────────────────┐                 │
│  │ JPA Entities    │  │ Repository      │                 │
│  │ - ChannelEntity │  │ Implementations │                 │
│  │ - HandleEntity  │  │ - ChannelRepoImpl│                │
│  │ - MemberEntity  │  │ - HandleRegistryImpl│             │
│  └─────────────────┘  │ - MemberRepoImpl │                 │
│  ┌─────────────────┐  └─────────────────┘                 │
│  │ Service         │  ┌─────────────────┐                 │
│  │ Implementations │  │ AOP Aspects     │                 │
│  │ - EventPublisherImpl│ - Logging       │                 │
│  │ - CacheServiceImpl│ - Metrics        │                 │
│  │ - BlobValidatorImpl│ - Validation    │                 │
│  │ - ReservedWordsImpl│ - Transaction   │                 │
│  └─────────────────┘  └─────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎨 **Design Patterns**

### **1. Clean Architecture Pattern**
- **Purpose**: Separation of concerns and dependency inversion
- **Implementation**: Four distinct layers with dependencies pointing inward
- **Benefits**: Testability, maintainability, framework independence

### **2. Command Pattern**
- **Purpose**: Encapsulate operations as objects
- **Implementation**: 
  - `Command` interface with `execute()` method
  - Concrete commands: `CreateChannelCommand`, `ChangeHandleCommand`, etc.
  - `ChannelCommandHandler` for command orchestration
- **Benefits**: Undo/redo support, queuing, logging, decoupling

### **3. Saga Pattern**
- **Purpose**: Manage distributed transactions with compensation
- **Implementation**:
  - `Saga` interface with `execute()` and compensation logic
  - `SagaStep` interface for individual operations
  - `SagaContext` for state management
  - Compensation methods for rollback
- **Benefits**: Eventual consistency, fault tolerance, distributed transactions

### **4. Repository Pattern**
- **Purpose**: Abstract data access layer
- **Implementation**:
  - Domain interfaces: `ChannelRepository`, `HandleRegistry`, `ChannelMemberRepository`
  - Infrastructure implementations: `ChannelRepositoryImpl`, `HandleRegistryImpl`
- **Benefits**: Testability, data access abstraction, easy implementation swapping

### **5. Factory Pattern**
- **Purpose**: Centralized object creation
- **Implementation**: `CommandFactory` for command instantiation
- **Benefits**: Consistent object creation, dependency injection, configuration management

### **6. Aspect-Oriented Programming (AOP)**
- **Purpose**: Cross-cutting concerns
- **Implementation**:
  - `LoggingAspect`: Method execution logging
  - `MetricsAspect`: Performance monitoring
  - `ValidationAspect`: Bean validation
  - `TransactionAspect`: Transaction management
- **Benefits**: Separation of concerns, code reuse, maintainability

---

## 🔄 **Saga Pattern Implementation**

### **Saga Structure**
```java
public interface Saga<T> {
    T execute() throws SagaExecutionException;
    String getSagaId();
    String getSagaType();
}
```

### **Saga Steps**
```java
public interface SagaStep {
    Object execute(SagaContext context) throws SagaStepException;
    void compensate(SagaContext context) throws SagaStepException;
    String getStepName();
}
```

### **Create Channel Saga Flow**
1. **Validate Handle Step**
   - Check handle format and availability
   - Validate against reserved words
   - Compensation: None (validation only)

2. **Reserve Handle Step**
   - Reserve handle in registry with TTL
   - Compensation: Release handle reservation

3. **Create Channel Step**
   - Persist channel entity
   - Compensation: Delete channel

4. **Commit Handle Step**
   - Commit handle reservation
   - Compensation: Release handle

5. **Publish Events Step**
   - Publish domain events
   - Update cache
   - Add owner as member
   - Compensation: Remove from cache, remove member

### **Saga Context**
```java
public class SagaContext {
    private final String sagaId;
    private final String sagaType;
    private final Map<String, Object> data = new HashMap<>();
    
    public <T> T get(String key, Class<T> type);
    public void put(String key, Object value);
}
```

---

## 📊 **Component Details**

### **Domain Layer Components**

#### **Entities**
- **Channel**: Core aggregate with validation constraints
- **Branding**: Value object for avatar, banner, accent color
- **Policy**: Value object for age gate and region blocks
- **Role**: Enum for member roles (OWNER, ADMIN, MODERATOR, MEMBER)

#### **Domain Events**
- **ChannelCreated**: Published when channel is created
- **ChannelHandleChanged**: Published when handle changes
- **ChannelMemberRoleChanged**: Published when member role changes

#### **Repository Interfaces**
- **ChannelRepository**: Channel CRUD operations
- **HandleRegistry**: Handle reservation and lookup
- **ChannelMemberRepository**: Member management operations

#### **Domain Services**
- **EventPublisher**: Event publishing contract
- **CacheService**: Caching operations contract
- **BlobUriValidator**: URI validation contract
- **ReservedWordsService**: Reserved words validation contract

### **Application Layer Components**

#### **Use Cases**
- **ChannelUseCase**: Application layer interface
- **ChannelUseCaseImpl**: Use case implementation

#### **Commands**
- **CreateChannelCommand**: Channel creation command
- **ChangeHandleCommand**: Handle change command
- **UpdateBrandingCommand**: Branding update command
- **SetMemberRoleCommand**: Member role command

#### **Sagas**
- **CreateChannelSaga**: Channel creation saga with 5 steps
- **ChangeHandleSaga**: Handle change saga with 6 steps
- **UpdateBrandingSaga**: Branding update saga with 4 steps
- **SetMemberRoleSaga**: Member role saga with 4 steps

#### **Command Handlers**
- **ChannelCommandHandler**: Command handler interface
- **ChannelCommandHandlerImpl**: Command handler implementation

### **Infrastructure Layer Components**

#### **JPA Entities**
- **ChannelEntity**: JPA entity for channels
- **HandleEntity**: JPA entity for handles
- **ChannelMemberEntity**: JPA entity for members
- **BrandingEmbeddable**: Embeddable for branding
- **PolicyEmbeddable**: Embeddable for policies

#### **Repository Implementations**
- **ChannelRepositoryImpl**: JPA implementation
- **HandleRegistryImpl**: JPA implementation with bucket partitioning
- **ChannelMemberRepositoryImpl**: JPA implementation

#### **Service Implementations**
- **EventPublisherImpl**: Event publishing implementation
- **CacheServiceImpl**: Redis cache implementation
- **BlobUriValidatorImpl**: URI validation implementation
- **ReservedWordsServiceImpl**: Reserved words implementation

#### **AOP Aspects**
- **LoggingAspect**: Method execution logging
- **MetricsAspect**: Performance metrics collection
- **ValidationAspect**: Bean validation
- **TransactionAspect**: Transaction management with retry logic

### **Interface Layer Components**

#### **REST Controller**
- **ChannelController**: REST API endpoints
  - `POST /channels` - Create channel
  - `GET /channels/{id}` - Get channel
  - `GET /channels/by-handle/{handle}` - Get by handle
  - `POST /channels/{id}/handle` - Change handle
  - `POST /channels/{id}/branding` - Update branding
  - `POST /channels/{id}/members` - Add member
  - `PATCH /channels/{id}/members/{userId}/role` - Set role

#### **Command Factory**
- **CommandFactory**: Centralized command creation

---

## 🔧 **Technical Implementation**

### **Database Schema**
```sql
-- Channels table
CREATE TABLE channels (
    id VARCHAR(26) PRIMARY KEY,
    owner_user_id VARCHAR(26) NOT NULL,
    handle_lower VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(100),
    description VARCHAR(5000),
    language VARCHAR(10),
    country VARCHAR(10),
    avatar_uri VARCHAR(500),
    banner_uri VARCHAR(500),
    accent_color VARCHAR(7),
    age_gate BOOLEAN DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    etag VARCHAR(255)
);

-- Handles table
CREATE TABLE handles (
    id VARCHAR(30) PRIMARY KEY,
    bucket INTEGER NOT NULL,
    channel_id VARCHAR(26),
    status VARCHAR(20) NOT NULL,
    reserved_by_user_id VARCHAR(26),
    reserved_at TIMESTAMP,
    committed_at TIMESTAMP,
    ttl_seconds INTEGER,
    version BIGINT
);

-- Channel members table
CREATE TABLE channel_members (
    id BIGSERIAL PRIMARY KEY,
    channel_id VARCHAR(26) NOT NULL,
    user_id VARCHAR(26) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT,
    UNIQUE(channel_id, user_id)
);
```

### **Configuration**
- **Spring Boot**: Application framework
- **Spring Data JPA**: Data access layer
- **Spring AOP**: Cross-cutting concerns
- **Redis**: Caching layer
- **Flyway**: Database migrations
- **MapStruct**: Object mapping
- **Lombok**: Boilerplate reduction

### **Dependencies**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

---

## 🔄 **Data Flow**

### **Channel Creation Flow**
1. **REST Request** → `ChannelController.create()`
2. **Command Creation** → `CommandFactory.createChannelCommand()`
3. **Use Case Execution** → `ChannelUseCaseImpl.createChannel()`
4. **Command Handler** → `ChannelCommandHandlerImpl.createChannel()`
5. **Saga Execution** → `CreateChannelSaga.execute()`
6. **Saga Steps**:
   - Validate handle
   - Reserve handle
   - Create channel
   - Commit handle
   - Publish events
7. **Repository Operations** → `ChannelRepositoryImpl.saveNew()`
8. **Event Publishing** → `EventPublisherImpl.publishChannelCreated()`
9. **Cache Update** → `CacheServiceImpl.putHandleMapping()`

### **Handle Change Flow**
1. **REST Request** → `ChannelController.changeHandle()`
2. **Command Creation** → `CommandFactory.changeHandleCommand()`
3. **Use Case Execution** → `ChannelUseCaseImpl.changeHandle()`
4. **Command Handler** → `ChannelCommandHandlerImpl.changeHandle()`
5. **Saga Execution** → `ChangeHandleSaga.execute()`
6. **Saga Steps**:
   - Validate new handle
   - Reserve new handle
   - Update channel
   - Commit new handle
   - Release old handle
   - Publish events
7. **Compensation Logic** → Rollback on failure

---

## 🛡️ **Error Handling**

### **Exception Hierarchy**
```
DomainException
├── ValidationException
├── NotFoundException
├── ConflictException
└── ForbiddenException

CommandExecutionException
SagaExecutionException
SagaStepException
```

### **Compensation Logic**
- **Handle Reservation**: Release reservation on failure
- **Channel Creation**: Delete channel on failure
- **Handle Commit**: Release handle on failure
- **Event Publishing**: Remove from cache on failure

---

## 📈 **Performance Considerations**

### **Caching Strategy**
- **Handle Mapping**: Redis cache with 6-hour TTL
- **Channel Data**: Optional caching for frequently accessed channels
- **Permissions**: Cache user permissions per channel

### **Database Optimization**
- **Indexes**: Handle, owner, creation date indexes
- **Bucket Partitioning**: Handle registry partitioned by hash
- **Connection Pooling**: HikariCP for database connections

### **AOP Monitoring**
- **Metrics Collection**: Command and saga execution times
- **Performance Logging**: Slow operation detection
- **Transaction Monitoring**: Database operation tracking

---

## 🔒 **Security Considerations**

### **Authentication**
- **JWT Token**: OAuth2 JWT validation
- **User Context**: Extracted from JWT claims

### **Authorization**
- **Role-Based Access**: Owner, Admin, Moderator, Member roles
- **Resource Ownership**: Channel owner validation
- **Handle Validation**: Reserved words and format validation

### **Data Validation**
- **Bean Validation**: JSR-303 annotations
- **Input Sanitization**: Handle format validation
- **URI Validation**: Blob URI origin validation

---

## 🧪 **Testing Strategy**

### **Unit Testing**
- **Domain Logic**: Pure business logic testing
- **Use Cases**: Application layer testing with mocks
- **Commands**: Command execution testing
- **Sagas**: Saga step testing with compensation

### **Integration Testing**
- **Repository**: Database integration testing
- **Service**: External service integration testing
- **Controller**: REST API integration testing

### **End-to-End Testing**
- **Complete Flows**: Full saga execution testing
- **Error Scenarios**: Compensation logic testing
- **Performance**: Load testing with metrics

---

## 📋 **Deployment Considerations**

### **Environment Configuration**
- **Database**: PostgreSQL with connection pooling
- **Cache**: Redis cluster for high availability
- **Monitoring**: Application metrics and health checks
- **Logging**: Structured logging with correlation IDs

### **Scalability**
- **Horizontal Scaling**: Stateless service design
- **Database Sharding**: Handle-based partitioning
- **Cache Distribution**: Redis cluster distribution
- **Load Balancing**: Round-robin load balancing

---

## 🔮 **Future Enhancements**

### **Planned Features**
- **GraphQL API**: Alternative to REST API
- **Event Sourcing**: Complete event history
- **CQRS**: Command Query Responsibility Segregation
- **Microservice Communication**: Service mesh integration

### **Performance Improvements**
- **Read Replicas**: Database read scaling
- **CDN Integration**: Static asset delivery
- **Message Queues**: Async event processing
- **Circuit Breakers**: Fault tolerance patterns

---

## 📚 **References**

- **Clean Architecture**: Robert C. Martin
- **Domain-Driven Design**: Eric Evans
- **Microservices Patterns**: Chris Richardson
- **Spring Framework**: Spring Boot Documentation
- **Saga Pattern**: Microservices.io Patterns

---

*This document represents the current implementation of the Channel Service following Clean Architecture principles with Command and Saga patterns for robust, maintainable, and scalable microservice design.*
