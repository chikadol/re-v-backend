create table if not exists rev.refresh_token(
                                                id uuid primary key default gen_random_uuid(),
                                                subject text not null,
                                                token_hash text not null unique,
                                                expires_at timestamptz not null,
                                                revoked boolean not null default false,
                                                created_at timestamptz not null default now()
);
create index if not exists ix_refresh_subject on rev.refresh_token(subject);
