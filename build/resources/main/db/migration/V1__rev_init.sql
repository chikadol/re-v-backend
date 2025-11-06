-- 스키마 & 확장
CREATE SCHEMA IF NOT EXISTS rev;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- users (기존 UserEntity와 필드 맞추세요)
CREATE TABLE IF NOT EXISTS rev.users (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         email TEXT UNIQUE NOT NULL,
                                         password TEXT NOT NULL,
                                         username TEXT NOT NULL
);

-- board
CREATE TABLE IF NOT EXISTS rev.board (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         name TEXT NOT NULL,
                                         slug TEXT UNIQUE NOT NULL,
                                         description TEXT,
                                         created_at TIMESTAMPTZ DEFAULT now(),
                                         updated_at TIMESTAMPTZ DEFAULT now()
);

-- thread
CREATE TABLE IF NOT EXISTS rev.thread (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          title TEXT NOT NULL,
                                          content TEXT NOT NULL,
                                          board_id UUID REFERENCES rev.board(id) ON DELETE CASCADE,
                                          author_id UUID REFERENCES rev.users(id) ON DELETE SET NULL,
                                          parent_id UUID REFERENCES rev.thread(id) ON DELETE SET NULL,
                                          is_private BOOLEAN NOT NULL DEFAULT FALSE,
                                          category_id UUID,
                                          created_at TIMESTAMPTZ DEFAULT now(),
                                          updated_at TIMESTAMPTZ DEFAULT now(),
                                          tags TEXT[] DEFAULT '{}'
);

-- comment
CREATE TABLE IF NOT EXISTS rev.comment (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           thread_id UUID REFERENCES rev.thread(id) ON DELETE CASCADE,
                                           author_id UUID REFERENCES rev.users(id) ON DELETE SET NULL,
                                           parent_id UUID REFERENCES rev.comment(id) ON DELETE SET NULL,
                                           content TEXT NOT NULL,
                                           created_at TIMESTAMPTZ DEFAULT now(),
                                           updated_at TIMESTAMPTZ DEFAULT now()
);

-- thread_bookmark
CREATE TABLE IF NOT EXISTS rev.thread_bookmark (
                                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                   thread_id UUID REFERENCES rev.thread(id) ON DELETE CASCADE,
                                                   user_id UUID REFERENCES rev.users(id) ON DELETE CASCADE,
                                                   UNIQUE (thread_id, user_id)
);
