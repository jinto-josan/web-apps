package com.youtube.common.domain.core;

/**
 * Interface for domain identifiers.
 * All identifiers in the domain should implement this interface.
 * 
 * @param <T> the type of the identifier value
 */
public interface Identifier<T> {
    /**
     * Returns the identifier value.
     * 
     * @return the identifier value
     */
    T getValue();
    
    /**
     * Returns the string representation of the identifier.
     * 
     * @return the identifier as a string
     */
    String asString();
}

