-- Migration script for Channel Service database schema
-- Creates tables for channels, handles, and channel members

-- Channels table
CREATE TABLE IF NOT EXISTS channels (
    id VARCHAR(26) PRIMARY KEY,
    owner_user_id VARCHAR(26) NOT NULL,
    handle_lower VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(100),
    description VARCHAR(5000),
    language VARCHAR(10),
    country VARCHAR(10),
    avatar_uri VARCHAR(500),
    banner_uri VARCHAR(500),
    accent_color VARCHAR(7),
    age_gate BOOLEAN DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    etag VARCHAR(255)
);

-- Indexes for channels table
CREATE INDEX IF NOT EXISTS idx_channels_handle ON channels(handle_lower);
CREATE INDEX IF NOT EXISTS idx_channels_owner ON channels(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_channels_created ON channels(created_at);

-- Handles table for handle reservation
CREATE TABLE IF NOT EXISTS handles (
    id VARCHAR(30) PRIMARY KEY,
    bucket INTEGER NOT NULL,
    channel_id VARCHAR(26),
    status VARCHAR(20) NOT NULL,
    reserved_by_user_id VARCHAR(26),
    reserved_at TIMESTAMP,
    committed_at TIMESTAMP,
    ttl_seconds INTEGER,
    version BIGINT
);

-- Indexes for handles table
CREATE INDEX IF NOT EXISTS idx_handles_bucket ON handles(bucket);
CREATE INDEX IF NOT EXISTS idx_handles_status ON handles(status);
CREATE INDEX IF NOT EXISTS idx_handles_reserved_at ON handles(reserved_at);

-- Channel members table
CREATE TABLE IF NOT EXISTS channel_members (
    id BIGSERIAL PRIMARY KEY,
    channel_id VARCHAR(26) NOT NULL,
    user_id VARCHAR(26) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT,
    UNIQUE(channel_id, user_id)
);

-- Indexes for channel members table
CREATE INDEX IF NOT EXISTS idx_channel_members_channel ON channel_members(channel_id);
CREATE INDEX IF NOT EXISTS idx_channel_members_user ON channel_members(user_id);
CREATE INDEX IF NOT EXISTS idx_channel_members_role ON channel_members(role);

-- Region blocks table (for policy)
CREATE TABLE IF NOT EXISTS channel_region_blocks (
    channel_id VARCHAR(26) NOT NULL,
    region_block VARCHAR(10) NOT NULL,
    PRIMARY KEY (channel_id, region_block),
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE
);

-- Insert some reserved handles
INSERT INTO handles (id, bucket, status, reserved_by_user_id, reserved_at, ttl_seconds, version) 
VALUES 
    ('admin', 0, 'COMMITTED', 'system', NOW(), NULL, 1),
    ('youtube', 0, 'COMMITTED', 'system', NOW(), NULL, 1),
    ('support', 0, 'COMMITTED', 'system', NOW(), NULL, 1),
    ('help', 0, 'COMMITTED', 'system', NOW(), NULL, 1),
    ('about', 0, 'COMMITTED', 'system', NOW(), NULL, 1)
ON CONFLICT (id) DO NOTHING;
