package com.youtube.channelservice.domain.services;

import java.util.Set;

/**
 * Domain service interface for checking reserved words.
 * Defines the contract for validating against reserved handles.
 */
public interface ReservedWordsService {
    
    /**
     * Checks if a handle is reserved.
     * @param handleLower The handle in lowercase to check
     * @return true if the handle is reserved, false otherwise
     */
    boolean isReserved(String handleLower);
    
    /**
     * Gets all reserved handles.
     * @return A copy of the set of reserved handles
     */
    Set<String> getReservedHandles();
}
