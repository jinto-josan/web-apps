CREATE TABLE IF NOT EXISTS campaign (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    daily_budget NUMERIC(18,2) NOT NULL,
    spent_today NUMERIC(18,2) NOT NULL DEFAULT 0,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS creative (
    id VARCHAR(64) PRIMARY KEY,
    campaign_id VARCHAR(64) NOT NULL REFERENCES campaign(id) ON DELETE CASCADE,
    asset_url TEXT NOT NULL,
    duration_seconds INT NOT NULL,
    mime_type VARCHAR(100) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_creative_campaign ON creative(campaign_id);


