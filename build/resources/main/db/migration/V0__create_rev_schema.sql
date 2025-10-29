-- 스키마 생성 (여러 번 실행돼도 안전)
CREATE SCHEMA IF NOT EXISTS rev;

-- 현재 세션 search_path 보정(마이그 실행 시점 보장용)
SET search_path TO rev, public;