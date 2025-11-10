CREATE TABLE IF NOT EXISTS rev.trending_thread (
                                                   day DATE NOT NULL,
                                                   thread_id UUID NOT NULL REFERENCES rev.thread(id) ON DELETE CASCADE,
                                                   score DOUBLE PRECISION NOT NULL,
                                                   PRIMARY KEY(day, thread_id)
);
CREATE INDEX IF NOT EXISTS ix_trending_score ON rev.trending_thread(day, score DESC);