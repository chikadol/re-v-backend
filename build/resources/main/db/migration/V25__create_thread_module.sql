-- schema는 이미 rev가 있다고 했지만, 혹시 몰라서 생성문은 주석으로만 남겨둠
-- create schema if not exists rev;

-- thread
create table if not exists rev.thread (
                                          id uuid primary key default gen_random_uuid(),
                                          title varchar(200) not null,
                                          content text not null,
                                          author_id uuid not null,
                                          category_id uuid,
                                          parent_thread_id uuid,
                                          is_private boolean not null default false,
                                          created_at timestamptz not null default now(),
                                          updated_at timestamptz not null default now()
);

-- thread_tag (ElementCollection 저장용)
create table if not exists rev.thread_tag (
                                              thread_id uuid not null,
                                              tag varchar(50) not null,
                                              primary key (thread_id, tag),
                                              constraint fk_thread_tag_thread foreign key (thread_id) references rev.thread(id) on delete cascade
);

-- thread_reaction
create table if not exists rev.thread_reaction (
                                                   id uuid primary key default gen_random_uuid(),
                                                   thread_id uuid not null,
                                                   user_id uuid not null,
                                                   type varchar(20) not null,
                                                   created_at timestamptz not null default now(),
                                                   updated_at timestamptz not null default now(),
                                                   constraint fk_thread_reaction_thread foreign key (thread_id) references rev.thread(id) on delete cascade
);
create unique index if not exists uk_thread_reaction_thread_user
    on rev.thread_reaction(thread_id, user_id);

-- thread_bookmark
create table if not exists rev.thread_bookmark (
                                                   id uuid primary key default gen_random_uuid(),
                                                   thread_id uuid not null,
                                                   user_id uuid not null,
                                                   created_at timestamptz not null default now(),
                                                   constraint fk_thread_bookmark_thread foreign key (thread_id) references rev.thread(id) on delete cascade
);
create unique index if not exists uk_thread_bookmark_thread_user
    on rev.thread_bookmark(thread_id, user_id);
