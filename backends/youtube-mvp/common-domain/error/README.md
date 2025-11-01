# Common Domain Error Handling

This module provides shared error handling infrastructure for consistent error handling across all microservices.

## Overview

The error module provides:
- Common domain exceptions
- RFC 7807 Problem Details support
- Global exception handler base class
- Error code constants
- Functional result type for error handling

## Components

### Domain Exceptions

- **`DomainException`** - Base exception for all domain errors
- **`ValidationException`** - Validation failures with field-level errors
- **`NotFoundException`** - Resource not found errors
- **`ConflictException`** - Conflict errors (concurrency, business rules)
- **`UnauthorizedException`** - Authentication required
- **`ForbiddenException`** - Authorization failed

### Utilities

- **`ErrorCodes`** - Common error code constants
- **`ProblemDetailBuilder`** - Fluent API for building RFC 7807 Problem Details
- **`GlobalExceptionHandler`** - Base exception handler with common handlers
- **`Result<T>`** - Functional result type for operations that can fail

## Usage

### Adding to Your Service

Add the dependency to your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.youtube.mvp</groupId>
    <artifactId>error</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Using Domain Exceptions

```java
import com.youtube.common.domain.error.DomainException;
import com.youtube.common.domain.error.NotFoundException;
import com.youtube.common.domain.error.ValidationException;

// Throw domain exceptions in your business logic
public User findUser(UserId userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User", userId.asString()));
}

// Validation with field errors
public void validateUser(CreateUserCommand command) {
    Map<String, List<String>> errors = new HashMap<>();
    
    if (command.getEmail() == null || !isValidEmail(command.getEmail())) {
        errors.put("email", List.of("Valid email is required"));
    }
    
    if (!errors.isEmpty()) {
        throw new ValidationException("Validation failed", errors);
    }
}
```

### Using Global Exception Handler

Extend the base handler and add service-specific handlers:

```java
import com.youtube.common.domain.error.GlobalExceptionHandler;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MyServiceExceptionHandler extends GlobalExceptionHandler {
    
    @ExceptionHandler(MyServiceSpecificException.class)
    public ProblemDetail handleMyException(MyServiceSpecificException ex) {
        return ProblemDetailBuilder.fromDomainException(
            new DomainException("MY_SERVICE_ERROR", ex.getMessage()),
            HttpStatus.BAD_REQUEST
        );
    }
}
```

### Using ProblemDetailBuilder

```java
import com.youtube.common.domain.error.ProblemDetailBuilder;

// Create validation error
ProblemDetail pd = ProblemDetailBuilder.validationError(
    "Validation failed",
    Map.of("email", List.of("Invalid email format"))
);

// Create not found error
ProblemDetail pd = ProblemDetailBuilder.notFound("User", "user-123");

// Create conflict error
ProblemDetail pd = ProblemDetailBuilder.conflict("Resource already exists");

// Custom problem detail
ProblemDetail pd = ProblemDetailBuilder.custom(
    HttpStatus.BAD_REQUEST,
    "Custom Error",
    "Detailed error message",
    "CUSTOM_ERROR_CODE",
    Map.of("field", "value")
);
```

### Using Result Type

```java
import com.youtube.common.domain.error.Result;

// Return Result instead of throwing exceptions
public Result<User> findUser(UserId userId) {
    return userRepository.findById(userId)
        .map(Result::success)
        .orElse(Result.failure(ErrorCodes.RESOURCE_NOT_FOUND, 
            "User not found: " + userId.asString()));
}

// Chaining operations
Result<String> email = findUser(userId)
    .map(User::getEmail)
    .map(String::toLowerCase);

// Handling results
findUser(userId)
    .ifSuccess(user -> {
        // Handle success
    })
    .ifFailure(error -> {
        // Handle error
    });

// Flat mapping
Result<Profile> profile = findUser(userId)
    .flatMap(user -> findProfile(user.getProfileId()));
```

## Error Codes

Use constants from `ErrorCodes` for consistency:

```java
import com.youtube.common.domain.error.ErrorCodes;

throw new DomainException(ErrorCodes.VALIDATION_FAILED, "Invalid input");
throw new DomainException(ErrorCodes.RESOURCE_NOT_FOUND, "Resource not found");
```

## Best Practices

1. **Use domain exceptions** for expected business errors
2. **Use Result type** for operations that commonly fail (optional pattern)
3. **Extend GlobalExceptionHandler** rather than creating from scratch
4. **Use ErrorCodes constants** instead of magic strings
5. **Include validation errors** in ValidationException for better UX
6. **Use ProblemDetailBuilder** for consistent error structure

