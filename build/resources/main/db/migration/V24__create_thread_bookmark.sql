-- V24__create_thread_module.sql
-- Thread / Reaction / Bookmark / Tag 모듈(모두 UUID 기반)

-- 스키마 및 확장
CREATE SCHEMA IF NOT EXISTS rev;
SET search_path TO rev, public;

-- Supabase/PG용 UUID 생성 확장 (이미 있으면 스킵)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================================
-- 1) THREAD
-- =========================================
CREATE TABLE IF NOT EXISTS thread (
                                      id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      board_key         TEXT            NOT NULL,              -- 게시판 구분 키
                                      title             VARCHAR(200)    NOT NULL,
                                      content           TEXT            NOT NULL,
                                      author_id         UUID            NOT NULL,              -- FK는 추후 users 테이블 확정 후 추가
                                      parent_thread_id  UUID            NULL,
                                      is_private        BOOLEAN         NOT NULL DEFAULT FALSE,
                                      created_at        TIMESTAMPTZ     NOT NULL DEFAULT now(),
                                      updated_at        TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- 부모 스레드 참조는 동일 테이블 내 UUID 기준
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_thread_parent'
        ) THEN
            ALTER TABLE thread
                ADD CONSTRAINT fk_thread_parent
                    FOREIGN KEY (parent_thread_id) REFERENCES thread(id) ON DELETE SET NULL;
        END IF;
    END$$;

-- 조회/정렬용 인덱스
CREATE INDEX IF NOT EXISTS idx_thread_board_created_desc ON thread (board_key, created_at DESC, id);

-- =========================================
-- 2) REACTION (스레드 반응)
--    ReactionType은 애플리케이션 Enum. DB는 텍스트로 저장(유연성↑).
-- =========================================
CREATE TABLE IF NOT EXISTS thread_reaction (
                                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                               thread_id   UUID        NOT NULL,
                                               user_id     UUID        NOT NULL,
                                               type        TEXT        NOT NULL,  -- 예: 'LIKE','LOVE','LAUGH' 등
                                               created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                                               CONSTRAINT fk_reaction_thread
                                                   FOREIGN KEY (thread_id) REFERENCES thread(id) ON DELETE CASCADE
);

-- 한 사용자는 한 스레드에 1개 반응만
CREATE UNIQUE INDEX IF NOT EXISTS uq_reaction_thread_user ON thread_reaction (thread_id, user_id);
CREATE INDEX IF NOT EXISTS idx_reaction_user ON thread_reaction (user_id);

-- =========================================
-- 3) BOOKMARK (즐겨찾기)
-- =========================================
CREATE TABLE IF NOT EXISTS thread_bookmark (
                                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                               thread_id   UUID        NOT NULL,
                                               user_id     UUID        NOT NULL,
                                               created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                                               CONSTRAINT fk_bookmark_thread
                                                   FOREIGN KEY (thread_id) REFERENCES thread(id) ON DELETE CASCADE
);

-- 동일 스레드-사용자 북마크 1개 제한
CREATE UNIQUE INDEX IF NOT EXISTS uq_bookmark_thread_user ON thread_bookmark (thread_id, user_id);
CREATE INDEX IF NOT EXISTS idx_bookmark_user ON thread_bookmark (user_id);

-- =========================================
-- 4) TAG & THREAD_TAG (다대다)
-- =========================================
CREATE TABLE IF NOT EXISTS tag (
                                   id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   name        TEXT NOT NULL UNIQUE,
                                   created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS thread_tag (
                                          thread_id   UUID NOT NULL,
                                          tag_id      UUID NOT NULL,
                                          created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          CONSTRAINT pk_thread_tag PRIMARY KEY (thread_id, tag_id),
                                          CONSTRAINT fk_thread_tag_thread FOREIGN KEY (thread_id) REFERENCES thread(id) ON DELETE CASCADE,
                                          CONSTRAINT fk_thread_tag_tag    FOREIGN KEY (tag_id)    REFERENCES tag(id)    ON DELETE CASCADE
);

-- 보조 인덱스
CREATE INDEX IF NOT EXISTS idx_thread_tag_tag ON thread_tag (tag_id);
