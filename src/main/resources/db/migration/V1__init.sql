-- V1__init.sql — schema & tables for 1차 (auth/artist/genba)
CREATE SCHEMA IF NOT EXISTS rev;

-- Users
CREATE TABLE IF NOT EXISTS rev."user" (
  id BIGSERIAL PRIMARY KEY,
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  status TEXT DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS rev.user_profile (
  user_id BIGINT PRIMARY KEY REFERENCES rev."user"(id) ON DELETE CASCADE,
  nickname TEXT,
  avatar_url TEXT
);

CREATE TABLE IF NOT EXISTS rev.user_session (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES rev."user"(id) ON DELETE CASCADE,
  refresh_token_hash TEXT NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- Artist
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE TABLE IF NOT EXISTS rev.artist (
  id BIGSERIAL PRIMARY KEY,
  stage_name TEXT NOT NULL,
  stage_name_kr TEXT,
  group_name TEXT,
  tags TEXT[],
  debut_date DATE,
  avatar_url TEXT,
  popularity_score INT DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_artist_stage_trgm ON rev.artist USING gin (stage_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_artist_stage_kr_trgm ON rev.artist USING gin (stage_name_kr gin_trgm_ops);

CREATE TABLE IF NOT EXISTS rev.artist_alias (
  id BIGSERIAL PRIMARY KEY,
  artist_id BIGINT NOT NULL REFERENCES rev.artist(id) ON DELETE CASCADE,
  alias TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_artist_alias_trgm ON rev.artist_alias USING gin (alias gin_trgm_ops);

-- Genba
CREATE TABLE IF NOT EXISTS rev.genba (
  id BIGSERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT,
  start_at TIMESTAMPTZ NOT NULL,
  end_at TIMESTAMPTZ,
  area_code TEXT,
  place_name TEXT,
  address TEXT,
  poster_url TEXT,
  popularity_score INT DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_genba_time ON rev.genba (start_at DESC);
CREATE INDEX IF NOT EXISTS idx_genba_area ON rev.genba (area_code);

-- N:M
CREATE TABLE IF NOT EXISTS rev.genba_artist (
  genba_id BIGINT NOT NULL REFERENCES rev.genba(id) ON DELETE CASCADE,
  artist_id BIGINT NOT NULL REFERENCES rev.artist(id) ON DELETE CASCADE,
  PRIMARY KEY(genba_id, artist_id)
);
