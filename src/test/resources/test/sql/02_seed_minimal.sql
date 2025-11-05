-- 포터블 시드 (PG/H2 둘 다 동작)
CREATE SCHEMA IF NOT EXISTS rev;

-- users 테이블이 없으면 생성 (기본키/유니크 포함)
CREATE TABLE IF NOT EXISTS rev.users (
                                         id       UUID PRIMARY KEY,
                                         email    VARCHAR(255) NOT NULL,
                                         username VARCHAR(255) NOT NULL,
                                         password VARCHAR(255) NOT NULL
);

-- email 유니크 인덱스 (H2/PG 모두 동작)
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON rev.users(email);

-- 시드: 이미 있으면 건너뜀 (ON CONFLICT 대신 WHERE NOT EXISTS)
INSERT INTO rev.users (id, email, username, password)
SELECT '11111111-1111-1111-1111-111111111111', 'e@example.com', 'u', '{noop}pw'
WHERE NOT EXISTS (
    SELECT 1 FROM rev.users WHERE email = 'e@example.com'
);
