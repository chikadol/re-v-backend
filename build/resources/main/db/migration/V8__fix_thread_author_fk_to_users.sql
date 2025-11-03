-- V8__fix_thread_author_fk_to_users
-- 1) 기존 FK 제거
ALTER TABLE rev.thread DROP CONSTRAINT IF EXISTS thread_author_id_fkey;

-- 2) 혹시 테이블명이 아직 "user" 인 경우, users 로 일괄 개명
DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.tables
            WHERE table_schema = 'rev' AND table_name = 'user'
        )
        THEN
            -- "user" -> users 로 rename (예약어/따옴표 제거 효과)
            ALTER TABLE rev."user" RENAME TO users;
        END IF;
    END $$;

-- 3) 올바른 FK 재생성 (users(id)을 참조)
ALTER TABLE rev.thread
    ADD CONSTRAINT thread_author_id_fkey
        FOREIGN KEY (author_id) REFERENCES rev.users(id) ON DELETE RESTRICT;
