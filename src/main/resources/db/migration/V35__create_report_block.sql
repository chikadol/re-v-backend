CREATE TABLE IF NOT EXISTS rev.report (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          reporter_id UUID NOT NULL REFERENCES rev.users(id) ON DELETE CASCADE,
                                          target_type VARCHAR(20) NOT NULL, -- THREAD/COMMENT/USER
                                          target_id UUID NOT NULL,
                                          reason TEXT NOT NULL,
                                          status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN/RESOLVED/REJECTED
                                          created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS rev.user_block (
                                              blocker_id UUID NOT NULL REFERENCES rev.users(id) ON DELETE CASCADE,
                                              blocked_id UUID NOT NULL REFERENCES rev.users(id) ON DELETE CASCADE,
                                              PRIMARY KEY(blocker_id, blocked_id)
);