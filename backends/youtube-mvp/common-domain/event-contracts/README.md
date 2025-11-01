# Event Contracts

This module contains shared domain events used for inter-service communication via event-driven architecture.

## Purpose

This module defines the contract (schema) for domain events that are published and consumed by multiple services. This ensures consistent event structure and type safety across the platform.

## Contents

### User Events

- **`UserCreatedEvent`**
  - Published by: identity-auth-service
  - Consumed by: user-profile-service, notifications-service, recommendations-service
  - Fields: userId, email, username

### Video Events

- **`VideoPublishedEvent`**
  - Published by: video-catalog-service
  - Consumed by: search-indexer-service, recommendations-service, notifications-service, analytics-telemetry-service
  - Fields: videoId, channelId, title, description

### Channel Events

- **`ChannelCreatedEvent`**
  - Published by: channel-service
  - Consumed by: user-profile-service, monetization-service, notifications-service
  - Fields: channelId, ownerId, channelName, description

## Usage

### Adding to Your Service

Add the dependency to your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.youtube.mvp</groupId>
    <artifactId>common-domain-event-contracts</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Publishing Events

```java
import com.youtube.common.domain.events.UserCreatedEvent;
import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.common.domain.event.EventPublisher;

@Service
public class UserService {
    
    @Autowired
    private EventPublisher eventPublisher;
    
    public void createUser(CreateUserCommand command) {
        // Create user...
        
        // Publish event
        UserCreatedEvent event = new UserCreatedEvent(
            UserId.from(command.getUserId()),
            command.getEmail(),
            command.getUsername()
        );
        
        eventPublisher.publishAll(List.of(event));
    }
}
```

### Consuming Events

```java
import com.youtube.common.domain.events.UserCreatedEvent;
import com.youtube.common.domain.event.EventRouter;

@Component
public class UserCreatedEventHandler implements EventRouter.EventHandler<UserCreatedEvent> {
    
    @Override
    @Transactional
    public void handle(UserCreatedEvent event, String correlationId) {
        // Process event
        userProfileService.createProfile(event.getUserId(), event.getEmail());
    }
}
```

## Best Practices

1. **Event Immutability** - All events are immutable once created
2. **Event Naming** - Use past tense (created, published, deleted)
3. **Event Versioning** - When breaking changes occur, create new event types
4. **Event Documentation** - Document who publishes and consumes each event
5. **Use Shared Value Objects** - Use UserId, VideoId, etc. from shared-models

## Adding New Events

Before adding a new event:
- It must be published/consumed by multiple services
- It represents a significant domain occurrence
- It uses shared value objects when possible
- It follows the same patterns as existing events

To add a new event:
1. Extend `DomainEvent` base class
2. Make all fields final
3. Implement `getEventType()` method
4. Add comprehensive JavaDoc documenting:
   - Who publishes it
   - Who consumes it
   - When it's published
5. Use shared value objects (UserId, VideoId, etc.) from shared-models
6. Update this README

## Event Schema Evolution

When you need to change an event:
1. **Non-breaking changes** (add optional fields): Update existing event
2. **Breaking changes** (remove/rename fields): Create new event version
   - Example: `UserCreatedEventV2`
   - Document migration path
   - Eventually deprecate old event

