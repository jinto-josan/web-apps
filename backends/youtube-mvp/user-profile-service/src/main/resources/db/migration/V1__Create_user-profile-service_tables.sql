-- User Profile Service Database Schema
-- Migration: V1__Create_user-profile-service_tables.sql

-- Create schema
CREATE SCHEMA IF NOT EXISTS user_profile;

-- Create account_profiles table
CREATE TABLE IF NOT EXISTS user_profile.account_profiles (
    id BIGSERIAL PRIMARY KEY,
    account_id VARCHAR(26) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    photo_url VARCHAR(500),
    locale VARCHAR(10),
    country VARCHAR(5),
    timezone VARCHAR(100),
    
    -- Privacy settings
    privacy_subscriptions_private BOOLEAN DEFAULT FALSE,
    privacy_saved_playlists_private BOOLEAN DEFAULT FALSE,
    privacy_restricted_mode_enabled BOOLEAN DEFAULT FALSE,
    privacy_watch_history_private BOOLEAN DEFAULT FALSE,
    privacy_like_history_private BOOLEAN DEFAULT FALSE,
    
    -- Notification settings
    notification_email_opt_in BOOLEAN DEFAULT TRUE,
    notification_push_opt_in BOOLEAN DEFAULT TRUE,
    notification_marketing_opt_in BOOLEAN DEFAULT FALSE,
    notification_channel_preferences JSONB,
    notification_email_preferences JSONB,
    notification_push_preferences JSONB,
    
    -- Accessibility preferences
    accessibility_captions_always_on BOOLEAN DEFAULT FALSE,
    accessibility_captions_language VARCHAR(10),
    accessibility_autoplay_default BOOLEAN DEFAULT FALSE,
    accessibility_autoplay_on_home BOOLEAN DEFAULT FALSE,
    accessibility_captions_font_size VARCHAR(20),
    accessibility_captions_background_opacity INTEGER DEFAULT 100,
    
    -- Audit fields
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(26),
    etag VARCHAR(50)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_account_profiles_account_id ON user_profile.account_profiles(account_id);
CREATE INDEX IF NOT EXISTS idx_account_profiles_updated_at ON user_profile.account_profiles(updated_at);

-- Add comments
COMMENT ON TABLE user_profile.account_profiles IS 'User account profiles with preferences and settings';
COMMENT ON COLUMN user_profile.account_profiles.account_id IS 'ULID of the user account';
COMMENT ON COLUMN user_profile.account_profiles.etag IS 'Optimistic locking version identifier';
COMMENT ON COLUMN user_profile.account_profiles.version IS 'Entity version for optimistic locking';
