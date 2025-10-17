package com.youtube.common.domain;

/**
 * Implementation of IdGenerator that generates ULIDs.
 */
public class UlidGenerator implements IdGenerator<Ulid> {
    
    @Override
    public Ulid newId() {
        return Ulid.generate();
    }
}
