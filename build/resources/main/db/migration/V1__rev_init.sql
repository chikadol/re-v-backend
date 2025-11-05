-- V1__rev_init.sql
-- schema & extension ----------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS rev;
CREATE EXTENSION IF NOT EXISTS pgcrypto; -- gen_random_uuid()

-- users -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rev.users (
                                         id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                         email      text UNIQUE NOT NULL,
                                         username   text NOT NULL,
                                         password   text NOT NULL,
                                         created_at timestamptz NOT NULL DEFAULT now(),
                                         updated_at timestamptz NOT NULL DEFAULT now()
);

-- board -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rev.board (
                                         id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                         name        text NOT NULL,
                                         slug        text UNIQUE NOT NULL,
                                         description text,
                                         created_at  timestamptz NOT NULL DEFAULT now(),
                                         updated_at  timestamptz NOT NULL DEFAULT now()
);

-- thread ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rev.thread (
                                          id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                          board_id    uuid NOT NULL
                                              REFERENCES rev.board(id) ON DELETE CASCADE,
                                          author_id   uuid
                                                           REFERENCES rev.users(id) ON DELETE SET NULL,
                                          parent_id   uuid
                                                           REFERENCES rev.thread(id) ON DELETE SET NULL,
                                          title       text NOT NULL,
                                          content     text NOT NULL,
                                          is_private  boolean NOT NULL DEFAULT false,
                                          category_id uuid,
                                          created_at  timestamptz NOT NULL DEFAULT now(),
                                          updated_at  timestamptz NOT NULL DEFAULT now()
);

-- comment ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rev.comment (
                                           id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                           thread_id  uuid NOT NULL
                                               REFERENCES rev.thread(id) ON DELETE CASCADE,
                                           author_id  uuid
                                                           REFERENCES rev.users(id) ON DELETE SET NULL,
                                           parent_id  uuid
                                                           REFERENCES rev.comment(id) ON DELETE SET NULL,
                                           content    text NOT NULL,
                                           created_at timestamptz NOT NULL DEFAULT now(),
                                           updated_at timestamptz NOT NULL DEFAULT now()
);

-- thread_tags -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rev.thread_tags (
                                               thread_id uuid NOT NULL
                                                   REFERENCES rev.thread(id) ON DELETE CASCADE,
                                               tag       text NOT NULL,
                                               PRIMARY KEY (thread_id, tag)
);

-- indexes ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_thread_board        ON rev.thread(board_id);
CREATE INDEX IF NOT EXISTS idx_thread_created_at   ON rev.thread(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comment_thread      ON rev.comment(thread_id);
-- thread_bookmark -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rev.thread_bookmark (
                                                   id        uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                                   thread_id uuid NOT NULL REFERENCES rev.thread(id) ON DELETE CASCADE,
                                                   user_id   uuid NOT NULL REFERENCES rev.users(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_thread_bookmark_thread_user
    ON rev.thread_bookmark(thread_id, user_id);
