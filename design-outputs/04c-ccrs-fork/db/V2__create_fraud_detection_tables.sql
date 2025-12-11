-- Fraud Detection Service Database Schema
-- Version: 1.0
-- Compatible with DIGIT Platform

-- Fraud check requests and results
CREATE TABLE IF NOT EXISTS eg_fraud_check (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    module_code VARCHAR(32) NOT NULL,
    business_service VARCHAR(64),
    application_id VARCHAR(128) NOT NULL,
    applicant_id VARCHAR(64) NOT NULL,
    applicant_type VARCHAR(32),
    check_type VARCHAR(16) NOT NULL,  -- QUICK, FULL, DEEP
    priority VARCHAR(16) DEFAULT 'NORMAL',
    status VARCHAR(32) NOT NULL,  -- PENDING, PROCESSING, COMPLETED, FAILED
    overall_score INTEGER,
    risk_level VARCHAR(16),
    flag_count INTEGER DEFAULT 0,
    recommendation VARCHAR(32),
    processing_time_ms INTEGER,
    rules_evaluated INTEGER,
    rules_passed INTEGER,
    rules_failed INTEGER,
    image_hash VARCHAR(128),
    content_hash VARCHAR(128),
    additional_data JSONB,
    created_by VARCHAR(64) NOT NULL,
    created_time BIGINT NOT NULL,
    last_modified_by VARCHAR(64),
    last_modified_time BIGINT
);

