-- 스키마가 분리되어 있다면 먼저 보장
CREATE SCHEMA IF NOT EXISTS rev;

-- thread_tags 컬렉션 테이블 생성
CREATE TABLE IF NOT EXISTS rev.thread_tags (
                                               thread_id BIGINT NOT NULL REFERENCES rev.thread(id) ON DELETE CASCADE,
                                               tag       VARCHAR(100) NOT NULL
);

-- 조회/중복 방지용 인덱스(선택)
CREATE INDEX IF NOT EXISTS idx_thread_tags_thread_id ON rev.thread_tags(thread_id);
-- 한 쓰레드에 같은 태그 한 번만 허용하고 싶다면
-- CREATE UNIQUE INDEX IF NOT EXISTS uq_thread_tags_thread_tag ON rev.thread_tags(thread_id, tag);
