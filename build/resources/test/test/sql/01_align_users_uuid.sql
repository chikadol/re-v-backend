-- 01_align_users_uuid.sql
-- 목적: rev.users.id 를 uuid 로 전환하고 기본값을 gen_random_uuid() 로 설정.
--       email 에 유니크 인덱스를 추가한다.
-- 주의: 기존 FK 의존성이 없다 가정(일반적으로 users.id 를 다른 곳에서 FK 로 쓰지 않는 테스트 스키마).
-- 실행 시점: seed 이전(예: 02_seed_minimal.sql 실행 전)

CREATE SCHEMA IF NOT EXISTS rev;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

-- 보조 uuid 컬럼 추가 후 값 채우기
ALTER TABLE rev.users
    ADD COLUMN IF NOT EXISTS id_uuid uuid;

UPDATE rev.users
SET id_uuid = COALESCE(id_uuid, gen_random_uuid());

-- 기존 PK/제약 방어적 제거
ALTER TABLE rev.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE rev.users DROP CONSTRAINT IF EXISTS pk_users;

-- 기존 id 제거 후 uuid 승격
ALTER TABLE rev.users DROP COLUMN IF EXISTS id;
ALTER TABLE rev.users RENAME COLUMN id_uuid TO id;

-- PK/DEFAULT 재구성
ALTER TABLE rev.users ALTER COLUMN id SET NOT NULL;
ALTER TABLE rev.users ADD PRIMARY KEY (id);
ALTER TABLE rev.users ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- ON CONFLICT (email)용 유니크 인덱스
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email
    ON rev.users(email);

COMMIT;
