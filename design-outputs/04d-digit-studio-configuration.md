# Design Output #4d: DIGIT Studio Configuration

## Option D: Use DIGIT Studio ServiceConfiguration Pattern

---

## Overview

This document describes how to implement SDCRS using the **DIGIT Studio ServiceConfiguration pattern** - a configuration-driven approach where form fields, workflow, documents, and billing are defined in MDMS and rendered/processed by a generic backend service.

**Approach:** Define a complete `ServiceConfiguration` JSON that includes typed form fields, workflow states, document requirements per action, and ID generation - all without writing custom backend code.

### Key Benefits

| Benefit | Description |
|---------|-------------|
| **Typed Fields** | Proper field types with validation (not JSONB) |
| **Integrated Workflow** | Workflow states embedded in same config |
| **Document per Action** | Specify required documents at each workflow step |
| **Auto Form Rendering** | Frontend renders forms from field definitions |
| **No Custom Backend** | Uses generic DIGIT Studio backend service |

---

## Architecture Decision

| Aspect | Decision |
|--------|----------|
| **Base Platform** | DIGIT Studio / Public Services |
| **Custom Code** | None - configuration only |
| **Data Model** | Generic service registry with typed attributes |
| **Workflow** | Embedded in ServiceConfiguration |
| **API Endpoints** | Generic DIGIT Studio endpoints |
| **Form Fields** | Defined in `fields` array with types, validation |

---

## 1. ServiceConfiguration (Complete)

**Config File:** `data/ncr/Studio/SDCRS.ServiceConfiguration.json`

