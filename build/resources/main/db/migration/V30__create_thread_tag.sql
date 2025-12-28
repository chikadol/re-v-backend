-- enable extension if not exists (for gen_random_uuid)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- tag 테이블 생성
CREATE TABLE IF NOT EXISTS rev.tag (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       name VARCHAR(50) NOT NULL UNIQUE
);

-- 기존 thread_tag 테이블이 tag (text) 컬럼을 사용하는 경우를 처리
DO $$
BEGIN
    -- 기존 thread_tag 테이블이 있고 tag_id 컬럼이 없으면 테이블 재생성
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'rev' AND table_name = 'thread_tag'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'rev' 
        AND table_name = 'thread_tag' 
        AND column_name = 'tag_id'
    ) THEN
        -- 기존 테이블 삭제 (데이터 마이그레이션이 필요한 경우 별도 처리 필요)
        DROP TABLE IF EXISTS rev.thread_tag CASCADE;
    END IF;
END $$;

-- thread_tag 테이블 생성 (tag_id 사용)
CREATE TABLE IF NOT EXISTS rev.thread_tag (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                              thread_id UUID NOT NULL REFERENCES rev.thread(id) ON DELETE CASCADE,
                                              tag_id UUID NOT NULL REFERENCES rev.tag(id) ON DELETE CASCADE,
                                              UNIQUE(thread_id, tag_id)
);

-- 인덱스 생성 (테이블이 존재하는 경우에만)
CREATE INDEX IF NOT EXISTS ix_thread_tag_thread ON rev.thread_tag(thread_id);
CREATE INDEX IF NOT EXISTS ix_thread_tag_tag ON rev.thread_tag(tag_id);