-- src/main/resources/db/migration/V1__init.sql
create extension if not exists pgcrypto;
create schema if not exists rev;

create table if not exists rev.users (
                                         id uuid primary key default gen_random_uuid(),
                                         email text unique not null,
                                         password text not null,
                                         username text not null
);

create table if not exists rev.board (
                                         id uuid primary key default gen_random_uuid(),
                                         name text not null,
                                         slug text not null unique,
                                         description text
);

create table if not exists rev.thread (
                                          id uuid primary key default gen_random_uuid(),
                                          title text not null,
                                          content text not null,
                                          board_id uuid references rev.board(id) on delete cascade,
                                          parent_id uuid references rev.thread(id) on delete set null,
                                          author_id uuid references rev.users(id) on delete cascade,
                                          is_private boolean not null default false,
                                          category_id uuid,
                                          created_at timestamp with time zone default now(),
                                          updated_at timestamp with time zone default now()
);

create table if not exists rev.thread_tags (
                                               thread_id uuid references rev.thread(id) on delete cascade,
                                               tag text not null
);

create table if not exists rev.comment (
                                           id uuid primary key default gen_random_uuid(),
                                           thread_id uuid not null references rev.thread(id) on delete cascade,
                                           author_id uuid not null references rev.users(id) on delete cascade,
                                           parent_id uuid references rev.comment(id) on delete set null,
                                           content text not null,
                                           created_at timestamp with time zone default now()
);

create table if not exists rev.thread_bookmark (
                                                   id uuid primary key default gen_random_uuid(),
                                                   thread_id uuid not null references rev.thread(id) on delete cascade,
                                                   user_id uuid not null references rev.users(id) on delete cascade,
                                                   unique (thread_id, user_id)
);
