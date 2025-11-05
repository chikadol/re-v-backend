-- 1) user 한 줄
INSERT INTO rev."user"(id, username, password, email)
VALUES ('00000000-0000-0000-0000-000000000001', 'u', 'p', 'e@example.com')
ON CONFLICT DO NOTHING;

-- 2) board 한 줄
INSERT INTO rev.board(name, slug, description)
VALUES ('b', 'b', 'desc')
ON CONFLICT DO NOTHING;