```json
{
  "tenantId": "ncr",
  "moduleName": "Studio",
  "ServiceConfiguration": [
    {
      "module": "SDCRS",
      "service": "STRAY_DOG_REPORT",
      "version": "1.0",

      "fields": [
        {
          "name": "reportType",
          "type": "dropdown",
          "label": "SDCRS_REPORT_TYPE",
          "required": true,
          "orderNumber": 1,
          "values": ["AGGRESSIVE", "INJURED", "PACK", "STANDARD"],
          "validation": {
            "message": "SDCRS_ERR_REPORT_TYPE_REQUIRED"
          }
        },
        {
          "name": "dogDescription",
          "type": "textarea",
          "label": "SDCRS_DOG_DESCRIPTION",
          "required": true,
          "orderNumber": 2,
          "minLength": 10,
          "maxLength": 500,
          "placeholder": "SDCRS_DOG_DESCRIPTION_PLACEHOLDER",
          "validation": {
            "min": 10,
            "max": 500,
            "message": "SDCRS_ERR_DESCRIPTION_LENGTH"
          }
        },
        {
          "name": "dogCount",
          "type": "number",
          "label": "SDCRS_DOG_COUNT",
          "required": true,
          "orderNumber": 3,
          "validation": {
            "min": 1,
            "max": 20,
            "message": "SDCRS_ERR_DOG_COUNT_RANGE"
          }
        },
        {
          "name": "isAggressive",
          "type": "checkbox",
          "label": "SDCRS_IS_AGGRESSIVE",
          "required": false,
          "orderNumber": 4
        },
        {
          "name": "estimatedAge",
          "type": "dropdown",
          "label": "SDCRS_ESTIMATED_AGE",
          "required": false,
          "orderNumber": 5,
          "values": ["PUPPY", "YOUNG", "ADULT", "SENIOR"]
        },
        {
          "name": "distinctiveMarks",
          "type": "text",
          "label": "SDCRS_DISTINCTIVE_MARKS",
          "required": false,
          "orderNumber": 6,
          "maxLength": 200
        },
        {
          "name": "location",
          "type": "location",
          "label": "SDCRS_LOCATION",
          "required": true,
          "orderNumber": 7,
          "schema": "gps",
          "validation": {
            "message": "SDCRS_ERR_LOCATION_REQUIRED"
          }
        },
        {
          "name": "landmark",
          "type": "text",
          "label": "SDCRS_LANDMARK",
          "required": false,
          "orderNumber": 8,
          "maxLength": 200
        },
        {
          "name": "teacherId",
          "type": "text",
          "label": "SDCRS_TEACHER_ID",
          "required": true,
          "orderNumber": 9,
          "reference": "HRMS.Employee"
        },
        {
          "name": "schoolCode",
          "type": "text",
          "label": "SDCRS_SCHOOL_CODE",
          "required": true,
          "orderNumber": 10
        },
        {
          "name": "resolutionType",
          "type": "dropdown",
          "label": "SDCRS_RESOLUTION_TYPE",
          "required": false,
          "orderNumber": 20,
          "values": ["CAPTURED", "RELOCATED", "STERILIZED_RELEASED", "UNABLE_TO_LOCATE", "ALREADY_RESOLVED"],
          "dependencies": ["state:IN_PROGRESS"]
        },
        {
          "name": "resolutionNotes",
          "type": "textarea",
          "label": "SDCRS_RESOLUTION_NOTES",
          "required": false,
          "orderNumber": 21,
          "maxLength": 1000,
          "dependencies": ["state:IN_PROGRESS"]
        },
        {
          "name": "shelterName",
          "type": "text",
          "label": "SDCRS_SHELTER_NAME",
          "required": false,
          "orderNumber": 22,
          "dependencies": ["resolutionType:CAPTURED"]
        },
        {
          "name": "rejectionReason",
          "type": "dropdown",
          "label": "SDCRS_REJECTION_REASON",
          "required": false,
          "orderNumber": 30,
          "values": ["UNCLEAR_PHOTO", "NO_DOG_VISIBLE", "DUPLICATE_REPORT", "OUTSIDE_BOUNDARY", "INVALID_LOCATION", "FRAUDULENT_SUBMISSION"],
          "dependencies": ["action:REJECT"]
        },
        {
          "name": "duplicateOfReportNumber",
          "type": "text",
          "label": "SDCRS_DUPLICATE_OF",
          "required": false,
          "orderNumber": 31,
          "dependencies": ["action:MARK_DUPLICATE"]
        }
      ],

      "workflow": {
        "businessService": "SDCRS",
        "business": "sdcrs-services",
        "businessServiceSla": 259200000,
        "generateDemandAt": ["CAPTURED"],
        "nextActionAfterPayment": null,
        "states": [
          {
            "tenantId": "ncr",
            "sla": null,
            "state": "",
            "applicationStatus": "",
            "docUploadRequired": true,
            "isStartState": true,
            "isTerminateState": false,
            "isStateUpdatable": true,
            "actions": [
              {
                "tenantId": "ncr",
                "currentState": "",
                "action": "SUBMIT",
                "nextState": "PENDING_VALIDATION",
                "roles": ["CITIZEN", "TEACHER"],
                "active": true
              }
            ]
          },
          {
            "tenantId": "ncr",
            "sla": 3600000,
            "state": "PENDING_VALIDATION",
            "applicationStatus": "PENDING_VALIDATION",
            "docUploadRequired": false,
            "isStartState": false,
            "isTerminateState": false,
            "isStateUpdatable": false,
            "actions": [
              {
                "tenantId": "ncr",
                "currentState": "PENDING_VALIDATION",
                "action": "AUTO_VALIDATE_PASS",
                "nextState": "PENDING_VERIFICATION",
                "roles": ["SYSTEM"],
                "active": true
              },
              {
                "tenantId": "ncr",
                "currentState": "PENDING_VALIDATION",
                "action": "AUTO_VALIDATE_FAIL",
                "nextState": "AUTO_REJECTED",
                "roles": ["SYSTEM"],
                "active": true
              }
            ]
          },
          {
            "tenantId": "ncr",
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
            "tenantId": "ncr",
            "sla": 86400000,
            "state": "PENDING_VERIFICATION",
            "applicationStatus": "PENDING_VERIFICATION",
            "docUploadRequired": false,
            "isStartState": false,
            "isTerminateState": false,
            "isStateUpdatable": false,
            "actions": [
              {
                "tenantId": "ncr",
                "currentState": "PENDING_VERIFICATION",
                "action": "VERIFY",
                "nextState": "VERIFIED",
                "roles": ["VERIFIER", "GRO"],
                "active": true
              },
              {
                "tenantId": "ncr",
                "currentState": "PENDING_VERIFICATION",
                "action": "REJECT",
                "nextState": "REJECTED",
                "roles": ["VERIFIER", "GRO"],
                "active": true
              },
              {
                "tenantId": "ncr",
                "currentState": "PENDING_VERIFICATION",
                "action": "MARK_DUPLICATE",
                "nextState": "DUPLICATE",
                "roles": ["VERIFIER", "GRO"],
                "active": true
              }
            ]
          },
          {
            "tenantId": "ncr",
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
            "tenantId": "ncr",
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
            "tenantId": "ncr",
            "sla": 3600000,
            "state": "VERIFIED",
            "applicationStatus": "VERIFIED",
            "docUploadRequired": false,
            "isStartState": false,
            "isTerminateState": false,
            "isStateUpdatable": false,
            "actions": [
              {
                "tenantId": "ncr",
                "currentState": "VERIFIED",
                "action": "ASSIGN",
                "nextState": "ASSIGNED",
                "roles": ["MC_SUPERVISOR", "GRO"],
                "active": true
              }
            ]
          },
          {
            "tenantId": "ncr",
            "sla": 172800000,
            "state": "ASSIGNED",
            "applicationStatus": "ASSIGNED",
            "docUploadRequired": false,
            "isStartState": false,
            "isTerminateState": false,
            "isStateUpdatable": true,
            "actions": [
              {
                "tenantId": "ncr",
                "currentState": "ASSIGNED",
                "action": "START_FIELD_VISIT",
                "nextState": "IN_PROGRESS",
                "roles": ["MC_OFFICER"],
                "active": true
              },
              {
                "tenantId": "ncr",
                "currentState": "ASSIGNED",
                "action": "REASSIGN",
                "nextState": "VERIFIED",
                "roles": ["MC_SUPERVISOR", "GRO"],
                "active": true
              }
            ]
          },
          {
            "tenantId": "ncr",
            "sla": 86400000,
            "state": "IN_PROGRESS",
            "applicationStatus": "IN_PROGRESS",
            "docUploadRequired": false,
            "isStartState": false,
            "isTerminateState": false,
            "isStateUpdatable": true,
            "actions": [
              {
                "tenantId": "ncr",
                "currentState": "IN_PROGRESS",
                "action": "MARK_CAPTURED",
                "nextState": "CAPTURED",
                "roles": ["MC_OFFICER"],
                "active": true
              },
              {
                "tenantId": "ncr",
                "currentState": "IN_PROGRESS",
                "action": "MARK_UNABLE_TO_LOCATE",
                "nextState": "UNABLE_TO_LOCATE",
                "roles": ["MC_OFFICER"],
                "active": true
              }
            ]
          },
          {
            "tenantId": "ncr",
            "sla": 3600000,
            "state": "CAPTURED",
            "applicationStatus": "CAPTURED",
            "docUploadRequired": true,
            "isStartState": false,
            "isTerminateState": false,
            "isStateUpdatable": false,
            "actions": [
              {
                "tenantId": "ncr",
                "currentState": "CAPTURED",
                "action": "INITIATE_PAYOUT",
                "nextState": "PAYOUT_PENDING",
                "roles": ["SYSTEM"],
                "active": true
              }
            ]
          },
          {
            "tenantId": "ncr",
            "sla": 86400000,
            "state": "PAYOUT_PENDING",
            "applicationStatus": "PAYOUT_PENDING",
            "docUploadRequired": false,
            "isStartState": false,
            "isTerminateState": false,
            "isStateUpdatable": false,
            "actions": [
              {
                "tenantId": "ncr",
                "currentState": "PAYOUT_PENDING",
                "action": "PAYOUT_SUCCESS",
                "nextState": "RESOLVED",
                "roles": ["SYSTEM"],
                "active": true
              },
              {
                "tenantId": "ncr",
                "currentState": "PAYOUT_PENDING",
                "action": "PAYOUT_FAILED",
                "nextState": "PAYOUT_FAILED",
                "roles": ["SYSTEM"],
                "active": true
              }
            ]
          },
          {
            "tenantId": "ncr",
            "sla": null,
            "state": "PAYOUT_FAILED",
            "applicationStatus": "PAYOUT_FAILED",
            "docUploadRequired": false,
            "isStartState": false,
            "isTerminateState": false,
            "isStateUpdatable": false,
            "actions": [
              {
                "tenantId": "ncr",
                "currentState": "PAYOUT_FAILED",
                "action": "RETRY_PAYOUT",
                "nextState": "PAYOUT_PENDING",
                "roles": ["STATE_ADMIN", "SYSTEM"],
                "active": true
              },
              {
                "tenantId": "ncr",
                "currentState": "PAYOUT_FAILED",
                "action": "MANUAL_RESOLVE",
                "nextState": "RESOLVED",
                "roles": ["STATE_ADMIN"],
                "active": true
              }
            ]
          },
          {
            "tenantId": "ncr",
            "sla": null,
            "state": "RESOLVED",
            "applicationStatus": "RESOLVED",
            "docUploadRequired": false,
            "isStartState": false,
            "isTerminateState": true,
            "isStateUpdatable": false,
            "actions": []
          },
          {
            "tenantId": "ncr",
            "sla": null,
            "state": "UNABLE_TO_LOCATE",
            "applicationStatus": "UNABLE_TO_LOCATE",
            "docUploadRequired": false,
            "isStartState": false,
            "isTerminateState": true,
            "isStateUpdatable": false,
            "actions": []
          }
        ]
      },

      "documents": [
        {
          "code": "SDCRS_DOG_PHOTO",
          "name": "dog-photo",
          "active": true,
          "isMandatory": true,
          "maxSizeInMB": 5,
          "maxFilesAllowed": 3,
          "allowedFileTypes": ["jpeg", "jpg", "png"],
          "workflowAction": ["SUBMIT"]
        },
        {
          "code": "SDCRS_REPORTER_SELFIE",
          "name": "reporter-selfie",
          "active": true,
          "isMandatory": true,
          "maxSizeInMB": 5,
          "maxFilesAllowed": 1,
          "allowedFileTypes": ["jpeg", "jpg", "png"],
          "workflowAction": ["SUBMIT"]
        },
        {
          "code": "SDCRS_CAPTURE_PROOF",
          "name": "capture-proof",
          "active": true,
          "isMandatory": true,
          "maxSizeInMB": 5,
          "maxFilesAllowed": 3,
          "allowedFileTypes": ["jpeg", "jpg", "png"],
          "workflowAction": ["MARK_CAPTURED"]
        },
        {
          "code": "SDCRS_SHELTER_RECEIPT",
          "name": "shelter-receipt",
          "active": true,
          "isMandatory": false,
          "maxSizeInMB": 5,
          "maxFilesAllowed": 1,
          "allowedFileTypes": ["jpeg", "jpg", "png", "pdf"],
          "workflowAction": ["MARK_CAPTURED"]
        }
      ],

      "idgen": [
        {
          "name": "reportNumber",
          "idname": "sdcrs.reportnumber",
          "format": "DJ-SDCRS-[fy:yyyy-yy]-[SEQ_SDCRS_REPORT]",
          "type": "report"
        },
        {
          "name": "trackingId",
          "idname": "sdcrs.trackingid",
          "format": "[CITY.CODE]-[SEQ_SDCRS_TRACK_6]",
          "type": "tracking"
        }
      ],

      "boundary": {
        "hierarchyType": "ADMIN",
        "lowestLevel": "locality"
      },

      "applicant": {
        "types": ["TEACHER"],
        "minimum": 1,
        "maximum": 1
      },

      "access": {
        "roles": ["TEACHER", "VERIFIER", "MC_OFFICER", "MC_SUPERVISOR", "DISTRICT_ADMIN", "STATE_ADMIN"]
      },

      "inbox": {
        "index": "sdcrs-services",
        "module": "SDCRS",
        "sortBy": {
          "path": "Data.auditDetails.createdTime",
          "defaultOrder": "DESC"
        },
        "allowedSearchCriteria": [
          {
            "name": "reportNumber",
            "path": "Data.reportNumber.keyword",
            "operator": "EQUAL",
            "isMandatory": false
          },
          {
            "name": "status",
            "path": "Data.applicationStatus.keyword",
            "operator": "EQUAL",
            "isMandatory": false
          },
          {
            "name": "reportType",
            "path": "Data.reportType.keyword",
            "operator": "EQUAL",
            "isMandatory": false
          },
          {
            "name": "assignee",
            "path": "Data.assignee.keyword",
            "operator": "EQUAL",
            "isMandatory": false
          },
          {
            "name": "locality",
            "path": "Data.address.locality.code.keyword",
            "operator": "EQUAL",
            "isMandatory": false
          }
        ]
      },

      "payout": {
        "enabled": true,
        "triggerState": "CAPTURED",
        "amount": 500,
        "currency": "INR",
        "method": "UPI",
        "maxMonthlyPerTeacher": 5000,
        "maxDailyReports": 5
      },

      "validation": {
        "autoValidation": true,
        "checks": [
          {
            "name": "GPS_MATCH",
            "enabled": true,
            "toleranceMeters": 100
          },
          {
            "name": "BOUNDARY_CHECK",
            "enabled": true
          },
          {
            "name": "TIMESTAMP_CHECK",
            "enabled": true,
            "maxAgeHours": 48
          },
          {
            "name": "DUPLICATE_IMAGE",
            "enabled": true,
            "similarityThreshold": 0.90
          }
        ]
      },

      "localization": {
        "modules": ["rainmaker-sdcrs"]
      }
    }
  ]
}
```

