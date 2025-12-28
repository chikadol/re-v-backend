-- 항상 rev 스키마 먼저
CREATE SCHEMA IF NOT EXISTS rev;
SET search_path TO rev, public;

CREATE TABLE IF NOT EXISTS rev."user" (
                                          id        BIGSERIAL PRIMARY KEY,
                                          email     VARCHAR(255),
                                          password  VARCHAR(255),
                                          roles     TEXT NOT NULL DEFAULT 'USER'
);

ALTER TABLE rev."user" ADD COLUMN IF NOT EXISTS email    VARCHAR(255);
ALTER TABLE rev."user" ADD COLUMN IF NOT EXISTS password VARCHAR(255);
ALTER TABLE rev."user" ADD COLUMN IF NOT EXISTS roles    TEXT;

-- 3) 인덱스(이메일 조회/중복 방지)
CREATE UNIQUE INDEX IF NOT EXISTS ux_user_email ON rev."user"(email);
CREATE        INDEX IF NOT EXISTS idx_user_email ON rev."user"(email);
