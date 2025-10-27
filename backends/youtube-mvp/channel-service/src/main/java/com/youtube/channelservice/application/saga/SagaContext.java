package com.youtube.channelservice.application.saga;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context object that carries data between saga steps.
 * Provides a way to share state and results between steps.
 */
public class SagaContext {
    
    private final Map<String, Object> data = new ConcurrentHashMap<>();
    private final String sagaId;
    private final String sagaType;
    
    public SagaContext(String sagaId, String sagaType) {
        this.sagaId = sagaId;
        this.sagaType = sagaType;
    }
    
    /**
     * Stores a value in the context.
     * @param key The key to store the value under
     * @param value The value to store
     */
    public void put(String key, Object value) {
        data.put(key, value);
    }
    
    /**
     * Retrieves a value from the context.
     * @param key The key to retrieve
     * @param type The expected type of the value
     * @return The value cast to the expected type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isAssignableFrom(value.getClass())) {
            throw new ClassCastException("Value for key '" + key + "' is not of type " + type.getName());
        }
        return (T) value;
    }
    
    /**
     * Retrieves a value from the context with a default value.
     * @param key The key to retrieve
     * @param defaultValue The default value to return if key is not found
     * @return The value or default value
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        Object value = data.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * Checks if a key exists in the context.
     * @param key The key to check
     * @return true if the key exists, false otherwise
     */
    public boolean containsKey(String key) {
        return data.containsKey(key);
    }
    
    /**
     * Gets the saga ID.
     * @return The saga ID
     */
    public String getSagaId() {
        return sagaId;
    }
    
    /**
     * Gets the saga type.
     * @return The saga type
     */
    public String getSagaType() {
        return sagaType;
    }
    
    /**
     * Gets all data in the context.
     * @return A copy of all data
     */
    public Map<String, Object> getAllData() {
        return Map.copyOf(data);
    }
}
