create schema if not exists rev;

-- 확장 보장 (Supabase면 이미 있을 가능성 높음)
create extension if not exists "pgcrypto";
create extension if not exists "uuid-ossp";

create table if not exists rev.thread_bookmark (
                                                   id          uuid primary key default gen_random_uuid(),
                                                   user_id     uuid not null,          -- ✅ username 대신 user_id
                                                   thread_id   uuid not null,
                                                   created_at  timestamptz not null default now(),

                                                   constraint uq_thread_bookmark unique (user_id, thread_id)
);

create index if not exists idx_tb_user   on rev.thread_bookmark(user_id);
create index if not exists idx_tb_thread on rev.thread_bookmark(thread_id);
