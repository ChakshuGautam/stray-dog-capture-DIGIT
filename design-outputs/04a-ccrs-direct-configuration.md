# Design Output #4a: CCRS Direct Configuration

## Option A: Use CCRS (PGR) Directly

---

## Overview

This document describes how to implement SDCRS by **directly using CCRS (Citizen Complaint Resolution System)** with configuration changes only. No custom service code is required.

**Approach:** Configure CCRS with new ServiceDefs, custom workflow, and use `additionalDetail` JSONB for dog-specific data.

---

## Architecture Decision

| Aspect | Decision |
|--------|----------|
| **Base Platform** | CCRS/PGR Service (existing) |
| **Custom Code** | None - configuration only |
| **Data Model** | Use existing `eg_pgr_service_v2` table |
| **Workflow** | Custom workflow config for SDCRS |
| **API Endpoints** | Use existing PGR endpoints |
| **Domain Data** | Store in `additionalDetail` JSONB |

---

## 1. ServiceDefs Configuration

### File: `RAINMAKER-PGR.ServiceDefs.json`

Add the following stray dog report types to the existing ServiceDefs:

```json
[
  {
    "serviceCode": "StrayDogAggressive",
    "name": "Aggressive Stray Dog",
    "keywords": "stray, dog, aggressive, bite, attack, dangerous, animal, street",
    "department": "DEPT_ANIMAL_CONTROL",
    "slaHours": 24,
    "active": true,
    "menuPath": "StrayDog",
    "menuPathName": "Stray Dog Reports",
    "order": 1
  },
  {
    "serviceCode": "StrayDogInjured",
    "name": "Injured Stray Dog",
    "keywords": "stray, dog, injured, hurt, wound, sick, animal, help",
    "department": "DEPT_ANIMAL_CONTROL",
    "slaHours": 24,
    "active": true,
    "menuPath": "StrayDog",
    "menuPathName": "Stray Dog Reports",
    "order": 2
  },
  {
    "serviceCode": "StrayDogPack",
    "name": "Stray Dog Pack",
    "keywords": "stray, dog, pack, group, multiple, dogs, street, area",
    "department": "DEPT_ANIMAL_CONTROL",
    "slaHours": 48,
    "active": true,
    "menuPath": "StrayDog",
    "menuPathName": "Stray Dog Reports",
    "order": 3
  },
  {
    "serviceCode": "StrayDogStandard",
    "name": "Standard Stray Dog Sighting",
    "keywords": "stray, dog, sighting, street, animal, report",
    "department": "DEPT_ANIMAL_CONTROL",
    "slaHours": 72,
    "active": true,
    "menuPath": "StrayDog",
    "menuPathName": "Stray Dog Reports",
    "order": 4
  }
]
```

---

## 2. Workflow Configuration

### File: `SdcrsWorkflowConfig.json`

Custom workflow for SDCRS that maps to the process workflow:

