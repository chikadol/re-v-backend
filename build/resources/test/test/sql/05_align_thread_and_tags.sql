-- 05_align_thread_and_tags.sql
-- Test-only schema align: drop & recreate tables with UUID PK/FK.
-- No DO $$, no procedural IFs. Pure SQL only (safe for ScriptUtils).

CREATE SCHEMA IF NOT EXISTS rev;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 0) Drop in dependency order (children first)
DROP TABLE IF EXISTS rev.thread_tags CASCADE;
DROP TABLE IF EXISTS rev.comment CASCADE;
DROP TABLE IF EXISTS rev.thread_reaction CASCADE;
DROP TABLE IF EXISTS rev.thread CASCADE;
DROP TABLE IF EXISTS rev.board CASCADE;
DROP TABLE IF EXISTS rev.users CASCADE;

-- 1) Users (UUID PK)
CREATE TABLE IF NOT EXISTS rev.users (
                                         id        uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                         email     text UNIQUE NOT NULL,
                                         password  text NOT NULL,
                                         username  text NOT NULL
);

-- 2) Boards (UUID PK)
CREATE TABLE IF NOT EXISTS rev.board (
                                         id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                         name        text NOT NULL,
                                         slug        text UNIQUE NOT NULL,
                                         description text
);

-- 3) Threads (UUID PK, all FKs as UUID)
CREATE TABLE IF NOT EXISTS rev.thread (
                                          id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                          title        text NOT NULL,
                                          content      text NOT NULL,
                                          board_id     uuid NOT NULL REFERENCES rev.board(id) ON DELETE CASCADE,
                                          parent_id    uuid NULL,
                                          author_id    uuid NULL REFERENCES rev.users(id) ON DELETE SET NULL,
                                          is_private   boolean NOT NULL DEFAULT false,
                                          category_id  uuid NULL,
                                          created_at   timestamptz NOT NULL DEFAULT now(),
                                          updated_at   timestamptz NOT NULL DEFAULT now()
);

-- self FK (parent)
ALTER TABLE rev.thread
    ADD CONSTRAINT thread_parent_id_fkey
        FOREIGN KEY (parent_id) REFERENCES rev.thread(id) ON DELETE SET NULL;

-- 4) Comments (UUID PK, FK to thread)
CREATE TABLE IF NOT EXISTS rev.comment (
                                           id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                           thread_id   uuid NOT NULL REFERENCES rev.thread(id) ON DELETE CASCADE,
                                           author_id   uuid NULL REFERENCES rev.users(id) ON DELETE SET NULL,
                                           content     text NOT NULL,
                                           parent_id   uuid NULL,
                                           created_at  timestamptz NOT NULL DEFAULT now(),
                                           updated_at  timestamptz NOT NULL DEFAULT now()
);

-- comment self FK (parent comment)
ALTER TABLE rev.comment
    ADD CONSTRAINT comment_parent_id_fkey
        FOREIGN KEY (parent_id) REFERENCES rev.comment(id) ON DELETE SET NULL;

-- 5) Thread reactions (optional, keep simple)
CREATE TABLE IF NOT EXISTS rev.thread_reaction (
                                                   id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                                   thread_id  uuid NOT NULL REFERENCES rev.thread(id) ON DELETE CASCADE,
                                                   user_id    uuid NULL REFERENCES rev.users(id) ON DELETE SET NULL,
                                                   reaction   text NOT NULL,
                                                   created_at timestamptz NOT NULL DEFAULT now()
);

-- 6) Thread tags (composite PK)
CREATE TABLE IF NOT EXISTS rev.thread_tags (
                                               thread_id uuid NOT NULL REFERENCES rev.thread(id) ON DELETE CASCADE,
                                               tag       text NOT NULL,
                                               PRIMARY KEY (thread_id, tag)
);

-- 7) Helpful indexes (optional)
CREATE INDEX IF NOT EXISTS idx_thread_board_created ON rev.thread(board_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comment_thread_created ON rev.comment(thread_id, created_at DESC);
