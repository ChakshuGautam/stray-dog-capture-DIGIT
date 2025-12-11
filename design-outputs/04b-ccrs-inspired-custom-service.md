# Design Output #4b: CCRS-Inspired Custom Service

## Option B: Custom Service Inspired by CCRS

---

## Overview

This document describes how to implement SDCRS by **building a custom service inspired by CCRS/PGR patterns**. This gives full control over the data model while following DIGIT conventions.

**Approach:** Create a dedicated `sdcrs-services` module with domain-specific entities, using CCRS architectural patterns.

---

## Architecture Decision

| Aspect | Decision |
|--------|----------|
| **Base Platform** | New custom service following PGR patterns |
| **Custom Code** | Full service implementation |
| **Data Model** | Custom `eg_sdcrs_report` table |
| **Workflow** | DIGIT Workflow Service (standard integration) |
| **API Endpoints** | Custom SDCRS endpoints |
| **Domain Data** | First-class columns + `additionalDetails` JSONB |

---

## 1. Service Architecture

### 1.1 New Custom Service

| Service | Package | Description |
|---------|---------|-------------|
| **sdcrs-services** | `org.digit.sdcrs` | Stray Dog Capture & Reporting Service |

### 1.2 Project Structure

```
sdcrs-services/
├── src/main/java/org/digit/sdcrs/
│   ├── config/
│   │   └── SdcrsConfiguration.java
│   ├── controller/
│   │   └── DogReportController.java
│   ├── service/
│   │   ├── DogReportService.java
│   │   ├── ValidationService.java
│   │   ├── DuplicateDetectionService.java
│   │   ├── PayoutService.java
│   │   └── WorkflowService.java
│   ├── repository/
│   │   ├── DogReportRepository.java
│   │   └── rowmapper/
│   │       └── DogReportRowMapper.java
│   ├── validator/
│   │   └── DogReportValidator.java
│   ├── enrichment/
│   │   └── DogReportEnrichment.java
│   ├── producer/
│   │   └── Producer.java
│   ├── consumer/
│   │   └── PayoutConsumer.java
│   └── web/models/
│       ├── DogReport.java
│       ├── DogDetails.java
│       ├── Evidence.java
│       ├── Location.java
│       ├── Resolution.java
│       ├── Payout.java
│       └── DogReportRequest.java
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/
│       └── V1__create_sdcrs_tables.sql
└── pom.xml
```

---

## 2. Data Model

### 2.1 Domain Entities

#### DogReport (Main Entity)

```java
@Data
@Builder
public class DogReport {
    private String id;
    private String tenantId;
    private String reportNumber;

    // Report Details
    private String serviceCode;        // StrayDogAggressive, StrayDogInjured, etc.
    private String description;
    private String status;

    // Embedded Objects
    private Reporter reporter;
    private Location location;
    private DogDetails dogDetails;
    private Evidence evidence;
    private Assignment assignment;
    private Resolution resolution;
    private Payout payout;

    // Validation
    private String rejectionReason;
    private ValidationResult validationResult;

    // Flexible Extension
    private Object additionalDetails;

    // Audit
    private AuditDetails auditDetails;
}
```

#### DogDetails

```java
@Data
@Builder
public class DogDetails {
    private String description;
    private Integer count;
    private Boolean isAggressive;
    private String breed;
    private String estimatedAge;
    private String distinctiveMarks;
    private String colorPattern;
}
```

#### Evidence

```java
@Data
@Builder
public class Evidence {
    private String photoFileStoreId;
    private String selfieFileStoreId;
    private String imageHash;
    private Long photoTimestamp;
    private Double exifGpsLatitude;
    private Double exifGpsLongitude;
    private String capturePhotoFileStoreId;
}
```

#### Location

```java
@Data
@Builder
public class Location {
    private Double latitude;
    private Double longitude;
    private String address;
    private String landmark;
    private String localityCode;
    private String localityName;
    private String wardCode;
    private String districtCode;
}
```

### 2.2 Database Schema

```sql
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
CREATE INDEX idx_sdcrs_district ON eg_sdcrs_report(district_code);
CREATE INDEX idx_sdcrs_assigned ON eg_sdcrs_report(assigned_officer_id);
CREATE INDEX idx_sdcrs_created ON eg_sdcrs_report(created_time);
CREATE INDEX idx_sdcrs_image_hash ON eg_sdcrs_report(image_hash);
CREATE INDEX idx_sdcrs_geo ON eg_sdcrs_report USING GIST (
    ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
);

-- Audit/History Table (optional)
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
```

