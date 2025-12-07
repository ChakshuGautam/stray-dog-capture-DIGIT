# Design Output #3: Service Design

## SDCRS - Stray Dog Capture & Reporting System

---

## Overview

This document provides the service design for SDCRS following the DIGIT Design Guide methodology. It identifies registries, services, workflows, MDMS configurations, and access control mappings required to implement the system on the DIGIT platform.

**Key Business Rule:** Teachers receive payout ONLY after Municipal Corporation successfully captures/resolves the reported dog.

---

## 1. Activity to Service Mapping

### 1.1 Activities List (from Process Workflow)

| Activity | Generalized Activity | Verb | Noun |
|----------|---------------------|------|------|
| Submit stray dog report | Create Dog Report | Create | Dog Report |
| Upload photo evidence | Upload Evidence | Upload | Evidence/File |
| Capture GPS coordinates | Capture Location | Capture | Location |
| Validate submission | Validate Dog Report | Validate | Dog Report |
| Check for duplicates | Detect Duplicate | Detect | Duplicate |
| Route to verification queue | Assign Verifier | Assign | Dog Report |
| Review evidence | Review Dog Report | Review | Dog Report |
| Approve application | Approve Dog Report | Approve | Dog Report |
| Reject application | Reject Dog Report | Reject | Dog Report |
| Mark as duplicate | Flag Duplicate | Flag | Dog Report |
| Assign to MC Officer | Assign MC Officer | Assign | Dog Report |
| Conduct field visit | Update Field Status | Update | Field Visit |
| Mark captured/resolved | Resolve Dog Report | Resolve | Dog Report |
| Mark unable to locate | Close Dog Report | Close | Dog Report |
| Award points | Award Points | Award | Points |
| Process payout | Create Payout | Create | Payout |
| Send notification | Send Notification | Send | Notification |
| View dashboard | View Dashboard | View | Dashboard |
| Generate report | Generate Report | Generate | Report |

### 1.2 Identified Services and Operations

| Service | Operations | Description |
|---------|------------|-------------|
| **Dog Report Service** | Create, Update, Search, Verify, Approve, Reject, Resolve, Close | Core registry for stray dog reports |
| **Evidence Service** | Upload, Validate, Search, Compare | Handles photo/selfie evidence and duplicate detection |
| **Payout Service** | Create, Process, Search, Calculate | Manages teacher payouts after successful capture |
| **Points Service** | Award, Deduct, Search, Calculate | Gamification - leaderboards and rankings |
| **Dashboard Service** | Aggregate, Query | Analytics and reporting aggregations |

---

## 2. DIGIT Reusable Services

Based on the SDCRS requirements, the following DIGIT core services will be reused:

| DIGIT Service | Purpose in SDCRS | Required |
|---------------|------------------|----------|
| **User Service** | Teacher, Verifier, MC Officer authentication | Yes |
| **Role Service** | Role-based access control | Yes |
| **MDMS Service** | Reference data (report types, payout rates, status codes) | Yes |
| **Workflow Service** | Dog report state management | Yes |
| **File Store Service** | Photo and selfie storage | Yes |
| **Location Service** | GPS validation, tenant boundary check | Yes |
| **Notification Service** | SMS/Email alerts to teachers | Yes |
| **Persister Service** | Async database writes via Kafka | Yes |
| **Indexer Service** | Elasticsearch indexing for search/analytics | Yes |
| **Localization Service** | Multi-language support | Yes |
| **Encryption Service** | PII data protection (teacher details) | Yes |
| **PDF Service** | Report/certificate generation | Optional |
| **Dashboard Backend** | DSS analytics integration | Yes |

---

## 3. Workflow Configuration

### 3.1 Workflow States Table

| State | Role(s) Who Can Act | Possible Actions |
|-------|---------------------|------------------|
| `null` (Start) | TEACHER | SUBMIT |
| PENDING_VALIDATION | SYSTEM | AUTO_VALIDATE |
| AUTO_REJECTED | - | - (Terminal) |
| PENDING_VERIFICATION | VERIFIER | VERIFY, REJECT, MARK_DUPLICATE |
| VERIFIED | SYSTEM | ASSIGN_MC |
| ASSIGNED | MC_OFFICER | START_FIELD_VISIT |
| IN_PROGRESS | MC_OFFICER | MARK_CAPTURED, MARK_UNABLE_TO_LOCATE |
| CAPTURED | SYSTEM | PROCESS_PAYOUT |
| RESOLVED | - | - (Terminal - Payout Complete) |
| UNABLE_TO_LOCATE | - | - (Terminal - No Payout) |
| REJECTED | - | - (Terminal) |
| DUPLICATE | - | - (Terminal) |

