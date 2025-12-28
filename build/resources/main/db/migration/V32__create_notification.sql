CREATE TABLE IF NOT EXISTS rev.notification (
                                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                receiver_id UUID NOT NULL REFERENCES rev.users(id) ON DELETE CASCADE,
                                                type VARCHAR(30) NOT NULL, -- COMMENT_REPLY, THREAD_REPLY 등
                                                thread_id UUID NULL REFERENCES rev.thread(id) ON DELETE CASCADE,
                                                comment_id UUID NULL REFERENCES rev.comment(id) ON DELETE CASCADE,
                                                message TEXT NOT NULL,
                                                is_read BOOLEAN NOT NULL DEFAULT FALSE,
                                                created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_notification_user ON rev.notification(receiver_id, is_read);