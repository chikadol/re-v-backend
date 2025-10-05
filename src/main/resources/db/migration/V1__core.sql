-- V1__core.sql
CREATE SCHEMA IF NOT EXISTS rev;

CREATE TABLE IF NOT EXISTS rev."user" (
  id BIGSERIAL PRIMARY KEY,
  handle TEXT UNIQUE,
  nickname TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);
