-- V3__thread_bookmark.sql
CREATE TABLE IF NOT EXISTS thread_bookmark (
    id         BIGSERIAL PRIMARY KEY,
    thread_id  BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_thread_bookmark UNIQUE (thread_id, user_id)
);

-- 외래키를 사용한다면 실제 테이블/PK 이름에 맞춰 주석 해제 후 적용
-- ALTER TABLE thread_bookmark
--   ADD CONSTRAINT fk_thread_bookmark_thread
--   FOREIGN KEY (thread_id) REFERENCES thread(id) ON DELETE CASCADE;

-- ALTER TABLE thread_bookmark
--   ADD CONSTRAINT fk_thread_bookmark_user
--   FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE;

-- 조회 성능용 인덱스
CREATE INDEX IF NOT EXISTS idx_thread_bookmark_thread ON thread_bookmark(thread_id);
CREATE INDEX IF NOT EXISTS idx_thread_bookmark_user   ON thread_bookmark(user_id);
