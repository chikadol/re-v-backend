-- 티켓(Ticket) 테이블 생성
CREATE TABLE IF NOT EXISTS rev.ticket (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    performance_id UUID NOT NULL,
    user_id UUID NOT NULL,
    price INTEGER NOT NULL,
    seat_number VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    purchase_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_ticket_performance FOREIGN KEY (performance_id) REFERENCES rev.performance(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_user FOREIGN KEY (user_id) REFERENCES rev.users(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_ticket_user_id ON rev.ticket(user_id);
CREATE INDEX IF NOT EXISTS idx_ticket_performance_id ON rev.ticket(performance_id);
CREATE INDEX IF NOT EXISTS idx_ticket_status ON rev.ticket(status);