---

## 3. API Specifications

### 3.1 Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/sdcrs-services/v1/report/_create` | Submit new report |
| POST | `/sdcrs-services/v1/report/_update` | Update report |
| POST | `/sdcrs-services/v1/report/_search` | Search reports |
| POST | `/sdcrs-services/v1/report/_count` | Count reports |
| POST | `/sdcrs-services/v1/report/_verify` | Verifier approve |
| POST | `/sdcrs-services/v1/report/_reject` | Verifier reject |
| POST | `/sdcrs-services/v1/report/_assign` | Assign to MC Officer |
| POST | `/sdcrs-services/v1/report/_resolve` | MC mark outcome |

### 3.2 Request/Response Models

#### Create Request

```json
{
  "RequestInfo": { "...": "..." },
  "DogReport": {
    "tenantId": "dj",
    "serviceCode": "StrayDogAggressive",
    "description": "Aggressive stray dog near school entrance",
    "reporter": {
      "name": "John Teacher",
      "phone": "1234567890",
      "type": "TEACHER",
      "schoolCode": "SCH-123"
    },
    "location": {
      "latitude": 11.5886,
      "longitude": 43.1456,
      "address": "Near Government School",
      "landmark": "Main Gate",
      "localityCode": "LOC-001"
    },
    "dogDetails": {
      "description": "Brown medium-sized dog with white patches",
      "count": 1,
      "isAggressive": true
    },
    "evidence": {
      "photoFileStoreId": "uuid-photo-1",
      "selfieFileStoreId": "uuid-selfie-1"
    }
  },
  "workflow": {
    "action": "SUBMIT"
  }
}
```

#### Search Request

```json
{
  "RequestInfo": { "...": "..." },
  "searchCriteria": {
    "tenantId": "dj",
    "status": ["PENDING_VERIFICATION", "VERIFIED"],
    "serviceCode": ["StrayDogAggressive", "StrayDogInjured"],
    "localityCode": "LOC-001",
    "assignedOfficerId": "MC-001",
    "fromDate": 1701234567890,
    "toDate": 1701334567890,
    "isAggressive": true,
    "offset": 0,
    "limit": 10,
    "sortBy": "createdTime",
    "sortOrder": "DESC"
  }
}
```

---

## 4. Workflow Configuration

### 4.1 Workflow JSON (Same as Option A)

The workflow configuration is identical to Option A - uses DIGIT Workflow Service.

```json
{
  "BusinessServices": [
    {
      "tenantId": "dj",
      "businessService": "SDCRS",
      "business": "sdcrs-services",
      "businessServiceSla": 259200000,
      "states": [
        // ... same states as Option A
      ]
    }
  ]
}
```

### 4.2 Workflow Integration Code

```java
@Service
public class WorkflowService {

    public ProcessInstance updateWorkflow(DogReportRequest request, String action) {
        ProcessInstanceRequest workflowRequest = ProcessInstanceRequest.builder()
            .requestInfo(request.getRequestInfo())
            .processInstances(Collections.singletonList(
                ProcessInstance.builder()
                    .businessId(request.getDogReport().getReportNumber())
                    .businessService("SDCRS")
                    .tenantId(request.getDogReport().getTenantId())
                    .action(action)
                    .moduleName("sdcrs-services")
                    .build()
            ))
            .build();

        return workflowClient.transition(workflowRequest);
    }
}
```

---

## 5. MDMS Configuration

### 5.1 Module: SDCRS

#### ServiceTypes (`SDCRS.ServiceType.json`)

```json
{
  "tenantId": "dj",
  "moduleName": "SDCRS",
  "ServiceType": [
    {
      "code": "StrayDogAggressive",
      "name": "Aggressive Stray Dog",
      "description": "Stray dog showing aggressive behavior",
      "priority": "HIGH",
      "slaHours": 24,
      "department": "ANIMAL_CONTROL",
      "active": true
    },
    {
      "code": "StrayDogInjured",
      "name": "Injured Stray Dog",
      "description": "Stray dog that appears injured",
      "priority": "HIGH",
      "slaHours": 24,
      "department": "ANIMAL_CONTROL",
      "active": true
    },
    {
      "code": "StrayDogPack",
      "name": "Stray Dog Pack",
      "description": "Group of 3+ stray dogs",
      "priority": "MEDIUM",
      "slaHours": 48,
      "department": "ANIMAL_CONTROL",
      "active": true
    },
    {
      "code": "StrayDogStandard",
      "name": "Standard Stray Dog Sighting",
      "description": "Single stray dog sighting",
      "priority": "LOW",
      "slaHours": 72,
      "department": "ANIMAL_CONTROL",
      "active": true
    }
  ]
}
```

