CREATE TABLE IF NOT EXISTS rev.thread_reaction (
                                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                   thread_id UUID NOT NULL REFERENCES rev.thread(id) ON DELETE CASCADE,
                                                   user_id UUID NOT NULL REFERENCES rev.users(id) ON DELETE CASCADE,
                                                   type VARCHAR(20) NOT NULL, -- 'LIKE','LOVE' 등
                                                   created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                   UNIQUE(thread_id, user_id, type)
);

CREATE INDEX IF NOT EXISTS ix_reaction_thread ON rev.thread_reaction(thread_id);
CREATE INDEX IF NOT EXISTS ix_reaction_user ON rev.thread_reaction(user_id);