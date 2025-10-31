-- Create SLO tables
CREATE TABLE IF NOT EXISTS slos (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    description TEXT,
    target_percent DECIMAL(5,2) NOT NULL,
    time_window_duration VARCHAR(50) NOT NULL,
    time_window_type VARCHAR(20) NOT NULL,
    error_budget DECIMAL(5,2),
    error_budget_remaining DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS slis (
    slo_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    query TEXT NOT NULL,
    last_calculated_at TIMESTAMP,
    last_value DECIMAL(10,4),
    PRIMARY KEY (slo_id, name),
    FOREIGN KEY (slo_id) REFERENCES slos(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS slo_labels (
    slo_id UUID NOT NULL,
    label_key VARCHAR(255) NOT NULL,
    label_value VARCHAR(500),
    PRIMARY KEY (slo_id, label_key),
    FOREIGN KEY (slo_id) REFERENCES slos(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_slos_service_name ON slos(service_name);
CREATE INDEX IF NOT EXISTS idx_slos_created_at ON slos(created_at);

