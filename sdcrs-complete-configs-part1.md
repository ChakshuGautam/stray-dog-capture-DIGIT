# SDCRS Complete Configuration Files
## Ready for Deployment - All Pending Items

This document contains ALL configuration files needed to deploy SDCRS on DIGIT platform, based on the official DIGIT documentation review.

---

# TABLE OF CONTENTS

1. [MDMS Configuration Files](#1-mdms-configuration-files)
2. [Workflow Business Service Configuration](#2-workflow-business-service-configuration)
3. [Persister Configuration](#3-persister-configuration)
4. [Indexer Configuration](#4-indexer-configuration)
5. [Access Control Configuration](#5-access-control-configuration)
6. [Localization Messages](#6-localization-messages)
7. [Inbox Service Configuration](#7-inbox-service-configuration)
8. [Security Policy Configuration](#8-security-policy-configuration)
9. [ID Generation Format](#9-id-generation-format)
10. [Helm/Deployment Configuration](#10-helmdeployment-configuration)
11. [UI Configuration (DIGIT-UI)](#11-ui-configuration-digit-ui)
12. [Flutter Mobile App Configuration](#12-flutter-mobile-app-configuration)
13. [DSS Dashboard Configuration](#13-dss-dashboard-configuration)
14. [PDF Service Configuration](#14-pdf-service-configuration)

---

# 1. MDMS CONFIGURATION FILES

## 1.1 master-config.json (Add to existing)

```json
{
  "SDCRS": {
    "ConditionTags": {
      "masterName": "ConditionTags",
      "isStateLevel": true,
      "uniqueKeys": ["code"]
    },
    "PayoutConfig": {
      "masterName": "PayoutConfig",
      "isStateLevel": true,
      "uniqueKeys": ["code"]
    },
    "PenaltyConfig": {
      "masterName": "PenaltyConfig",
      "isStateLevel": true,
      "uniqueKeys": []
    },
    "RejectionReasons": {
      "masterName": "RejectionReasons",
      "isStateLevel": true,
      "uniqueKeys": ["code"]
    },
    "MCResolutions": {
      "masterName": "MCResolutions",
      "isStateLevel": true,
      "uniqueKeys": ["code"]
    },
    "FraudIndicators": {
      "masterName": "FraudIndicators",
      "isStateLevel": true,
      "uniqueKeys": ["code"]
    },
    "UIConfig": {
      "masterName": "UIConfig",
      "isStateLevel": true,
      "uniqueKeys": ["code"]
    }
  }
}
```

## 1.2 File: `data/pb/SDCRS/ConditionTags.json`

```json
{
  "tenantId": "pb",
  "moduleName": "SDCRS",
  "ConditionTags": [
    {
      "code": "NORMAL",
      "name": "Normal",
      "description": "Dog appears healthy with no visible issues",
      "bonusPoints": 0,
      "active": true,
      "order": 1
    },
    {
      "code": "INJURED",
      "name": "Injured",
      "description": "Dog has visible injuries requiring attention",
      "bonusPoints": 25,
      "active": true,
      "order": 2
    },
    {
      "code": "AGGRESSIVE",
      "name": "Aggressive",
      "description": "Dog showing aggressive behavior",
      "bonusPoints": 25,
      "active": true,
      "order": 3
    },
    {
      "code": "PUPPIES",
      "name": "With Puppies",
      "description": "Dog accompanied by puppies",
      "bonusPoints": 10,
      "active": true,
      "order": 4
    },
    {
      "code": "COLLARED",
      "name": "Collared",
      "description": "Dog has a collar (may be owned)",
      "bonusPoints": 0,
      "active": true,
      "order": 5
    },
    {
      "code": "RABIES_SUSPECT",
      "name": "Rabies Suspect",
      "description": "Dog showing signs of rabies - URGENT",
      "bonusPoints": 50,
      "active": true,
      "order": 6
    }
  ]
}
```

## 1.3 File: `data/pb/SDCRS/PayoutConfig.json`

```json
{
  "tenantId": "pb",
  "moduleName": "SDCRS",
  "PayoutConfig": [
    {
      "code": "DEFAULT",
      "basePoints": 50,
      "pointsToCurrencyRatio": 1.0,
      "monthlyCap": 5000,
      "payoutCycle": "WEEKLY",
      "payoutDay": "FRIDAY",
      "minimumPayoutAmount": 100,
      "bonusConfig": {
        "newLocalityBonus": 25,
        "newLocalityWindowDays": 30,
        "firstReportBonus": 100,
        "consecutiveDaysBonus": 10,
        "consecutiveDaysThreshold": 7
      },
      "active": true
    }
  ]
}
```

## 1.4 File: `data/pb/SDCRS/PenaltyConfig.json`

```json
{
  "tenantId": "pb",
  "moduleName": "SDCRS",
  "PenaltyConfig": [
    {
      "code": "DEFAULT",
      "levels": [
        {
          "level": 1,
          "name": "WARNING",
          "fraudCountThreshold": 1,
          "penaltyType": "WARNING",
          "penaltyDays": 0,
          "pointsDeduction": 0,
          "description": "First offense - warning issued"
        },
        {
          "level": 2,
          "name": "COOLDOWN",
          "fraudCountThreshold": 3,
          "penaltyType": "SUSPENSION",
          "penaltyDays": 7,
          "pointsDeduction": 100,
          "description": "7-day cooldown period"
        },
        {
          "level": 3,
          "name": "SUSPENSION",
          "fraudCountThreshold": 5,
          "penaltyType": "SUSPENSION",
          "penaltyDays": 30,
          "pointsDeduction": 500,
          "description": "30-day suspension"
        },
        {
          "level": 4,
          "name": "BAN",
          "fraudCountThreshold": 10,
          "penaltyType": "PERMANENT_BAN",
          "penaltyDays": -1,
          "pointsDeduction": -1,
          "description": "Permanent ban from program"
        }
      ],
      "active": true
    }
  ]
}
```

## 1.5 File: `data/pb/SDCRS/RejectionReasons.json`

```json
{
  "tenantId": "pb",
  "moduleName": "SDCRS",
  "RejectionReasons": [
    {
      "code": "DUPLICATE",
      "name": "Duplicate Report",
      "description": "Same dog already reported",
      "isFraud": false,
      "active": true
    },
    {
      "code": "FAKE_IMAGE",
      "name": "Fake/AI-Generated Image",
      "description": "Image appears to be fake or AI-generated",
      "isFraud": true,
      "active": true
    },
    {
      "code": "NOT_A_DOG",
      "name": "Not a Dog",
      "description": "Image does not contain a dog",
      "isFraud": true,
      "active": true
    },
    {
      "code": "GPS_SPOOFED",
      "name": "GPS Spoofed",
      "description": "GPS location appears to be spoofed",
      "isFraud": true,
      "active": true
    },
    {
      "code": "TIMESTAMP_INVALID",
      "name": "Invalid Timestamp",
      "description": "Photo timestamp doesn't match submission time",
      "isFraud": true,
      "active": true
    },
    {
      "code": "SELFIE_MISMATCH",
      "name": "Selfie Mismatch",
      "description": "Selfie doesn't match registered teacher",
      "isFraud": true,
      "active": true
    },
    {
      "code": "POOR_QUALITY",
      "name": "Poor Image Quality",
      "description": "Image quality too poor to verify",
      "isFraud": false,
      "active": true
    },
    {
      "code": "STOCK_IMAGE",
      "name": "Stock/Internet Image",
      "description": "Image found on internet",
      "isFraud": true,
      "active": true
    },
    {
      "code": "OTHER",
      "name": "Other",
      "description": "Other reason (requires comment)",
      "isFraud": false,
      "active": true
    }
  ]
}
```

## 1.6 File: `data/pb/SDCRS/MCResolutions.json`

```json
{
  "tenantId": "pb",
  "moduleName": "SDCRS",
  "MCResolutions": [
    {
      "code": "CAPTURED",
      "name": "Dog Captured",
      "description": "Dog was successfully captured",
      "requiresPhoto": true,
      "active": true
    },
    {
      "code": "RELOCATED",
      "name": "Dog Relocated",
      "description": "Dog was relocated to shelter",
      "requiresPhoto": true,
      "active": true
    },
    {
      "code": "STERILIZED",
      "name": "Dog Sterilized",
      "description": "Dog was sterilized and released",
      "requiresPhoto": true,
      "active": true
    },
    {
      "code": "NOT_FOUND",
      "name": "Dog Not Found",
      "description": "Dog could not be located",
      "requiresPhoto": false,
      "active": true
    },
    {
      "code": "DECEASED",
      "name": "Dog Deceased",
      "description": "Dog was found deceased",
      "requiresPhoto": true,
      "active": true
    },
    {
      "code": "ALREADY_HANDLED",
      "name": "Already Handled",
      "description": "Dog was already captured/handled",
      "requiresPhoto": false,
      "active": true
    },
    {
      "code": "OWNED_DOG",
      "name": "Owned Dog",
      "description": "Dog was identified as owned pet",
      "requiresPhoto": false,
      "active": true
    }
  ]
}
```

## 1.7 File: `data/pb/SDCRS/FraudIndicators.json`

```json
{
  "tenantId": "pb",
  "moduleName": "SDCRS",
  "FraudIndicators": [
    {
      "code": "GPS_MISMATCH",
      "name": "GPS Mismatch",
      "description": "Photo GPS doesn't match submission GPS",
      "severity": "HIGH",
      "autoReject": false,
      "thresholdMeters": 500,
      "active": true
    },
    {
      "code": "MOCK_LOCATION",
      "name": "Mock Location Detected",
      "description": "Device using mock location provider",
      "severity": "CRITICAL",
      "autoReject": true,
      "active": true
    },
    {
      "code": "RAPID_SUBMISSION",
      "name": "Rapid Submission",
      "description": "Multiple submissions in short time",
      "severity": "MEDIUM",
      "autoReject": false,
      "thresholdMinutes": 5,
      "maxSubmissions": 3,
      "active": true
    },
    {
      "code": "IMAGE_HASH_MATCH",
      "name": "Duplicate Image",
      "description": "Image hash matches existing submission",
      "severity": "HIGH",
      "autoReject": true,
      "hashThreshold": 0.95,
      "active": true
    },
    {
      "code": "TIMESTAMP_MISMATCH",
      "name": "Timestamp Mismatch",
      "description": "Photo EXIF timestamp differs significantly",
      "severity": "HIGH",
      "autoReject": false,
      "thresholdMinutes": 30,
      "active": true
    },
    {
      "code": "IMPOSSIBLE_TRAVEL",
      "name": "Impossible Travel",
      "description": "Location change too fast to be real",
      "severity": "CRITICAL",
      "autoReject": true,
      "maxSpeedKmh": 120,
      "active": true
    }
  ]
}
```

## 1.8 File: `data/pb/Workflow/AutoEscalation.json` (Add to existing)

```json
{
  "tenantId": "pb",
  "moduleName": "Workflow",
  "AutoEscalation": [
    {
      "businessService": "SDCRS",
      "module": "sdcrs-services",
      "state": "REJECTED",
      "action": "CLOSE",
      "active": "true",
      "stateSLA": 5.0,
      "businessSLA": null,
      "topic": "sdcrs-auto-escalation"
    },
    {
      "businessService": "SDCRS",
      "module": "sdcrs-services", 
      "state": "PENDINGFORVERIFICATION",
      "action": "AUTO_ESCALATE",
      "active": "true",
      "stateSLA": 1.0,
      "businessSLA": null,
      "topic": "sdcrs-auto-escalation"
    },
    {
      "businessService": "SDCRS_MC",
      "module": "sdcrs-mc-services",
      "state": "PENDINGFORASSIGNMENT",
      "action": "AUTO_ESCALATE",
      "active": "true",
      "stateSLA": 1.0,
      "businessSLA": null,
      "topic": "sdcrs-mc-auto-escalation"
    }
  ]
}
```

---

# 2. WORKFLOW BUSINESS SERVICE CONFIGURATION

## 2.1 SDCRS Incident Workflow

**API Call**: `POST /egov-workflow-v2/egov-wf/businessservice/_create`

```json
{
  "RequestInfo": {
    "apiId": "Rainmaker",
    "action": "_create",
    "did": "1",
    "key": "",
    "msgId": "sdcrs-workflow-create",
    "ts": 1234567890,
    "ver": ".01",
    "authToken": "{{auth-token}}",
    "userInfo": {
      "id": 1,
      "uuid": "{{user-uuid}}",
      "userName": "admin",
      "type": "EMPLOYEE",
      "roles": [
        {
          "code": "SUPERUSER",
          "tenantId": "pb"
        }
      ],
      "tenantId": "pb"
    }
  },
  "BusinessServices": [
    {
      "tenantId": "pb",
      "businessService": "SDCRS",
      "business": "sdcrs-services",
      "businessServiceSla": 604800000,
      "states": [
        {
          "sla": null,
          "state": null,
          "applicationStatus": null,
          "docUploadRequired": false,
          "isStartState": true,
          "isTerminateState": false,
          "isStateUpdatable": true,
          "actions": [
            {
              "action": "SUBMIT",
              "nextState": "PENDINGFORVERIFICATION",
              "roles": ["TEACHER"]
            },
            {
              "action": "SUBMIT_AUTOAPPROVE",
              "nextState": "APPROVED",
              "roles": ["TEACHER"]
            },
            {
              "action": "SUBMIT_AUTOREJECT",
              "nextState": "REJECTED",
              "roles": ["TEACHER"]
            }
          ]
        },
        {
          "sla": 86400000,
          "state": "PENDINGFORVERIFICATION",
          "applicationStatus": "PENDINGFORVERIFICATION",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "action": "APPROVE",
              "nextState": "APPROVED",
              "roles": ["VERIFIER", "SDCRS_ADMIN"]
            },
            {
              "action": "REJECT",
              "nextState": "REJECTED",
              "roles": ["VERIFIER", "SDCRS_ADMIN"]
            },
            {
              "action": "MARK_DUPLICATE",
              "nextState": "DUPLICATE",
              "roles": ["VERIFIER", "SDCRS_ADMIN"]
            },
            {
              "action": "ESCALATE",
              "nextState": "PENDINGATADMIN",
              "roles": ["VERIFIER"]
            },
            {
              "action": "SENDBACK",
              "nextState": "PENDINGATADMIN",
              "roles": ["VERIFIER"]
            },
            {
              "action": "AUTO_ESCALATE",
              "nextState": "PENDINGATADMIN",
              "roles": ["AUTO_ESCALATE"]
            }
          ]
        },
        {
          "sla": 172800000,
          "state": "PENDINGATADMIN",
          "applicationStatus": "PENDINGATADMIN",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "action": "APPROVE",
              "nextState": "APPROVED",
              "roles": ["SDCRS_ADMIN"]
            },
            {
              "action": "REJECT",
              "nextState": "REJECTED",
              "roles": ["SDCRS_ADMIN"]
            },
            {
              "action": "MARK_DUPLICATE",
              "nextState": "DUPLICATE",
              "roles": ["SDCRS_ADMIN"]
            },
            {
              "action": "ASSIGN",
              "nextState": "PENDINGFORVERIFICATION",
              "roles": ["SDCRS_ADMIN"]
            }
          ]
        },
        {
          "sla": 432000000,
          "state": "REJECTED",
          "applicationStatus": "REJECTED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "action": "REOPEN",
              "nextState": "PENDINGFORVERIFICATION",
              "roles": ["TEACHER"]
            },
            {
              "action": "CLOSE",
              "nextState": "CLOSED",
              "roles": ["AUTO_ESCALATE"]
            }
          ]
        },
        {
          "sla": null,
          "state": "APPROVED",
          "applicationStatus": "APPROVED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": true,
          "isStateUpdatable": false,
          "actions": []
        },
        {
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

## 2.2 SDCRS MC Action Workflow

**API Call**: `POST /egov-workflow-v2/egov-wf/businessservice/_create`

```json
{
  "RequestInfo": {
    "apiId": "Rainmaker",
    "action": "_create",
    "msgId": "sdcrs-mc-workflow-create",
    "authToken": "{{auth-token}}"
  },
  "BusinessServices": [
    {
      "tenantId": "pb",
      "businessService": "SDCRS_MC",
      "business": "sdcrs-mc-services",
      "businessServiceSla": 259200000,
      "states": [
        {
          "sla": null,
          "state": null,
          "applicationStatus": null,
          "docUploadRequired": false,
          "isStartState": true,
          "isTerminateState": false,
          "isStateUpdatable": true,
          "actions": [
            {
              "action": "CREATE",
              "nextState": "PENDINGFORASSIGNMENT",
              "roles": ["SDCRS_ADMIN", "SYSTEM"]
            }
          ]
        },
        {
          "sla": 86400000,
          "state": "PENDINGFORASSIGNMENT",
          "applicationStatus": "PENDINGFORASSIGNMENT",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": true,
          "actions": [
            {
              "action": "ASSIGN",
              "nextState": "PENDINGATLME",
              "roles": ["MC_ADMIN"]
            },
            {
              "action": "AUTO_ESCALATE",
              "nextState": "PENDINGATLME",
              "roles": ["AUTO_ESCALATE"]
            }
          ]
        },
        {
          "sla": 172800000,
          "state": "PENDINGATLME",
          "applicationStatus": "PENDINGATLME",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "action": "RESOLVE",
              "nextState": "RESOLVED",
              "roles": ["MC_OFFICER"]
            },
            {
              "action": "SENDBACK",
              "nextState": "PENDINGFORASSIGNMENT",
              "roles": ["MC_OFFICER"]
            },
            {
              "action": "UNABLETOLOCATE",
              "nextState": "UNABLETOLOCATE",
              "roles": ["MC_OFFICER"]
            }
          ]
        },
        {
          "sla": 86400000,
          "state": "UNABLETOLOCATE",
          "applicationStatus": "UNABLETOLOCATE",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "action": "RETRY",
              "nextState": "PENDINGFORASSIGNMENT",
              "roles": ["MC_ADMIN"]
            },
            {
              "action": "CLOSENOTFOUND",
              "nextState": "CLOSEDNOTFOUND",
              "roles": ["MC_ADMIN"]
            }
          ]
        },
        {
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
          "sla": null,
          "state": "CLOSEDNOTFOUND",
          "applicationStatus": "CLOSEDNOTFOUND",
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

# 3. PERSISTER CONFIGURATION

## File: `configs/egov-persister/sdcrs-persister.yml`

```yaml
serviceMaps:
  serviceName: sdcrs-services
  mappings:

  # ═══════════════════════════════════════════════════════════════════════════
  # SAVE SDCRS INCIDENT
  # ═══════════════════════════════════════════════════════════════════════════
  - version: 1.0
    description: Persists SDCRS incident on create
    fromTopic: save-sdcrs-incident
    isTransaction: true
    queryMaps:

    - query: >
        INSERT INTO eg_sdcrs_incident (
          id, tenantid, incidentnumber, teacherid, status,
          dogimagefilestoreid, selfieimagefilestoreid, dogimagehash,
          conditiontags, notes, capturedat,
          latitude, longitude, accuracy,
          district, block, locality, pincode,
          deviceid, appversion, platform,
          createdby, createdtime, lastmodifiedby, lastmodifiedtime
        ) VALUES (
          ?, ?, ?, ?, ?,
          ?, ?, ?,
          ?, ?, ?,
          ?, ?, ?,
          ?, ?, ?, ?,
          ?, ?, ?,
          ?, ?, ?, ?
        );
      basePath: Incident
      jsonMaps:
        - jsonPath: $.Incident.id
        - jsonPath: $.Incident.tenantId
        - jsonPath: $.Incident.incidentNumber
        - jsonPath: $.Incident.teacherId
        - jsonPath: $.Incident.status
        - jsonPath: $.Incident.dogImageFileStoreId
        - jsonPath: $.Incident.selfieImageFileStoreId
        - jsonPath: $.Incident.dogImageHash
        - jsonPath: $.Incident.conditionTags
          type: JSON
          dbType: JSONB
        - jsonPath: $.Incident.notes
        - jsonPath: $.Incident.capturedAt
        - jsonPath: $.Incident.address.latitude
        - jsonPath: $.Incident.address.longitude
        - jsonPath: $.Incident.address.accuracy
        - jsonPath: $.Incident.address.district
        - jsonPath: $.Incident.address.block
        - jsonPath: $.Incident.address.locality
        - jsonPath: $.Incident.address.pincode
        - jsonPath: $.Incident.deviceInfo.deviceId
        - jsonPath: $.Incident.deviceInfo.appVersion
        - jsonPath: $.Incident.deviceInfo.platform
        - jsonPath: $.Incident.auditDetails.createdBy
        - jsonPath: $.Incident.auditDetails.createdTime
        - jsonPath: $.Incident.auditDetails.lastModifiedBy
        - jsonPath: $.Incident.auditDetails.lastModifiedTime

  # ═══════════════════════════════════════════════════════════════════════════
  # UPDATE SDCRS INCIDENT
  # ═══════════════════════════════════════════════════════════════════════════
  - version: 1.0
    description: Updates SDCRS incident on workflow transition
    fromTopic: update-sdcrs-incident
    isTransaction: true
    queryMaps:

    - query: >
        UPDATE eg_sdcrs_incident SET
          status = ?,
          verifiedat = ?,
          verifiedby = ?,
          rejectionreason = ?,
          rejectioncomment = ?,
          duplicateof = ?,
          fraudflags = ?,
          pointsawarded = ?,
          payoutamount = ?,
          payoutstatus = ?,
          payoutreference = ?,
          lastmodifiedby = ?,
          lastmodifiedtime = ?
        WHERE id = ?;
      basePath: Incident
      jsonMaps:
        - jsonPath: $.Incident.status
        - jsonPath: $.Incident.verifiedAt
        - jsonPath: $.Incident.verifiedBy
        - jsonPath: $.Incident.rejectionReason
        - jsonPath: $.Incident.rejectionComment
        - jsonPath: $.Incident.duplicateOf
        - jsonPath: $.Incident.fraudFlags
          type: JSON
          dbType: JSONB
        - jsonPath: $.Incident.pointsAwarded
        - jsonPath: $.Incident.payoutAmount
        - jsonPath: $.Incident.payoutStatus
        - jsonPath: $.Incident.payoutReference
        - jsonPath: $.Incident.auditDetails.lastModifiedBy
        - jsonPath: $.Incident.auditDetails.lastModifiedTime
        - jsonPath: $.Incident.id

  # ═══════════════════════════════════════════════════════════════════════════
  # SAVE VERIFICATION RECORD
  # ═══════════════════════════════════════════════════════════════════════════
  - version: 1.0
    description: Saves verification decision record
    fromTopic: save-sdcrs-verification
    isTransaction: true
    queryMaps:

    - query: >
        INSERT INTO eg_sdcrs_verification (
          id, incidentid, tenantid,
          action, previousstatus, newstatus,
          verifierid, verifiername, verifierrole,
          rejectionreason, comment, fraudflagsdetected,
          createdby, createdtime
        ) VALUES (
          ?, ?, ?,
          ?, ?, ?,
          ?, ?, ?,
          ?, ?, ?,
          ?, ?
        );
      basePath: Verification
      jsonMaps:
        - jsonPath: $.Verification.id
        - jsonPath: $.Verification.incidentId
        - jsonPath: $.Verification.tenantId
        - jsonPath: $.Verification.action
        - jsonPath: $.Verification.previousStatus
        - jsonPath: $.Verification.newStatus
        - jsonPath: $.Verification.verifierId
        - jsonPath: $.Verification.verifierName
        - jsonPath: $.Verification.verifierRole
        - jsonPath: $.Verification.rejectionReason
        - jsonPath: $.Verification.comment
        - jsonPath: $.Verification.fraudFlagsDetected
          type: JSON
          dbType: JSONB
        - jsonPath: $.Verification.auditDetails.createdBy
        - jsonPath: $.Verification.auditDetails.createdTime

  # ═══════════════════════════════════════════════════════════════════════════
  # SAVE MC ACTION
  # ═══════════════════════════════════════════════════════════════════════════
  - version: 1.0
    description: Saves MC action record
    fromTopic: save-sdcrs-mc-action
    isTransaction: true
    queryMaps:

    - query: >
        INSERT INTO eg_sdcrs_mc_action (
          id, incidentid, tenantid, actionnumber, status,
          assignedofficer, assignedofficername,
          resolution, resolutionimagefilestoreid, resolutionnotes,
          scheduleddate,
          createdby, createdtime, lastmodifiedby, lastmodifiedtime
        ) VALUES (
          ?, ?, ?, ?, ?,
          ?, ?,
          ?, ?, ?,
          ?,
          ?, ?, ?, ?
        );
      basePath: MCAction
      jsonMaps:
        - jsonPath: $.MCAction.id
        - jsonPath: $.MCAction.incidentId
        - jsonPath: $.MCAction.tenantId
        - jsonPath: $.MCAction.actionNumber
        - jsonPath: $.MCAction.status
        - jsonPath: $.MCAction.assignedOfficer
        - jsonPath: $.MCAction.assignedOfficerName
        - jsonPath: $.MCAction.resolution
        - jsonPath: $.MCAction.resolutionImageFileStoreId
        - jsonPath: $.MCAction.resolutionNotes
        - jsonPath: $.MCAction.scheduledDate
        - jsonPath: $.MCAction.auditDetails.createdBy
        - jsonPath: $.MCAction.auditDetails.createdTime
        - jsonPath: $.MCAction.auditDetails.lastModifiedBy
        - jsonPath: $.MCAction.auditDetails.lastModifiedTime

  # ═══════════════════════════════════════════════════════════════════════════
  # UPDATE MC ACTION
  # ═══════════════════════════════════════════════════════════════════════════
  - version: 1.0
    description: Updates MC action on workflow transition
    fromTopic: update-sdcrs-mc-action
    isTransaction: true
    queryMaps:

    - query: >
        UPDATE eg_sdcrs_mc_action SET
          status = ?,
          assignedofficer = ?,
          assignedofficername = ?,
          resolution = ?,
          resolutionimagefilestoreid = ?,
          resolutionnotes = ?,
          resolvedat = ?,
          lastmodifiedby = ?,
          lastmodifiedtime = ?
        WHERE id = ?;
      basePath: MCAction
      jsonMaps:
        - jsonPath: $.MCAction.status
        - jsonPath: $.MCAction.assignedOfficer
        - jsonPath: $.MCAction.assignedOfficerName
        - jsonPath: $.MCAction.resolution
        - jsonPath: $.MCAction.resolutionImageFileStoreId
        - jsonPath: $.MCAction.resolutionNotes
        - jsonPath: $.MCAction.resolvedAt
        - jsonPath: $.MCAction.auditDetails.lastModifiedBy
        - jsonPath: $.MCAction.auditDetails.lastModifiedTime
        - jsonPath: $.MCAction.id

  # ═══════════════════════════════════════════════════════════════════════════
  # SAVE TEACHER PENALTY
  # ═══════════════════════════════════════════════════════════════════════════
  - version: 1.0
    description: Saves teacher penalty record
    fromTopic: save-sdcrs-penalty
    isTransaction: true
    queryMaps:

    - query: >
        INSERT INTO eg_sdcrs_teacher_penalty (
          id, teacherid, tenantid,
          penaltylevel, penaltytype,
          incidentid, reason,
          startdate, enddate,
          pointsdeducted, isactive,
          createdby, createdtime
        ) VALUES (
          ?, ?, ?,
          ?, ?,
          ?, ?,
          ?, ?,
          ?, ?,
          ?, ?
        );
      basePath: Penalty
      jsonMaps:
        - jsonPath: $.Penalty.id
        - jsonPath: $.Penalty.teacherId
        - jsonPath: $.Penalty.tenantId
        - jsonPath: $.Penalty.penaltyLevel
        - jsonPath: $.Penalty.penaltyType
        - jsonPath: $.Penalty.incidentId
        - jsonPath: $.Penalty.reason
        - jsonPath: $.Penalty.startDate
        - jsonPath: $.Penalty.endDate
        - jsonPath: $.Penalty.pointsDeducted
        - jsonPath: $.Penalty.isActive
        - jsonPath: $.Penalty.auditDetails.createdBy
        - jsonPath: $.Penalty.auditDetails.createdTime

  # ═══════════════════════════════════════════════════════════════════════════
  # SAVE PAYOUT RECORD
  # ═══════════════════════════════════════════════════════════════════════════
  - version: 1.0
    description: Saves payout batch record
    fromTopic: save-sdcrs-payout
    isTransaction: true
    queryMaps:

    - query: >
        INSERT INTO eg_sdcrs_payout (
          id, teacherid, tenantid, payoutnumber,
          periodstart, periodend,
          totalpoints, totalamount,
          incidentcount, status,
          paymentreference, paymentdate, paymentmode,
          bankaccounthash, failurereason,
          createdby, createdtime, lastmodifiedby, lastmodifiedtime
        ) VALUES (
          ?, ?, ?, ?,
          ?, ?,
          ?, ?,
          ?, ?,
          ?, ?, ?,
          ?, ?,
          ?, ?, ?, ?
        );
      basePath: Payout
      jsonMaps:
        - jsonPath: $.Payout.id
        - jsonPath: $.Payout.teacherId
        - jsonPath: $.Payout.tenantId
        - jsonPath: $.Payout.payoutNumber
        - jsonPath: $.Payout.periodStart
        - jsonPath: $.Payout.periodEnd
        - jsonPath: $.Payout.totalPoints
        - jsonPath: $.Payout.totalAmount
        - jsonPath: $.Payout.incidentCount
        - jsonPath: $.Payout.status
        - jsonPath: $.Payout.paymentReference
        - jsonPath: $.Payout.paymentDate
        - jsonPath: $.Payout.paymentMode
        - jsonPath: $.Payout.bankAccountHash
        - jsonPath: $.Payout.failureReason
        - jsonPath: $.Payout.auditDetails.createdBy
        - jsonPath: $.Payout.auditDetails.createdTime
        - jsonPath: $.Payout.auditDetails.lastModifiedBy
        - jsonPath: $.Payout.auditDetails.lastModifiedTime
```

---

# 4. INDEXER CONFIGURATION

## File: `configs/egov-indexer/sdcrs-indexer.yml`

```yaml
ServiceMaps:
  serviceName: SDCRS Indexer
  version: 1.0
  mappings:

  # ═══════════════════════════════════════════════════════════════════════════
  # SDCRS INCIDENT INDEX
  # ═══════════════════════════════════════════════════════════════════════════
  - version: 1.0
    name: sdcrs-incident-indexer
    description: Indexes SDCRS incidents for search and dashboards
    fromTopic: update-sdcrs-incident
    isTransaction: false
    indexes:
      - name: sdcrs-incident
        type: general
        id: $.Incident.id
        isBulk: false
        timeStampField: $.Incident.auditDetails.createdTime
        
        # Mask PII fields
        fieldsToBeMasked:
          - $.Incident.teacherMobileNumber
        
        customJsonMapping:
          indexMapping: |
            {
              "Data": {
                "id": null,
                "tenantId": null,
                "incidentNumber": null,
                "teacherId": null,
                "status": null,
                "district": null,
                "block": null,
                "locality": null,
                "latitude": null,
                "longitude": null,
                "conditionTags": null,
                "pointsAwarded": null,
                "payoutAmount": null,
                "payoutStatus": null,
                "rejectionReason": null,
                "fraudFlags": null,
                "capturedAt": null,
                "verifiedAt": null,
                "createdTime": null,
                "slaStatus": null
              }
            }
          
          fieldMapping:
            - inJsonPath: $.Incident.id
              outJsonPath: $.Data.id
            - inJsonPath: $.Incident.tenantId
              outJsonPath: $.Data.tenantId
            - inJsonPath: $.Incident.incidentNumber
              outJsonPath: $.Data.incidentNumber
            - inJsonPath: $.Incident.teacherId
              outJsonPath: $.Data.teacherId
            - inJsonPath: $.Incident.status
              outJsonPath: $.Data.status
            - inJsonPath: $.Incident.address.district
              outJsonPath: $.Data.district
            - inJsonPath: $.Incident.address.block
              outJsonPath: $.Data.block
            - inJsonPath: $.Incident.address.locality
              outJsonPath: $.Data.locality
            - inJsonPath: $.Incident.address.latitude
              outJsonPath: $.Data.latitude
            - inJsonPath: $.Incident.address.longitude
              outJsonPath: $.Data.longitude
            - inJsonPath: $.Incident.conditionTags
              outJsonPath: $.Data.conditionTags
            - inJsonPath: $.Incident.pointsAwarded
              outJsonPath: $.Data.pointsAwarded
            - inJsonPath: $.Incident.payoutAmount
              outJsonPath: $.Data.payoutAmount
            - inJsonPath: $.Incident.payoutStatus
              outJsonPath: $.Data.payoutStatus
            - inJsonPath: $.Incident.rejectionReason
              outJsonPath: $.Data.rejectionReason
            - inJsonPath: $.Incident.fraudFlags
              outJsonPath: $.Data.fraudFlags
            - inJsonPath: $.Incident.capturedAt
              outJsonPath: $.Data.capturedAt
            - inJsonPath: $.Incident.verifiedAt
              outJsonPath: $.Data.verifiedAt
            - inJsonPath: $.Incident.auditDetails.createdTime
              outJsonPath: $.Data.createdTime

  # ═══════════════════════════════════════════════════════════════════════════
  # MC ACTION INDEX
  # ═══════════════════════════════════════════════════════════════════════════
  - version: 1.0
    name: sdcrs-mc-action-indexer
    description: Indexes MC actions for search and dashboards
    fromTopic: update-sdcrs-mc-action
    isTransaction: false
    indexes:
      - name: sdcrs-mc-action
        type: general
        id: $.MCAction.id
        isBulk: false
        timeStampField: $.MCAction.auditDetails.createdTime
        
        customJsonMapping:
          indexMapping: |
            {
              "Data": {
                "id": null,
                "incidentId": null,
                "tenantId": null,
                "actionNumber": null,
                "status": null,
                "assignedOfficer": null,
                "resolution": null,
                "scheduledDate": null,
                "resolvedAt": null,
                "createdTime": null
              }
            }
          
          fieldMapping:
            - inJsonPath: $.MCAction.id
              outJsonPath: $.Data.id
            - inJsonPath: $.MCAction.incidentId
              outJsonPath: $.Data.incidentId
            - inJsonPath: $.MCAction.tenantId
              outJsonPath: $.Data.tenantId
            - inJsonPath: $.MCAction.actionNumber
              outJsonPath: $.Data.actionNumber
            - inJsonPath: $.MCAction.status
              outJsonPath: $.Data.status
            - inJsonPath: $.MCAction.assignedOfficer
              outJsonPath: $.Data.assignedOfficer
            - inJsonPath: $.MCAction.resolution
              outJsonPath: $.Data.resolution
            - inJsonPath: $.MCAction.scheduledDate
              outJsonPath: $.Data.scheduledDate
            - inJsonPath: $.MCAction.resolvedAt
              outJsonPath: $.Data.resolvedAt
            - inJsonPath: $.MCAction.auditDetails.createdTime
              outJsonPath: $.Data.createdTime

  # ═══════════════════════════════════════════════════════════════════════════
  # PAYOUT INDEX
  # ═══════════════════════════════════════════════════════════════════════════
  - version: 1.0
    name: sdcrs-payout-indexer
    description: Indexes payouts for dashboards
    fromTopic: save-sdcrs-payout
    isTransaction: false
    indexes:
      - name: sdcrs-payout
        type: general
        id: $.Payout.id
        isBulk: false
        timeStampField: $.Payout.auditDetails.createdTime
        
        customJsonMapping:
          indexMapping: |
            {
              "Data": {
                "id": null,
                "teacherId": null,
                "tenantId": null,
                "payoutNumber": null,
                "periodStart": null,
                "periodEnd": null,
                "totalPoints": null,
                "totalAmount": null,
                "incidentCount": null,
                "status": null,
                "paymentDate": null,
                "createdTime": null
              }
            }
          
          fieldMapping:
            - inJsonPath: $.Payout.id
              outJsonPath: $.Data.id
            - inJsonPath: $.Payout.teacherId
              outJsonPath: $.Data.teacherId
            - inJsonPath: $.Payout.tenantId
              outJsonPath: $.Data.tenantId
            - inJsonPath: $.Payout.payoutNumber
              outJsonPath: $.Data.payoutNumber
            - inJsonPath: $.Payout.periodStart
              outJsonPath: $.Data.periodStart
            - inJsonPath: $.Payout.periodEnd
              outJsonPath: $.Data.periodEnd
            - inJsonPath: $.Payout.totalPoints
              outJsonPath: $.Data.totalPoints
            - inJsonPath: $.Payout.totalAmount
              outJsonPath: $.Data.totalAmount
            - inJsonPath: $.Payout.incidentCount
              outJsonPath: $.Data.incidentCount
            - inJsonPath: $.Payout.status
              outJsonPath: $.Data.status
            - inJsonPath: $.Payout.paymentDate
              outJsonPath: $.Data.paymentDate
            - inJsonPath: $.Payout.auditDetails.createdTime
              outJsonPath: $.Data.createdTime
```

---

*Continued in Part 2...*
