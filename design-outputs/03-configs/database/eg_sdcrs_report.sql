-- SDCRS Dog Report Table
-- Database: PostgreSQL

CREATE TABLE eg_sdcrs_report (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    report_number VARCHAR(64) UNIQUE NOT NULL,
    tracking_id VARCHAR(8) UNIQUE NOT NULL,     -- Short ID for public tracking (e.g., ABC123)
    tracking_url VARCHAR(256),                   -- Shortened URL from URL Shortener service
    report_type VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,

    -- Reporter Details
    reporter_id VARCHAR(64) NOT NULL,
    reporter_name VARCHAR(256),
    reporter_phone VARCHAR(20),

    -- Location Details (Geographic Hierarchy)
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    address TEXT,
    locality_code VARCHAR(64),
    ward_code VARCHAR(64),                       -- Ward boundary code
    ward_name VARCHAR(256),                      -- Ward name for display
    district_code VARCHAR(64),                   -- District boundary code
    district_name VARCHAR(256),                  -- District name for display

    -- Dog Details
    dog_description TEXT,
    dog_count INTEGER DEFAULT 1,
    is_aggressive BOOLEAN DEFAULT FALSE,

    -- Evidence
    photo_file_store_id VARCHAR(64),
    selfie_file_store_id VARCHAR(64),
    image_hash VARCHAR(256),

    -- Verification Details
    verifier_id VARCHAR(64),                     -- User ID of verifier
    verifier_name VARCHAR(256),                  -- Name for display
    verified_time BIGINT,                        -- Timestamp of verification

    -- Assignment Details
    assigned_mc_id VARCHAR(64),
    assigned_mc_name VARCHAR(256),
    assigned_time BIGINT,                        -- Timestamp of MC assignment
    field_visit_start_time BIGINT,               -- Timestamp when MC started field visit

    -- Resolution
    resolution_type VARCHAR(64),
    resolution_notes TEXT,
    resolution_time BIGINT,

    -- Payout (references to Collection Service)
    payout_status VARCHAR(64),
    payout_amount DECIMAL(10, 2),                -- Actual payout amount
    payout_demand_id VARCHAR(64),
    payout_time BIGINT,                          -- Timestamp of payout completion

    -- SLA Tracking
    sla_breached BOOLEAN DEFAULT FALSE,          -- Whether report exceeded SLA
    sla_breach_reason VARCHAR(256),              -- Stage where SLA was breached

    -- Fraud Prevention
    fraud_flags JSONB,                           -- Array of fraud indicators detected
    penalty_type VARCHAR(64),                    -- Warning, cooldown, suspension, ban

    -- Rejection
    rejection_reason VARCHAR(64),
    rejection_notes TEXT,                        -- Optional notes from verifier

    -- Additional
    additional_details JSONB,

    -- Audit
    created_by VARCHAR(64) NOT NULL,
    created_time BIGINT NOT NULL,
    last_modified_by VARCHAR(64),
    last_modified_time BIGINT,

    -- Foreign Key
    CONSTRAINT fk_tenant FOREIGN KEY (tenant_id) REFERENCES eg_tenant(code)
);

-- Performance indexes
CREATE INDEX idx_sdcrs_report_tenant ON eg_sdcrs_report(tenant_id);
CREATE INDEX idx_sdcrs_report_status ON eg_sdcrs_report(status);
CREATE INDEX idx_sdcrs_report_reporter ON eg_sdcrs_report(reporter_id);
CREATE INDEX idx_sdcrs_report_mc ON eg_sdcrs_report(assigned_mc_id);
CREATE INDEX idx_sdcrs_report_verifier ON eg_sdcrs_report(verifier_id);
CREATE INDEX idx_sdcrs_report_locality ON eg_sdcrs_report(locality_code);
CREATE INDEX idx_sdcrs_report_ward ON eg_sdcrs_report(ward_code);
CREATE INDEX idx_sdcrs_report_district ON eg_sdcrs_report(district_code);
CREATE INDEX idx_sdcrs_report_created ON eg_sdcrs_report(created_time);
CREATE INDEX idx_sdcrs_report_image_hash ON eg_sdcrs_report(image_hash);
CREATE INDEX idx_sdcrs_report_tracking_id ON eg_sdcrs_report(tracking_id);
CREATE INDEX idx_sdcrs_report_sla_breach ON eg_sdcrs_report(sla_breached) WHERE sla_breached = TRUE;
CREATE INDEX idx_sdcrs_report_report_type ON eg_sdcrs_report(report_type);

-- Composite indexes for common query patterns
CREATE INDEX idx_sdcrs_report_tenant_status ON eg_sdcrs_report(tenant_id, status);
CREATE INDEX idx_sdcrs_report_district_status ON eg_sdcrs_report(district_code, status);
CREATE INDEX idx_sdcrs_report_created_status ON eg_sdcrs_report(created_time, status);