```json
{
  "RequestInfo": {
    "apiId": "Rainmaker",
    "ver": ".01",
    "ts": null,
    "action": "",
    "did": 1,
    "key": "",
    "msgId": "20170310130900|en_IN",
    "requesterId": "",
    "userInfo": {
      "id": 1,
      "type": "EMPLOYEE",
      "roles": [{ "code": "ADMIN" }]
    }
  },
  "BusinessServices": [
    {
      "tenantId": "dj",
      "businessService": "SDCRS",
      "business": "pgr-services",
      "businessServiceSla": 259200000,
      "states": [
        {
          "tenantId": "dj",
          "sla": null,
          "state": null,
          "applicationStatus": null,
          "docUploadRequired": true,
          "isStartState": true,
          "isTerminateState": false,
          "isStateUpdatable": true,
          "actions": [
            {
              "tenantId": "dj",
              "action": "SUBMIT",
              "nextState": "PENDING_VALIDATION",
              "roles": ["CITIZEN", "TEACHER"],
              "active": true
            }
          ]
        },
        {
          "tenantId": "dj",
          "sla": 3600000,
          "state": "PENDING_VALIDATION",
          "applicationStatus": "PENDING_VALIDATION",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "tenantId": "dj",
              "action": "AUTO_VALIDATE_PASS",
              "nextState": "PENDING_VERIFICATION",
              "roles": ["SYSTEM"],
              "active": true
            },
            {
              "tenantId": "dj",
              "action": "AUTO_VALIDATE_FAIL",
              "nextState": "AUTO_REJECTED",
              "roles": ["SYSTEM"],
              "active": true
            }
          ]
        },
        {
          "tenantId": "dj",
          "sla": null,
          "state": "AUTO_REJECTED",
          "applicationStatus": "AUTO_REJECTED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": true,
          "isStateUpdatable": false,
          "actions": []
        },
        {
          "tenantId": "dj",
          "sla": 86400000,
          "state": "PENDING_VERIFICATION",
          "applicationStatus": "PENDING_VERIFICATION",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "tenantId": "dj",
              "action": "VERIFY",
              "nextState": "VERIFIED",
              "roles": ["GRO", "VERIFIER"],
              "active": true
            },
            {
              "tenantId": "dj",
              "action": "REJECT",
              "nextState": "REJECTED",
              "roles": ["GRO", "VERIFIER"],
              "active": true
            },
            {
              "tenantId": "dj",
              "action": "MARK_DUPLICATE",
              "nextState": "DUPLICATE",
              "roles": ["GRO", "VERIFIER"],
              "active": true
            },
            {
              "tenantId": "dj",
              "action": "COMMENT",
              "nextState": "PENDING_VERIFICATION",
              "roles": ["CITIZEN", "TEACHER"],
              "active": true
            }
          ]
        },
        {
          "tenantId": "dj",
          "sla": null,
          "state": "REJECTED",
          "applicationStatus": "REJECTED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": true,
          "isStateUpdatable": false,
          "actions": []
        },
        {
          "tenantId": "dj",
          "sla": null,
          "state": "DUPLICATE",
          "applicationStatus": "DUPLICATE",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": true,
          "isStateUpdatable": false,
          "actions": []
        },
        {
          "tenantId": "dj",
          "sla": 3600000,
          "state": "VERIFIED",
          "applicationStatus": "VERIFIED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "tenantId": "dj",
              "action": "ASSIGN",
              "nextState": "ASSIGNED",
              "roles": ["GRO", "MC_SUPERVISOR"],
              "active": true
            }
          ]
        },
        {
          "tenantId": "dj",
          "sla": 172800000,
          "state": "ASSIGNED",
          "applicationStatus": "ASSIGNED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": true,
          "actions": [
            {
              "tenantId": "dj",
              "action": "START_FIELD_VISIT",
              "nextState": "IN_PROGRESS",
              "roles": ["PGR_LME", "MC_OFFICER"],
              "active": true
            },
            {
              "tenantId": "dj",
              "action": "REASSIGN",
              "nextState": "VERIFIED",
              "roles": ["GRO", "MC_SUPERVISOR"],
              "active": true
            }
          ]
        },
        {
          "tenantId": "dj",
          "sla": 86400000,
          "state": "IN_PROGRESS",
          "applicationStatus": "IN_PROGRESS",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": true,
          "actions": [
            {
              "tenantId": "dj",
              "action": "MARK_CAPTURED",
              "nextState": "CAPTURED",
              "roles": ["PGR_LME", "MC_OFFICER"],
              "active": true
            },
            {
              "tenantId": "dj",
              "action": "MARK_UNABLE_TO_LOCATE",
              "nextState": "UNABLE_TO_LOCATE",
              "roles": ["PGR_LME", "MC_OFFICER"],
              "active": true
            }
          ]
        },
        {
          "tenantId": "dj",
          "sla": 3600000,
          "state": "CAPTURED",
          "applicationStatus": "CAPTURED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "tenantId": "dj",
              "action": "PROCESS_PAYOUT",
              "nextState": "RESOLVED",
              "roles": ["SYSTEM"],
              "active": true
            }
          ]
        },
        {
          "tenantId": "dj",
          "sla": null,
          "state": "RESOLVED",
          "applicationStatus": "RESOLVED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": true,
          "isStateUpdatable": false,
          "actions": [
            {
              "tenantId": "dj",
              "action": "RATE",
              "nextState": "CLOSED",
              "roles": ["CITIZEN", "TEACHER"],
              "active": true
            }
          ]
        },
        {
          "tenantId": "dj",
          "sla": null,
          "state": "UNABLE_TO_LOCATE",
          "applicationStatus": "UNABLE_TO_LOCATE",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": true,
          "isStateUpdatable": false,
          "actions": []
        },
        {
          "tenantId": "dj",
          "sla": null,
          "state": "CLOSED",
          "applicationStatus": "CLOSED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": true,
          "isStateUpdatable": false,
          "actions": []
        }
      ]
    }
  ]
}
```

---

## 3. Role Mapping

### SDCRS Roles → PGR Roles

