package com.youtube.common.domain.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Utility class for date and time operations.
 */
public final class DateTimeUtils {
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final ZoneId UTC = ZoneId.of("UTC");

    private DateTimeUtils() {
        // Utility class
    }

    /**
     * Gets the current UTC instant.
     * 
     * @return current UTC instant
     */
    public static Instant now() {
        return Instant.now();
    }

    /**
     * Gets the current UTC zoned date time.
     * 
     * @return current UTC zoned date time
     */
    public static ZonedDateTime nowUtc() {
        return ZonedDateTime.now(UTC);
    }

    /**
     * Converts an instant to UTC zoned date time.
     * 
     * @param instant the instant to convert
     * @return UTC zoned date time
     */
    public static ZonedDateTime toUtc(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(UTC);
    }

    /**
     * Converts a zoned date time to UTC instant.
     * 
     * @param zonedDateTime the zoned date time to convert
     * @return UTC instant
     */
    public static Instant toInstant(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.toInstant();
    }

    /**
     * Formats an instant as ISO date time string.
     * 
     * @param instant the instant to format
     * @return ISO formatted string
     */
    public static String formatIso(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(UTC).format(ISO_FORMATTER);
    }

    /**
     * Parses an ISO date time string to instant.
     * 
     * @param isoString the ISO formatted string
     * @return instant, or null if input is null
     * @throws IllegalArgumentException if string is not in ISO format
     */
    public static Instant parseIso(String isoString) {
        if (isoString == null || isoString.isBlank()) {
            return null;
        }
        try {
            return Instant.from(ISO_FORMATTER.parse(isoString));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ISO date time format: " + isoString, e);
        }
    }

    /**
     * Checks if an instant is in the past.
     * 
     * @param instant the instant to check
     * @return true if in the past, false otherwise
     */
    public static boolean isPast(Instant instant) {
        if (instant == null) {
            return false;
        }
        return instant.isBefore(now());
    }

    /**
     * Checks if an instant is in the future.
     * 
     * @param instant the instant to check
     * @return true if in the future, false otherwise
     */
    public static boolean isFuture(Instant instant) {
        if (instant == null) {
            return false;
        }
        return instant.isAfter(now());
    }

    /**
     * Checks if an instant is between two instants (inclusive).
     * 
     * @param instant the instant to check
     * @param start the start instant
     * @param end the end instant
     * @return true if between start and end (inclusive), false otherwise
     */
    public static boolean isBetween(Instant instant, Instant start, Instant end) {
        if (instant == null || start == null || end == null) {
            return false;
        }
        return !instant.isBefore(start) && !instant.isAfter(end);
    }

    /**
     * Calculates the duration in seconds between two instants.
     * 
     * @param start the start instant
     * @param end the end instant
     * @return duration in seconds
     */
    public static long durationSeconds(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end instants cannot be null");
        }
        return java.time.Duration.between(start, end).getSeconds();
    }
}

