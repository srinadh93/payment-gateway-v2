-- Refunds Table
CREATE TABLE refunds (
    id VARCHAR(64) PRIMARY KEY,
    payment_id VARCHAR(64) NOT NULL,
    merchant_id UUID NOT NULL,
    amount INTEGER NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id),
    FOREIGN KEY (merchant_id) REFERENCES merchants(id)
);

CREATE INDEX idx_refund_payment_id ON refunds(payment_id);

-- Webhook Logs Table
CREATE TABLE webhook_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL,
    event VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    attempts INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP,
    next_retry_at TIMESTAMP,
    response_code INTEGER,
    response_body TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id)
);

CREATE INDEX idx_webhook_merchant_id ON webhook_logs(merchant_id);
CREATE INDEX idx_webhook_status ON webhook_logs(status);
CREATE INDEX idx_webhook_retry_at ON webhook_logs(next_retry_at) WHERE status = 'pending';

-- Idempotency Keys Table
CREATE TABLE idempotency_keys (
    key VARCHAR(255) PRIMARY KEY,
    merchant_id UUID NOT NULL,
    response JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id)
);

-- Alter Merchants Table
ALTER TABLE merchants ADD COLUMN webhook_url VARCHAR(255);
ALTER TABLE merchants ADD COLUMN webhook_secret VARCHAR(64);

-- Alter Payments Table
ALTER TABLE payments ADD COLUMN merchant_id UUID;
ALTER TABLE payments ADD COLUMN order_id VARCHAR(64);
ALTER TABLE payments ADD COLUMN currency VARCHAR(10);
ALTER TABLE payments ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE payments ADD COLUMN captured BOOLEAN DEFAULT false;
