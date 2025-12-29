#!/bin/bash

echo "PostgreSQL 데이터베이스 설정을 시작합니다..."

# PostgreSQL에 접속 시도
psql -U postgres <<EOF
-- 데이터베이스가 이미 존재하는지 확인
SELECT 'Database rev_db exists' WHERE EXISTS (SELECT FROM pg_database WHERE datname = 'rev_db');

-- 데이터베이스 생성 (이미 존재하면 무시)
CREATE DATABASE rev_db;

-- 사용자가 이미 존재하는지 확인
SELECT 'User rev_user exists' WHERE EXISTS (SELECT FROM pg_user WHERE usename = 'rev_user');

-- 사용자 생성 (이미 존재하면 무시)
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'rev_user') THEN
        CREATE USER rev_user WITH PASSWORD 'rev_password';
    ELSE
        ALTER USER rev_user WITH PASSWORD 'rev_password';
    END IF;
END
\$\$;

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

\q
EOF

if [ $? -eq 0 ]; then
    echo "✅ 데이터베이스 설정이 완료되었습니다!"
    echo ""
    echo "연결 테스트:"
    echo "psql -U rev_user -d rev_db -h localhost"
    echo ""
    echo "비밀번호: rev_password"
else
    echo "❌ 데이터베이스 설정에 실패했습니다."
    echo ""
    echo "다음 방법을 시도해보세요:"
    echo "1. sudo -u postgres psql 로 직접 접속하여 위의 SQL 명령을 실행"
    echo "2. 또는 기존 데이터베이스의 자격 증명을 사용"
fi
