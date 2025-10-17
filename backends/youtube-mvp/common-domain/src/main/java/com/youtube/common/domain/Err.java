package com.youtube.common.domain;

import java.util.Objects;

/**
 * Represents an error result containing an exception.
 * 
 * @param <T> the type of the value
 */
public class Err<T> implements Result<T> {
    private final DomainException error;

    public Err(DomainException error) {
        this.error = Objects.requireNonNull(error, "Error cannot be null");
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isError() {
        return true;
    }

    @Override
    public T getValue() {
        throw new IllegalStateException("Cannot get value from error result");
    }

    @Override
    public DomainException getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Err<?> err = (Err<?>) o;
        return Objects.equals(error, err.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error);
    }

    @Override
    public String toString() {
        return "Err{error=" + error + "}";
    }
}
