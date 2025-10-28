package com.youtube.userprofileservice.domain.services;

/**
 * Domain service interface for caching operations.
 * Abstracts caching to maintain clean architecture.
 */
public interface CacheService {
    
    /**
     * Caches a profile by account ID.
     * 
     * @param accountId the account ID
     * @param profileJson the JSON representation of the profile
     * @param ttlSeconds time to live in seconds
     */
    void cacheProfile(String accountId, String profileJson, long ttlSeconds);
    
    /**
     * Gets a cached profile by account ID.
     * 
     * @param accountId the account ID
     * @return the JSON representation of the profile, or null if not cached
     */
    String getCachedProfile(String accountId);
    
    /**
     * Invalidates a cached profile.
     * 
     * @param accountId the account ID
     */
    void invalidateProfile(String accountId);
    
    /**
     * Caches privacy settings by account ID.
     * 
     * @param accountId the account ID
     * @param privacyJson the JSON representation of privacy settings
     * @param ttlSeconds time to live in seconds
     */
    void cachePrivacySettings(String accountId, String privacyJson, long ttlSeconds);
    
    /**
     * Gets cached privacy settings by account ID.
     * 
     * @param accountId the account ID
     * @return the JSON representation of privacy settings, or null if not cached
     */
    String getCachedPrivacySettings(String accountId);
    
    /**
     * Invalidates cached privacy settings.
     * 
     * @param accountId the account ID
     */
    void invalidatePrivacySettings(String accountId);
    
    /**
     * Caches notification settings by account ID.
     * 
     * @param accountId the account ID
     * @param notificationJson the JSON representation of notification settings
     * @param ttlSeconds time to live in seconds
     */
    void cacheNotificationSettings(String accountId, String notificationJson, long ttlSeconds);
    
    /**
     * Gets cached notification settings by account ID.
     * 
     * @param accountId the account ID
     * @return the JSON representation of notification settings, or null if not cached
     */
    String getCachedNotificationSettings(String accountId);
    
    /**
     * Invalidates cached notification settings.
     * 
     * @param accountId the account ID
     */
    void invalidateNotificationSettings(String accountId);
}

