-- Channel Service Database Schema
-- Migration: V1__Create_channels_table.sql

CREATE TABLE channels (
    id VARCHAR(26) PRIMARY KEY,
    owner_user_id VARCHAR(26) NOT NULL,
    handle VARCHAR(50) NOT NULL UNIQUE,
    handle_lower VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    country VARCHAR(2),
    branding JSONB NOT NULL DEFAULT '{}',
    policy JSONB NOT NULL DEFAULT '{}',
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE channel_members (
    channel_id VARCHAR(26) NOT NULL,
    user_id VARCHAR(26) NOT NULL,
    role VARCHAR(20) NOT NULL,
    added_by VARCHAR(26) NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (channel_id, user_id),
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES channels(owner_user_id) ON DELETE CASCADE,
    FOREIGN KEY (added_by) REFERENCES channels(owner_user_id) ON DELETE CASCADE
);

CREATE TABLE handle_reservations (
    handle_lower VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_channels_owner_user_id ON channels(owner_user_id);
CREATE INDEX idx_channels_handle_lower ON channels(handle_lower);
CREATE INDEX idx_channels_language ON channels(language);
CREATE INDEX idx_channels_country ON channels(country);
CREATE INDEX idx_channel_members_channel_id ON channel_members(channel_id);
CREATE INDEX idx_channel_members_user_id ON channel_members(user_id);
CREATE INDEX idx_channel_members_role ON channel_members(role);
CREATE INDEX idx_handle_reservations_user_id ON handle_reservations(user_id);
CREATE INDEX idx_handle_reservations_expires_at ON handle_reservations(expires_at);