---

## 2. DocumentConfig (Per-Action Documents)

**Config File:** `data/ncr/DigitStudio/DocumentConfig.SDCRS.json`

```json
{
  "tenantId": "ncr",
  "moduleName": "DigitStudio",
  "DocumentConfig": [
    {
      "module": "SDCRS.STRAY_DOG_REPORT",
      "actions": [
        {
          "action": "SUBMIT",
          "assignee": {
            "show": false,
            "isMandatory": false
          },
          "comments": {
            "show": true,
            "isMandatory": false
          },
          "documents": [
            {
              "code": "SDCRS_DOG_PHOTO",
              "name": "dog-photo",
              "active": true,
              "isMandatory": true,
              "maxSizeInMB": 5,
              "maxFilesAllowed": 3,
              "allowedFileTypes": ["jpeg", "jpg", "png"],
              "showTextInput": false,
              "extractExif": true
            },
            {
              "code": "SDCRS_REPORTER_SELFIE",
              "name": "reporter-selfie",
              "active": true,
              "isMandatory": true,
              "maxSizeInMB": 5,
              "maxFilesAllowed": 1,
              "allowedFileTypes": ["jpeg", "jpg", "png"],
              "showTextInput": false,
              "requireFrontCamera": true
            }
          ]
        },
        {
          "action": "VERIFY",
          "assignee": {
            "show": false,
            "isMandatory": false
          },
          "comments": {
            "show": true,
            "isMandatory": false
          },
          "documents": []
        },
        {
          "action": "REJECT",
          "assignee": {
            "show": false,
            "isMandatory": false
          },
          "comments": {
            "show": true,
            "isMandatory": true
          },
          "documents": []
        },
        {
          "action": "MARK_DUPLICATE",
          "assignee": {
            "show": false,
            "isMandatory": false
          },
          "comments": {
            "show": true,
            "isMandatory": true
          },
          "documents": []
        },
        {
          "action": "ASSIGN",
          "assignee": {
            "show": true,
            "isMandatory": true,
            "roles": ["MC_OFFICER"]
          },
          "comments": {
            "show": true,
            "isMandatory": false
          },
          "documents": []
        },
        {
          "action": "START_FIELD_VISIT",
          "assignee": {
            "show": false,
            "isMandatory": false
          },
          "comments": {
            "show": true,
            "isMandatory": false
          },
          "documents": []
        },
        {
          "action": "MARK_CAPTURED",
          "assignee": {
            "show": false,
            "isMandatory": false
          },
          "comments": {
            "show": true,
            "isMandatory": true
          },
          "documents": [
            {
              "code": "SDCRS_CAPTURE_PROOF",
              "name": "capture-proof",
              "active": true,
              "isMandatory": true,
              "maxSizeInMB": 5,
              "maxFilesAllowed": 3,
              "allowedFileTypes": ["jpeg", "jpg", "png"],
              "showTextInput": false
            },
            {
              "code": "SDCRS_SHELTER_RECEIPT",
              "name": "shelter-receipt",
              "active": true,
              "isMandatory": false,
              "maxSizeInMB": 5,
              "maxFilesAllowed": 1,
              "allowedFileTypes": ["jpeg", "jpg", "png", "pdf"],
              "showTextInput": false
            }
          ]
        },
        {
          "action": "MARK_UNABLE_TO_LOCATE",
          "assignee": {
            "show": false,
            "isMandatory": false
          },
          "comments": {
            "show": true,
            "isMandatory": true
          },
          "documents": [
            {
              "code": "SDCRS_LOCATION_VISIT_PROOF",
              "name": "location-visit-proof",
              "active": true,
              "isMandatory": false,
              "maxSizeInMB": 5,
              "maxFilesAllowed": 2,
              "allowedFileTypes": ["jpeg", "jpg", "png"],
              "showTextInput": false
            }
          ]
        },
        {
          "action": "MANUAL_RESOLVE",
          "assignee": {
            "show": false,
            "isMandatory": false
          },
          "comments": {
            "show": true,
            "isMandatory": true
          },
          "documents": [
            {
              "code": "SDCRS_MANUAL_PAYMENT_PROOF",
              "name": "manual-payment-proof",
              "active": true,
              "isMandatory": true,
              "maxSizeInMB": 5,
              "maxFilesAllowed": 1,
              "allowedFileTypes": ["jpeg", "jpg", "png", "pdf"],
              "showTextInput": true
            }
          ]
        }
      ]
    }
  ]
}
```