#### PayoutConfig (`SDCRS.PayoutConfig.json`)

```json
{
  "tenantId": "dj",
  "moduleName": "SDCRS",
  "PayoutConfig": [
    {
      "code": "STANDARD_PAYOUT",
      "amountPerReport": 500,
      "currency": "DJF",
      "minimumPhotos": 2,
      "maximumDailyReports": 5,
      "monthlyPayoutCap": 5000,
      "active": true
    }
  ]
}
```

#### RejectionReason (`SDCRS.RejectionReason.json`)

```json
{
  "tenantId": "dj",
  "moduleName": "SDCRS",
  "RejectionReason": [
    { "code": "INVALID_GPS", "name": "Invalid GPS", "isAuto": true },
    { "code": "OUTSIDE_BOUNDARY", "name": "Outside Boundary", "isAuto": true },
    { "code": "STALE_TIMESTAMP", "name": "Stale Timestamp", "isAuto": true },
    { "code": "DUPLICATE_IMAGE", "name": "Duplicate Image", "isAuto": true },
    { "code": "POOR_QUALITY", "name": "Poor Image Quality", "isAuto": false },
    { "code": "NOT_A_DOG", "name": "Not a Dog", "isAuto": false },
    { "code": "DOMESTIC_DOG", "name": "Domestic Dog", "isAuto": false },
    { "code": "FRAUDULENT", "name": "Fraudulent Submission", "isAuto": false }
  ]
}
```

#### ResolutionType (`SDCRS.ResolutionType.json`)

```json
{
  "tenantId": "dj",
  "moduleName": "SDCRS",
  "ResolutionType": [
    { "code": "CAPTURED", "name": "Captured", "triggersPayout": true },
    { "code": "RELOCATED", "name": "Relocated", "triggersPayout": true },
    { "code": "STERILIZED_RELEASED", "name": "Sterilized & Released", "triggersPayout": true },
    { "code": "UNABLE_TO_LOCATE", "name": "Unable to Locate", "triggersPayout": false },
    { "code": "ALREADY_RESOLVED", "name": "Already Resolved", "triggersPayout": false }
  ]
}
```

---

## 6. Persister Configuration

### File: `sdcrs-persister.yml`

