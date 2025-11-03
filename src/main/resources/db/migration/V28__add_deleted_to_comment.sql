-- V27__normalize_timestamps.sql
-- 표준 타임스탬프 컬럼 정규화: created_at / updated_at
-- createdAt / updatedAt / createdat / updatedat 정리 (idempotent)

SET search_path TO rev, public;

DO $$
    DECLARE
        t TEXT;
        tbls TEXT[] := ARRAY['thread', 'thread_reaction', 'thread_bookmark', 'thread_tag'];
    BEGIN
        FOREACH t IN ARRAY tbls LOOP
            -- ---------- created ----------
            -- "createdAt" -> created_at
                IF EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_schema='rev' AND table_name=t AND column_name='createdAt'
                ) THEN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='rev' AND table_name=t AND column_name='created_at'
                    ) THEN
                        EXECUTE format('ALTER TABLE rev.%I DROP COLUMN "createdAt"', t);
                    ELSE
                        EXECUTE format('ALTER TABLE rev.%I RENAME COLUMN "createdAt" TO created_at', t);
                    END IF;
                END IF;

                -- createdat -> created_at (따옴표 없이 잘못된 케이스)
                IF EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_schema='rev' AND table_name=t AND column_name='createdat'
                ) THEN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='rev' AND table_name=t AND column_name='created_at'
                    ) THEN
                        EXECUTE format('ALTER TABLE rev.%I DROP COLUMN createdat', t);
                    ELSE
                        EXECUTE format('ALTER TABLE rev.%I RENAME COLUMN createdat TO created_at', t);
                    END IF;
                END IF;

                -- created_at 없으면 추가
                IF NOT EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_schema='rev' AND table_name=t AND column_name='created_at'
                ) THEN
                    EXECUTE format('ALTER TABLE rev.%I ADD COLUMN created_at timestamptz', t);
                END IF;

                -- ---------- updated ----------
                -- "updatedAt" -> updated_at
                IF EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_schema='rev' AND table_name=t AND column_name='updatedAt'
                ) THEN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='rev' AND table_name=t AND column_name='updated_at'
                    ) THEN
                        EXECUTE format('ALTER TABLE rev.%I DROP COLUMN "updatedAt"', t);
                    ELSE
                        EXECUTE format('ALTER TABLE rev.%I RENAME COLUMN "updatedAt" TO updated_at', t);
                    END IF;
                END IF;

                -- updatedat -> updated_at
                IF EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_schema='rev' AND table_name=t AND column_name='updatedat'
                ) THEN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema='rev' AND table_name=t AND column_name='updated_at'
                    ) THEN
                        EXECUTE format('ALTER TABLE rev.%I DROP COLUMN updatedat', t);
                    ELSE
                        EXECUTE format('ALTER TABLE rev.%I RENAME COLUMN updatedat TO updated_at', t);
                    END IF;
                END IF;

                -- updated_at 없으면 추가
                IF NOT EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_schema='rev' AND table_name=t AND column_name='updated_at'
                ) THEN
                    EXECUTE format('ALTER TABLE rev.%I ADD COLUMN updated_at timestamptz', t);
                END IF;

            END LOOP;
    END
$$;