---

## 3. Access Control Configuration

Access control in DIGIT requires **three** separate configuration files:

| File | Purpose |
|------|---------|
| `ACCESSCONTROL-ROLES/roles.SDCRS.json` | Role definitions (code, name, description) |
| `ACCESSCONTROL-ACTIONS-TEST/actions-test.SDCRS.json` | Action definitions (id, url, name, serviceCode) |
| `ACCESSCONTROL-ROLEACTIONS/roleactions.SDCRS.json` | Role-to-action mappings (rolecode → actionid) |

### 3.1 Roles

**Config File:** `data/ncr/ACCESSCONTROL-ROLES/roles.SDCRS.json`

```json
{
  "tenantId": "ncr",
  "moduleName": "ACCESSCONTROL-ROLES",
  "roles": [
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
      "description": "Supervisor for MC Officers, assigns field work"
    },
    {
      "code": "DISTRICT_ADMIN",
      "name": "District Admin",
      "description": "District-level program administrator"
    },
    {
      "code": "STATE_ADMIN",
      "name": "State Admin",
      "description": "State-level program administrator"
    }
  ]
}
```

### 3.2 Actions (API URLs)

**Config File:** `data/ncr/ACCESSCONTROL-ACTIONS-TEST/actions-test.SDCRS.json`

