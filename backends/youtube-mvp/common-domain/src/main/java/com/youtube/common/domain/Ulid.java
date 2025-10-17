package com.youtube.common.domain;

import java.util.Objects;

/**
 * ULID (Universally Unique Lexicographically Sortable Identifier) implementation.
 * Provides time-ordered unique identifiers that are URL-safe.
 */
public class Ulid implements Identifier {
    private final String value;

    public Ulid(String value) {
        this.value = Objects.requireNonNull(value, "ULID value cannot be null");
        if (!isValidUlid(value)) {
            throw new IllegalArgumentException("Invalid ULID format: " + value);
        }
    }

    public static Ulid generate() {
        return new Ulid(generateUlid());
    }

    public static Ulid fromString(String value) {
        return new Ulid(value);
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ulid ulid = (Ulid) o;
        return Objects.equals(value, ulid.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

    private static boolean isValidUlid(String value) {
        return value != null && value.length() == 26 && value.matches("[0-9A-HJKMNP-TV-Z]{26}");
    }

    private static String generateUlid() {
        // Simplified ULID generation - in a real implementation, you'd use a proper ULID library
        long timestamp = System.currentTimeMillis();
        String random = Long.toHexString(System.nanoTime()).toUpperCase();
        return String.format("%010X%s", timestamp, random.substring(0, 16));
    }
}
