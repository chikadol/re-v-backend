-- 스키마 보장
CREATE SCHEMA IF NOT EXISTS rev;

-- 기존 FK 있으면 제거 (DO $$ 없이 안전하게)
ALTER TABLE IF EXISTS rev.thread
    DROP CONSTRAINT IF EXISTS thread_author_id_fkey;

-- FK를 rev."user"(id)로 다시 연결
ALTER TABLE rev.thread
    ADD CONSTRAINT thread_author_id_fkey
        FOREIGN KEY (author_id)
            REFERENCES rev."user"(id) ON DELETE RESTRICT;