```json
{
  "tenantId": "ncr",
  "moduleName": "ACCESSCONTROL-ACTIONS-TEST",
  "actions-test": [
    {
      "id": 5001,
      "url": "/sdcrs-services/v1/report/_create",
      "name": "SDCRS Report Create",
      "displayName": "Create Stray Dog Report",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 1
    },
    {
      "id": 5002,
      "url": "/sdcrs-services/v1/report/_update",
      "name": "SDCRS Report Update",
      "displayName": "Update Stray Dog Report",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 2
    },
    {
      "id": 5003,
      "url": "/sdcrs-services/v1/report/_search",
      "name": "SDCRS Report Search",
      "displayName": "Search Stray Dog Reports",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 3
    },
    {
      "id": 5004,
      "url": "/sdcrs-services/v1/report/_track",
      "name": "SDCRS Report Track",
      "displayName": "Track Stray Dog Report",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 4
    },
    {
      "id": 5005,
      "url": "/sdcrs-services/v1/report/_count",
      "name": "SDCRS Report Count",
      "displayName": "Count Stray Dog Reports",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 5
    },
    {
      "id": 5006,
      "url": "/payout/v1/_create",
      "name": "SDCRS Payout Create",
      "displayName": "Create Payout",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 6
    },
    {
      "id": 5007,
      "url": "/payout/v1/_search",
      "name": "SDCRS Payout Search",
      "displayName": "Search Payouts",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 7
    },
    {
      "id": 5008,
      "url": "/payout/v1/_retry",
      "name": "SDCRS Payout Retry",
      "displayName": "Retry Failed Payout",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 8
    },
    {
      "id": 5009,
      "url": "/fraud/v1/_check",
      "name": "SDCRS Fraud Check",
      "displayName": "Check Fraud Risk",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 9
    },
    {
      "id": 5010,
      "url": "/fraud/v1/_search",
      "name": "SDCRS Fraud Search",
      "displayName": "Search Fraud Records",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 10
    },
    {
      "id": 5011,
      "url": "/sdcrs-services/v1/inbox/_search",
      "name": "SDCRS Inbox Search",
      "displayName": "Search SDCRS Inbox",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 11
    },
    {
      "id": 5012,
      "url": "/sdcrs-services/v1/inbox/_count",
      "name": "SDCRS Inbox Count",
      "displayName": "Count SDCRS Inbox",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 12
    },
    {
      "id": 5013,
      "url": "/sdcrs-services/v1/assignment/_create",
      "name": "SDCRS Assignment Create",
      "displayName": "Assign Report to Officer",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 13
    },
    {
      "id": 5014,
      "url": "/sdcrs-services/v1/assignment/_search",
      "name": "SDCRS Assignment Search",
      "displayName": "Search Assignments",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 14
    },
    {
      "id": 5015,
      "url": "/sdcrs-services/v1/dashboard/_stats",
      "name": "SDCRS Dashboard Stats",
      "displayName": "View Dashboard Statistics",
      "serviceCode": "SDCRS",
      "enabled": true,
      "orderNumber": 15
    }
  ]
}
```

