package com.youtube.common.domain;

import java.util.Objects;

/**
 * Represents a successful result containing a value.
 * 
 * @param <T> the type of the value
 */
public class Ok<T> implements Result<T> {
    private final T value;

    public Ok(T value) {
        this.value = value;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public DomainException getError() {
        throw new IllegalStateException("Cannot get error from successful result");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ok<?> ok = (Ok<?>) o;
        return Objects.equals(value, ok.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Ok{value=" + value + "}";
    }
}
