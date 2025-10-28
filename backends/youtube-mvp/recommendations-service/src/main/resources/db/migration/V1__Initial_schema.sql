-- User Features Table
CREATE TABLE user_features (
    user_id VARCHAR(255) PRIMARY KEY,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE user_feature_embeddings (
    user_features_user_id VARCHAR(255) NOT NULL,
    embedding DOUBLE PRECISION,
    FOREIGN KEY (user_features_user_id) REFERENCES user_features(user_id) ON DELETE CASCADE
);

CREATE TABLE user_feature_categorical (
    user_features_user_id VARCHAR(255) NOT NULL,
    key VARCHAR(255),
    value VARCHAR(255),
    FOREIGN KEY (user_features_user_id) REFERENCES user_features(user_id) ON DELETE CASCADE
);

CREATE TABLE user_feature_numerical (
    user_features_user_id VARCHAR(255) NOT NULL,
    key VARCHAR(255),
    value DOUBLE PRECISION,
    FOREIGN KEY (user_features_user_id) REFERENCES user_features(user_id) ON DELETE CASCADE
);

CREATE TABLE user_categories (
    user_features_user_id VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    FOREIGN KEY (user_features_user_id) REFERENCES user_features(user_id) ON DELETE CASCADE
);

CREATE TABLE user_languages (
    user_features_user_id VARCHAR(255) NOT NULL,
    language VARCHAR(255),
    FOREIGN KEY (user_features_user_id) REFERENCES user_features(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_user_features_user_id ON user_features(user_id);

-- Recommendations Cache Table
CREATE TABLE recommendations_cache (
    cache_key VARCHAR(255) PRIMARY KEY,
    cache_value TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recommendations_cache_expires ON recommendations_cache(expires_at);

