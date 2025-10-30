-- V25__create_thread_module.sql (수정안)

-- thread 테이블은 이미 존재하므로 생성문이 있다면 반드시 IF NOT EXISTS 사용
CREATE TABLE IF NOT EXISTS rev.thread (
                                          id BIGSERIAL PRIMARY KEY,
                                          title        TEXT        NOT NULL,
                                          content      TEXT        NOT NULL,
                                          author_id    UUID        NOT NULL,
                                          created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- tag 테이블(예시)도 없으면 생성
CREATE TABLE IF NOT EXISTS rev.tag (
                                       id BIGSERIAL PRIMARY KEY,
                                       name TEXT UNIQUE NOT NULL
);

-- 🔧 문제 테이블: thread_tag
-- thread_id를 UUID가 아니라 BIGINT로!
CREATE TABLE IF NOT EXISTS rev.thread_tag (
                                              id BIGSERIAL PRIMARY KEY,
                                              thread_id BIGINT NOT NULL,
                                              tag_id    BIGINT NOT NULL,
                                              CONSTRAINT thread_tag_thread_id_fkey
                                                  FOREIGN KEY (thread_id) REFERENCES rev.thread(id) ON DELETE CASCADE,
                                              CONSTRAINT thread_tag_tag_id_fkey
                                                  FOREIGN KEY (tag_id)    REFERENCES rev.tag(id)    ON DELETE CASCADE
);

-- 필요시 인덱스
CREATE INDEX IF NOT EXISTS idx_thread_tag_thread_id ON rev.thread_tag(thread_id);
CREATE INDEX IF NOT EXISTS idx_thread_tag_tag_id    ON rev.thread_tag(tag_id);

-- (다른 모듈 테이블들도 thread.id를 참조한다면 전부 thread_id를 BIGINT로 통일)
-- 예: 리액션, 북마크 등
CREATE TABLE IF NOT EXISTS rev.thread_reaction (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   thread_id BIGINT NOT NULL,
                                                   user_id   UUID   NOT NULL,
                                                   type      TEXT   NOT NULL, -- enum 매핑은 애플리케이션에서
                                                   created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                   CONSTRAINT fk_thread_reaction_thread
                                                       FOREIGN KEY (thread_id) REFERENCES rev.thread(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_thread_reaction_thread_id ON rev.thread_reaction(thread_id);

CREATE TABLE IF NOT EXISTS rev.thread_bookmark (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   thread_id BIGINT NOT NULL,
                                                   user_id   UUID   NOT NULL,
                                                   created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                   CONSTRAINT fk_thread_bookmark_thread
                                                       FOREIGN KEY (thread_id) REFERENCES rev.thread(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_thread_bookmark_thread_id ON rev.thread_bookmark(thread_id);