CREATE INDEX IF NOT EXISTS idx_fraud_check_tenant ON eg_fraud_check(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fraud_check_module ON eg_fraud_check(module_code);
CREATE INDEX IF NOT EXISTS idx_fraud_check_application ON eg_fraud_check(application_id);
CREATE INDEX IF NOT EXISTS idx_fraud_check_applicant ON eg_fraud_check(applicant_id);
CREATE INDEX IF NOT EXISTS idx_fraud_check_status ON eg_fraud_check(status);
CREATE INDEX IF NOT EXISTS idx_fraud_check_created ON eg_fraud_check(created_time);
CREATE INDEX IF NOT EXISTS idx_fraud_check_risk ON eg_fraud_check(risk_level);

-- Fraud flags raised during checks
CREATE TABLE IF NOT EXISTS eg_fraud_flag (
    id VARCHAR(64) PRIMARY KEY,
    fraud_check_id VARCHAR(64) NOT NULL REFERENCES eg_fraud_check(id),
    tenant_id VARCHAR(64) NOT NULL,
    module_code VARCHAR(32) NOT NULL,
    application_id VARCHAR(128) NOT NULL,
    applicant_id VARCHAR(64) NOT NULL,
    rule_id VARCHAR(32) NOT NULL,
    rule_code VARCHAR(64) NOT NULL,
    category VARCHAR(8) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL,  -- OPEN, UNDER_REVIEW, RESOLVED, DISMISSED, ESCALATED
    score INTEGER,
    details JSONB,
    recommended_action VARCHAR(32),
    auto_action VARCHAR(32),
    resolution VARCHAR(32),
    resolution_reason TEXT,
    resolver_id VARCHAR(64),
    resolved_time BIGINT,
    escalated_to VARCHAR(64),
    escalated_time BIGINT,
    linked_flag_ids TEXT,
    linked_application_ids TEXT,
    created_by VARCHAR(64) NOT NULL,
    created_time BIGINT NOT NULL,
    last_modified_by VARCHAR(64),
    last_modified_time BIGINT
);

CREATE INDEX IF NOT EXISTS idx_fraud_flag_tenant ON eg_fraud_flag(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fraud_flag_module ON eg_fraud_flag(module_code);
CREATE INDEX IF NOT EXISTS idx_fraud_flag_application ON eg_fraud_flag(application_id);
CREATE INDEX IF NOT EXISTS idx_fraud_flag_applicant ON eg_fraud_flag(applicant_id);
CREATE INDEX IF NOT EXISTS idx_fraud_flag_status ON eg_fraud_flag(status);
CREATE INDEX IF NOT EXISTS idx_fraud_flag_severity ON eg_fraud_flag(severity);
CREATE INDEX IF NOT EXISTS idx_fraud_flag_category ON eg_fraud_flag(category);
CREATE INDEX IF NOT EXISTS idx_fraud_flag_created ON eg_fraud_flag(created_time);
CREATE INDEX IF NOT EXISTS idx_fraud_flag_rule ON eg_fraud_flag(rule_id);
CREATE INDEX IF NOT EXISTS idx_fraud_flag_check ON eg_fraud_flag(fraud_check_id);

-- Applicant risk profiles
CREATE TABLE IF NOT EXISTS eg_applicant_risk_profile (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    applicant_id VARCHAR(64) NOT NULL,
    risk_score INTEGER DEFAULT 0,
    risk_level VARCHAR(16) DEFAULT 'LOW',
    total_applications INTEGER DEFAULT 0,
    flagged_applications INTEGER DEFAULT 0,
    confirmed_frauds INTEGER DEFAULT 0,
    false_positives INTEGER DEFAULT 0,
    penalty_level INTEGER DEFAULT 0,
    penalty_status VARCHAR(32),
    penalty_reason TEXT,
    penalty_start_time BIGINT,
    penalty_expiry_time BIGINT,
    last_application_time BIGINT,
    last_fraud_time BIGINT,
    last_review_time BIGINT,
    reviewed_by VARCHAR(64),
    additional_data JSONB,
    created_time BIGINT NOT NULL,
    last_modified_time BIGINT,
    CONSTRAINT uk_risk_profile_applicant UNIQUE (tenant_id, applicant_id)
);

CREATE INDEX IF NOT EXISTS idx_risk_profile_tenant ON eg_applicant_risk_profile(tenant_id);
CREATE INDEX IF NOT EXISTS idx_risk_profile_applicant ON eg_applicant_risk_profile(applicant_id);
CREATE INDEX IF NOT EXISTS idx_risk_profile_score ON eg_applicant_risk_profile(risk_score);
CREATE INDEX IF NOT EXISTS idx_risk_profile_level ON eg_applicant_risk_profile(risk_level);
CREATE INDEX IF NOT EXISTS idx_risk_profile_penalty ON eg_applicant_risk_profile(penalty_level);

-- Image hashes for duplicate detection
CREATE TABLE IF NOT EXISTS eg_image_hash (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    module_code VARCHAR(32) NOT NULL,
    application_id VARCHAR(128) NOT NULL,
    applicant_id VARCHAR(64) NOT NULL,
    file_store_id VARCHAR(64) NOT NULL,
    purpose VARCHAR(32),  -- DOG_PHOTO, SELFIE, EVIDENCE, CAPTURE_PHOTO
    hash_algorithm VARCHAR(16) NOT NULL,  -- pHash, dHash, aHash, MD5, SHA256
    hash_value VARCHAR(256) NOT NULL,
    hash_binary BYTEA,  -- Binary hash for efficient comparison
    image_width INTEGER,
    image_height INTEGER,
    file_size_bytes BIGINT,
    created_time BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_image_hash_tenant ON eg_image_hash(tenant_id);
CREATE INDEX IF NOT EXISTS idx_image_hash_module ON eg_image_hash(module_code);
CREATE INDEX IF NOT EXISTS idx_image_hash_application ON eg_image_hash(application_id);
CREATE INDEX IF NOT EXISTS idx_image_hash_applicant ON eg_image_hash(applicant_id);
CREATE INDEX IF NOT EXISTS idx_image_hash_value ON eg_image_hash(hash_value);
CREATE INDEX IF NOT EXISTS idx_image_hash_algorithm ON eg_image_hash(hash_algorithm);
CREATE INDEX IF NOT EXISTS idx_image_hash_created ON eg_image_hash(created_time);
CREATE INDEX IF NOT EXISTS idx_image_hash_purpose ON eg_image_hash(purpose);

-- Fraud detection audit log
CREATE TABLE IF NOT EXISTS eg_fraud_audit_log (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    entity_type VARCHAR(32) NOT NULL,  -- FRAUD_CHECK, FRAUD_FLAG, RISK_PROFILE
    entity_id VARCHAR(64) NOT NULL,
    action VARCHAR(32) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    performed_by VARCHAR(64) NOT NULL,
    performed_time BIGINT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    notes TEXT
);

CREATE INDEX IF NOT EXISTS idx_fraud_audit_tenant ON eg_fraud_audit_log(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fraud_audit_entity ON eg_fraud_audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_fraud_audit_time ON eg_fraud_audit_log(performed_time);
CREATE INDEX IF NOT EXISTS idx_fraud_audit_action ON eg_fraud_audit_log(action);

-- Device fingerprint tracking for collusion detection
CREATE TABLE IF NOT EXISTS eg_device_fingerprint (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    device_id VARCHAR(128) NOT NULL,
    applicant_id VARCHAR(64) NOT NULL,
    first_seen_time BIGINT NOT NULL,
    last_seen_time BIGINT NOT NULL,
    submission_count INTEGER DEFAULT 1,
    device_info JSONB,
    flagged BOOLEAN DEFAULT FALSE,
    flag_reason TEXT
);

CREATE INDEX IF NOT EXISTS idx_device_fp_tenant ON eg_device_fingerprint(tenant_id);
CREATE INDEX IF NOT EXISTS idx_device_fp_device ON eg_device_fingerprint(device_id);
CREATE INDEX IF NOT EXISTS idx_device_fp_applicant ON eg_device_fingerprint(applicant_id);
CREATE INDEX IF NOT EXISTS idx_device_fp_flagged ON eg_device_fingerprint(flagged);
CREATE UNIQUE INDEX IF NOT EXISTS idx_device_fp_unique ON eg_device_fingerprint(tenant_id, device_id, applicant_id);
