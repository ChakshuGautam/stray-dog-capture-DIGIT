# Design Output #4c: CCRS Fork with Modified Data Model

## Option C: Fork CCRS and Modify Data Model

---

## Overview

This document describes how to implement SDCRS by **forking the CCRS (Citizen Complaint Resolution System) repository and modifying its data model** to add first-class columns for dog-specific data. This approach gives the benefits of the battle-tested CCRS codebase while allowing schema customization.

**Approach:** Fork CCRS/PGR service, modify the database schema to add domain-specific columns, and update the entity mappings accordingly.

---

## Architecture Decision

| Aspect | Decision |
|--------|----------|
| **Base Platform** | Forked CCRS/PGR Service |
| **Custom Code** | Schema modifications + entity updates |
| **Data Model** | Modified `eg_pgr_service_v2` with new columns |
| **Workflow** | Existing CCRS workflow integration (unchanged) |
| **API Endpoints** | Existing PGR endpoints (mostly unchanged) |
| **Domain Data** | First-class columns + `additionalDetail` JSONB |

---

## 1. Fork Strategy

### 1.1 Repository Setup

```bash
# Fork the CCRS repository
git clone https://github.com/egovernments/DIGIT-Works.git sdcrs-ccrs-fork
cd sdcrs-ccrs-fork

# Create SDCRS branch
git checkout -b sdcrs-customization

# Focus on pgr-services module
cd municipal-services/pgr-services
```

### 1.2 Files to Modify

| File/Folder | Changes Required |
|-------------|------------------|
| `src/main/resources/db/migration/` | Add new migration for SDCRS columns |
| `src/main/java/.../web/models/Service.java` | Add new fields for dog details |
| `src/main/java/.../repository/rowmapper/` | Update row mappers |
| `src/main/java/.../repository/ServiceRequestRepository.java` | Update queries |
| `src/main/java/.../service/` | Add validation, payout logic |
| `src/main/resources/application.properties` | Update service name |

---

## 2. Database Schema Modifications

### 2.1 Migration Script

**File:** `V2__add_sdcrs_columns.sql`

```sql
-- Add SDCRS-specific columns to existing PGR table
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS dog_count INTEGER DEFAULT 1;
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS is_aggressive BOOLEAN DEFAULT FALSE;
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS breed VARCHAR(64);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS estimated_age VARCHAR(32);

-- Evidence columns
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS photo_file_store_id VARCHAR(64);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS selfie_file_store_id VARCHAR(64);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS image_hash VARCHAR(256);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS photo_timestamp BIGINT;
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS exif_gps_latitude DECIMAL(10, 8);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS exif_gps_longitude DECIMAL(11, 8);

-- Reporter columns (teacher-specific)
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS reporter_type VARCHAR(32);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS school_code VARCHAR(64);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS school_name VARCHAR(256);

-- Assignment columns
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS assigned_officer_id VARCHAR(64);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS assigned_officer_name VARCHAR(256);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS assigned_time BIGINT;

-- Resolution columns
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS resolution_type VARCHAR(64);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS resolution_notes TEXT;
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS resolution_time BIGINT;
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS capture_photo_file_store_id VARCHAR(64);

-- Payout columns
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS payout_eligible BOOLEAN DEFAULT FALSE;
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS payout_amount DECIMAL(10, 2);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS payout_demand_id VARCHAR(64);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS payout_status VARCHAR(32);

-- Validation columns
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(64);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS validation_result JSONB;

-- Tracking ID for public tracking
ALTER TABLE eg_pgr_service_v2 ADD COLUMN IF NOT EXISTS tracking_id VARCHAR(10);

-- Create indexes for new columns
CREATE INDEX IF NOT EXISTS idx_pgr_dog_count ON eg_pgr_service_v2(dog_count);
CREATE INDEX IF NOT EXISTS idx_pgr_aggressive ON eg_pgr_service_v2(is_aggressive);
CREATE INDEX IF NOT EXISTS idx_pgr_school ON eg_pgr_service_v2(school_code);
CREATE INDEX IF NOT EXISTS idx_pgr_assigned ON eg_pgr_service_v2(assigned_officer_id);
CREATE INDEX IF NOT EXISTS idx_pgr_image_hash ON eg_pgr_service_v2(image_hash);
CREATE INDEX IF NOT EXISTS idx_pgr_tracking ON eg_pgr_service_v2(tracking_id);
CREATE INDEX IF NOT EXISTS idx_pgr_reporter_type ON eg_pgr_service_v2(reporter_type);
CREATE INDEX IF NOT EXISTS idx_pgr_payout_status ON eg_pgr_service_v2(payout_status);
```

