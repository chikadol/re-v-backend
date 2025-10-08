-- V3__thread_bookmark.sql
-- Thread 북마크 테이블 (필드명은 관례에 맞춰 가정)
CREATE TABLE IF NOT EXISTS thread_bookmark (
    id           BIGSERIAL PRIMARY KEY,
    thread_id    BIGINT      NOT NULL,
    user_id      BIGINT      NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_thread_bookmark UNIQUE (thread_id, user_id)
);

-- (있는 경우에만 FK 추가 – 실제 테이블/PK 이름에 맞춰 조정)
-- ALTER TABLE thread_bookmark
--   ADD CONSTRAINT fk_thread_bookmark_thread
--   FOREIGN KEY (thread_id) REFERENCES thread(id) ON DELETE CASCADE;

-- ALTER TABLE thread_bookmark
--   ADD CONSTRAINT fk_thread_bookmark_user
--   FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE;  -- 사용자 테이블명에 맞게 수정
