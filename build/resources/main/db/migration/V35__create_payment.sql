-- 결제(Payment) 테이블 생성
CREATE TABLE IF NOT EXISTS rev.payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL UNIQUE,
    amount INTEGER NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_id VARCHAR(255),
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_payment_ticket FOREIGN KEY (ticket_id) REFERENCES rev.ticket(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_payment_ticket_id ON rev.payment(ticket_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON rev.payment(status);