| SDCRS Role | PGR Role | Description |
|------------|----------|-------------|
| TEACHER | CITIZEN | Reports stray dog sightings |
| VERIFIER | GRO | Reviews and verifies reports |
| MC_OFFICER | PGR_LME | Field officer who captures dogs |
| MC_SUPERVISOR | GRO + PGR_VIEWER | Supervises MC Officers, assigns work |
| DISTRICT_ADMIN | PGR_VIEWER | District-level reporting |
| STATE_ADMIN | PGR_VIEWER | State-level management |
| SYSTEM | AUTO_ESCALATE | Automated operations |

### New Roles to Add

If using SDCRS-specific role codes, add to `ACCESSCONTROL-ROLES/roles.json`:

```json
[
  {
    "code": "TEACHER",
    "name": "Teacher",
    "description": "School teacher who reports stray dog sightings"
  },
  {
    "code": "VERIFIER",
    "name": "Verifier",
    "description": "Backend operator who verifies submitted reports"
  },
  {
    "code": "MC_OFFICER",
    "name": "MC Officer",
    "description": "Municipal Corporation officer who captures stray dogs"
  },
  {
    "code": "MC_SUPERVISOR",
    "name": "MC Supervisor",
    "description": "Supervisor for MC Officers"
  }
]
```

---

## 4. additionalDetail JSONB Structure

Dog-specific data stored in PGR's `additionalDetail` field:

```json
{
  "dogDetails": {
    "description": "Brown medium-sized dog with white patches",
    "count": 1,
    "isAggressive": true,
    "breed": "Mixed",
    "estimatedAge": "Adult",
    "distinctiveMarks": "White patch on chest"
  },
  "evidence": {
    "photoFileStoreId": "uuid-photo-1",
    "selfieFileStoreId": "uuid-selfie-1",
    "imageHash": "phash-abc123",
    "photoTimestamp": 1701234567890,
    "exifGpsLat": 11.5886,
    "exifGpsLon": 43.1456
  },
  "validation": {
    "gpsValid": true,
    "boundaryValid": true,
    "timestampValid": true,
    "duplicateCheckPassed": true,
    "autoValidationResult": "PASS",
    "autoValidationTime": 1701234568000
  },
  "resolution": {
    "type": "CAPTURED",
    "notes": "Dog captured and taken to city shelter",
    "shelterName": "City Animal Shelter",
    "capturePhotoFileStoreId": "uuid-capture-photo",
    "resolvedTime": 1701334567890
  },
  "payout": {
    "eligible": true,
    "amount": 500,
    "currency": "DJF",
    "demandId": "uuid-demand-1",
    "paymentStatus": "COMPLETED",
    "paymentTime": 1701434567890
  },
  "reporter": {
    "teacherId": "TCH-001",
    "schoolCode": "SCH-123",
    "schoolName": "Government Primary School",
    "districtCode": "DIST-01"
  }
}
```

---

## 5. API Endpoints (Existing PGR)

Use existing PGR endpoints with SDCRS business service:

| Operation | Endpoint | Notes |
|-----------|----------|-------|
| Create Report | `POST /pgr-services/v2/request/_create` | Set `serviceCode` to SDCRS type |
| Update Report | `POST /pgr-services/v2/request/_update` | Workflow transitions |
| Search Reports | `POST /pgr-services/v2/request/_search` | Filter by serviceCode |
| Count Reports | `POST /pgr-services/v2/request/_count` | Analytics |

### Sample Create Request

```json
{
  "RequestInfo": { "...": "..." },
  "service": {
    "tenantId": "dj",
    "serviceCode": "StrayDogAggressive",
    "description": "Aggressive stray dog spotted near school",
    "source": "mobile",
    "address": {
      "tenantId": "dj",
      "latitude": 11.5886,
      "longitude": 43.1456,
      "city": "Djibouti",
      "locality": { "code": "LOC-001" },
      "landmark": "Near Government School"
    },
    "additionalDetail": {
      "dogDetails": {
        "description": "Brown medium-sized dog",
        "count": 1,
        "isAggressive": true
      },
      "evidence": {
        "photoFileStoreId": "uuid-photo-1",
        "selfieFileStoreId": "uuid-selfie-1"
      },
      "reporter": {
        "teacherId": "TCH-001",
        "schoolCode": "SCH-123"
      }
    }
  },
  "workflow": {
    "action": "SUBMIT",
    "businessService": "SDCRS"
  }
}
```

---

## 6. Database (Existing Tables)

### Uses existing PGR tables:

| Table | Purpose |
|-------|---------|
| `eg_pgr_service_v2` | Main complaint/report table |
| `eg_pgr_address_v2` | Location data |

