package com.youtube.common.domain.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Examples demonstrating error handling patterns.
 * This file serves as documentation - remove before production.
 */
@SuppressWarnings("unused")
public class ErrorHandlingExamples {
    
    // Example 1: Using DomainException
    public void exampleDomainException() {
        // Example: throw new DomainException(ErrorCodes.VALIDATION_FAILED, "Invalid input");
        // Example: throw new NotFoundException("User", "user-123");
        // Example: throw new ConflictException("Resource already exists");
    }

    // Example 2: Using ValidationException with field errors
    public void exampleValidationException() {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put("email", List.of("Email is required", "Email format is invalid"));
        errors.put("password", List.of("Password must be at least 8 characters"));

        throw new ValidationException("Validation failed", errors);
    }

    // Example 3: Using Result type
    public Result<String> exampleResultType(String input) {
        if (input == null || input.isBlank()) {
            return Result.failure(ErrorCodes.VALIDATION_FAILED, "Input cannot be empty");
        }
        return Result.success(input.toUpperCase());
    }

    // Example 4: Chaining Result operations
    public Result<String> exampleResultChaining(String userId) {
        return findUser(userId)
            .flatMap(user -> findProfile(user.getProfileId()))
            .map(profile -> profile.getEmail());
    }

    // Example 5: Using ProblemDetailBuilder in controllers
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        // User user = userRepository.findById(id); // Replace with actual repository call
        
        // if (user == null) {
        //     ProblemDetail problem = ProblemDetailBuilder.notFound("User", id);
        //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
        // }
        
        // return ResponseEntity.ok(user);
        
        // Example implementation:
        // User user = userRepository.findById(id)
        //     .orElseThrow(() -> new NotFoundException("User", id));
        // return ResponseEntity.ok(user);
        return ResponseEntity.ok().build();
    }

    // Example 6: Extending GlobalExceptionHandler
    @RestControllerAdvice
    public static class MyServiceExceptionHandler extends GlobalExceptionHandler {

        @ExceptionHandler(MyCustomException.class)
        public ProblemDetail handleMyCustomException(MyCustomException ex) {
            return ProblemDetailBuilder.fromDomainException(
                new DomainException("MY_CUSTOM_ERROR", ex.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        }
    }

    // Helper methods for examples
    private Result<User> findUser(String userId) {
        // Implementation
        return Result.success(new User());
    }

    private Result<Profile> findProfile(String profileId) {
        // Implementation
        return Result.success(new Profile());
    }

    // Placeholder classes
    static class User {
        String getProfileId() { return "profile-1"; }
    }

    static class Profile {
        String getEmail() { return "user@example.com"; }
    }

    static class MyCustomException extends RuntimeException {
        MyCustomException(String message) { super(message); }
    }
}

