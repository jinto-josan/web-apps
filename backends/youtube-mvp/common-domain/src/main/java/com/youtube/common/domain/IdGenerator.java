package com.youtube.common.domain;

/**
 * Interface for generating unique identifiers.
 * 
 * @param <ID> the type of identifier to generate
 */
public interface IdGenerator<ID extends Identifier> {
    
    /**
     * Generates a new unique identifier.
     * 
     * @return a new unique identifier
     */
    ID newId();
}