### 2.2 Schema Comparison

| Column | Option A (JSONB) | Option C (First-class) |
|--------|------------------|------------------------|
| `dog_count` | `additionalDetail->>'dogDetails'->>'count'` | `dog_count` |
| `is_aggressive` | `additionalDetail->>'dogDetails'->>'isAggressive'` | `is_aggressive` |
| `school_code` | `additionalDetail->>'reporter'->>'schoolCode'` | `school_code` |
| `image_hash` | `additionalDetail->>'evidence'->>'imageHash'` | `image_hash` |
| `payout_status` | `additionalDetail->>'payout'->>'status'` | `payout_status` |

---

## 3. Entity Modifications

### 3.1 Service.java (Modified)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Service {

    // Existing PGR fields
    private String id;
    private String tenantId;
    private String serviceCode;
    private String serviceRequestId;
    private String description;
    private String accountId;
    private String applicationStatus;
    private String source;
    private Address address;
    private Object additionalDetail;
    private AuditDetails auditDetails;

    // === NEW SDCRS FIELDS ===

    // Dog Details (first-class columns)
    @JsonProperty("dogCount")
    private Integer dogCount;

    @JsonProperty("isAggressive")
    private Boolean isAggressive;

    @JsonProperty("breed")
    private String breed;

    @JsonProperty("estimatedAge")
    private String estimatedAge;

    // Evidence
    @JsonProperty("photoFileStoreId")
    private String photoFileStoreId;

    @JsonProperty("selfieFileStoreId")
    private String selfieFileStoreId;

    @JsonProperty("imageHash")
    private String imageHash;

    @JsonProperty("photoTimestamp")
    private Long photoTimestamp;

    @JsonProperty("exifGpsLatitude")
    private Double exifGpsLatitude;

    @JsonProperty("exifGpsLongitude")
    private Double exifGpsLongitude;

    // Reporter (teacher-specific)
    @JsonProperty("reporterType")
    private String reporterType;

    @JsonProperty("schoolCode")
    private String schoolCode;

    @JsonProperty("schoolName")
    private String schoolName;

    // Assignment
    @JsonProperty("assignedOfficerId")
    private String assignedOfficerId;

    @JsonProperty("assignedOfficerName")
    private String assignedOfficerName;

    @JsonProperty("assignedTime")
    private Long assignedTime;

    // Resolution
    @JsonProperty("resolutionType")
    private String resolutionType;

    @JsonProperty("resolutionNotes")
    private String resolutionNotes;

    @JsonProperty("resolutionTime")
    private Long resolutionTime;

    @JsonProperty("capturePhotoFileStoreId")
    private String capturePhotoFileStoreId;

    // Payout
    @JsonProperty("payoutEligible")
    private Boolean payoutEligible;

    @JsonProperty("payoutAmount")
    private BigDecimal payoutAmount;

    @JsonProperty("payoutDemandId")
    private String payoutDemandId;

    @JsonProperty("payoutStatus")
    private String payoutStatus;

    // Validation
    @JsonProperty("rejectionReason")
    private String rejectionReason;

    @JsonProperty("validationResult")
    private Object validationResult;

    // Public tracking
    @JsonProperty("trackingId")
    private String trackingId;
}
```

### 3.2 Row Mapper Updates

Update `PGRRowMapper.java` to include new columns:

```java
public Service extractData(ResultSet rs) throws SQLException {
    Service service = Service.builder()
        // Existing fields...
        .id(rs.getString("id"))
        .tenantId(rs.getString("tenantid"))
        .serviceCode(rs.getString("servicecode"))
        // ... other existing fields ...

        // NEW SDCRS fields
        .dogCount(rs.getInt("dog_count"))
        .isAggressive(rs.getBoolean("is_aggressive"))
        .breed(rs.getString("breed"))
        .estimatedAge(rs.getString("estimated_age"))
        .photoFileStoreId(rs.getString("photo_file_store_id"))
        .selfieFileStoreId(rs.getString("selfie_file_store_id"))
        .imageHash(rs.getString("image_hash"))
        .photoTimestamp(rs.getLong("photo_timestamp"))
        .exifGpsLatitude(rs.getDouble("exif_gps_latitude"))
        .exifGpsLongitude(rs.getDouble("exif_gps_longitude"))
        .reporterType(rs.getString("reporter_type"))
        .schoolCode(rs.getString("school_code"))
        .schoolName(rs.getString("school_name"))
        .assignedOfficerId(rs.getString("assigned_officer_id"))
        .assignedOfficerName(rs.getString("assigned_officer_name"))
        .assignedTime(rs.getLong("assigned_time"))
        .resolutionType(rs.getString("resolution_type"))
        .resolutionNotes(rs.getString("resolution_notes"))
        .resolutionTime(rs.getLong("resolution_time"))
        .capturePhotoFileStoreId(rs.getString("capture_photo_file_store_id"))
        .payoutEligible(rs.getBoolean("payout_eligible"))
        .payoutAmount(rs.getBigDecimal("payout_amount"))
        .payoutDemandId(rs.getString("payout_demand_id"))
        .payoutStatus(rs.getString("payout_status"))
        .rejectionReason(rs.getString("rejection_reason"))
        .trackingId(rs.getString("tracking_id"))
        .build();

    return service;
}
```

---

## 4. Persister Configuration Updates

### File: `pgr-services-persister.yml` (Modified)

```yaml
serviceMaps:
  serviceName: pgr-services
  mappings:
    - version: 1.0
      description: Persists pgr service request
      fromTopic: save-pgr-request
      isTransaction: true
      queryMaps:
        - query: >
            INSERT INTO eg_pgr_service_v2
            (id, tenantid, servicecode, servicerequestid, description,
             accountid, rating, applicationstatus, source,
             additional_detail, createdby, createdtime, lastmodifiedby, lastmodifiedtime,
             -- NEW SDCRS columns
             dog_count, is_aggressive, breed, estimated_age,
             photo_file_store_id, selfie_file_store_id, image_hash, photo_timestamp,
             exif_gps_latitude, exif_gps_longitude,
             reporter_type, school_code, school_name,
             tracking_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
          basePath: service
          jsonMaps:
            # Existing fields...
            - jsonPath: $.service.id
            - jsonPath: $.service.tenantId
            - jsonPath: $.service.serviceCode
            - jsonPath: $.service.serviceRequestId
            - jsonPath: $.service.description
            - jsonPath: $.service.accountId
            - jsonPath: $.service.rating
            - jsonPath: $.service.applicationStatus
            - jsonPath: $.service.source
            - jsonPath: $.service.additionalDetail
              type: JSON
              dbType: JSONB
            - jsonPath: $.service.auditDetails.createdBy
            - jsonPath: $.service.auditDetails.createdTime
            - jsonPath: $.service.auditDetails.lastModifiedBy
            - jsonPath: $.service.auditDetails.lastModifiedTime
            # NEW SDCRS fields
            - jsonPath: $.service.dogCount
            - jsonPath: $.service.isAggressive
            - jsonPath: $.service.breed
            - jsonPath: $.service.estimatedAge
            - jsonPath: $.service.photoFileStoreId
            - jsonPath: $.service.selfieFileStoreId
            - jsonPath: $.service.imageHash
            - jsonPath: $.service.photoTimestamp
            - jsonPath: $.service.exifGpsLatitude
            - jsonPath: $.service.exifGpsLongitude
            - jsonPath: $.service.reporterType
            - jsonPath: $.service.schoolCode
            - jsonPath: $.service.schoolName
            - jsonPath: $.service.trackingId

    - version: 1.0
      description: Updates pgr service request
      fromTopic: update-pgr-request
      isTransaction: true
      queryMaps:
        - query: >
            UPDATE eg_pgr_service_v2 SET
              applicationstatus = ?,
              additional_detail = ?::jsonb,
              lastmodifiedby = ?,
              lastmodifiedtime = ?,
              -- NEW SDCRS update columns
              assigned_officer_id = ?,
              assigned_officer_name = ?,
              assigned_time = ?,
              resolution_type = ?,
              resolution_notes = ?,
              resolution_time = ?,
              capture_photo_file_store_id = ?,
              payout_eligible = ?,
              payout_amount = ?,
              payout_demand_id = ?,
              payout_status = ?,
              rejection_reason = ?,
              validation_result = ?::jsonb
            WHERE id = ?;
          basePath: service
          jsonMaps:
            - jsonPath: $.service.applicationStatus
            - jsonPath: $.service.additionalDetail
              type: JSON
              dbType: JSONB
            - jsonPath: $.service.auditDetails.lastModifiedBy
            - jsonPath: $.service.auditDetails.lastModifiedTime
            # NEW SDCRS fields
            - jsonPath: $.service.assignedOfficerId
            - jsonPath: $.service.assignedOfficerName
            - jsonPath: $.service.assignedTime
            - jsonPath: $.service.resolutionType
            - jsonPath: $.service.resolutionNotes
            - jsonPath: $.service.resolutionTime
            - jsonPath: $.service.capturePhotoFileStoreId
            - jsonPath: $.service.payoutEligible
            - jsonPath: $.service.payoutAmount
            - jsonPath: $.service.payoutDemandId
            - jsonPath: $.service.payoutStatus
            - jsonPath: $.service.rejectionReason
            - jsonPath: $.service.validationResult
              type: JSON
              dbType: JSONB
            - jsonPath: $.service.id
```

---

## 5. Custom Services to Add

### 5.1 ValidationService.java (New)

```java
@Service
@Slf4j
public class SdcrsValidationService {

    @Autowired
    private FileStoreService fileStoreService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ServiceRequestRepository repository;

    public ValidationResult validateReport(ServiceRequest request) {
        ValidationResult result = new ValidationResult();
        Service service = request.getService();

        // 1. GPS Validation
        if (!validateGps(service)) {
            result.setValid(false);
            result.setReason("INVALID_GPS");
            return result;
        }

        // 2. Boundary Validation
        if (!validateBoundary(service)) {
            result.setValid(false);
            result.setReason("OUTSIDE_BOUNDARY");
            return result;
        }

        // 3. Timestamp Validation (within 48 hours)
        if (!validateTimestamp(service)) {
            result.setValid(false);
            result.setReason("STALE_TIMESTAMP");
            return result;
        }

        // 4. Duplicate Detection
        if (isDuplicate(service)) {
            result.setValid(false);
            result.setReason("DUPLICATE_IMAGE");
            return result;
        }

        result.setValid(true);
        return result;
    }

    private boolean validateGps(Service service) {
        // Compare submitted GPS with EXIF GPS
        double distance = calculateDistance(
            service.getAddress().getLatitude(),
            service.getAddress().getLongitude(),
            service.getExifGpsLatitude(),
            service.getExifGpsLongitude()
        );
        return distance <= 100; // 100 meters tolerance
    }

    private boolean isDuplicate(Service service) {
        // Check image hash against existing reports
        List<Service> similar = repository.findByImageHashSimilar(
            service.getTenantId(),
            service.getImageHash(),
            0.90 // 90% similarity threshold
        );
        return !similar.isEmpty();
    }
}
```

### 5.2 PayoutService.java (New)

```java
@Service
@Slf4j
public class SdcrsPayoutService {

    @Autowired
    private BillingServiceClient billingClient;

    @Autowired
    private MDMSService mdmsService;

    public void processPayoutOnCapture(ServiceRequest request) {
        Service service = request.getService();

        // Get payout config from MDMS
        PayoutConfig config = mdmsService.getPayoutConfig(service.getTenantId());

        // Check monthly cap
        BigDecimal monthlyTotal = getMonthlyPayoutTotal(service.getAccountId());
        if (monthlyTotal.add(config.getAmountPerReport()).compareTo(config.getMonthlyCap()) > 0) {
            service.setPayoutStatus("CAP_EXCEEDED");
            return;
        }

        // Create demand in Billing Service
        DemandRequest demandRequest = buildDemandRequest(service, config);
        Demand demand = billingClient.createDemand(demandRequest);

        // Update service with payout info
        service.setPayoutEligible(true);
        service.setPayoutAmount(config.getAmountPerReport());
        service.setPayoutDemandId(demand.getId());
        service.setPayoutStatus("PENDING");
    }
}
```

---

## 6. API Endpoints

Uses existing PGR endpoints with extended request/response:

| Method | Endpoint | Changes |
|--------|----------|---------|
| POST | `/pgr-services/v2/request/_create` | Request body includes new SDCRS fields |
| POST | `/pgr-services/v2/request/_update` | Supports assignment, resolution, payout updates |
| POST | `/pgr-services/v2/request/_search` | Can filter by new columns |
| GET | `/pgr-services/v2/request/_track` | **NEW** - Public tracking endpoint |

### Sample Create Request

```json
{
  "RequestInfo": { "...": "..." },
  "service": {
    "tenantId": "ncr",
    "serviceCode": "StrayDogAggressive",
    "description": "Aggressive stray dog near school entrance",
    "address": {
      "latitude": 11.5886,
      "longitude": 43.1456,
      "locality": { "code": "LOC-001" }
    },
    "dogCount": 1,
    "isAggressive": true,
    "breed": "Mixed",
    "photoFileStoreId": "uuid-photo-1",
    "selfieFileStoreId": "uuid-selfie-1",
    "reporterType": "TEACHER",
    "schoolCode": "SCH-123",
    "schoolName": "Government Primary School"
  },
  "workflow": {
    "action": "SUBMIT",
    "businessService": "SDCRS"
  }
}
```

---

## 7. Workflow Configuration

Same as Option A - uses standard DIGIT Workflow Service with SDCRS business service config.

**File:** [`SdcrsWorkflowConfig.json`](./04c-ccrs-fork/data/ncr/Workflow/SdcrsWorkflowConfig.json)

---

## 8. MDMS Configuration

Same MDMS masters as Option B, placed in `04c-ccrs-fork/data/ncr/SDCRS/`:

| File | Purpose |
|------|---------|
| `ServiceType.json` | Dog report types |
| `PayoutConfig.json` | Payout configuration |
| `RejectionReason.json` | Auto/manual rejection reasons |
| `ResolutionType.json` | MC resolution outcomes |

---

## 9. Merge Strategy

### 9.1 Keeping Up with Upstream

```bash
# Add upstream remote
git remote add upstream https://github.com/egovernments/DIGIT-Works.git

# Periodically sync
git fetch upstream
git checkout sdcrs-customization
git merge upstream/master

# Resolve conflicts (mainly in modified files)
# - Service.java
# - RowMapper files
# - Persister configs
```

### 9.2 Conflict-Prone Files

| File | Conflict Risk | Strategy |
|------|---------------|----------|
| `Service.java` | High | Add fields at end, use `@JsonProperty` |
| `PGRRowMapper.java` | High | Extend existing mapper methods |
| `pgr-services-persister.yml` | Medium | Keep query structure, add columns |
| `pom.xml` | Low | Merge dependency versions |

---

## 10. Comparison Summary

| Aspect | Option A | Option B | Option C |
|--------|----------|----------|----------|
| **Approach** | Config only | Build from scratch | Fork & modify |
| **Development Time** | ~2-3 weeks | ~6-8 weeks | ~4-5 weeks |
| **Code Changes** | Minimal | Full service | Schema + entities |
| **Data Model** | JSONB only | Full custom | Hybrid (columns + JSONB) |
| **Query Performance** | Medium | High | High |
| **Upstream Updates** | Automatic | N/A | Manual merge |
| **Maintenance** | Low | High | Medium |
| **API Compatibility** | 100% PGR | Custom | ~90% PGR |

---

## Pros and Cons

### Pros

| Benefit | Description |
|---------|-------------|
| **Proven codebase** | Inherits battle-tested PGR/CCRS code |
| **First-class columns** | Efficient queries on dog-specific fields |
| **Faster than Option B** | Don't need to build everything from scratch |
| **Familiar API** | Uses existing PGR endpoints |
| **Existing integrations** | Workflow, notification, etc. already wired |

### Cons

| Limitation | Description |
|------------|-------------|
| **Merge conflicts** | Must resolve conflicts on upstream updates |
| **Fork maintenance** | Need to track upstream changes |
| **Partial compatibility** | Some PGR features may not apply |
| **Testing overhead** | Must test modified code paths |
| **Deployment complexity** | Deploy forked version instead of standard |

---

## Implementation Checklist

- [ ] Fork CCRS/PGR repository
- [ ] Create `sdcrs-customization` branch
- [ ] Add database migration script
- [ ] Update `Service.java` with new fields
- [ ] Update row mappers
- [ ] Modify persister configuration
- [ ] Add ValidationService
- [ ] Add PayoutService
- [ ] Add public tracking endpoint
- [ ] Configure SDCRS workflow
- [ ] Load MDMS masters
- [ ] Update indexer configuration
- [ ] Add notification templates
- [ ] Write integration tests
- [ ] Document merge procedure for upstream updates

---

## Configuration Files

All configuration files for this option are in [`04c-ccrs-fork/`](./04c-ccrs-fork/):

```
04c-ccrs-fork/
├── data/ncr/
│   ├── SDCRS/
│   │   ├── ServiceType.json
│   │   ├── PayoutConfig.json
│   │   ├── RejectionReason.json
│   │   └── ResolutionType.json
│   ├── ACCESSCONTROL-ROLES/
│   │   └── roles.json
│   └── Workflow/
│       └── SdcrsWorkflowConfig.json
├── db/
│   └── V2__add_sdcrs_columns.sql
├── configs/
│   ├── pgr-services-persister-modified.yml
│   ├── pgr-services-indexer-modified.yml
│   └── notification-templates.json
└── README.md
```

---

*Document Version: 1.0*
*Last Updated: December 2025*
*Option: C - CCRS Fork with Modified Data Model*
