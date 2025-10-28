package com.youtube.recommendationsservice.shared.constants;

public final class ValidationConstants {
    
    private ValidationConstants() {
        // Utility class
    }
    
    public static final int MIN_RECOMMENDATION_LIMIT = 1;
    public static final int MAX_RECOMMENDATION_LIMIT = 100;
    public static final int DEFAULT_RECOMMENDATION_LIMIT = 20;
    
    public static final int MAX_USER_ID_LENGTH = 255;
    public static final int MAX_VIDEO_ID_LENGTH = 255;
    
    public static final int CACHE_TTL_SECONDS = 3600; // 1 hour
    public static final int REDIS_TTL_HOURS = 24;
    
    public static final int MAX_CATEGORIES_PER_USER = 50;
    public static final int MAX_EMBEDDING_DIMENSION = 1024;
}