### 3.3 Role-Action Mappings

**Config File:** `data/ncr/ACCESSCONTROL-ROLEACTIONS/roleactions.SDCRS.json`

Role-action mappings control which roles can access which API endpoints:

| actionid | url | Roles with Access |
|----------|-----|-------------------|
| 5001 | `/sdcrs-services/v1/report/_create` | TEACHER |
| 5002 | `/sdcrs-services/v1/report/_update` | VERIFIER, MC_OFFICER, MC_SUPERVISOR, SYSTEM |
| 5003 | `/sdcrs-services/v1/report/_search` | TEACHER, VERIFIER, MC_OFFICER, MC_SUPERVISOR, DISTRICT_ADMIN, STATE_ADMIN |
| 5004 | `/sdcrs-services/v1/report/_track` | TEACHER, MC_OFFICER |
| 5005 | `/sdcrs-services/v1/report/_count` | DISTRICT_ADMIN, STATE_ADMIN |
| 5006 | `/payout/v1/_create` | SYSTEM |
| 5007 | `/payout/v1/_search` | DISTRICT_ADMIN, STATE_ADMIN |
| 5008 | `/payout/v1/_retry` | STATE_ADMIN |
| 5009 | `/fraud/v1/_check` | SYSTEM |
| 5010 | `/fraud/v1/_search` | DISTRICT_ADMIN, STATE_ADMIN |
| 5011 | `/sdcrs-services/v1/inbox/_search` | VERIFIER, MC_OFFICER, MC_SUPERVISOR |
| 5012 | `/sdcrs-services/v1/inbox/_count` | VERIFIER, MC_OFFICER, MC_SUPERVISOR |
| 5013 | `/sdcrs-services/v1/assignment/_create` | MC_SUPERVISOR |
| 5014 | `/sdcrs-services/v1/assignment/_search` | MC_SUPERVISOR |
| 5015 | `/sdcrs-services/v1/dashboard/_stats` | DISTRICT_ADMIN, STATE_ADMIN |

```json
{
  "tenantId": "ncr",
  "moduleName": "ACCESSCONTROL-ROLEACTIONS",
  "roleactions": [
    { "rolecode": "TEACHER", "actionid": 5001, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "TEACHER", "actionid": 5003, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "TEACHER", "actionid": 5004, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "VERIFIER", "actionid": 5002, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "VERIFIER", "actionid": 5003, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "VERIFIER", "actionid": 5011, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "VERIFIER", "actionid": 5012, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "MC_OFFICER", "actionid": 5002, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "MC_OFFICER", "actionid": 5003, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "MC_OFFICER", "actionid": 5004, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "MC_OFFICER", "actionid": 5011, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "MC_OFFICER", "actionid": 5012, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "MC_SUPERVISOR", "actionid": 5002, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "MC_SUPERVISOR", "actionid": 5003, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "MC_SUPERVISOR", "actionid": 5011, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "MC_SUPERVISOR", "actionid": 5012, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "MC_SUPERVISOR", "actionid": 5013, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "MC_SUPERVISOR", "actionid": 5014, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "DISTRICT_ADMIN", "actionid": 5003, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "DISTRICT_ADMIN", "actionid": 5005, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "DISTRICT_ADMIN", "actionid": 5007, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "DISTRICT_ADMIN", "actionid": 5010, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "DISTRICT_ADMIN", "actionid": 5015, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "STATE_ADMIN", "actionid": 5003, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "STATE_ADMIN", "actionid": 5005, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "STATE_ADMIN", "actionid": 5007, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "STATE_ADMIN", "actionid": 5008, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "STATE_ADMIN", "actionid": 5010, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "STATE_ADMIN", "actionid": 5015, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "SYSTEM", "actionid": 5002, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "SYSTEM", "actionid": 5006, "actioncode": "", "tenantId": "ncr" },
    { "rolecode": "SYSTEM", "actionid": 5009, "actioncode": "", "tenantId": "ncr" }
  ]
}
```

