-- 현재 유저가 postgres.<...> 라면 필요한 범위만 GRANT
-- 서비스 계정 외 계정을 쓸 때 대비한 예시
DO $$
BEGIN
  -- rev 스키마 사용 권한
  EXECUTE 'GRANT USAGE ON SCHEMA rev TO ' || current_user;

  -- 기존 테이블 권한
EXECUTE 'GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA rev TO ' || current_user;

  -- 앞으로 생성될 테이블에 대한 기본 권한
EXECUTE 'ALTER DEFAULT PRIVILEGES IN SCHEMA rev GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO ' || current_user;

  -- 시퀀스 권한도 필요하면
EXECUTE 'GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA rev TO ' || current_user;
EXECUTE 'ALTER DEFAULT PRIVILEGES IN SCHEMA rev GRANT USAGE, SELECT ON SEQUENCES TO ' || current_user;
END $$;