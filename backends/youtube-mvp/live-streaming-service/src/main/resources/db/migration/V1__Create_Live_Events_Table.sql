-- Create live_events table
CREATE TABLE live_events (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    channel_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    description VARCHAR(2000),
    state VARCHAR(50) NOT NULL,
    ams_live_event_id VARCHAR(255),
    ams_live_event_name VARCHAR(255),
    ingest_url VARCHAR(1000),
    preview_url VARCHAR(1000),
    region VARCHAR(100),
    dvr_enabled BOOLEAN DEFAULT true,
    dvr_window_in_minutes INTEGER DEFAULT 120,
    low_latency_enabled BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    stopped_at TIMESTAMP,
    archived_at TIMESTAMP,
    failure_reason VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for common queries
CREATE INDEX idx_live_events_user_id ON live_events(user_id);
CREATE INDEX idx_live_events_channel_id ON live_events(channel_id);
CREATE INDEX idx_live_events_state ON live_events(state);
CREATE INDEX idx_live_events_created_at ON live_events(created_at DESC);
CREATE INDEX idx_live_events_user_state ON live_events(user_id, state);

COMMENT ON TABLE live_events IS 'Stores live streaming events with lifecycle management';
COMMENT ON COLUMN live_events.ams_live_event_id IS 'Azure Media Services live event ID';
COMMENT ON COLUMN live_events.ams_live_event_name IS 'Azure Media Services live event name';
COMMENT ON COLUMN live_events.dvr_enabled IS 'Whether DVR (Digital Video Recording) is enabled';
COMMENT ON COLUMN live_events.dvr_window_in_minutes IS 'DVR window duration in minutes';
COMMENT ON COLUMN live_events.low_latency_enabled IS 'Whether low-latency streaming is enabled';

