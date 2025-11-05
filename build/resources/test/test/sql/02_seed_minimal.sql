-- 02_seed_minimal.sql
-- Pure SQL seed; assumes tables exist. No DO $$ blocks.

CREATE SCHEMA IF NOT EXISTS rev;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Make sure UUID defaults exist (harmless if already set)
ALTER TABLE rev.users ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE rev.board ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE rev.thread ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- Seed one user
INSERT INTO rev.users (email, username, password)
VALUES ('e@example.com', 'u', '{noop}pw')
ON CONFLICT (email) DO NOTHING;

-- Seed one board
INSERT INTO rev.board (id, name, slug, description)
VALUES (gen_random_uuid(), 'General', 'general', 'default board')
ON CONFLICT (slug) DO NOTHING;
