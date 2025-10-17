CREATE SCHEMA IF NOT EXISTS auth;  
  
CREATE TABLE IF NOT EXISTS auth.users (  
  id                CHAR(26)        PRIMARY KEY,  
  email             VARCHAR(320)    NOT NULL,  
  normalized_email  VARCHAR(320)    NOT NULL,  
  display_name      VARCHAR(200)    NOT NULL,  
  status            SMALLINT        NOT NULL DEFAULT 1, -- 0=PENDING,1=ACTIVE,2=LOCKED,3=DISABLED  
  email_verified    BOOLEAN         NOT NULL DEFAULT FALSE,  
  password_hash     VARCHAR(255),  
  password_alg      SMALLINT,  
  mfa_enabled       BOOLEAN         NOT NULL DEFAULT FALSE,  
  terms_version     VARCHAR(32),  
  terms_accepted_at TIMESTAMPTZ,  
  created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),  
  updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),  
  version           INTEGER         NOT NULL DEFAULT 0,  
  CONSTRAINT ck_auth_users_status CHECK (status BETWEEN 0 AND 3)  
);  
CREATE UNIQUE INDEX IF NOT EXISTS ux_auth_users_normalized_email ON auth.users (normalized_email);  
CREATE INDEX IF NOT EXISTS ix_auth_users_status ON auth.users (status);  
CREATE INDEX IF NOT EXISTS ix_auth_users_updated_at ON auth.users (updated_at);  
  
CREATE TABLE IF NOT EXISTS auth.sessions (  
  id               CHAR(26)      PRIMARY KEY,  
  user_id          CHAR(26)      NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,  
  jti              VARCHAR(64)   NOT NULL,  
  device_id        VARCHAR(64),  
  user_agent       VARCHAR(512),  
  ip               VARCHAR(45),  
  created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),  
  revoked_at       TIMESTAMPTZ,  
  revoke_reason    VARCHAR(200),  
  mfa_verified_at  TIMESTAMPTZ,  
  version          INTEGER       NOT NULL DEFAULT 0  
);  
CREATE UNIQUE INDEX IF NOT EXISTS ux_auth_sessions_jti ON auth.sessions (jti);  
CREATE INDEX IF NOT EXISTS ix_auth_sessions_user_created ON auth.sessions (user_id, created_at DESC);  
CREATE INDEX IF NOT EXISTS ix_auth_sessions_revoked_at ON auth.sessions (revoked_at);  
  
CREATE TABLE IF NOT EXISTS auth.refresh_tokens (  
  id                   CHAR(26)       PRIMARY KEY,  
  session_id           CHAR(26)       NOT NULL REFERENCES auth.sessions(id) ON DELETE CASCADE,  
  token_hash           BYTEA          NOT NULL,  
  expires_at           TIMESTAMPTZ    NOT NULL,  
  replaced_by_token_id CHAR(26),  
  revoked_at           TIMESTAMPTZ,  
  revoke_reason        VARCHAR(200),  
  created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),  
  CONSTRAINT fk_auth_rt_replaced_by FOREIGN KEY (replaced_by_token_id) REFERENCES auth.refresh_tokens(id)  
);  
CREATE UNIQUE INDEX IF NOT EXISTS ux_auth_rt_token_hash ON auth.refresh_tokens (token_hash);  
CREATE INDEX IF NOT EXISTS ix_auth_rt_session ON auth.refresh_tokens (session_id, created_at DESC);  
CREATE INDEX IF NOT EXISTS ix_auth_rt_expires ON auth.refresh_tokens (expires_at);  
CREATE INDEX IF NOT EXISTS ix_auth_rt_revoked ON auth.refresh_tokens (revoked_at);  
CREATE UNIQUE INDEX IF NOT EXISTS ux_auth_rt_replaced_by ON auth.refresh_tokens (replaced_by_token_id) WHERE replaced_by_token_id IS NOT NULL;  