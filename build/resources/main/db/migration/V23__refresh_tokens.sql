CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id           BIGSERIAL PRIMARY KEY,
                                              username     VARCHAR(100) NOT NULL,
                                              token        TEXT NOT NULL UNIQUE,
                                              expires_at   TIMESTAMPTZ NOT NULL,
                                              revoked      BOOLEAN NOT NULL DEFAULT FALSE,
                                              last_used_at TIMESTAMPTZ,
                                              created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_username
    ON refresh_tokens(username);