---

## 4. MDMS Master Data

### 4.1 Report Types

**Config File:** `data/ncr/SDCRS/ReportType.json`

```json
{
  "tenantId": "ncr",
  "moduleName": "SDCRS",
  "ReportType": [
    {
      "code": "AGGRESSIVE",
      "name": "Aggressive Stray Dog",
      "description": "Dog showing aggressive behavior, potential bite risk",
      "priority": "HIGH",
      "slaHours": 24,
      "active": true,
      "order": 1
    },
    {
      "code": "INJURED",
      "name": "Injured Stray Dog",
      "description": "Dog appears injured, sick, or in distress",
      "priority": "HIGH",
      "slaHours": 24,
      "active": true,
      "order": 2
    },
    {
      "code": "PACK",
      "name": "Stray Dog Pack",
      "description": "Group of 3 or more stray dogs in an area",
      "priority": "MEDIUM",
      "slaHours": 48,
      "active": true,
      "order": 3
    },
    {
      "code": "STANDARD",
      "name": "Standard Sighting",
      "description": "Regular stray dog sighting",
      "priority": "NORMAL",
      "slaHours": 72,
      "active": true,
      "order": 4
    }
  ]
}
```

### 4.2 Resolution Types

**Config File:** `data/ncr/SDCRS/ResolutionType.json`

```json
{
  "tenantId": "ncr",
  "moduleName": "SDCRS",
  "ResolutionType": [
    {
      "code": "CAPTURED",
      "name": "Captured",
      "description": "Dog captured and taken to shelter",
      "payoutEligible": true,
      "active": true
    },
    {
      "code": "RELOCATED",
      "name": "Relocated",
      "description": "Dog relocated to safer area",
      "payoutEligible": true,
      "active": true
    },
    {
      "code": "STERILIZED_RELEASED",
      "name": "Sterilized & Released",
      "description": "Dog sterilized and released (ABC program)",
      "payoutEligible": true,
      "active": true
    },
    {
      "code": "UNABLE_TO_LOCATE",
      "name": "Unable to Locate",
      "description": "Dog could not be found at reported location",
      "payoutEligible": false,
      "active": true
    },
    {
      "code": "ALREADY_RESOLVED",
      "name": "Already Resolved",
      "description": "Issue already resolved by other means",
      "payoutEligible": false,
      "active": true
    }
  ]
}
```

### 4.3 Rejection Reasons

**Config File:** `data/ncr/SDCRS/RejectionReason.json`

```json
{
  "tenantId": "ncr",
  "moduleName": "SDCRS",
  "RejectionReason": [
    {
      "code": "UNCLEAR_PHOTO",
      "name": "Unclear Photo",
      "description": "Photo is blurry or dog is not clearly visible",
      "type": "MANUAL",
      "active": true
    },
    {
      "code": "NO_DOG_VISIBLE",
      "name": "No Dog Visible",
      "description": "No dog visible in the submitted photo",
      "type": "MANUAL",
      "active": true
    },
    {
      "code": "DUPLICATE_REPORT",
      "name": "Duplicate Report",
      "description": "This dog has already been reported",
      "type": "MANUAL",
      "active": true
    },
    {
      "code": "OUTSIDE_BOUNDARY",
      "name": "Outside Service Area",
      "description": "Location is outside the service boundary",
      "type": "AUTO",
      "active": true
    },
    {
      "code": "INVALID_LOCATION",
      "name": "Invalid Location",
      "description": "GPS coordinates do not match photo EXIF data",
      "type": "AUTO",
      "active": true
    },
    {
      "code": "STALE_PHOTO",
      "name": "Stale Photo",
      "description": "Photo is older than 48 hours",
      "type": "AUTO",
      "active": true
    },
    {
      "code": "DUPLICATE_IMAGE",
      "name": "Duplicate Image",
      "description": "Image matches a previously submitted report",
      "type": "AUTO",
      "active": true
    },
    {
      "code": "FRAUDULENT_SUBMISSION",
      "name": "Fraudulent Submission",
      "description": "Report flagged as potentially fraudulent",
      "type": "MANUAL",
      "active": true
    }
  ]
}
```

### 4.4 Payout Configuration

**Config File:** `data/ncr/SDCRS/PayoutConfig.json`

