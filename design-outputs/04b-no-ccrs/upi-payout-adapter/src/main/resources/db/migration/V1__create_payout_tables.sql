-- UPI Payout Adapter Database Schema
-- Version: 1.0.0

-- Payout table
CREATE TABLE IF NOT EXISTS eg_upi_payout (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    payout_number VARCHAR(128) NOT NULL UNIQUE,
    reference_id VARCHAR(128) NOT NULL,
    reference_type VARCHAR(64) DEFAULT 'DOG_REPORT',
    beneficiary_id VARCHAR(128) NOT NULL,
    beneficiary_type VARCHAR(64) DEFAULT 'INDIVIDUAL',
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(8) DEFAULT 'INR',
    mode VARCHAR(16) DEFAULT 'UPI',
    purpose VARCHAR(64) DEFAULT 'payout',
    narration VARCHAR(256),
    status VARCHAR(32) NOT NULL,
    provider_payout_id VARCHAR(128),
    utr VARCHAR(64),
    processed_at BIGINT,
    failure_reason TEXT,
    error_code VARCHAR(64),
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    additional_details JSONB,
    created_by VARCHAR(128),
    created_time BIGINT,
    last_modified_by VARCHAR(128),
    last_modified_time BIGINT
);

-- Fund account table
CREATE TABLE IF NOT EXISTS eg_upi_fund_account (
    id VARCHAR(64) PRIMARY KEY,
    payout_id VARCHAR(64) NOT NULL REFERENCES eg_upi_payout(id),
    account_type VARCHAR(32) DEFAULT 'vpa',
    vpa VARCHAR(256),
    bank_account_number VARCHAR(32),
    ifsc_code VARCHAR(16),
    beneficiary_name VARCHAR(256) NOT NULL,
    mobile_number VARCHAR(16),
    email VARCHAR(128),
    provider_fund_account_id VARCHAR(128),
    provider_contact_id VARCHAR(128),
    is_active BOOLEAN DEFAULT true,
    created_time BIGINT
);

-- Webhook event log
CREATE TABLE IF NOT EXISTS eg_upi_webhook_log (
    id VARCHAR(64) PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL,
    provider_payout_id VARCHAR(128),
    payload JSONB,
    signature VARCHAR(512),
    processed BOOLEAN DEFAULT false,
    processed_time BIGINT,
    error_message TEXT,
    created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000
);

-- Idempotency table
CREATE TABLE IF NOT EXISTS eg_upi_idempotency (
    idempotency_key VARCHAR(256) PRIMARY KEY,
    payout_id VARCHAR(64) NOT NULL,
    response JSONB,
    created_time BIGINT,
    expires_at BIGINT
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_payout_tenant_id ON eg_upi_payout(tenant_id);
CREATE INDEX IF NOT EXISTS idx_payout_reference_id ON eg_upi_payout(reference_id);
CREATE INDEX IF NOT EXISTS idx_payout_beneficiary_id ON eg_upi_payout(beneficiary_id);
CREATE INDEX IF NOT EXISTS idx_payout_status ON eg_upi_payout(status);
CREATE INDEX IF NOT EXISTS idx_payout_provider_id ON eg_upi_payout(provider_payout_id);
CREATE INDEX IF NOT EXISTS idx_payout_created_time ON eg_upi_payout(created_time);
CREATE INDEX IF NOT EXISTS idx_payout_number ON eg_upi_payout(payout_number);

CREATE INDEX IF NOT EXISTS idx_fund_account_payout ON eg_upi_fund_account(payout_id);
CREATE INDEX IF NOT EXISTS idx_fund_account_vpa ON eg_upi_fund_account(vpa);

CREATE INDEX IF NOT EXISTS idx_webhook_provider_id ON eg_upi_webhook_log(provider_payout_id);
CREATE INDEX IF NOT EXISTS idx_webhook_processed ON eg_upi_webhook_log(processed);

-- Sequence for payout number
CREATE SEQUENCE IF NOT EXISTS seq_sdcrs_upi_payout START 1;

-- Comments
COMMENT ON TABLE eg_upi_payout IS 'UPI payouts for SDCRS teacher disbursements';
COMMENT ON TABLE eg_upi_fund_account IS 'Fund account (VPA/Bank) details for payouts';
COMMENT ON TABLE eg_upi_webhook_log IS 'Log of incoming webhook events from payment provider';
COMMENT ON TABLE eg_upi_idempotency IS 'Idempotency keys to prevent duplicate payouts';
