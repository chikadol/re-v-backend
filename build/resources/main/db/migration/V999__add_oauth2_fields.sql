-- OAuth2 소셜 로그인을 위한 필드 추가
ALTER TABLE rev.users 
ADD COLUMN IF NOT EXISTS provider VARCHAR(50),
ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255);

-- Provider와 Provider ID로 조회를 위한 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_users_provider_provider_id ON rev.users(provider, provider_id);

-- Provider ID는 unique (같은 provider 내에서)
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_provider_id_unique ON rev.users(provider, provider_id) WHERE provider IS NOT NULL;
