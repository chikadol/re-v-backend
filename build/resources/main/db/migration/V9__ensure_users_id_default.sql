-- UUID 생성 함수 확장 보장
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- users.id 컬럼에 기본값 설정 (이미 있으면 그대로 유지)
ALTER TABLE IF EXISTS rev.users
    ALTER COLUMN id SET DEFAULT gen_random_uuid();
