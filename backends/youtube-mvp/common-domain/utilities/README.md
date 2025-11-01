# Common Domain Utilities

This module contains common utility classes and helper methods used across multiple microservices.

## Purpose

This module provides reusable utility functions that don't belong to any specific domain but are commonly needed across services.

## Contents

### ValidationUtils

Utility class for common validation operations:

- **`isValidEmail(String email)`** - Validates email format
- **`validateEmail(String email)`** - Validates and throws exception if invalid
- **`isValidUrl(String url)`** - Validates URL format
- **`validateUrl(String url)`** - Validates and throws exception if invalid
- **`validateNotBlank(String value, String fieldName)`** - Validates non-blank strings
- **`validateNotNull(Object value, String fieldName)`** - Validates non-null objects
- **`validate(T object)`** - Bean Validation integration
- **`validateAndThrow(T object)`** - Validates and throws if violations exist

### DateTimeUtils

Utility class for date and time operations (all UTC-based):

- **`now()`** - Current UTC instant
- **`nowUtc()`** - Current UTC zoned date time
- **`toUtc(Instant)`** - Converts instant to UTC zoned date time
- **`formatIso(Instant)`** - Formats instant as ISO string
- **`parseIso(String)`** - Parses ISO string to instant
- **`isPast(Instant)`** - Checks if instant is in the past
- **`isFuture(Instant)`** - Checks if instant is in the future
- **`isBetween(Instant, Instant, Instant)`** - Checks if instant is within range
- **`durationSeconds(Instant, Instant)`** - Calculates duration in seconds

## Usage

### Adding to Your Service

Add the dependency to your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.youtube.mvp</groupId>
    <artifactId>common-domain-utilities</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Using ValidationUtils

```java
import com.youtube.common.domain.utils.ValidationUtils;

// Validate email
ValidationUtils.validateEmail(user.getEmail());

// Validate URL
if (ValidationUtils.isValidUrl(url)) {
    // Process URL
}

// Validate object with Bean Validation
ValidationUtils.validateAndThrow(command);
```

### Using DateTimeUtils

```java
import com.youtube.common.domain.utils.DateTimeUtils;
import java.time.Instant;

// Get current time
Instant now = DateTimeUtils.now();

// Format as ISO
String isoString = DateTimeUtils.formatIso(now);

// Parse ISO string
Instant parsed = DateTimeUtils.parseIso("2024-01-01T00:00:00Z");

// Check if in range
if (DateTimeUtils.isBetween(eventTime, startTime, endTime)) {
    // Event is in range
}
```

## Best Practices

1. **Use for common operations** - Only use utilities for truly common operations
2. **Service-specific logic** - Keep service-specific validation in your service
3. **UTC everywhere** - DateTimeUtils uses UTC consistently
4. **Validation early** - Use validation utilities at service boundaries

## Adding New Utilities

Before adding a new utility:
- It must be used by at least 2-3 services
- It doesn't contain service-specific logic
- It's a pure function (no side effects)
- It follows the same patterns as existing utilities

To add a new utility:
1. Create the class in the `utils` package
2. Make all methods static
3. Add comprehensive JavaDoc
4. Include null-safety checks
5. Update this README

