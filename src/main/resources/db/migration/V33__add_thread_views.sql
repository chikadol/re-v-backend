ALTER TABLE rev.thread ADD COLUMN IF NOT EXISTS view_count BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS ix_thread_views ON rev.thread(view_count);