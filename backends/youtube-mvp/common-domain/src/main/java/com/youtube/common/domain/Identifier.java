package com.youtube.common.domain;

/**
 * Marker interface for identifiers in the domain.
 * All domain identifiers should implement this interface.
 */
public interface Identifier {
    /**
     * Returns the string representation of this identifier.
     * 
     * @return the string value of the identifier
     */
    String asString();
}
