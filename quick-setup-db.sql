-- 빠른 데이터베이스 설정 스크립트
-- PostgreSQL에 postgres 사용자로 접속하여 실행하세요: psql -U postgres -f quick-setup-db.sql

-- 기존 사용자/데이터베이스가 있으면 삭제 (선택사항)
-- DROP DATABASE IF EXISTS rev_db;
-- DROP USER IF EXISTS rev_user;

-- 데이터베이스 생성
CREATE DATABASE rev_db;

-- 사용자 생성
CREATE USER rev_user WITH PASSWORD 'rev_password';

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE rev_db TO rev_user;

-- 데이터베이스에 연결하여 스키마 생성
\c rev_db

-- 스키마 생성
CREATE SCHEMA IF NOT EXISTS rev;

-- 스키마 권한 부여
GRANT ALL ON SCHEMA rev TO rev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA rev GRANT ALL ON TABLES TO rev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA rev GRANT ALL ON SEQUENCES TO rev_user;

-- 연결 테스트
\c rev_db rev_user

SELECT 'Database setup completed successfully!' AS status;