```yaml
serviceMaps:
  serviceName: sdcrs-services
  mappings:
    - version: 1.0
      description: Persists dog report
      fromTopic: save-sdcrs-report
      isTransaction: true
      queryMaps:
        - query: >
            INSERT INTO eg_sdcrs_report
            (id, tenant_id, report_number, service_code, description, status,
             reporter_id, reporter_name, reporter_phone, reporter_type, school_code,
             latitude, longitude, address, landmark, locality_code, ward_code, district_code,
             dog_count, is_aggressive,
             photo_file_store_id, selfie_file_store_id, image_hash, photo_timestamp,
             additional_details,
             created_by, created_time, last_modified_by, last_modified_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?);
          basePath: DogReport
          jsonMaps:
            - jsonPath: $.DogReport.id
            - jsonPath: $.DogReport.tenantId
            - jsonPath: $.DogReport.reportNumber
            - jsonPath: $.DogReport.serviceCode
            - jsonPath: $.DogReport.description
            - jsonPath: $.DogReport.status
            - jsonPath: $.DogReport.reporter.id
            - jsonPath: $.DogReport.reporter.name
            - jsonPath: $.DogReport.reporter.phone
            - jsonPath: $.DogReport.reporter.type
            - jsonPath: $.DogReport.reporter.schoolCode
            - jsonPath: $.DogReport.location.latitude
            - jsonPath: $.DogReport.location.longitude
            - jsonPath: $.DogReport.location.address
            - jsonPath: $.DogReport.location.landmark
            - jsonPath: $.DogReport.location.localityCode
            - jsonPath: $.DogReport.location.wardCode
            - jsonPath: $.DogReport.location.districtCode
            - jsonPath: $.DogReport.dogDetails.count
            - jsonPath: $.DogReport.dogDetails.isAggressive
            - jsonPath: $.DogReport.evidence.photoFileStoreId
            - jsonPath: $.DogReport.evidence.selfieFileStoreId
            - jsonPath: $.DogReport.evidence.imageHash
            - jsonPath: $.DogReport.evidence.photoTimestamp
            - jsonPath: $.DogReport.additionalDetails
              type: JSON
              dbType: JSONB
            - jsonPath: $.DogReport.auditDetails.createdBy
            - jsonPath: $.DogReport.auditDetails.createdTime
            - jsonPath: $.DogReport.auditDetails.lastModifiedBy
            - jsonPath: $.DogReport.auditDetails.lastModifiedTime

    - version: 1.0
      description: Updates dog report
      fromTopic: update-sdcrs-report
      isTransaction: true
      queryMaps:
        - query: >
            UPDATE eg_sdcrs_report SET
              status = ?,
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
              validation_result = ?::jsonb,
              additional_details = ?::jsonb,
              last_modified_by = ?,
              last_modified_time = ?
            WHERE id = ?;
          basePath: DogReport
          jsonMaps:
            - jsonPath: $.DogReport.status
            - jsonPath: $.DogReport.assignment.officerId
            - jsonPath: $.DogReport.assignment.officerName
            - jsonPath: $.DogReport.assignment.assignedTime
            - jsonPath: $.DogReport.resolution.type
            - jsonPath: $.DogReport.resolution.notes
            - jsonPath: $.DogReport.resolution.time
            - jsonPath: $.DogReport.resolution.capturePhotoFileStoreId
            - jsonPath: $.DogReport.payout.eligible
            - jsonPath: $.DogReport.payout.amount
            - jsonPath: $.DogReport.payout.demandId
            - jsonPath: $.DogReport.payout.status
            - jsonPath: $.DogReport.rejectionReason
            - jsonPath: $.DogReport.validationResult
              type: JSON
              dbType: JSONB
            - jsonPath: $.DogReport.additionalDetails
              type: JSON
              dbType: JSONB
            - jsonPath: $.DogReport.auditDetails.lastModifiedBy
            - jsonPath: $.DogReport.auditDetails.lastModifiedTime
            - jsonPath: $.DogReport.id
```

---

## 7. Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `save-sdcrs-report` | sdcrs-services | persister | Create report |
| `update-sdcrs-report` | sdcrs-services | persister | Update report |
| `sdcrs-report-index` | sdcrs-services | indexer | Elasticsearch |
| `sdcrs-notification` | sdcrs-services | notification-service | SMS/Email |
| `sdcrs-payout-trigger` | sdcrs-services | sdcrs-services | Payout processing |
| `sdcrs-validation` | sdcrs-services | sdcrs-services | Auto-validation |

---

## 8. Indexer Configuration

### File: `sdcrs-indexer.yml`

```yaml
ServiceMaps:
  serviceName: SDCRS Indexer
  mappings:
    - version: 1.0
      name: sdcrs-report-indexer
      fromTopic: sdcrs-report-index
      customJsonMapping:
        indexMapping: '{"settings":{"number_of_shards":3},"mappings":{"properties":{"id":{"type":"keyword"},"tenantId":{"type":"keyword"},"reportNumber":{"type":"keyword"},"serviceCode":{"type":"keyword"},"status":{"type":"keyword"},"reporter":{"type":"object","properties":{"id":{"type":"keyword"},"name":{"type":"text"},"phone":{"type":"keyword"},"schoolCode":{"type":"keyword"}}},"location":{"type":"geo_point"},"dogDetails":{"type":"object","properties":{"count":{"type":"integer"},"isAggressive":{"type":"boolean"}}},"createdTime":{"type":"date","format":"epoch_millis"}}}}'
        fieldMaps:
          - inJsonPath: $.DogReport
            outJsonPath: $.Data
          - inJsonPath: $.DogReport.location
            outJsonPath: $.Data.location
            transform: |
              {"lat": $.latitude, "lon": $.longitude}
```

---

## 9. Roles & Access Control

### 9.1 Custom Roles

