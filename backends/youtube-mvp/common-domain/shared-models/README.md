# Common Domain Shared Models

This module contains shared domain models, value objects, and enums used across multiple microservices in the YouTube MVP platform.

## Purpose

This module provides a centralized location for shared domain concepts that are used by multiple services, ensuring consistency and preventing duplication.

## Contents

### Value Objects

- **`Money`** - Monetary amounts with currency support
  - Used by: monetization-service, billing-payout-service, ads-decision-service
  - Features: immutable, currency-aware operations, validation

- **`UserId`** - User identifier
  - Used by: user-profile-service, channel-service, engagement-service, recommendations-service
  - Type-safe identifier with validation

- **`VideoId`** - Video identifier
  - Used by: video-catalog-service, content-id-service, recommendations-service, streaming-session-service
  - Supports both UUID and string formats

- **`ChannelId`** - Channel identifier
  - Used by: channel-service, video-catalog-service, monetization-service, studio-analytics-service
  - Type-safe identifier with validation

### Enums

- **`VideoStatus`** - Video lifecycle status
  - Values: UPLOADING, PROCESSING, PUBLISHED, SCHEDULED, PRIVATE, UNLISTED, DELETED, TAKEN_DOWN
  - Used by: video-catalog-service, video-upload-service, video-transcode-service

- **`ContentType`** - Content classification
  - Values: VIDEO, SHORT, LIVE, LIVE_REPLAY, VOD, PODCAST, MUSIC, EDUCATIONAL, GAMING
  - Used by: video-catalog-service, recommendations-service, policy-enforcement-service

## Usage

### Adding to Your Service

Add the dependency to your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.youtube.mvp</groupId>
    <artifactId>common-domain-shared-models</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Using Value Objects

```java
import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.common.domain.shared.valueobjects.VideoId;
import com.youtube.common.domain.shared.valueobjects.Money;

// Create identifiers
UserId userId = UserId.from("user-123");
VideoId videoId = VideoId.from("video-456");

// Work with money
Money price = new Money(new BigDecimal("9.99"), "USD");
Money total = price.add(new Money(new BigDecimal("1.00"), "USD"));
```

### Using Enums

```java
import com.youtube.common.domain.shared.enums.VideoStatus;
import com.youtube.common.domain.shared.enums.ContentType;

VideoStatus status = VideoStatus.PUBLISHED;
if (status.isViewable()) {
    // Video can be viewed
}

ContentType type = ContentType.SHORT;
if (type.isShort()) {
    // Handle short-form content
}
```

## Best Practices

1. **Always use shared models** when they exist - don't create duplicates
2. **Extend, don't modify** - if you need additional behavior, extend these classes in your service
3. **Validation** - All value objects perform validation on construction
4. **Immutability** - All value objects are immutable for thread safety

## Adding New Shared Models

Before adding a new shared model, ensure:
- It's used by at least 2-3 services
- It represents a core domain concept
- It doesn't contain service-specific logic
- It follows the same patterns as existing shared models

To add a new shared model:
1. Create the class in the appropriate package (`valueobjects`, `enums`, etc.)
2. Add comprehensive JavaDoc with usage examples
3. Include validation
4. Make it immutable
5. Update this README

