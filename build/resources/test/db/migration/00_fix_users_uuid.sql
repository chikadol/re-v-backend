-- 00_fix_users_uuid.sql
-- 목적: rev.users.id 를 UUID + 기본값(gen_random_uuid())로 강제 정렬하고
--      email 에 유니크 인덱스를 보장한다.
--      순수 SQL만 사용 (DO $$ 사용 안 함).

CREATE SCHEMA IF NOT EXISTS rev;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1) id 컬럼 존재 보장 (없으면 추가)
ALTER TABLE IF EXISTS rev.users
    ADD COLUMN IF NOT EXISTS id uuid;

-- 2) id 타입을 uuid로 강제 (기존 bigint 등은 새 UUID로 치환)
--    이미 uuid라면 그대로 유지됨.
ALTER TABLE IF EXISTS rev.users
    ALTER COLUMN id TYPE uuid USING
        CASE
            WHEN pg_typeof(id)::text = 'uuid' THEN id
            ELSE gen_random_uuid()
            END;

-- 3) null 채우기 후 NOT NULL + DEFAULT 설정
UPDATE rev.users SET id = COALESCE(id, gen_random_uuid());
ALTER TABLE IF EXISTS rev.users
    ALTER COLUMN id SET NOT NULL;
ALTER TABLE IF EXISTS rev.users
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- 4) PK 재구성(안전 드롭 후 재생성)
ALTER TABLE IF EXISTS rev.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS rev.users ADD PRIMARY KEY (id);

-- 5) email 유니크 인덱스 보장 (시드의 ON CONFLICT (email) 위해 필요)
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email ON rev.users(email);
