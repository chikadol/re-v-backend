-- src/test/resources/test/sql/02_seed_minimal.sql
insert into rev.users (id, email, username, password)
values (gen_random_uuid(), 'e@example.com', 'u', '{noop}pw')
on conflict (email) do nothing;

-- (필요하면 보드 한 개)
insert into rev.board (id, name, slug, description)
values (gen_random_uuid(), 'General', 'general', 'General board')
on conflict (slug) do nothing;
