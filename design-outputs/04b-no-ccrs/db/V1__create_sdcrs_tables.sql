-- SDCRS Database Schema
-- Version: 1.0
-- Database: PostgreSQL with PostGIS extension

-- Main Report Table
CREATE TABLE eg_sdcrs_report (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    report_number VARCHAR(64) UNIQUE NOT NULL,
    service_code VARCHAR(64) NOT NULL,
    description TEXT,
    status VARCHAR(64) NOT NULL,

    -- Reporter (first-class columns for common queries)
    reporter_id VARCHAR(64) NOT NULL,
    reporter_name VARCHAR(256),
    reporter_phone VARCHAR(20),
    reporter_type VARCHAR(32),
    school_code VARCHAR(64),

    -- Location (first-class for geo queries)
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    address TEXT,
    landmark TEXT,
    locality_code VARCHAR(64),
    ward_code VARCHAR(64),
    district_code VARCHAR(64),

    -- Dog Details (first-class for filtering)
    dog_count INTEGER DEFAULT 1,
    is_aggressive BOOLEAN DEFAULT FALSE,

    -- Evidence (file references)
    photo_file_store_id VARCHAR(64),
    selfie_file_store_id VARCHAR(64),
    image_hash VARCHAR(256),
    photo_timestamp BIGINT,

    -- Assignment
    assigned_officer_id VARCHAR(64),
    assigned_officer_name VARCHAR(256),
    assigned_time BIGINT,

    -- Resolution
    resolution_type VARCHAR(64),
    resolution_notes TEXT,
    resolution_time BIGINT,
    capture_photo_file_store_id VARCHAR(64),

    -- Payout (references to Collection Service)
    payout_eligible BOOLEAN DEFAULT FALSE,
    payout_amount DECIMAL(10, 2),
    payout_demand_id VARCHAR(64),
    payout_status VARCHAR(32),

    -- Validation
    rejection_reason VARCHAR(64),
    validation_result JSONB,

    -- Flexible Extension
    additional_details JSONB,

    -- Audit
    created_by VARCHAR(64) NOT NULL,
    created_time BIGINT NOT NULL,
    last_modified_by VARCHAR(64),
    last_modified_time BIGINT
);

-- Indexes for common queries
CREATE INDEX idx_sdcrs_tenant ON eg_sdcrs_report(tenant_id);
CREATE INDEX idx_sdcrs_status ON eg_sdcrs_report(status);
CREATE INDEX idx_sdcrs_reporter ON eg_sdcrs_report(reporter_id);
CREATE INDEX idx_sdcrs_service_code ON eg_sdcrs_report(service_code);
CREATE INDEX idx_sdcrs_locality ON eg_sdcrs_report(locality_code);
CREATE INDEX idx_sdcrs_ward ON eg_sdcrs_report(ward_code);
CREATE INDEX idx_sdcrs_district ON eg_sdcrs_report(district_code);
CREATE INDEX idx_sdcrs_assigned ON eg_sdcrs_report(assigned_officer_id);
CREATE INDEX idx_sdcrs_created ON eg_sdcrs_report(created_time);
CREATE INDEX idx_sdcrs_image_hash ON eg_sdcrs_report(image_hash);
CREATE INDEX idx_sdcrs_school ON eg_sdcrs_report(school_code);
CREATE INDEX idx_sdcrs_payout_status ON eg_sdcrs_report(payout_status);

-- Geo index for location-based queries (requires PostGIS)
-- CREATE INDEX idx_sdcrs_geo ON eg_sdcrs_report USING GIST (
--     ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
-- );

-- Composite indexes for common query patterns
CREATE INDEX idx_sdcrs_tenant_status ON eg_sdcrs_report(tenant_id, status);
CREATE INDEX idx_sdcrs_tenant_created ON eg_sdcrs_report(tenant_id, created_time DESC);
CREATE INDEX idx_sdcrs_officer_status ON eg_sdcrs_report(assigned_officer_id, status);


-- Audit/History Table for tracking state changes
CREATE TABLE eg_sdcrs_report_audit (
    id VARCHAR(64) PRIMARY KEY,
    report_id VARCHAR(64) NOT NULL,
    status_from VARCHAR(64),
    status_to VARCHAR(64),
    action VARCHAR(64),
    action_by VARCHAR(64),
    action_time BIGINT,
    comments TEXT,
    CONSTRAINT fk_report FOREIGN KEY (report_id) REFERENCES eg_sdcrs_report(id)
);

CREATE INDEX idx_sdcrs_audit_report ON eg_sdcrs_report_audit(report_id);
CREATE INDEX idx_sdcrs_audit_action ON eg_sdcrs_report_audit(action);
CREATE INDEX idx_sdcrs_audit_time ON eg_sdcrs_report_audit(action_time);
