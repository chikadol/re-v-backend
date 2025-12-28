-- 0) 스키마/확장 보장
CREATE SCHEMA IF NOT EXISTS rev;
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1) thread 테이블 (없으면 생성)
CREATE TABLE IF NOT EXISTS rev.thread (
                                          id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
                                          title       text        NOT NULL,
                                          content     text        NOT NULL,
                                          author_id   uuid,
                                          board_id    bigint,                       -- ★ board.id = bigint 에 맞춤
                                          is_private  boolean     NOT NULL DEFAULT false,
                                          category_id uuid,
                                          parent_id   uuid,
                                          tags        text[],
                                          created_at  timestamptz NOT NULL DEFAULT now(),
                                          updated_at  timestamptz NOT NULL DEFAULT now()
);

-- 2) 이미 있었던 컬럼/타입 정렬 (있으면 수정, 없으면 추가)
ALTER TABLE IF EXISTS rev.thread
    ADD COLUMN IF NOT EXISTS author_id uuid;

ALTER TABLE IF EXISTS rev.thread
    ADD COLUMN IF NOT EXISTS board_id bigint;

-- author_id 는 uuid로 고정 (이미 uuid면 그대로 통과)
ALTER TABLE IF EXISTS rev.thread
    ALTER COLUMN author_id TYPE uuid USING author_id::uuid;

-- board_id 는 bigint로 고정
-- (uuid 등 다른 타입이었다면 모두 NULL로 치환되어 타입 변경)
ALTER TABLE IF EXISTS rev.thread
    ALTER COLUMN board_id TYPE bigint USING NULL;

-- 타임스탬프 컬럼 누락 시 보강
ALTER TABLE IF EXISTS rev.thread
    ADD COLUMN IF NOT EXISTS created_at timestamptz NOT NULL DEFAULT now();

ALTER TABLE IF EXISTS rev.thread
    ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now();

-- 3) FK 재정의 (먼저 드롭 후 재생성)
ALTER TABLE IF EXISTS rev.thread
    DROP CONSTRAINT IF EXISTS thread_author_id_fkey;

ALTER TABLE IF EXISTS rev.thread
    DROP CONSTRAINT IF EXISTS thread_board_id_fkey;

ALTER TABLE IF EXISTS rev.thread
    ADD CONSTRAINT thread_author_id_fkey
        FOREIGN KEY (author_id) REFERENCES rev.users(id) ON DELETE SET NULL;

ALTER TABLE IF EXISTS rev.thread
    ADD CONSTRAINT thread_board_id_fkey
        FOREIGN KEY (board_id) REFERENCES rev.board(id) ON DELETE CASCADE;

-- 4) 조회 인덱스 (선택)
CREATE INDEX IF NOT EXISTS idx_thread_author_id ON rev.thread (author_id);
CREATE INDEX IF NOT EXISTS idx_thread_board_id  ON rev.thread (board_id);
CREATE INDEX IF NOT EXISTS idx_thread_created   ON rev.thread (created_at);
