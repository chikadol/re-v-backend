-- V24__create_thread_bookmark.sql  (PostgreSQL / Flyway)
-- 목적: thread.parent_id FK 정합성 보장 + thread_bookmark 생성

CREATE SCHEMA IF NOT EXISTS rev;

-- 1) thread 테이블 보정: parent_id 컬럼 보장
ALTER TABLE IF EXISTS rev.thread
    ADD COLUMN IF NOT EXISTS parent_id uuid;

-- 2) parent FK 보장 (존재하지 않을 때만 추가)
DO $do$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.table_constraints tc
            WHERE tc.constraint_schema = 'rev'
              AND tc.table_name = 'thread'
              AND tc.constraint_name = 'fk_thread_parent'
        ) THEN
            ALTER TABLE rev.thread
                ADD CONSTRAINT fk_thread_parent
                    FOREIGN KEY (parent_id) REFERENCES rev.thread(id) ON DELETE SET NULL;
        END IF;
    END
$do$;

-- 3) thread_bookmark 테이블 생성
CREATE TABLE IF NOT EXISTS rev.thread_bookmark (
                                                   id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                                   thread_id  uuid NOT NULL,
                                                   user_id    uuid NOT NULL,
                                                   created_at timestamptz NOT NULL DEFAULT now(),
                                                   CONSTRAINT fk_thread_bookmark_thread
                                                       FOREIGN KEY (thread_id) REFERENCES rev.thread(id) ON DELETE CASCADE,
                                                   CONSTRAINT fk_thread_bookmark_user
                                                       FOREIGN KEY (user_id) REFERENCES rev.users(id) ON DELETE CASCADE,
                                                   CONSTRAINT uq_thread_bookmark UNIQUE (thread_id, user_id)
);
