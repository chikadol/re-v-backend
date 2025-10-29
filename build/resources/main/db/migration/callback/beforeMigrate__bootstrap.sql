-- 스키마 준비
CREATE SCHEMA IF NOT EXISTS rev;

-- 이 세션에서 기본 스키마를 rev로
SET search_path TO rev, public;

-- 필요한 확장 (없으면 건너뜀)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- (선택) 타임아웃 완화
SET statement_timeout = '60s';