### Key Fields in `eg_pgr_service_v2`:

| Field | SDCRS Usage |
|-------|-------------|
| `id` | Unique report ID |
| `tenant_id` | Tenant (dj) |
| `service_code` | StrayDogAggressive, StrayDogInjured, etc. |
| `description` | Free text description |
| `account_id` | Reporter (teacher) user ID |
| `application_status` | Workflow state |
| `additional_detail` | JSONB with dog details, evidence, payout |

---

## 7. Persister Configuration

**No changes required** - uses existing PGR persister:
- `pgr-services-persister.yml`
- Kafka topics: `save-pgr-request`, `update-pgr-request`

---

## 8. Indexer Configuration

**No changes required** - uses existing PGR indexer:
- `pgr-services-indexer.yml`
- Index: `pgr-services`

For custom analytics on dog-specific fields, can add custom indexer mappings for `additionalDetail` nested fields.

---

## 9. Notification Templates

### File: `channel/templates.json`

Add SDCRS-specific notification templates:

```json
[
  {
    "name": "sdcrs.report.submitted",
    "channel": "SMS",
    "templateId": "SDCRS_SUBMITTED",
    "locale": "en_IN",
    "content": "Your stray dog report {serviceRequestId} has been submitted. Track at {trackingUrl}"
  },
  {
    "name": "sdcrs.report.verified",
    "channel": "SMS",
    "templateId": "SDCRS_VERIFIED",
    "locale": "en_IN",
    "content": "Your report {serviceRequestId} has been verified and assigned to Municipal Corporation."
  },
  {
    "name": "sdcrs.report.captured",
    "channel": "SMS",
    "templateId": "SDCRS_CAPTURED",
    "locale": "en_IN",
    "content": "Good news! The dog in your report {serviceRequestId} has been captured. Payout of {amount} will be processed."
  },
  {
    "name": "sdcrs.report.rejected",
    "channel": "SMS",
    "templateId": "SDCRS_REJECTED",
    "locale": "en_IN",
    "content": "Your report {serviceRequestId} could not be verified. Reason: {rejectionReason}"
  },
  {
    "name": "sdcrs.report.unable_to_locate",
    "channel": "SMS",
    "templateId": "SDCRS_UNABLE",
    "locale": "en_IN",
    "content": "MC Officer could not locate the dog in report {serviceRequestId}. No payout applicable."
  }
]
```

---

## 10. Custom Logic Requirements

Even with CCRS direct usage, some custom logic is needed:

| Logic | Implementation |
|-------|----------------|
| **Auto-validation** | Custom service/consumer to validate GPS, boundary, timestamp, duplicates |
| **Image hash computation** | Custom service to compute pHash for duplicate detection |
| **Payout trigger** | Custom consumer on workflow topic to create Collection Service demand |
| **Duplicate comparison** | UI component or API to compare images |

### Minimal Custom Services:

```
sdcrs-validation-service/
├── Auto-validate GPS, boundary, timestamp
├── Compute image hash
└── Publish validation result to workflow

sdcrs-payout-consumer/
├── Listen to workflow topic
├── On CAPTURED → create Collection Service demand
└── Update additionalDetail.payout
```

---

## Pros and Cons

### Pros

| Benefit | Description |
|---------|-------------|
| **Faster implementation** | No new service code, just configuration |
| **Proven platform** | Uses battle-tested PGR code |
| **Existing UI** | Can reuse PGR UI components |
| **Lower maintenance** | Platform updates automatically apply |
| **Consistent patterns** | Follows DIGIT conventions exactly |

### Cons

| Limitation | Description |
|------------|-------------|
| **Less flexibility** | Constrained to PGR data model |
| **JSONB queries** | Complex queries on additionalDetail are slower |
| **Custom logic still needed** | Auto-validation, payout trigger require custom code |
| **UI customization** | Need to customize PGR UI for dog-specific flows |
| **Schema evolution** | additionalDetail changes need careful migration |

---

## Implementation Checklist

- [ ] Add ServiceDefs for stray dog types
- [ ] Configure SDCRS workflow
- [ ] Add new roles (TEACHER, MC_OFFICER, etc.)
- [ ] Define additionalDetail schema
- [ ] Create validation service (GPS, boundary, duplicate)
- [ ] Create payout consumer (Collection Service integration)
- [ ] Add notification templates
- [ ] Customize PGR UI for dog reports
- [ ] Configure DSS dashboards

---

*Document Version: 1.0*
*Last Updated: December 2024*
*Option: A - CCRS Direct Configuration*
