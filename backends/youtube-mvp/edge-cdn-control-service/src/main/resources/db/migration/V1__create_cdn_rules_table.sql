CREATE TABLE cdn_rules (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    rule_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    resource_group VARCHAR(255) NOT NULL,
    profile_name VARCHAR(255) NOT NULL,
    priority INTEGER,
    match_conditions TEXT,
    action TEXT,
    metadata TEXT,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version VARCHAR(255),
    rollback_from_rule_id VARCHAR(255)
);

CREATE INDEX idx_resource_group_profile ON cdn_rules(resource_group, profile_name);
CREATE INDEX idx_status ON cdn_rules(status);
CREATE INDEX idx_created_at ON cdn_rules(created_at);

