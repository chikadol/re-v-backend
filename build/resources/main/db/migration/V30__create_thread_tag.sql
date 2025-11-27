-- enable extension if not exists (for gen_random_uuid)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS rev.tag (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS rev.thread_tag (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                              thread_id UUID NOT NULL REFERENCES rev.thread(id) ON DELETE CASCADE,
                                              tag_id UUID NOT NULL REFERENCES rev.tag(id) ON DELETE CASCADE,
                                              UNIQUE(thread_id, tag_id)
);

CREATE INDEX IF NOT EXISTS ix_thread_tag_thread ON rev.thread_tag(thread_id);
CREATE INDEX IF NOT EXISTS ix_thread_tag_tag ON rev.thread_tag(tag_id);