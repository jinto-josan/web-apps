package com.youtube.userprofileservice.application.saga;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Context for saga execution, storing state between steps.
 */
public class SagaContext {
    
    private final String sagaId;
    private final String sagaType;
    private final Map<String, Object> data;
    
    public SagaContext(String sagaId, String sagaType) {
        this.sagaId = Objects.requireNonNull(sagaId);
        this.sagaType = Objects.requireNonNull(sagaType);
        this.data = new HashMap<>();
    }
    
    public void put(String key, Object value) {
        data.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        return value != null ? (T) value : null;
    }
    
    public boolean contains(String key) {
        return data.containsKey(key);
    }
    
    public String getSagaId() {
        return sagaId;
    }
    
    public String getSagaType() {
        return sagaType;
    }
    
    public Map<String, Object> getAll() {
        return new HashMap<>(data);
    }
}

