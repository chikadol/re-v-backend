-- 99_cleanup.sql
-- Robust cleanup for tests: avoid TRUNCATE ... IF EXISTS (not supported on some PG versions)
-- Simply drop and recreate the schema. No DO $$ blocks (Spring ScriptUtils-safe).

DROP SCHEMA IF EXISTS rev CASCADE;
CREATE SCHEMA IF NOT EXISTS rev;
