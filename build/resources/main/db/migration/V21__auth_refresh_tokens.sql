SET search_path TO rev, public;

CREATE TABLE IF NOT EXISTS rev.refresh_token (
                                                 id         BIGSERIAL PRIMARY KEY,
                                                 user_id    BIGINT NOT NULL,
                                                 token      VARCHAR(512) NOT NULL,
                                                 issued_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                 expires_at TIMESTAMPTZ NOT NULL,
                                                 revoked    BOOLEAN NOT NULL DEFAULT FALSE
);

-- FK는 존재하지 않을 때만 추가
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.table_constraints
            WHERE constraint_schema = 'rev'
              AND table_name = 'refresh_token'
              AND constraint_name = 'fk_refresh_user'
        ) THEN
            ALTER TABLE rev.refresh_token
                ADD CONSTRAINT fk_refresh_user
                    FOREIGN KEY (user_id) REFERENCES rev."user"(id) ON DELETE CASCADE;
        END IF;
    END $$;

CREATE INDEX IF NOT EXISTS idx_refresh_token_token ON rev.refresh_token(token);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user  ON rev.refresh_token(user_id);
