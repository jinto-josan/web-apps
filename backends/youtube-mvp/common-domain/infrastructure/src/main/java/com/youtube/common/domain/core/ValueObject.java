package com.youtube.common.domain.core;

/**
 * Marker interface for value objects.
 * Value objects are immutable and defined by their attributes rather than identity.
 * 
 * <p>All value objects should:</p>
 * <ul>
 *   <li>Be immutable</li>
 *   <li>Implement equals() and hashCode() based on all attributes</li>
 *   <li>Have no identity (no unique ID)</li>
 * </ul>
 */
public interface ValueObject {
}

