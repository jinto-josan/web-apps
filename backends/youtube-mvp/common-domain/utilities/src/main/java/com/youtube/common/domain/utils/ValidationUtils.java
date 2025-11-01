package com.youtube.common.domain.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for common validation operations.
 */
public final class ValidationUtils {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^https?://([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();

    private ValidationUtils() {
        // Utility class
    }

    /**
     * Validates an email address format.
     * 
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates an email address and throws exception if invalid.
     * 
     * @param email the email to validate
     * @throws IllegalArgumentException if email is invalid
     */
    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address: " + email);
        }
    }

    /**
     * Validates a URL format.
     * 
     * @param url the URL to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }

    /**
     * Validates a URL and throws exception if invalid.
     * 
     * @param url the URL to validate
     * @throws IllegalArgumentException if URL is invalid
     */
    public static void validateUrl(String url) {
        if (!isValidUrl(url)) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }

    /**
     * Validates that a string is not null or blank.
     * 
     * @param value the value to validate
     * @param fieldName the name of the field for error message
     * @throws IllegalArgumentException if value is null or blank
     */
    public static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
    }

    /**
     * Validates that an object is not null.
     * 
     * @param value the value to validate
     * @param fieldName the name of the field for error message
     * @throws IllegalArgumentException if value is null
     */
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Validates an object using Bean Validation annotations.
     * 
     * @param object the object to validate
     * @param <T> the type of the object
     * @return set of constraint violations (empty if valid)
     */
    public static <T> Set<ConstraintViolation<T>> validate(T object) {
        return validator.validate(object);
    }

    /**
     * Validates an object and throws exception if validation fails.
     * 
     * @param object the object to validate
     * @param <T> the type of the object
     * @throws IllegalArgumentException if validation fails
     */
    public static <T> void validateAndThrow(T object) {
        Set<ConstraintViolation<T>> violations = validate(object);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder("Validation failed: ");
            violations.forEach(v -> 
                message.append(v.getPropertyPath())
                    .append(": ")
                    .append(v.getMessage())
                    .append("; ")
            );
            throw new IllegalArgumentException(message.toString());
        }
    }

    /**
     * Checks if a string is null or blank.
     * 
     * @param value the value to check
     * @return true if null or blank, false otherwise
     */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Checks if a string is not null and not blank.
     * 
     * @param value the value to check
     * @return true if not null and not blank, false otherwise
     */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }
}

