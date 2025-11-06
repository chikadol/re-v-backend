SET search_path TO rev, public;

-- 유저 1명
INSERT INTO users (id, email, username, password)
VALUES ('11111111-1111-1111-1111-111111111111'::uuid, 'e@example.com', 'u', '{noop}pw')
ON CONFLICT (email) DO NOTHING;

-- 기본 보드 1개
INSERT INTO board (id, name, slug, description)
VALUES ('22222222-2222-2222-2222-222222222222'::uuid, 'General', 'general', 'General board')
ON CONFLICT (slug) DO NOTHING;
