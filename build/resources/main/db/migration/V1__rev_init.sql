-- src/main/resources/db/migration/V1__init.sql
create extension if not exists pgcrypto;

create schema if not exists rev;

create table if not exists rev.users(
                                        id uuid primary key default gen_random_uuid(),
                                        email text unique not null,
                                        password text not null,
                                        username text not null
);

create table if not exists rev.board(
                                        id uuid primary key default gen_random_uuid(),
                                        name text not null,
                                        slug text unique not null,
                                        description text
);

create table if not exists rev.thread(
                                         id uuid primary key default gen_random_uuid(),
                                         title text not null,
                                         content text not null,
                                         board_id uuid references rev.board(id) on delete cascade,
                                         author_id uuid references rev.users(id) on delete cascade,
                                         parent_id uuid references rev.thread(id) on delete set null,
                                         is_private boolean not null default false,
                                         category_id uuid,
                                         created_at timestamptz not null default now(),
                                         updated_at timestamptz not null default now()
);

create table if not exists rev.comment(
                                          id uuid primary key default gen_random_uuid(),
                                          thread_id uuid references rev.thread(id) on delete cascade,
                                          author_id uuid references rev.users(id) on delete cascade,
                                          parent_id uuid references rev.comment(id) on delete set null,
                                          content text not null,
                                          created_at timestamptz not null default now()
);

create table if not exists rev.thread_bookmark(
                                                  id uuid primary key default gen_random_uuid(),
                                                  thread_id uuid not null references rev.thread(id) on delete cascade,
                                                  user_id uuid not null references rev.users(id) on delete cascade,
                                                  created_at timestamptz not null default now(),
                                                  unique(thread_id, user_id)
);

-- indices
create index if not exists idx_thread_board_priv_created
    on rev.thread(board_id, is_private, created_at desc);
create index if not exists idx_comment_thread_created
    on rev.comment(thread_id, created_at asc);
