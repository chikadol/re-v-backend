-- 공연(Performance) 테이블 생성
CREATE TABLE IF NOT EXISTS rev.performance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    venue VARCHAR(255) NOT NULL,
    performance_date_time TIMESTAMPTZ NOT NULL,
    price INTEGER NOT NULL,
    total_seats INTEGER NOT NULL,
    remaining_seats INTEGER NOT NULL,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_performance_status ON rev.performance(status);
CREATE INDEX IF NOT EXISTS idx_performance_date_time ON rev.performance(performance_date_time);

