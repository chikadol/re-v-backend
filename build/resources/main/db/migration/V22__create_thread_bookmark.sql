create table if not exists rev.thread_bookmark (
                                                   id          uuid primary key default gen_random_uuid(),
                                                   member_id   uuid not null,
                                                   thread_id   uuid not null,
                                                   created_at  timestamptz not null default now(),
                                                   updated_at  timestamptz not null default now(),
                                                   unique (member_id, thread_id)
    -- 필요하면 FK도 추가:
    -- , foreign key (member_id) references rev.member(id)
    -- , foreign key (thread_id) references rev.thread(id)
);
