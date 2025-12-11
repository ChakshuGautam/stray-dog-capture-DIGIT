-- ==============================================================================
-- SDCRS Database Schema
-- Stray Dog Capture & Reporting System
-- ==============================================================================

-- ==============================================================================
-- Main Dog Report Table
-- ==============================================================================
CREATE TABLE IF NOT EXISTS eg_sdcrs_report (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    report_number VARCHAR(64) NOT NULL UNIQUE,
    tracking_id VARCHAR(16) NOT NULL UNIQUE,
    tracking_url VARCHAR(256),

    -- Status & Workflow
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_VALIDATION',

    -- Reporter Information
    reporter_id VARCHAR(64),
    reporter_name VARCHAR(128),
    reporter_phone VARCHAR(16),
    reporter_type VARCHAR(32),  -- TEACHER, CITIZEN

    -- Dog Details
    dog_description TEXT,
    dog_behavior VARCHAR(32),   -- AGGRESSIVE, INJURED, NORMAL
    dog_count INTEGER DEFAULT 1,
    image_hash VARCHAR(64),     -- pHash for duplicate detection

    -- Assignment (MC Officer)
    assigned_officer_id VARCHAR(64),
    assigned_officer_name VARCHAR(128),

    -- Verification
    verified_by VARCHAR(64),
    verified_date BIGINT,
    verification_remarks TEXT,

    -- Resolution
    resolved_by VARCHAR(64),
    resolved_date BIGINT,
    resolution_type VARCHAR(32),    -- CAPTURED, UNABLE_TO_LOCATE
    resolution_remarks TEXT,

    -- Rejection
    rejection_reason VARCHAR(64),
    rejection_remarks TEXT,

    -- Payout
    payout_status VARCHAR(32),      -- PENDING, PROCESSED, COMPLETED, CAPPED
    payout_amount NUMERIC(10,2),
    payout_reference VARCHAR(64),   -- Demand ID from billing service

    -- Audit
    created_by VARCHAR(64),
    created_time BIGINT NOT NULL,
    last_modified_by VARCHAR(64),
    last_modified_time BIGINT,

    -- Constraints
    CONSTRAINT fk_tenant CHECK (tenant_id IS NOT NULL)
);

-- Indexes for common queries
CREATE INDEX idx_sdcrs_report_tenant ON eg_sdcrs_report(tenant_id);
CREATE INDEX idx_sdcrs_report_status ON eg_sdcrs_report(status);
CREATE INDEX idx_sdcrs_report_reporter ON eg_sdcrs_report(reporter_id);
CREATE INDEX idx_sdcrs_report_assigned ON eg_sdcrs_report(assigned_officer_id);
CREATE INDEX idx_sdcrs_report_created ON eg_sdcrs_report(created_time);
CREATE INDEX idx_sdcrs_report_tracking ON eg_sdcrs_report(tracking_id);
CREATE INDEX idx_sdcrs_report_image_hash ON eg_sdcrs_report(image_hash);

