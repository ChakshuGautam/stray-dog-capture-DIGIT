-- SDCRS Schema Migration for CCRS Fork
-- Adds first-class columns to existing PGR table
-- Version: 2.0
-- Author: SDCRS Team

-- =====================================================
-- DOG DETAILS COLUMNS
-- =====================================================

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS dog_count INTEGER DEFAULT 1;

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS is_aggressive BOOLEAN DEFAULT FALSE;

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS breed VARCHAR(64);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS estimated_age VARCHAR(32);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS distinctive_marks TEXT;

-- =====================================================
-- EVIDENCE COLUMNS
-- =====================================================

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS photo_file_store_id VARCHAR(64);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS selfie_file_store_id VARCHAR(64);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS image_hash VARCHAR(256);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS photo_timestamp BIGINT;

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS exif_gps_latitude DECIMAL(10, 8);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS exif_gps_longitude DECIMAL(11, 8);

-- =====================================================
-- REPORTER COLUMNS (Teacher-specific)
-- =====================================================

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS reporter_type VARCHAR(32);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS school_code VARCHAR(64);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS school_name VARCHAR(256);

-- =====================================================
-- ASSIGNMENT COLUMNS
-- =====================================================

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS assigned_officer_id VARCHAR(64);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS assigned_officer_name VARCHAR(256);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS assigned_time BIGINT;

-- =====================================================
-- RESOLUTION COLUMNS
-- =====================================================

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS resolution_type VARCHAR(64);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS resolution_notes TEXT;

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS resolution_time BIGINT;

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS capture_photo_file_store_id VARCHAR(64);

-- =====================================================
-- PAYOUT COLUMNS
-- =====================================================

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS payout_eligible BOOLEAN DEFAULT FALSE;

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS payout_amount DECIMAL(10, 2);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS payout_demand_id VARCHAR(64);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS payout_status VARCHAR(32);

-- =====================================================
-- VALIDATION COLUMNS
-- =====================================================

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(64);

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS validation_result JSONB;

-- =====================================================
-- PUBLIC TRACKING
-- =====================================================

ALTER TABLE eg_pgr_service_v2
    ADD COLUMN IF NOT EXISTS tracking_id VARCHAR(10);

-- =====================================================
-- INDEXES FOR SDCRS-SPECIFIC QUERIES
-- =====================================================

-- Dog details indexes
CREATE INDEX IF NOT EXISTS idx_pgr_sdcrs_dog_count
    ON eg_pgr_service_v2(dog_count);

CREATE INDEX IF NOT EXISTS idx_pgr_sdcrs_aggressive
    ON eg_pgr_service_v2(is_aggressive);

-- Reporter indexes
CREATE INDEX IF NOT EXISTS idx_pgr_sdcrs_reporter_type
    ON eg_pgr_service_v2(reporter_type);

CREATE INDEX IF NOT EXISTS idx_pgr_sdcrs_school
    ON eg_pgr_service_v2(school_code);

-- Assignment indexes
CREATE INDEX IF NOT EXISTS idx_pgr_sdcrs_assigned
    ON eg_pgr_service_v2(assigned_officer_id);

-- Duplicate detection
CREATE INDEX IF NOT EXISTS idx_pgr_sdcrs_image_hash
    ON eg_pgr_service_v2(image_hash);

-- Public tracking
CREATE INDEX IF NOT EXISTS idx_pgr_sdcrs_tracking
    ON eg_pgr_service_v2(tracking_id);

-- Payout queries
CREATE INDEX IF NOT EXISTS idx_pgr_sdcrs_payout_status
    ON eg_pgr_service_v2(payout_status);

CREATE INDEX IF NOT EXISTS idx_pgr_sdcrs_payout_eligible
    ON eg_pgr_service_v2(payout_eligible);

-- Resolution queries
CREATE INDEX IF NOT EXISTS idx_pgr_sdcrs_resolution_type
    ON eg_pgr_service_v2(resolution_type);

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON COLUMN eg_pgr_service_v2.dog_count IS 'Number of dogs reported (1-10)';
COMMENT ON COLUMN eg_pgr_service_v2.is_aggressive IS 'Whether dog is displaying aggressive behavior';
COMMENT ON COLUMN eg_pgr_service_v2.image_hash IS 'pHash of photo for duplicate detection';
COMMENT ON COLUMN eg_pgr_service_v2.tracking_id IS 'Short public tracking ID (e.g., ABC123)';
COMMENT ON COLUMN eg_pgr_service_v2.payout_status IS 'PENDING, COMPLETED, FAILED, CAP_EXCEEDED';
