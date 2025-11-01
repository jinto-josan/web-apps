package com.youtube.common.domain.core;

import com.github.f4b6a3.ulid.UlidCreator;

/**
 * Interface for generating unique identifiers.
 * 
 * @param <T> the type of identifier to generate
 */
public interface IdGenerator<T extends Identifier<?>> {
    /**
     * Generates a new unique identifier.
     * 
     * @return a new unique identifier
     */
    T nextId();
    
    /**
     * ULID-based identifier generator.
     */
    class UlidIdGenerator implements IdGenerator<UlidIdentifier> {
        @Override
        public UlidIdentifier nextId() {
            return new UlidIdentifier(UlidCreator.getUlid().toString());
        }
    }
}

/**
 * ULID-based identifier implementation.
 */
class UlidIdentifier implements Identifier<String> {
    private final String value;
    
    UlidIdentifier(String value) {
        this.value = value;
    }
    
    @Override
    public String getValue() {
        return value;
    }
    
    @Override
    public String asString() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UlidIdentifier that = (UlidIdentifier) o;
        return value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