-- ==============================================================================
-- Location Table
-- ==============================================================================
CREATE TABLE IF NOT EXISTS eg_sdcrs_location (
    id VARCHAR(64) PRIMARY KEY,
    report_id VARCHAR(64) NOT NULL REFERENCES eg_sdcrs_report(id),

    -- GPS Coordinates
    latitude NUMERIC(10,7) NOT NULL,
    longitude NUMERIC(10,7) NOT NULL,

    -- Address Details
    address TEXT,
    landmark VARCHAR(256),

    -- Administrative Boundaries
    locality_code VARCHAR(64),
    locality_name VARCHAR(128),
    ward_code VARCHAR(64),
    ward_name VARCHAR(128),
    district_code VARCHAR(64),
    district_name VARCHAR(128),
    pincode VARCHAR(10),

    -- Validation
    gps_accuracy NUMERIC(6,2),
    gps_validated BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_sdcrs_location_report ON eg_sdcrs_location(report_id);
CREATE INDEX idx_sdcrs_location_coords ON eg_sdcrs_location(latitude, longitude);
CREATE INDEX idx_sdcrs_location_locality ON eg_sdcrs_location(locality_code);
CREATE INDEX idx_sdcrs_location_ward ON eg_sdcrs_location(ward_code);

-- ==============================================================================
-- Evidence Table (Photos)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS eg_sdcrs_evidence (
    id VARCHAR(64) PRIMARY KEY,
    report_id VARCHAR(64) NOT NULL REFERENCES eg_sdcrs_report(id),

    -- File Store Reference
    file_store_id VARCHAR(64) NOT NULL,

    -- Evidence Type
    evidence_type VARCHAR(32) NOT NULL,  -- DOG_PHOTO, REPORTER_SELFIE, CAPTURE_PHOTO

    -- EXIF Metadata (extracted from photo)
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    capture_time BIGINT,
    file_hash VARCHAR(64),

    -- Audit
    created_time BIGINT NOT NULL
);

CREATE INDEX idx_sdcrs_evidence_report ON eg_sdcrs_evidence(report_id);
CREATE INDEX idx_sdcrs_evidence_type ON eg_sdcrs_evidence(evidence_type);
CREATE INDEX idx_sdcrs_evidence_hash ON eg_sdcrs_evidence(file_hash);

-- ==============================================================================
-- Payout Tracking Table
-- ==============================================================================
CREATE TABLE IF NOT EXISTS eg_sdcrs_payout (
    id VARCHAR(64) PRIMARY KEY,
    report_id VARCHAR(64) NOT NULL REFERENCES eg_sdcrs_report(id),
    teacher_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,

    -- Payout Details
    amount NUMERIC(10,2) NOT NULL,
    demand_id VARCHAR(64),          -- Reference to billing service
    payment_id VARCHAR(64),         -- Payment ID when completed
    status VARCHAR(32) NOT NULL,    -- PENDING, PROCESSED, COMPLETED, FAILED

    -- Monthly Tracking
    month_year VARCHAR(7) NOT NULL, -- Format: YYYY-MM

    -- Audit
    created_time BIGINT NOT NULL,
    completed_time BIGINT
);

CREATE INDEX idx_sdcrs_payout_report ON eg_sdcrs_payout(report_id);
CREATE INDEX idx_sdcrs_payout_teacher ON eg_sdcrs_payout(teacher_id);
CREATE INDEX idx_sdcrs_payout_month ON eg_sdcrs_payout(teacher_id, month_year);
CREATE INDEX idx_sdcrs_payout_status ON eg_sdcrs_payout(status);

-- ==============================================================================
-- Audit Log Table
-- ==============================================================================
CREATE TABLE IF NOT EXISTS eg_sdcrs_audit_log (
    id VARCHAR(64) PRIMARY KEY,
    report_id VARCHAR(64) NOT NULL REFERENCES eg_sdcrs_report(id),

    -- Action Details
    action VARCHAR(64) NOT NULL,        -- SUBMIT, VERIFY, REJECT, ASSIGN, etc.
    previous_status VARCHAR(32),
    new_status VARCHAR(32),

    -- Actor
    actor_id VARCHAR(64),
    actor_name VARCHAR(128),
    comments TEXT,

    -- Timestamp
    created_time BIGINT NOT NULL
);

CREATE INDEX idx_sdcrs_audit_report ON eg_sdcrs_audit_log(report_id);
CREATE INDEX idx_sdcrs_audit_action ON eg_sdcrs_audit_log(action);
CREATE INDEX idx_sdcrs_audit_time ON eg_sdcrs_audit_log(created_time);

-- ==============================================================================
-- Monthly Cap View (for quick payout cap checking)
-- ==============================================================================
CREATE OR REPLACE VIEW vw_sdcrs_monthly_payout AS
SELECT
    teacher_id,
    tenant_id,
    month_year,
    COUNT(*) as payout_count,
    SUM(amount) as total_amount
FROM eg_sdcrs_payout
WHERE status IN ('PROCESSED', 'COMPLETED')
GROUP BY teacher_id, tenant_id, month_year;

-- ==============================================================================
-- Report Statistics View (for dashboards)
-- ==============================================================================
CREATE OR REPLACE VIEW vw_sdcrs_stats AS
SELECT
    tenant_id,
    status,
    DATE_TRUNC('day', TO_TIMESTAMP(created_time/1000)) as report_date,
    COUNT(*) as report_count
FROM eg_sdcrs_report
GROUP BY tenant_id, status, DATE_TRUNC('day', TO_TIMESTAMP(created_time/1000));