### 3.2 DIGIT Workflow JSON Configuration

```json
{
  "BusinessServices": [
    {
      "tenantId": "dj",
      "businessService": "SDCRS",
      "business": "sdcrs-services",
      "businessServiceSla": 259200000,
      "states": [
        {
          "sla": null,
          "state": null,
          "applicationStatus": null,
          "docUploadRequired": true,
          "isStartState": true,
          "isTerminateState": false,
          "isStateUpdatable": true,
          "actions": [
            {
              "action": "SUBMIT",
              "nextState": "PENDING_VALIDATION",
              "roles": ["TEACHER"]
            }
          ]
        },
        {
          "sla": 3600000,
          "state": "PENDING_VALIDATION",
          "applicationStatus": "PENDING_VALIDATION",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "action": "AUTO_VALIDATE_PASS",
              "nextState": "PENDING_VERIFICATION",
              "roles": ["SYSTEM"]
            },
            {
              "action": "AUTO_VALIDATE_FAIL",
              "nextState": "AUTO_REJECTED",
              "roles": ["SYSTEM"]
            }
          ]
        },
        {
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
          "sla": 86400000,
          "state": "PENDING_VERIFICATION",
          "applicationStatus": "PENDING_VERIFICATION",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "action": "VERIFY",
              "nextState": "VERIFIED",
              "roles": ["VERIFIER"]
            },
            {
              "action": "REJECT",
              "nextState": "REJECTED",
              "roles": ["VERIFIER"]
            },
            {
              "action": "MARK_DUPLICATE",
              "nextState": "DUPLICATE",
              "roles": ["VERIFIER"]
            }
          ]
        },
        {
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
          "sla": 3600000,
          "state": "VERIFIED",
          "applicationStatus": "VERIFIED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "action": "ASSIGN_MC",
              "nextState": "ASSIGNED",
              "roles": ["SYSTEM"]
            }
          ]
        },
        {
          "sla": 172800000,
          "state": "ASSIGNED",
          "applicationStatus": "ASSIGNED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": true,
          "actions": [
            {
              "action": "START_FIELD_VISIT",
              "nextState": "IN_PROGRESS",
              "roles": ["MC_OFFICER"]
            }
          ]
        },
        {
          "sla": 86400000,
          "state": "IN_PROGRESS",
          "applicationStatus": "IN_PROGRESS",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": true,
          "actions": [
            {
              "action": "MARK_CAPTURED",
              "nextState": "CAPTURED",
              "roles": ["MC_OFFICER"]
            },
            {
              "action": "MARK_UNABLE_TO_LOCATE",
              "nextState": "UNABLE_TO_LOCATE",
              "roles": ["MC_OFFICER"]
            }
          ]
        },
        {
          "sla": 3600000,
          "state": "CAPTURED",
          "applicationStatus": "CAPTURED",
          "docUploadRequired": false,
          "isStartState": false,
          "isTerminateState": false,
          "isStateUpdatable": false,
          "actions": [
            {
              "action": "PROCESS_PAYOUT",
              "nextState": "RESOLVED",
              "roles": ["SYSTEM"]
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
          "state": "UNABLE_TO_LOCATE",
          "applicationStatus": "UNABLE_TO_LOCATE",
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

### 3.3 SLA Configuration

| State | SLA (ms) | SLA (Human Readable) | Escalation |
|-------|----------|----------------------|------------|
| PENDING_VALIDATION | 3,600,000 | 1 hour | Auto-escalate to admin |
| PENDING_VERIFICATION | 86,400,000 | 24 hours | Escalate to senior verifier |
| VERIFIED | 3,600,000 | 1 hour | Auto-assign to nearest MC |
| ASSIGNED | 172,800,000 | 48 hours | Escalate to MC supervisor |
| IN_PROGRESS | 86,400,000 | 24 hours | Escalate to MC supervisor |
| CAPTURED | 3,600,000 | 1 hour | Auto-process payout |

---

## 4. MDMS Reference Data Configuration

### 4.1 Module: SDCRS

#### 4.1.1 Report Types (`ReportType.json`)

```json
{
  "tenantId": "dj",
  "moduleName": "SDCRS",
  "ReportType": [
    {
      "code": "STRAY_DOG_AGGRESSIVE",
      "name": "Aggressive Stray Dog",
      "description": "Stray dog showing aggressive behavior",
      "priority": "HIGH",
      "basePoints": 150,
      "active": true
    },
    {
      "code": "STRAY_DOG_INJURED",
      "name": "Injured Stray Dog",
      "description": "Stray dog that appears injured",
      "priority": "HIGH",
      "basePoints": 200,
      "active": true
    },
    {
      "code": "STRAY_DOG_PACK",
      "name": "Stray Dog Pack",
      "description": "Group of 3+ stray dogs",
      "priority": "MEDIUM",
      "basePoints": 250,
      "active": true
    },
    {
      "code": "STRAY_DOG_STANDARD",
      "name": "Standard Stray Dog",
      "description": "Single stray dog sighting",
      "priority": "LOW",
      "basePoints": 100,
      "active": true
    }
  ]
}
```

#### 4.1.2 Payout Configuration (`PayoutConfig.json`)

```json
{
  "tenantId": "dj",
  "moduleName": "SDCRS",
  "PayoutConfig": [
    {
      "code": "STANDARD_PAYOUT",
      "name": "Standard Payout",
      "amountPerReport": 500,
      "currency": "DJF",
      "minimumPhotos": 2,
      "maximumDailyReports": 5,
      "cooldownPeriodHours": 24,
      "active": true
    },
    {
      "code": "BONUS_TIER_1",
      "name": "Bronze Tier Bonus",
      "bonusMultiplier": 1.1,
      "requiredReports": 10,
      "active": true
    },
    {
      "code": "BONUS_TIER_2",
      "name": "Silver Tier Bonus",
      "bonusMultiplier": 1.25,
      "requiredReports": 50,
      "active": true
    },
    {
      "code": "BONUS_TIER_3",
      "name": "Gold Tier Bonus",
      "bonusMultiplier": 1.5,
      "requiredReports": 100,
      "active": true
    }
  ]
}
```

#### 4.1.3 Rejection Reasons (`RejectionReason.json`)

```json
{
  "tenantId": "dj",
  "moduleName": "SDCRS",
  "RejectionReason": [
    {
      "code": "INVALID_GPS",
      "name": "Invalid GPS Coordinates",
      "description": "GPS data missing or invalid",
      "isAutoRejection": true,
      "active": true
    },
    {
      "code": "OUTSIDE_BOUNDARY",
      "name": "Outside Tenant Boundary",
      "description": "Location is outside the service area",
      "isAutoRejection": true,
      "active": true
    },
    {
      "code": "STALE_TIMESTAMP",
      "name": "Stale Timestamp",
      "description": "Photo taken more than 48 hours ago",
      "isAutoRejection": true,
      "active": true
    },
    {
      "code": "DUPLICATE_IMAGE",
      "name": "Duplicate Image",
      "description": "Same image already submitted",
      "isAutoRejection": true,
      "active": true
    },
    {
      "code": "POOR_IMAGE_QUALITY",
      "name": "Poor Image Quality",
      "description": "Image too blurry or dark to identify",
      "isAutoRejection": false,
      "active": true
    },
    {
      "code": "NOT_A_DOG",
      "name": "Not a Dog",
      "description": "Image does not contain a dog",
      "isAutoRejection": false,
      "active": true
    },
    {
      "code": "DOMESTIC_DOG",
      "name": "Domestic Dog",
      "description": "Dog appears to be a pet with collar/tag",
      "isAutoRejection": false,
      "active": true
    },
    {
      "code": "FRAUDULENT_SUBMISSION",
      "name": "Fraudulent Submission",
      "description": "Evidence of fraud or manipulation",
      "isAutoRejection": false,
      "active": true
    }
  ]
}
```

#### 4.1.4 Resolution Types (`ResolutionType.json`)

```json
{
  "tenantId": "dj",
  "moduleName": "SDCRS",
  "ResolutionType": [
    {
      "code": "CAPTURED",
      "name": "Captured",
      "description": "Dog was captured and taken to shelter",
      "triggersPayout": true,
      "active": true
    },
    {
      "code": "RELOCATED",
      "name": "Relocated",
      "description": "Dog was relocated to safe area",
      "triggersPayout": true,
      "active": true
    },
    {
      "code": "STERILIZED_RELEASED",
      "name": "Sterilized and Released",
      "description": "Dog was sterilized and released (ABC program)",
      "triggersPayout": true,
      "active": true
    },
    {
      "code": "UNABLE_TO_LOCATE",
      "name": "Unable to Locate",
      "description": "Dog could not be found at location",
      "triggersPayout": false,
      "active": true
    },
    {
      "code": "ALREADY_RESOLVED",
      "name": "Already Resolved",
      "description": "Another team already handled this dog",
      "triggersPayout": false,
      "active": true
    }
  ]
}
```

---

## 5. Roles & Access Control Configuration

### 5.1 Roles (`ACCESSCONTROL-ROLES/roles.json`)

```json
{
  "tenantId": "dj",
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
      "description": "Supervisor for MC Officers"
    },
    {
      "code": "DISTRICT_ADMIN",
      "name": "District Admin",
      "description": "District level administrator"
    },
    {
      "code": "STATE_ADMIN",
      "name": "State Admin",
      "description": "State level administrator"
    },
    {
      "code": "SYSTEM",
      "name": "System",
      "description": "Automated system operations"
    }
  ]
}
```

### 5.2 Actions (`ACCESSCONTROL-ACTIONS-TEST/actions-test.json`)

```json
{
  "tenantId": "dj",
  "moduleName": "ACCESSCONTROL-ACTIONS-TEST",
  "actions-test": [
    {
      "id": 2001,
      "name": "Create Dog Report",
      "url": "/sdcrs-services/v1/report/_create",
      "parentModule": "sdcrs-services",
      "displayName": "Create Dog Report",
      "orderNumber": 1,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2002,
      "name": "Update Dog Report",
      "url": "/sdcrs-services/v1/report/_update",
      "parentModule": "sdcrs-services",
      "displayName": "Update Dog Report",
      "orderNumber": 2,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2003,
      "name": "Search Dog Reports",
      "url": "/sdcrs-services/v1/report/_search",
      "parentModule": "sdcrs-services",
      "displayName": "Search Dog Reports",
      "orderNumber": 3,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2004,
      "name": "Verify Dog Report",
      "url": "/sdcrs-services/v1/report/_verify",
      "parentModule": "sdcrs-services",
      "displayName": "Verify Dog Report",
      "orderNumber": 4,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2005,
      "name": "Reject Dog Report",
      "url": "/sdcrs-services/v1/report/_reject",
      "parentModule": "sdcrs-services",
      "displayName": "Reject Dog Report",
      "orderNumber": 5,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2006,
      "name": "Resolve Dog Report",
      "url": "/sdcrs-services/v1/report/_resolve",
      "parentModule": "sdcrs-services",
      "displayName": "Resolve Dog Report",
      "orderNumber": 6,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2007,
      "name": "Upload Evidence",
      "url": "/sdcrs-services/v1/evidence/_upload",
      "parentModule": "sdcrs-services",
      "displayName": "Upload Evidence",
      "orderNumber": 7,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2008,
      "name": "Search Evidence",
      "url": "/sdcrs-services/v1/evidence/_search",
      "parentModule": "sdcrs-services",
      "displayName": "Search Evidence",
      "orderNumber": 8,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2009,
      "name": "Compare Duplicates",
      "url": "/sdcrs-services/v1/evidence/_compare",
      "parentModule": "sdcrs-services",
      "displayName": "Compare Duplicates",
      "orderNumber": 9,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2010,
      "name": "Create Payout",
      "url": "/sdcrs-services/v1/payout/_create",
      "parentModule": "sdcrs-services",
      "displayName": "Create Payout",
      "orderNumber": 10,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2011,
      "name": "Search Payouts",
      "url": "/sdcrs-services/v1/payout/_search",
      "parentModule": "sdcrs-services",
      "displayName": "Search Payouts",
      "orderNumber": 11,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2012,
      "name": "Process Payout",
      "url": "/sdcrs-services/v1/payout/_process",
      "parentModule": "sdcrs-services",
      "displayName": "Process Payout",
      "orderNumber": 12,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2013,
      "name": "Award Points",
      "url": "/sdcrs-services/v1/points/_award",
      "parentModule": "sdcrs-services",
      "displayName": "Award Points",
      "orderNumber": 13,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2014,
      "name": "Search Points",
      "url": "/sdcrs-services/v1/points/_search",
      "parentModule": "sdcrs-services",
      "displayName": "Search Points",
      "orderNumber": 14,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2015,
      "name": "Get Leaderboard",
      "url": "/sdcrs-services/v1/points/_leaderboard",
      "parentModule": "sdcrs-services",
      "displayName": "Get Leaderboard",
      "orderNumber": 15,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2016,
      "name": "Get Dashboard",
      "url": "/sdcrs-services/v1/dashboard/_get",
      "parentModule": "sdcrs-services",
      "displayName": "Get Dashboard",
      "orderNumber": 16,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2017,
      "name": "Get Analytics",
      "url": "/sdcrs-services/v1/analytics/_get",
      "parentModule": "sdcrs-services",
      "displayName": "Get Analytics",
      "orderNumber": 17,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2018,
      "name": "Generate Report",
      "url": "/sdcrs-services/v1/report/_generate",
      "parentModule": "sdcrs-services",
      "displayName": "Generate Report",
      "orderNumber": 18,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2019,
      "name": "Assign MC Officer",
      "url": "/sdcrs-services/v1/assignment/_assign",
      "parentModule": "sdcrs-services",
      "displayName": "Assign MC Officer",
      "orderNumber": 19,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    },
    {
      "id": 2020,
      "name": "Get Assignments",
      "url": "/sdcrs-services/v1/assignment/_search",
      "parentModule": "sdcrs-services",
      "displayName": "Get Assignments",
      "orderNumber": 20,
      "enabled": true,
      "serviceCode": "sdcrs-services",
      "code": "null",
      "path": ""
    }
  ]
}
```

### 5.3 Role-Action Mapping (`ACCESSCONTROL-ROLEACTIONS/roleactions.json`)

```json
{
  "tenantId": "dj",
  "moduleName": "ACCESSCONTROL-ROLEACTIONS",
  "roleactions": [
    {
      "rolecode": "TEACHER",
      "actionid": 2001,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "TEACHER",
      "actionid": 2003,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "TEACHER",
      "actionid": 2007,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "TEACHER",
      "actionid": 2011,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "TEACHER",
      "actionid": 2014,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "TEACHER",
      "actionid": 2015,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "VERIFIER",
      "actionid": 2002,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "VERIFIER",
      "actionid": 2003,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "VERIFIER",
      "actionid": 2004,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "VERIFIER",
      "actionid": 2005,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "VERIFIER",
      "actionid": 2008,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "VERIFIER",
      "actionid": 2009,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "VERIFIER",
      "actionid": 2016,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "MC_OFFICER",
      "actionid": 2003,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "MC_OFFICER",
      "actionid": 2006,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "MC_OFFICER",
      "actionid": 2008,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "MC_OFFICER",
      "actionid": 2016,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "MC_OFFICER",
      "actionid": 2020,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "MC_SUPERVISOR",
      "actionid": 2003,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "MC_SUPERVISOR",
      "actionid": 2006,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "MC_SUPERVISOR",
      "actionid": 2016,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "MC_SUPERVISOR",
      "actionid": 2017,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "MC_SUPERVISOR",
      "actionid": 2019,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "MC_SUPERVISOR",
      "actionid": 2020,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "DISTRICT_ADMIN",
      "actionid": 2003,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "DISTRICT_ADMIN",
      "actionid": 2011,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "DISTRICT_ADMIN",
      "actionid": 2012,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "DISTRICT_ADMIN",
      "actionid": 2016,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "DISTRICT_ADMIN",
      "actionid": 2017,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "DISTRICT_ADMIN",
      "actionid": 2018,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "STATE_ADMIN",
      "actionid": 2003,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "STATE_ADMIN",
      "actionid": 2011,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "STATE_ADMIN",
      "actionid": 2012,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "STATE_ADMIN",
      "actionid": 2016,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "STATE_ADMIN",
      "actionid": 2017,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "STATE_ADMIN",
      "actionid": 2018,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "SYSTEM",
      "actionid": 2002,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "SYSTEM",
      "actionid": 2010,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "SYSTEM",
      "actionid": 2012,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "SYSTEM",
      "actionid": 2013,
      "actioncode": "",
      "tenantId": "dj"
    },
    {
      "rolecode": "SYSTEM",
      "actionid": 2019,
      "actioncode": "",
      "tenantId": "dj"
    }
  ]
}
```

### 5.4 Role-Action Matrix Summary

| Action | TEACHER | VERIFIER | MC_OFFICER | MC_SUPERVISOR | DISTRICT_ADMIN | STATE_ADMIN | SYSTEM |
|--------|---------|----------|------------|---------------|----------------|-------------|--------|
| Create Report | X | | | | | | |
| Update Report | | X | | | | | X |
| Search Reports | X | X | X | X | X | X | |
| Verify Report | | X | | | | | |
| Reject Report | | X | | | | | |
| Resolve Report | | | X | X | | | |
| Upload Evidence | X | | | | | | |
| Search Evidence | | X | X | | | | |
| Compare Duplicates | | X | | | | | |
| Create Payout | | | | | | | X |
| Search Payouts | X | | | | X | X | |
| Process Payout | | | | | X | X | X |
| Award Points | | | | | | | X |
| Search Points | X | | | | | | |
| Get Leaderboard | X | | | | | | |
| Get Dashboard | | X | X | X | X | X | |
| Get Analytics | | | | X | X | X | |
| Generate Report | | | | | X | X | |
| Assign MC Officer | | | | X | | | X |
| Get Assignments | | | X | X | | | |

---

## 6. Persister Configuration

### 6.1 Dog Report Persister (`sdcrs-persister.yml`)

```yaml
serviceMaps:
  serviceName: sdcrs-services
  mappings:
    - version: 1.0
      description: Persists dog report in tables
      fromTopic: save-sdcrs-report
      isTransaction: true
      queryMaps:
        - query: >
            INSERT INTO eg_sdcrs_report
            (id, tenant_id, report_number, report_type, status,
             reporter_id, reporter_name, reporter_phone,
             latitude, longitude, address, locality_code,
             dog_description, dog_count, is_aggressive,
             photo_file_store_id, selfie_file_store_id, image_hash,
             assigned_mc_id, assigned_mc_name,
             resolution_type, resolution_notes, resolution_time,
             payout_status, payout_amount, payout_id,
             points_awarded, rejection_reason,
             additional_details,
             created_by, created_time, last_modified_by, last_modified_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
          basePath: DogReport
          jsonMaps:
            - jsonPath: $.DogReport.id
            - jsonPath: $.DogReport.tenantId
            - jsonPath: $.DogReport.reportNumber
            - jsonPath: $.DogReport.reportType
            - jsonPath: $.DogReport.status
            - jsonPath: $.DogReport.reporter.id
            - jsonPath: $.DogReport.reporter.name
            - jsonPath: $.DogReport.reporter.phone
            - jsonPath: $.DogReport.location.latitude
            - jsonPath: $.DogReport.location.longitude
            - jsonPath: $.DogReport.location.address
            - jsonPath: $.DogReport.location.localityCode
            - jsonPath: $.DogReport.dogDetails.description
            - jsonPath: $.DogReport.dogDetails.count
            - jsonPath: $.DogReport.dogDetails.isAggressive
            - jsonPath: $.DogReport.evidence.photoFileStoreId
            - jsonPath: $.DogReport.evidence.selfieFileStoreId
            - jsonPath: $.DogReport.evidence.imageHash
            - jsonPath: $.DogReport.assignment.mcOfficerId
            - jsonPath: $.DogReport.assignment.mcOfficerName
            - jsonPath: $.DogReport.resolution.type
            - jsonPath: $.DogReport.resolution.notes
            - jsonPath: $.DogReport.resolution.time
            - jsonPath: $.DogReport.payout.status
            - jsonPath: $.DogReport.payout.amount
            - jsonPath: $.DogReport.payout.id
            - jsonPath: $.DogReport.pointsAwarded
            - jsonPath: $.DogReport.rejectionReason
            - jsonPath: $.DogReport.additionalDetails
              type: JSON
              dbType: JSONB
            - jsonPath: $.DogReport.auditDetails.createdBy
            - jsonPath: $.DogReport.auditDetails.createdTime
            - jsonPath: $.DogReport.auditDetails.lastModifiedBy
            - jsonPath: $.DogReport.auditDetails.lastModifiedTime

    - version: 1.0
      description: Updates dog report in tables
      fromTopic: update-sdcrs-report
      isTransaction: true
      queryMaps:
        - query: >
            UPDATE eg_sdcrs_report SET
              status = ?,
              assigned_mc_id = ?,
              assigned_mc_name = ?,
              resolution_type = ?,
              resolution_notes = ?,
              resolution_time = ?,
              payout_status = ?,
              payout_amount = ?,
              payout_id = ?,
              points_awarded = ?,
              rejection_reason = ?,
              additional_details = ?,
              last_modified_by = ?,
              last_modified_time = ?
            WHERE id = ?;
          basePath: DogReport
          jsonMaps:
            - jsonPath: $.DogReport.status
            - jsonPath: $.DogReport.assignment.mcOfficerId
            - jsonPath: $.DogReport.assignment.mcOfficerName
            - jsonPath: $.DogReport.resolution.type
            - jsonPath: $.DogReport.resolution.notes
            - jsonPath: $.DogReport.resolution.time
            - jsonPath: $.DogReport.payout.status
            - jsonPath: $.DogReport.payout.amount
            - jsonPath: $.DogReport.payout.id
            - jsonPath: $.DogReport.pointsAwarded
            - jsonPath: $.DogReport.rejectionReason
            - jsonPath: $.DogReport.additionalDetails
              type: JSON
              dbType: JSONB
            - jsonPath: $.DogReport.auditDetails.lastModifiedBy
            - jsonPath: $.DogReport.auditDetails.lastModifiedTime
            - jsonPath: $.DogReport.id
```

### 6.2 Payout Persister

```yaml
serviceMaps:
  serviceName: sdcrs-services
  mappings:
    - version: 1.0
      description: Persists payout records
      fromTopic: save-sdcrs-payout
      isTransaction: true
      queryMaps:
        - query: >
            INSERT INTO eg_sdcrs_payout
            (id, tenant_id, report_id, teacher_id, teacher_name,
             amount, currency, status, payment_mode,
             transaction_id, transaction_time,
             created_by, created_time, last_modified_by, last_modified_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
          basePath: Payout
          jsonMaps:
            - jsonPath: $.Payout.id
            - jsonPath: $.Payout.tenantId
            - jsonPath: $.Payout.reportId
            - jsonPath: $.Payout.teacherId
            - jsonPath: $.Payout.teacherName
            - jsonPath: $.Payout.amount
            - jsonPath: $.Payout.currency
            - jsonPath: $.Payout.status
            - jsonPath: $.Payout.paymentMode
            - jsonPath: $.Payout.transactionId
            - jsonPath: $.Payout.transactionTime
            - jsonPath: $.Payout.auditDetails.createdBy
            - jsonPath: $.Payout.auditDetails.createdTime
            - jsonPath: $.Payout.auditDetails.lastModifiedBy
            - jsonPath: $.Payout.auditDetails.lastModifiedTime
```

---

## 7. Kafka Topics

| Topic Name | Producer | Consumer | Purpose |
|------------|----------|----------|---------|
| `save-sdcrs-report` | sdcrs-services | persister | Save new dog report |
| `update-sdcrs-report` | sdcrs-services | persister | Update dog report |
| `save-sdcrs-payout` | sdcrs-services | persister | Save payout record |
| `update-sdcrs-payout` | sdcrs-services | persister | Update payout status |
| `sdcrs-report-indexer` | sdcrs-services | indexer | Index for search |
| `sdcrs-notification` | sdcrs-services | notification | Trigger notifications |
| `sdcrs-workflow-transition` | sdcrs-services | workflow | Workflow state changes |

---

## 8. Database Schema

### 8.1 Dog Report Table

```sql
CREATE TABLE eg_sdcrs_report (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    report_number VARCHAR(64) UNIQUE NOT NULL,
    report_type VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,

    -- Reporter Details
    reporter_id VARCHAR(64) NOT NULL,
    reporter_name VARCHAR(256),
    reporter_phone VARCHAR(20),

    -- Location Details
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    address TEXT,
    locality_code VARCHAR(64),

    -- Dog Details
    dog_description TEXT,
    dog_count INTEGER DEFAULT 1,
    is_aggressive BOOLEAN DEFAULT FALSE,

    -- Evidence
    photo_file_store_id VARCHAR(64),
    selfie_file_store_id VARCHAR(64),
    image_hash VARCHAR(256),

    -- Assignment
    assigned_mc_id VARCHAR(64),
    assigned_mc_name VARCHAR(256),

    -- Resolution
    resolution_type VARCHAR(64),
    resolution_notes TEXT,
    resolution_time BIGINT,

    -- Payout
    payout_status VARCHAR(64),
    payout_amount DECIMAL(10, 2),
    payout_id VARCHAR(64),

    -- Points
    points_awarded INTEGER DEFAULT 0,

    -- Rejection
    rejection_reason VARCHAR(64),

    -- Additional
    additional_details JSONB,

    -- Audit
    created_by VARCHAR(64) NOT NULL,
    created_time BIGINT NOT NULL,
    last_modified_by VARCHAR(64),
    last_modified_time BIGINT,

    -- Indexes
    CONSTRAINT fk_tenant FOREIGN KEY (tenant_id) REFERENCES eg_tenant(code)
);

CREATE INDEX idx_sdcrs_report_tenant ON eg_sdcrs_report(tenant_id);
CREATE INDEX idx_sdcrs_report_status ON eg_sdcrs_report(status);
CREATE INDEX idx_sdcrs_report_reporter ON eg_sdcrs_report(reporter_id);
CREATE INDEX idx_sdcrs_report_mc ON eg_sdcrs_report(assigned_mc_id);
CREATE INDEX idx_sdcrs_report_locality ON eg_sdcrs_report(locality_code);
CREATE INDEX idx_sdcrs_report_created ON eg_sdcrs_report(created_time);
CREATE INDEX idx_sdcrs_report_image_hash ON eg_sdcrs_report(image_hash);
```

### 8.2 Payout Table

```sql
CREATE TABLE eg_sdcrs_payout (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    report_id VARCHAR(64) NOT NULL,
    teacher_id VARCHAR(64) NOT NULL,
    teacher_name VARCHAR(256),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'DJF',
    status VARCHAR(64) NOT NULL,
    payment_mode VARCHAR(64),
    transaction_id VARCHAR(256),
    transaction_time BIGINT,

    -- Audit
    created_by VARCHAR(64) NOT NULL,
    created_time BIGINT NOT NULL,
    last_modified_by VARCHAR(64),
    last_modified_time BIGINT,

    CONSTRAINT fk_payout_report FOREIGN KEY (report_id) REFERENCES eg_sdcrs_report(id)
);

CREATE INDEX idx_sdcrs_payout_teacher ON eg_sdcrs_payout(teacher_id);
CREATE INDEX idx_sdcrs_payout_status ON eg_sdcrs_payout(status);
CREATE INDEX idx_sdcrs_payout_created ON eg_sdcrs_payout(created_time);
```

### 8.3 Points Table

```sql
CREATE TABLE eg_sdcrs_points (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    teacher_id VARCHAR(64) NOT NULL,
    report_id VARCHAR(64),
    points INTEGER NOT NULL,
    transaction_type VARCHAR(64) NOT NULL,
    description TEXT,

    -- Audit
    created_by VARCHAR(64) NOT NULL,
    created_time BIGINT NOT NULL
);

CREATE INDEX idx_sdcrs_points_teacher ON eg_sdcrs_points(teacher_id);
CREATE INDEX idx_sdcrs_points_created ON eg_sdcrs_points(created_time);

-- Leaderboard view
CREATE VIEW vw_sdcrs_leaderboard AS
SELECT
    teacher_id,
    tenant_id,
    SUM(points) as total_points,
    COUNT(DISTINCT report_id) as report_count
FROM eg_sdcrs_points
GROUP BY teacher_id, tenant_id
ORDER BY total_points DESC;
```

---

## 9. Service Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           API Gateway                                     │
│                     (Authentication & Routing)                            │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│ SDCRS Service │         │ User Service  │         │ Access Control│
│               │◄───────►│               │◄───────►│    Service    │
│  - Reports    │         │ - Auth        │         │  - Roles      │
│  - Evidence   │         │ - Profile     │         │  - Actions    │
│  - Payout     │         │               │         │               │
│  - Points     │         └───────────────┘         └───────────────┘
└───────────────┘                 │
        │                         │
        ├─────────────────────────┼─────────────────────────────┐
        │                         │                             │
        ▼                         ▼                             ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│   Workflow    │         │  File Store   │         │   Location    │
│   Service     │         │   Service     │         │   Service     │
│               │         │               │         │               │
│ - States      │         │ - Photos      │         │ - GPS Valid   │
│ - Actions     │         │ - Selfies     │         │ - Boundary    │
└───────────────┘         └───────────────┘         └───────────────┘
        │                         │                             │
        └─────────────────────────┼─────────────────────────────┘
                                  │
                                  ▼
                        ┌───────────────┐
                        │     Kafka     │
                        │    Topics     │
                        └───────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────────┐
        │                         │                             │
        ▼                         ▼                             ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│   Persister   │         │    Indexer    │         │ Notification  │
│   Service     │         │   Service     │         │   Service     │
│               │         │               │         │               │
│ - PostgreSQL  │         │ - Elastic     │         │ - SMS         │
│ - CRUD        │         │ - Search      │         │ - Email       │
└───────────────┘         └───────────────┘         └───────────────┘
```

---

## 10. Next Steps

1. **Design Output #4:** Create detailed OpenAPI/Swagger specifications for all SDCRS APIs
2. **Design Output #5:** Develop sequence diagrams for key user flows
3. **Design Output #6:** Create UI wireframes and component specifications
4. **Implementation:** Set up DIGIT development environment and begin coding

---

## References

- [DIGIT Design Services Guide](../../core-docs/get-started/design-guide/design-services.md)
- [DIGIT Workflow Configuration](../../core-docs/platform/core-services/workflow/configuring-workflows-for-an-entity.md)
- [DIGIT MDMS Service](../../core-docs/platform/core-services/mdms-v2-master-data-management-service/mdms-master-data-management-service/README.md)
- [DIGIT Access Control](../../core-docs/platform/core-services/access-control-services.md)
- [DIGIT Persister Configuration](../../core-docs/platform/core-services/persister-service/persister-configuration.md)

---

*Document Version: 1.0*
*Last Updated: December 2024*
*Status: Draft*
