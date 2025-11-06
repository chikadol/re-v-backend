-- V2__community_nonpoly.sql (안전/멱등 버전)

CREATE SCHEMA IF NOT EXISTS rev;

-- 1) created_at 없으면 추가 후 백필
ALTER TABLE IF EXISTS rev.board
    ADD COLUMN IF NOT EXISTS created_at timestamptz;

UPDATE rev.board
SET created_at = COALESCE(created_at, now())
WHERE created_at IS NULL;

ALTER TABLE IF EXISTS rev.board
    ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE IF EXISTS rev.board
    ALTER COLUMN created_at SET DEFAULT now();

-- 2) display_no 없으면 추가
ALTER TABLE IF EXISTS rev.board
    ADD COLUMN IF NOT EXISTS display_no integer;

-- 3) display_no 초기화 (created_at 있는 상태이므로 안전)
WITH numbered AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at NULLS LAST, id) AS rn
    FROM rev.board
)
UPDATE rev.board b
SET display_no = n.rn
FROM numbered n
WHERE b.id = n.id
  AND b.display_no IS NULL;

-- 필요하면 NOT NULL/DEFAULT 정책 추가
-- ALTER TABLE rev.board ALTER COLUMN display_no SET NOT NULL;
-- ALTER TABLE rev.board ALTER COLUMN display_no SET DEFAULT 0;