```json
{
  "tenantId": "ncr",
  "moduleName": "SDCRS",
  "PayoutConfig": [
    {
      "code": "DEFAULT",
      "amountPerCapture": 500,
      "currency": "INR",
      "paymentMethod": "UPI",
      "maxMonthlyPayoutPerTeacher": 5000,
      "maxDailyReportsPerTeacher": 5,
      "minIntervalBetweenReportsMinutes": 30,
      "payoutTriggerState": "CAPTURED",
      "eligibleResolutionTypes": ["CAPTURED", "RELOCATED", "STERILIZED_RELEASED"],
      "active": true
    }
  ]
}
```

---

## 5. Workflow State Summary

| State | SLA | Terminal | Payout | Actions Available |
|-------|-----|----------|--------|-------------------|
| `PENDING_VALIDATION` | 1 hour | No | No | AUTO_VALIDATE_PASS, AUTO_VALIDATE_FAIL |
| `AUTO_REJECTED` | - | Yes | No | - |
| `PENDING_VERIFICATION` | 24 hours | No | No | VERIFY, REJECT, MARK_DUPLICATE |
| `REJECTED` | - | Yes | No | - |
| `DUPLICATE` | - | Yes | No | - |
| `VERIFIED` | 1 hour | No | No | ASSIGN |
| `ASSIGNED` | 48 hours | No | No | START_FIELD_VISIT, REASSIGN |
| `IN_PROGRESS` | 24 hours | No | No | MARK_CAPTURED, MARK_UNABLE_TO_LOCATE |
| `CAPTURED` | 1 hour | No | No | INITIATE_PAYOUT (auto) |
| `PAYOUT_PENDING` | 24 hours | No | No | PAYOUT_SUCCESS, PAYOUT_FAILED |
| `PAYOUT_FAILED` | - | No | No | RETRY_PAYOUT, MANUAL_RESOLVE |
| `RESOLVED` | - | Yes | Yes | - |
| `UNABLE_TO_LOCATE` | - | Yes | No | - |

---

## 6. Comparison with Other Options

| Aspect | Option A (CCRS) | Option B (Custom) | Option D (Studio) |
|--------|-----------------|-------------------|-------------------|
| **Custom Code** | Minimal | Full service | None |
| **Data Model** | JSONB additionalDetail | Custom schema | Typed fields |
| **Form Rendering** | Manual | Manual | Auto from config |
| **Workflow** | Separate config | Separate config | Embedded in config |
| **Documents** | Manual handling | Manual handling | Per-action config |
| **Validation** | Custom service | Custom service | Configurable |
| **Maintainability** | Medium | High effort | Low - config only |

---

## 7. Configuration File Summary

All configuration files for Option D:

| Path | Description |
|------|-------------|
| `data/ncr/Studio/SDCRS.ServiceConfiguration.json` | Complete form fields + workflow |
| `data/ncr/DigitStudio/DocumentConfig.SDCRS.json` | Per-action document requirements |
| `data/ncr/ACCESSCONTROL-ROLES/roles.SDCRS.json` | SDCRS role definitions |
| `data/ncr/ACCESSCONTROL-ACTIONS-TEST/actions-test.SDCRS.json` | API URL action definitions |
| `data/ncr/ACCESSCONTROL-ROLEACTIONS/roleactions.SDCRS.json` | Role-to-action mappings |
| `data/ncr/SDCRS/ReportType.json` | Report type master data |
| `data/ncr/SDCRS/ResolutionType.json` | Resolution type master data |
| `data/ncr/SDCRS/RejectionReason.json` | Rejection reason master data |
| `data/ncr/SDCRS/PayoutConfig.json` | Payout configuration |

---

## 8. Implementation Checklist

- [x] Create `SDCRS.ServiceConfiguration.json` in Studio module
- [x] Create `DocumentConfig.SDCRS.json` in DigitStudio module
- [x] Add SDCRS roles to ACCESSCONTROL-ROLES
- [x] Create ACCESSCONTROL-ACTIONS-TEST with API URLs
- [x] Create ACCESSCONTROL-ROLEACTIONS with role-to-action mappings
- [x] Create SDCRS MDMS module with master data
- [ ] Configure ID generation formats (idgen service)
- [ ] Add localization keys for all labels
- [ ] Configure inbox search criteria
- [ ] Set up auto-validation service (hooks into Studio)
- [ ] Configure UPI payout adapter integration
- [ ] Test form rendering on frontend

---

## 9. Custom Logic Still Required

Even with DIGIT Studio, some custom logic is needed:

| Logic | Implementation |
|-------|----------------|
| **Auto-validation** | Validation service triggered on SUBMIT |
| **Image hash computation** | Service to compute pHash for duplicate detection |
| **EXIF extraction** | Extract GPS/timestamp from uploaded photos |
| **UPI Payout** | UPI Payout Adapter for teacher payments |
| **Fraud scoring** | Fraud detection service integration |

These can be implemented as **hooks** or **consumers** that listen to workflow transitions rather than custom endpoints.

---

*Document Version: 1.0*
*Last Updated: December 2025*
*Option: D - DIGIT Studio Configuration*