```json
{
  "tenantId": "dj",
  "moduleName": "ACCESSCONTROL-ROLES",
  "roles": [
    { "code": "TEACHER", "name": "Teacher", "description": "Reports stray dogs" },
    { "code": "VERIFIER", "name": "Verifier", "description": "Verifies reports" },
    { "code": "MC_OFFICER", "name": "MC Officer", "description": "Captures dogs" },
    { "code": "MC_SUPERVISOR", "name": "MC Supervisor", "description": "Supervises officers" },
    { "code": "DISTRICT_ADMIN", "name": "District Admin", "description": "District reporting" },
    { "code": "STATE_ADMIN", "name": "State Admin", "description": "State management" }
  ]
}
```

### 9.2 Role-Action Matrix

| Action | TEACHER | VERIFIER | MC_OFFICER | MC_SUPERVISOR | DISTRICT_ADMIN | STATE_ADMIN |
|--------|---------|----------|------------|---------------|----------------|-------------|
| `/_create` | ✓ | | | | | |
| `/_search` | ✓ (own) | ✓ | ✓ (assigned) | ✓ | ✓ | ✓ |
| `/_verify` | | ✓ | | | | |
| `/_reject` | | ✓ | | | | |
| `/_assign` | | | | ✓ | | |
| `/_resolve` | | | ✓ | ✓ | | |
| `/_count` | | ✓ | | ✓ | ✓ | ✓ |

---

## 10. Service Integration

### 10.1 DIGIT Services Used

| Service | Purpose | Integration |
|---------|---------|-------------|
| User Service | Authentication, reporter lookup | REST API |
| MDMS Service | Reference data | REST API |
| Workflow Service | State management | REST API |
| File Store Service | Photo storage | REST API |
| Location Service | Boundary validation | REST API |
| Collection Service | Payout demands | REST API |
| Notification Service | SMS/Email | Kafka topic |
| Persister Service | Database writes | Kafka topic |
| Indexer Service | Elasticsearch | Kafka topic |

### 10.2 Custom Services

| Service | Purpose |
|---------|---------|
| ValidationService | GPS, boundary, timestamp, duplicate checks |
| DuplicateDetectionService | Image hash comparison |
| PayoutService | Collection Service demand creation |

---

## Pros and Cons

### Pros

| Benefit | Description |
|---------|-------------|
| **Full control** | Complete control over data model and logic |
| **Optimized queries** | First-class columns for common filters |
| **Clean API** | Domain-specific endpoints |
| **Type safety** | Strongly-typed entities |
| **Easier evolution** | Schema changes via migrations |
| **Better performance** | Indexed columns vs JSONB queries |

### Cons

| Limitation | Description |
|------------|-------------|
| **More code** | Full service implementation required |
| **Maintenance** | Must maintain custom codebase |
| **UI from scratch** | Need to build custom UI |
| **Testing** | More test coverage needed |
| **Deployment** | Additional service to deploy |

---

## Implementation Checklist

- [ ] Create Maven project structure
- [ ] Implement domain models (DogReport, etc.)
- [ ] Create database migration scripts
- [ ] Implement repository layer
- [ ] Implement service layer
- [ ] Create REST controllers
- [ ] Configure persister YAML
- [ ] Configure indexer YAML
- [ ] Set up workflow integration
- [ ] Implement validation service
- [ ] Implement payout integration
- [ ] Add MDMS configurations
- [ ] Configure roles and access control
- [ ] Add notification templates
- [ ] Build UI components
- [ ] Write unit and integration tests

---

## Comparison Summary

| Aspect | Option A (CCRS Direct) | Option B (Custom Service) |
|--------|------------------------|---------------------------|
| **Development Time** | ~2-3 weeks | ~6-8 weeks |
| **Code Complexity** | Low (config only) | High (full service) |
| **Data Model Flexibility** | Limited (JSONB) | Full (custom schema) |
| **Query Performance** | Medium (JSONB queries) | High (indexed columns) |
| **Maintenance Effort** | Low | Medium-High |
| **UI Customization** | Modify PGR UI | Build from scratch |
| **Platform Upgrades** | Automatic | Manual verification |
| **Domain Fit** | Generic complaint | Domain-specific |

---

*Document Version: 1.0*
*Last Updated: December 2025*
*Option: B - CCRS-Inspired Custom Service*
