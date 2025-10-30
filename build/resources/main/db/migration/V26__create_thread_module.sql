-- Ensure schema & extensions (id 생성에 pgcrypto 사용)
CREATE SCHEMA IF NOT EXISTS rev;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========== THREAD CORE ==========
-- thread 본문: 모든 PK/FK를 UUID로 통일
CREATE TABLE IF NOT EXISTS rev.thread (
                                          id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                          title             text        NOT NULL,
                                          content           text        NOT NULL,
                                          author_id         uuid        NOT NULL,      -- 사용자 FK(조건부로 나중에 추가)
                                          category_id       uuid,
                                          parent_thread_id  uuid,
                                          is_private        boolean     NOT NULL DEFAULT false,
                                          created_at        timestamptz NOT NULL DEFAULT now(),
                                          updated_at        timestamptz NOT NULL DEFAULT now()
);

-- 업데이트 타임스탬프 자동 갱신 트리거
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_proc p
                              JOIN pg_namespace n ON n.oid = p.pronamespace
            WHERE p.proname = 'rev_set_updated_at' AND n.nspname = 'rev'
        ) THEN
            CREATE FUNCTION rev.rev_set_updated_at() RETURNS trigger AS $f$
            BEGIN
                NEW.updated_at := now();
                RETURN NEW;
            END;
            $f$ LANGUAGE plpgsql;
        END IF;
    END $$;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_trigger t
                              JOIN pg_class c ON c.oid = t.tgrelid
                              JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE t.tgname = 'trg_thread_set_updated_at'
              AND c.relname = 'thread'
              AND n.nspname = 'rev'
        ) THEN
            CREATE TRIGGER trg_thread_set_updated_at
                BEFORE UPDATE ON rev.thread
                FOR EACH ROW EXECUTE FUNCTION rev.rev_set_updated_at();
        END IF;
    END $$;

-- ========== REACTION ==========
-- enum type (조건부 생성)
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_type t
                              JOIN pg_namespace n ON n.oid = t.typnamespace
            WHERE t.typname = 'reaction_type' AND n.nspname = 'rev'
        ) THEN
            CREATE TYPE rev.reaction_type AS ENUM ('LIKE','LOVE','LAUGH','SAD','ANGRY');
        END IF;
    END $$;

CREATE TABLE IF NOT EXISTS rev.thread_reaction (
                                                   thread_id   uuid                NOT NULL,
                                                   user_id     uuid                NOT NULL,
                                                   type        rev.reaction_type   NOT NULL,
                                                   created_at  timestamptz         NOT NULL DEFAULT now(),
                                                   PRIMARY KEY (thread_id, user_id)
);

-- ========== BOOKMARK ==========
CREATE TABLE IF NOT EXISTS rev.thread_bookmark (
                                                   thread_id   uuid                NOT NULL,
                                                   user_id     uuid                NOT NULL,
                                                   created_at  timestamptz         NOT NULL DEFAULT now(),
                                                   PRIMARY KEY (thread_id, user_id)
);

-- ========== TAG ==========
CREATE TABLE IF NOT EXISTS rev.thread_tag (
                                              thread_id   uuid        NOT NULL,
                                              tag         text        NOT NULL,
                                              PRIMARY KEY (thread_id, tag)
);

-- ========== FK & INDEX ==========
-- thread_* -> thread 고정 FK (thread 테이블은 본 마이그레이션에서 생성되므로 바로 추가 가능)
DO $$
    BEGIN
        -- thread_reaction.thread_id -> thread.id
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_thread_reaction_thread'
        ) THEN
            ALTER TABLE rev.thread_reaction
                ADD CONSTRAINT fk_thread_reaction_thread
                    FOREIGN KEY (thread_id) REFERENCES rev.thread(id) ON DELETE CASCADE;
        END IF;

        -- thread_bookmark.thread_id -> thread.id
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_thread_bookmark_thread'
        ) THEN
            ALTER TABLE rev.thread_bookmark
                ADD CONSTRAINT fk_thread_bookmark_thread
                    FOREIGN KEY (thread_id) REFERENCES rev.thread(id) ON DELETE CASCADE;
        END IF;

        -- thread_tag.thread_id -> thread.id
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_thread_tag_thread'
        ) THEN
            ALTER TABLE rev.thread_tag
                ADD CONSTRAINT fk_thread_tag_thread
                    FOREIGN KEY (thread_id) REFERENCES rev.thread(id) ON DELETE CASCADE;
        END IF;
    END $$;

-- 사용자 테이블이 있을 때만 FK 추가 (스키마/테이블명이 환경마다 다를 수 있으므로 조건부)
DO $$
    DECLARE
        has_app_user bool;
    BEGIN
        SELECT EXISTS (
            SELECT 1
            FROM pg_class c
                     JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE n.nspname = 'rev' AND c.relname = 'app_user'
        ) INTO has_app_user;

        IF has_app_user THEN
            -- thread.author_id -> app_user.id
            IF NOT EXISTS (
                SELECT 1 FROM pg_constraint WHERE conname = 'fk_thread_author'
            ) THEN
                ALTER TABLE rev.thread
                    ADD CONSTRAINT fk_thread_author
                        FOREIGN KEY (author_id) REFERENCES rev.app_user(id) ON DELETE CASCADE;
            END IF;

            -- thread_reaction.user_id -> app_user.id
            IF NOT EXISTS (
                SELECT 1 FROM pg_constraint WHERE conname = 'fk_thread_reaction_user'
            ) THEN
                ALTER TABLE rev.thread_reaction
                    ADD CONSTRAINT fk_thread_reaction_user
                        FOREIGN KEY (user_id) REFERENCES rev.app_user(id) ON DELETE CASCADE;
            END IF;

            -- thread_bookmark.user_id -> app_user.id
            IF NOT EXISTS (
                SELECT 1 FROM pg_constraint WHERE conname = 'fk_thread_bookmark_user'
            ) THEN
                ALTER TABLE rev.thread_bookmark
                    ADD CONSTRAINT fk_thread_bookmark_user
                        FOREIGN KEY (user_id) REFERENCES rev.app_user(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END $$;

-- 인덱스(정렬/조회 핵심 컬럼)
CREATE INDEX IF NOT EXISTS idx_thread_created_at    ON rev.thread (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_thread_author_id     ON rev.thread (author_id);
CREATE INDEX IF NOT EXISTS idx_reaction_user        ON rev.thread_reaction (user_id);
CREATE INDEX IF NOT EXISTS idx_bookmark_user        ON rev.thread_bookmark (user_id);
