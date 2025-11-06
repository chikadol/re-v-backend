-- 최소 시드: users, board
INSERT INTO rev.users (id, email, username, password)
VALUES (gen_random_uuid(), 'e@example.com', 'u', '{noop}pw')
ON CONFLICT (email) DO NOTHING;

INSERT INTO rev.board (id, name, slug, description)
VALUES (gen_random_uuid(), 'General', 'general', 'Default board')
ON CONFLICT (slug) DO NOTHING;
