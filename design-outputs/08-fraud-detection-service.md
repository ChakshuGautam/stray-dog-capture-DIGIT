# Fraud Detection Service Design

## Overview

The **Fraud Detection Service** is a configurable, reusable DIGIT platform service that identifies potential fraud, abuse, and quality issues in citizen applications. It operates asynchronously to analyze submissions, apply configurable fraud rules, and raise flags for human review or automated rejection.

### Design Rationale

Instead of embedding fraud detection logic directly in each domain service (PGR, SDCRS, Trade License, etc.), a centralized Fraud Detection Service provides:

| Benefit | Description |
|---------|-------------|
| **Separation of Concerns** | Business logic remains in domain services; fraud logic is isolated |
| **Reusability** | Same service can protect multiple DIGIT modules |
| **Configurability** | Fraud rules defined in MDMS, no code changes needed |
| **Scalability** | Can be scaled independently based on load |
| **Async Processing** | Doesn't block the submission flow |
| **Auditability** | Centralized fraud flag history for analytics |

---

## Table of Contents

1. [Architecture](#1-architecture)
2. [Fraud Rule Types](#2-fraud-rule-types)
   - [2.4 Condition Types](#24-condition-types)
   - [2.5 SpEL Expression Evaluator](#25-spel-expression-evaluator)
3. [Data Model](#3-data-model)
4. [API Specifications](#4-api-specifications)
5. [Kafka Integration](#5-kafka-integration)
6. [MDMS Configuration](#6-mdms-configuration)
7. [Database Schema](#7-database-schema)
8. [Integration Patterns](#8-integration-patterns)
9. [Fraud Flag Lifecycle](#9-fraud-flag-lifecycle)
10. [Dashboard & Analytics](#10-dashboard--analytics)
11. [SDCRS-Specific Rules](#11-sdcrs-specific-rules)
12. [External AI/ML Service Integration](#12-external-aiml-service-integration)
13. [Implementation Phases](#13-implementation-phases)

---

## 1. Architecture

### 1.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DIGIT Platform                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                   │
│  │   SDCRS      │    │    PGR       │    │ Trade License│                   │
│  │   Service    │    │   Service    │    │   Service    │                   │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘                   │
│         │                   │                   │                           │
│         ▼                   ▼                   ▼                           │
│  ┌──────────────────────────────────────────────────────────────┐           │
│  │                      Kafka Topics                            │           │
│  │   fraud-check-request   fraud-flag-created   fraud-resolved  │           │
│  └──────────────────────────────────────────────────────────────┘           │
│                              │                                              │
│                              ▼                                              │
│         ┌────────────────────────────────────────────┐                      │
│         │         FRAUD DETECTION SERVICE            │                      │
│         ├────────────────────────────────────────────┤                      │
│         │  ┌─────────────┐  ┌──────────────────────┐ │                      │
│         │  │ Rule Engine │  │ Duplicate Detector   │ │                      │
│         │  │  (Drools)   │  │  (pHash/SimHash)     │ │                      │
│         │  └─────────────┘  └──────────────────────┘ │                      │
│         │  ┌─────────────┐  ┌──────────────────────┐ │                      │
│         │  │ Velocity    │  │ Geospatial           │ │                      │
│         │  │ Checker     │  │ Validator            │ │                      │
│         │  └─────────────┘  └──────────────────────┘ │                      │
│         │  ┌─────────────┐  ┌──────────────────────┐ │                      │
│         │  │ Pattern     │  │ Network Analysis     │ │                      │
│         │  │ Detector    │  │ (Collusion)          │ │                      │
│         │  └─────────────┘  └──────────────────────┘ │                      │
│         └────────────────────────────────────────────┘                      │
│                              │                                              │
│              ┌───────────────┼───────────────┐                              │
│              ▼               ▼               ▼                              │
│         ┌─────────-┐    ┌──────────┐    ┌──────────┐                        │
│         │PostgreSQL│    │  Redis   │    │Elastic   │                        │
│         │(Flags)   │    │ (Cache)  │    │(Hashes)  │                        │
│         └─────────-┘    └──────────┘    └──────────┘                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Component Responsibilities

| Component | Responsibility |
|-----------|----------------|
| **Rule Engine** | Evaluates configurable business rules from MDMS |
| **Duplicate Detector** | Computes and compares perceptual hashes (pHash) for images |
| **Velocity Checker** | Detects abnormal submission rates by user/location/time |
| **Geospatial Validator** | Validates GPS coordinates, distances, boundaries |
| **Pattern Detector** | Identifies suspicious patterns (time, location, metadata) |
| **Network Analysis** | Detects collusion between users (shared devices, locations) |

---

## 2. Fraud Rule Types

### 2.1 Rule Categories

| Category | Code | Description | Severity |
|----------|------|-------------|----------|
| **Data Quality** | `DQ` | Missing, malformed, or inconsistent data | LOW |
| **Duplicate** | `DUP` | Same or similar submission already exists | MEDIUM |
| **Location** | `LOC` | GPS spoofing, boundary violations | HIGH |
| **Velocity** | `VEL` | Abnormal submission rate | MEDIUM |
| **Temporal** | `TMP` | Timestamp manipulation, stale data | MEDIUM |
| **Identity** | `IDN` | Account anomalies, impersonation | HIGH |
| **Collusion** | `COL` | Coordinated fraud by multiple users | CRITICAL |
| **Evidence** | `EVD` | Manipulated photos, metadata stripping | HIGH |

### 2.2 Standard Rules (Applicable to All Modules)

| Rule ID | Name | Category | Description |
|---------|------|----------|-------------|
| `STD-001` | Missing GPS | DQ | Application lacks GPS coordinates |
| `STD-002` | GPS Outside Boundary | LOC | GPS not within tenant/jurisdiction |
| `STD-003` | High Velocity | VEL | >N submissions in T hours by same user |
| `STD-004` | Stale Timestamp | TMP | Evidence timestamp >N hours old |
| `STD-005` | Exact Duplicate | DUP | Hash match with existing application |
| `STD-006` | Near Duplicate | DUP | Similarity >N% with existing application |
| `STD-007` | Rapid Fire | VEL | Submissions <N minutes apart |
| `STD-008` | Device Sharing | COL | Multiple users from same device ID |
| `STD-009` | Location Clustering | COL | Unusual clustering of submissions |
| `STD-010` | EXIF Stripped | EVD | Photo metadata intentionally removed |

### 2.3 Rule Severity Levels

| Severity | Code | Action | SLA |
|----------|------|--------|-----|
| `LOW` | 1 | Flag for review, allow processing | No SLA |
| `MEDIUM` | 2 | Flag for review, may proceed | 24 hours |
| `HIGH` | 3 | Pause processing, require human review | 4 hours |
| `CRITICAL` | 4 | Block processing, escalate to admin | 1 hour |

### 2.4 Condition Types

The fraud detection service supports 16 condition types for evaluating rules:

| Type | Parameters | Description |
|------|------------|-------------|
| `NULL_CHECK` | `field` | Checks if a required field is null/missing |
| `GEO_BOUNDARY` | `boundaryType` | Validates GPS coordinates are within allowed boundaries |
| `GEO_DISTANCE` | `maxDistanceMeters` | Checks distance between two GPS points (Haversine) |
| `VELOCITY` | `threshold`, `windowHours` | Detects high submission rates per user |
| `TIMESTAMP_AGE` | `maxAgeHours` | Validates evidence timestamp is recent |
| `TIMESTAMP_DIFF` | `maxDiffMinutes` | Compares timestamps between evidences |
| `HASH_MATCH` | `algorithm`, `threshold` | Exact hash comparison for duplicate detection |
| `IMAGE_SIMILARITY` | `algorithm`, `threshold` | Perceptual hash (pHash) similarity comparison |
| `INTERVAL` | `minIntervalMinutes` | Minimum time between submissions |
| `DEVICE_SHARING` | `minUniqueUsers`, `windowDays` | Detects multiple users on same device |
| `GEO_CLUSTER` | `radiusMeters`, `windowHours`, `minCount` | Spatial clustering of submissions |
| `METADATA_CHECK` | `field`, `expectedValue` | Validates metadata fields (e.g., EXIF presence) |
| `TIME_WINDOW` | `allowedWindows`, `timezone` | Validates submission time (e.g., school hours) |
| `AGGREGATE_COUNT` | `periodDays`, `threshold` | Count-based thresholds over time |
| `GPS_VELOCITY` | `maxSpeedKmh`, `minIntervalMinutes` | Detects physically impossible travel speeds |
| `CUSTOM` | `expression` | SpEL expression for complex conditions |

### 2.5 SpEL Expression Evaluator

The fraud detection service uses **Spring Expression Language (SpEL)** for flexible, configurable rule expressions. This enables complex fraud rules without code changes.

#### 2.5.1 Expression Types

| Location | Field | Purpose |
|----------|-------|---------|
| Internal rules | `condition.expression` | CUSTOM type rules with arbitrary logic |
| External rules | `condition.checkExpression` | Evaluate ML/AI predictions |

#### 2.5.2 Available Variables in SpEL Context

| Variable | Type | Description |
|----------|------|-------------|
| `#request` | Object | Full `FraudEvaluationRequest` object |
| `#applicantInfo` | Object | Applicant information object |
| `#applicantId` | String | Unique applicant identifier |
| `#applicantName` | String | Applicant's name |
| `#deviceId` | String | Device identifier |
| `#mobileNumber` | String | Applicant's mobile number |
| `#locationData` | Object | Location data object |
| `#latitude` | Double | GPS latitude |
| `#longitude` | Double | GPS longitude |
| `#locality` | String | Locality code |
| `#evidences` | List | List of evidence objects |
| `#evidenceCount` | Integer | Number of evidences |
| `#metadata` | Map | First evidence's metadata |
| `#additionalData` | Map | Application-specific data |
| `#predictions` | Map | ML model predictions (external rules) |
| `#now` | Long | Current timestamp in milliseconds |

#### 2.5.3 Example CUSTOM Rule Expressions

```json
{
  "id": "SDCRS-013",
  "code": "PACK_SIZE_SUSPICIOUSLY_LARGE",
  "category": "DQ",
  "severity": "MEDIUM",
  "condition": {
    "type": "CUSTOM",
    "expression": "#additionalData != null && #additionalData['dogCount'] != null && #additionalData['dogCount'] > 10"
  },
  "action": { "type": "FLAG" }
}
```

**More expression examples:**

| Use Case | SpEL Expression |
|----------|-----------------|
| Incomplete evidence | `#evidences == null \|\| #evidenceCount < 2` |
| Restricted location zone | `#latitude > 28.55 && #latitude < 28.57 && #longitude > 77.08 && #longitude < 77.12` |
| High-volume reporter | `#additionalData['lifetimeReportCount'] > 50` |
| Emulator detection | `#deviceId != null && (#deviceId.contains('emulator') \|\| #deviceId.contains('sdk'))` |
| School hours check | `T(java.time.LocalTime).now().isAfter(T(java.time.LocalTime).of(9, 0)) && T(java.time.LocalTime).now().isBefore(T(java.time.LocalTime).of(15, 0))` |

#### 2.5.4 External Validator checkExpression Examples

For external ML/AI validators, the `#predictions` map contains model outputs:

```json
{
  "id": "EXT-001",
  "code": "NO_DOG_DETECTED",
  "validatorId": "OBJECT_DETECTOR",
  "condition": {
    "type": "EXTERNAL_VALIDATOR",
    "checkExpression": "dogCount == 0"
  }
}
```

| Validator | Predictions Available | Example checkExpression |
|-----------|----------------------|------------------------|
| `OBJECT_DETECTOR` | `dogCount`, `confidence` | `"dogCount == 0"` |
| `FACE_MATCHER` | `facesDetected`, `match`, `similarity` | `"facesDetected == 0 \|\| (match == false && similarity < 0.7)"` |
| `IMAGE_QUALITY_ANALYZER` | `blurScore`, `overallQuality`, `isAcceptable` | `"blurScore < 0.2 \|\| overallQuality < 0.4"` |
| `ANOMALY_DETECTOR` | `isAnomaly`, `anomalyScore` | `"isAnomaly == true && anomalyScore > 0.85"` |
| `GPS_SPOOFING_DETECTOR` | `isSpoofed`, `spoofingConfidence` | `"isSpoofed == true && spoofingConfidence > 0.6"` |

#### 2.5.5 SpEL Implementation Notes

- **Parser**: `SpelExpressionParser` from Spring Framework
- **Context**: `StandardEvaluationContext` with registered variables
- **Null Safety**: Expressions should include null checks for optional fields
- **Type Coercion**: SpEL handles automatic type conversion
- **Error Handling**: Invalid expressions return false and log errors
- **Validation**: `isValidExpression()` method validates syntax before evaluation

---

## 3. Data Model

### 3.1 FraudCheckRequest

The input payload sent by domain services for fraud analysis.

```json
{
  "FraudCheckRequest": {
    "requestInfo": { /* Standard DIGIT RequestInfo */ },
    "fraudCheck": {
      "id": "uuid",
      "tenantId": "ncr",
      "moduleCode": "SDCRS",
      "businessService": "SDCRS",
      "applicationId": "NCR-SDCRS-2024-000123",
      "applicationNumber": "NCR-SDCRS-2024-000123",
      "applicantId": "user-uuid-123",
      "applicantType": "TEACHER",
      "createdTime": 1702300000000,
      "checkType": "FULL",
      "priority": "NORMAL",
      "evidences": [
        {
          "type": "PHOTO",
          "fileStoreId": "fs-uuid-1",
          "purpose": "DOG_PHOTO",
          "metadata": {
            "gpsLatitude": 28.6139,
            "gpsLongitude": 77.2090,
            "timestamp": 1702299000000,
            "deviceId": "device-123",
            "exifPresent": true
          }
        },
        {
          "type": "PHOTO",
          "fileStoreId": "fs-uuid-2",
          "purpose": "SELFIE",
          "metadata": {
            "gpsLatitude": 28.6140,
            "gpsLongitude": 77.2091,
            "timestamp": 1702299100000,
            "deviceId": "device-123",
            "exifPresent": true
          }
        }
      ],
      "locationData": {
        "reportedLatitude": 28.6139,
        "reportedLongitude": 77.2090,
        "locality": "LOC-001",
        "ward": "WARD-A",
        "district": "CENTRAL"
      },
      "additionalData": {
        "dogCount": 2,
        "isAggressive": true,
        "breed": "MIXED"
      }
    }
  }
}
```

### 3.2 FraudFlag

The output when a fraud rule is triggered.

```json
{
  "FraudFlag": {
    "id": "flag-uuid-1",
    "tenantId": "ncr",
    "moduleCode": "SDCRS",
    "applicationId": "NCR-SDCRS-2024-000123",
    "ruleId": "SDCRS-003",
    "ruleCode": "GPS_PHOTO_SELFIE_MISMATCH",
    "category": "LOC",
    "severity": "HIGH",
    "status": "OPEN",
    "score": 85,
    "details": {
      "message": "Dog photo GPS is 850m from selfie GPS",
      "threshold": 500,
      "actualValue": 850,
      "unit": "meters",
      "evidence": {
        "dogPhotoGps": { "lat": 28.6139, "lon": 77.2090 },
        "selfieGps": { "lat": 28.6200, "lon": 77.2150 }
      }
    },
    "recommendedAction": "MANUAL_REVIEW",
    "autoAction": null,
    "linkedFlags": [],
    "linkedApplications": [],
    "resolution": null,
    "auditDetails": {
      "createdBy": "SYSTEM",
      "createdTime": 1702300500000,
      "lastModifiedBy": null,
      "lastModifiedTime": null
    }
  }
}
```

### 3.3 FraudCheckResult

The complete result of fraud analysis.

```json
{
  "FraudCheckResult": {
    "id": "result-uuid-1",
    "fraudCheckId": "check-uuid-1",
    "applicationId": "NCR-SDCRS-2024-000123",
    "status": "FLAGGED",
    "overallScore": 72,
    "riskLevel": "HIGH",
    "flagCount": 2,
    "flags": [ /* Array of FraudFlag */ ],
    "recommendation": "HOLD_FOR_REVIEW",
    "processingTime": 1250,
    "rulesEvaluated": 15,
    "rulesPassed": 13,
    "rulesFailed": 2,
    "checksPerformed": {
      "duplicateCheck": true,
      "velocityCheck": true,
      "geoCheck": true,
      "temporalCheck": true,
      "patternCheck": true
    },
    "hashes": {
      "imageHash": "a4b3c2d1e0f9...",
      "contentHash": "sha256:..."
    }
  }
}
```

---

## 4. API Specifications

### 4.1 Endpoint Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/fraud-detection/v1/_check` | Submit application for fraud check |
| `POST` | `/fraud-detection/v1/_checkAsync` | Async fraud check via Kafka |
| `POST` | `/fraud-detection/v1/flags/_search` | Search fraud flags |
| `GET` | `/fraud-detection/v1/flags/{id}` | Get flag by ID |
| `POST` | `/fraud-detection/v1/flags/_resolve` | Resolve a fraud flag |
| `POST` | `/fraud-detection/v1/flags/_escalate` | Escalate flag to higher authority |
| `POST` | `/fraud-detection/v1/rules/_search` | Search configured rules |
| `POST` | `/fraud-detection/v1/stats/_aggregate` | Get fraud statistics |
| `POST` | `/fraud-detection/v1/applicant/_riskProfile` | Get applicant risk profile |

### 4.2 Synchronous Check API

**POST** `/fraud-detection/v1/_check`

Used for real-time fraud check during application submission.

**Request:**
```json
{
  "RequestInfo": { /* Standard DIGIT RequestInfo */ },
  "fraudCheck": {
    "tenantId": "ncr",
    "moduleCode": "SDCRS",
    "applicationId": "NCR-SDCRS-2024-000123",
    "applicantId": "user-uuid-123",
    "checkType": "QUICK",
    "evidences": [ /* ... */ ],
    "locationData": { /* ... */ }
  }
}
```

**Response:**
```json
{
  "ResponseInfo": { /* Standard DIGIT ResponseInfo */ },
  "fraudCheckResult": {
    "status": "CLEAN",
    "overallScore": 15,
    "riskLevel": "LOW",
    "flagCount": 0,
    "flags": [],
    "recommendation": "ALLOW",
    "processingTime": 450
  }
}
```

### 4.3 Flag Search API

**POST** `/fraud-detection/v1/flags/_search`

```json
{
  "RequestInfo": { /* ... */ },
  "searchCriteria": {
    "tenantId": "ncr",
    "moduleCode": "SDCRS",
    "applicationIds": ["NCR-SDCRS-2024-000123"],
    "applicantIds": ["user-uuid-123"],
    "status": ["OPEN", "UNDER_REVIEW"],
    "severity": ["HIGH", "CRITICAL"],
    "category": ["LOC", "DUP"],
    "fromDate": 1702000000000,
    "toDate": 1702400000000,
    "offset": 0,
    "limit": 50,
    "sortBy": "createdTime",
    "sortOrder": "DESC"
  }
}
```

### 4.4 Resolve Flag API

**POST** `/fraud-detection/v1/flags/_resolve`

```json
{
  "RequestInfo": { /* ... */ },
  "flagResolution": {
    "flagId": "flag-uuid-1",
    "resolution": "FALSE_POSITIVE",
    "resolutionReason": "Photos taken in quick succession; GPS drift is within acceptable range",
    "action": "ALLOW_APPLICATION",
    "reviewerId": "verifier-uuid"
  }
}
```

**Resolution Types:**
- `FALSE_POSITIVE` - Flag was incorrect, application is legitimate
- `TRUE_POSITIVE` - Fraud confirmed, take action
- `INCONCLUSIVE` - Cannot determine, escalate or allow with monitoring
- `DUPLICATE_FLAG` - Already addressed by another flag

---

## 5. Kafka Integration

### 5.1 Topics

| Topic | Publisher | Consumer | Purpose |
|-------|-----------|----------|---------|
| `fraud-check-request` | Domain Services | Fraud Detection | Async check requests |
| `fraud-check-result` | Fraud Detection | Domain Services | Check results |
| `fraud-flag-created` | Fraud Detection | Notification, DSS | New flag alerts |
| `fraud-flag-resolved` | Fraud Detection | DSS, Audit | Flag resolution events |
| `fraud-applicant-risk` | Fraud Detection | User Service | Risk profile updates |

### 5.2 Message Formats

**fraud-check-request:**
```json
{
  "topic": "fraud-check-request",
  "key": "NCR-SDCRS-2024-000123",
  "value": {
    "fraudCheck": { /* FraudCheckRequest */ }
  }
}
```

**fraud-flag-created:**
```json
{
  "topic": "fraud-flag-created",
  "key": "flag-uuid-1",
  "value": {
    "flag": { /* FraudFlag */ },
    "application": {
      "id": "NCR-SDCRS-2024-000123",
      "moduleCode": "SDCRS",
      "applicantId": "user-uuid-123"
    }
  }
}
```

---

## 6. MDMS Configuration

### 6.1 Module: FRAUD-DETECTION

#### FraudRules.json

```json
{
  "tenantId": "ncr",
  "moduleName": "FRAUD-DETECTION",
  "FraudRules": [
    {
      "id": "STD-001",
      "code": "MISSING_GPS",
      "name": "Missing GPS Coordinates",
      "description": "Application evidence lacks GPS coordinates",
      "category": "DQ",
      "severity": "MEDIUM",
      "enabled": true,
      "applicableModules": ["SDCRS", "PGR", "TL"],
      "condition": {
        "type": "NULL_CHECK",
        "field": "evidences[*].metadata.gpsLatitude"
      },
      "action": {
        "type": "FLAG",
        "autoReject": false,
        "notifyApplicant": false,
        "notifyVerifier": true
      }
    },
    {
      "id": "STD-002",
      "code": "GPS_OUTSIDE_BOUNDARY",
      "name": "GPS Outside Tenant Boundary",
      "description": "GPS coordinates are outside the tenant jurisdiction",
      "category": "LOC",
      "severity": "HIGH",
      "enabled": true,
      "applicableModules": ["SDCRS", "PGR"],
      "condition": {
        "type": "GEO_BOUNDARY",
        "field": "locationData",
        "boundaryType": "TENANT"
      },
      "action": {
        "type": "FLAG",
        "autoReject": true,
        "rejectionReason": "LOC_OUTSIDE_BOUNDARY",
        "notifyApplicant": true
      }
    },
    {
      "id": "STD-003",
      "code": "HIGH_VELOCITY",
      "name": "High Submission Velocity",
      "description": "User submitted too many applications in short time",
      "category": "VEL",
      "severity": "MEDIUM",
      "enabled": true,
      "applicableModules": ["SDCRS"],
      "condition": {
        "type": "VELOCITY",
        "field": "applicantId",
        "threshold": 5,
        "windowHours": 1
      },
      "action": {
        "type": "FLAG",
        "autoReject": false,
        "cooldownMinutes": 60
      }
    },
    {
      "id": "STD-006",
      "code": "NEAR_DUPLICATE",
      "name": "Near-Duplicate Submission",
      "description": "Image is highly similar to a recent submission",
      "category": "DUP",
      "severity": "HIGH",
      "enabled": true,
      "applicableModules": ["SDCRS"],
      "condition": {
        "type": "IMAGE_SIMILARITY",
        "field": "evidences[purpose=DOG_PHOTO].fileStoreId",
        "algorithm": "pHash",
        "threshold": 0.90,
        "lookbackDays": 7
      },
      "action": {
        "type": "FLAG",
        "autoReject": false,
        "linkToOriginal": true
      }
    }
  ]
}
```

#### RiskScoreConfig.json

```json
{
  "tenantId": "ncr",
  "moduleName": "FRAUD-DETECTION",
  "RiskScoreConfig": {
    "weights": {
      "DQ": 10,
      "DUP": 30,
      "LOC": 40,
      "VEL": 20,
      "TMP": 25,
      "IDN": 50,
      "COL": 60,
      "EVD": 35
    },
    "thresholds": {
      "LOW": { "min": 0, "max": 25 },
      "MEDIUM": { "min": 26, "max": 50 },
      "HIGH": { "min": 51, "max": 75 },
      "CRITICAL": { "min": 76, "max": 100 }
    },
    "autoRejectThreshold": 80,
    "escalationThreshold": 60
  }
}
```

#### ApplicantPenalties.json

```json
{
  "tenantId": "ncr",
  "moduleName": "FRAUD-DETECTION",
  "ApplicantPenalties": [
    {
      "level": 1,
      "name": "WARNING",
      "triggerCondition": { "confirmedFrauds": 1 },
      "action": "NOTIFY_WARNING",
      "duration": null
    },
    {
      "level": 2,
      "name": "COOLDOWN",
      "triggerCondition": { "confirmedFrauds": 2, "withinDays": 30 },
      "action": "SUBMISSION_BLOCK",
      "duration": { "days": 7 }
    },
    {
      "level": 3,
      "name": "SUSPENSION",
      "triggerCondition": { "confirmedFrauds": 3, "withinDays": 90 },
      "action": "ACCOUNT_SUSPEND",
      "duration": { "days": 30 }
    },
    {
      "level": 4,
      "name": "BAN",
      "triggerCondition": { "confirmedFrauds": 5 },
      "action": "PERMANENT_BAN",
      "duration": null,
      "appealProcess": true
    }
  ]
}
```

---

## 7. Database Schema

### 7.1 Evidence-Based Duplicate Detection Architecture

The Fraud Detection Service is designed as a **query consumer**, not a data store for evidence. Evidence metadata (hashes, GPS coordinates, timestamps) is computed and persisted during the Dog Report submission workflow, and the Fraud Detection Service queries this data when evaluating duplicate detection rules.

#### Design Rationale

| Approach | Pros | Cons |
|----------|------|------|
| **Fraud service stores hashes** | Simple isolation | Data duplication, sync issues, lost on restart |
| **Fraud service queries report DB** ✓ | Single source of truth, always current | Requires DB access, slightly higher latency |

**Recommended: Query-based approach** - The Dog Report Service computes and stores evidence metadata (pHash, EXIF GPS, timestamps) at submission time. The Fraud Detection Service queries this data when evaluating HASH_MATCH, IMAGE_SIMILARITY, and GEO_CLUSTER rules.

#### Evidence Data Flow

```
SUBMISSION TIME                        FRAUD EVALUATION TIME
───────────────                        ─────────────────────
Teacher App                            Fraud Detection Service
    │                                          │
    │ POST /sdcrs/v1/report/_create            │ Query existing evidence
    ▼                                          ▼
SDCRS Service                          SELECT * FROM eg_sdcrs_report_evidence
  • Compute pHash from photo           WHERE image_hash_hex = ?
  • Extract EXIF GPS coordinates       OR bit_count(image_phash # ?) <= 10
  • Store evidence metadata            OR ST_DWithin(gps_location, ?, 50m)
    │
    │ Kafka: save-sdcrs-report
    ▼
PostgreSQL
  • eg_sdcrs_report (main report table)
  • eg_sdcrs_report_evidence (evidence metadata)
```

### 7.2 Evidence Table Schema

```sql
-- Evidence metadata for duplicate detection (populated by SDCRS Service at submission)
CREATE TABLE eg_sdcrs_report_evidence (
    id VARCHAR(64) PRIMARY KEY,
    report_id VARCHAR(64) NOT NULL REFERENCES eg_sdcrs_report(id),
    tenant_id VARCHAR(64) NOT NULL,
    reporter_id VARCHAR(64) NOT NULL,
    evidence_type VARCHAR(32) NOT NULL,      -- DOG_PHOTO, SELFIE, CAPTURE_PHOTO
    filestore_id VARCHAR(64) NOT NULL,

    -- Hash columns for duplicate detection
    image_phash BIGINT,                       -- Perceptual hash (64-bit)
    image_hash_hex VARCHAR(64),               -- SHA-256 hex for exact match

    -- Location data from EXIF
    gps_location GEOGRAPHY(POINT, 4326),     -- PostGIS point (lon, lat)
    gps_latitude DECIMAL(10, 8),
    gps_longitude DECIMAL(11, 8),

    -- Temporal data
    capture_timestamp BIGINT,                 -- EXIF timestamp (epoch ms)

    -- Device data
    device_id VARCHAR(128),
    device_model VARCHAR(128),
    exif_present BOOLEAN DEFAULT false,

    -- Audit
    created_time BIGINT NOT NULL,
    created_by VARCHAR(64) NOT NULL
);

-- Indexes optimized for fraud detection queries
CREATE INDEX idx_evidence_report ON eg_sdcrs_report_evidence(report_id);
CREATE INDEX idx_evidence_reporter ON eg_sdcrs_report_evidence(reporter_id);
CREATE INDEX idx_evidence_tenant ON eg_sdcrs_report_evidence(tenant_id);

-- B-tree for exact hash lookups: O(log n), <1ms
CREATE INDEX idx_evidence_hash_hex ON eg_sdcrs_report_evidence(image_hash_hex);

-- B-tree for pHash similarity (enables bit_count filtering): O(log n)
CREATE INDEX idx_evidence_phash ON eg_sdcrs_report_evidence(image_phash);

-- GiST spatial index for geo proximity: O(log n), 2-5ms
CREATE INDEX idx_evidence_location ON eg_sdcrs_report_evidence
    USING GIST (gps_location);

-- Composite for time-bounded queries
CREATE INDEX idx_evidence_created ON eg_sdcrs_report_evidence(created_time DESC);
CREATE INDEX idx_evidence_device ON eg_sdcrs_report_evidence(device_id);
```

### 7.3 Duplicate Detection Query Patterns

The Fraud Detection Service executes these queries against the evidence table:

#### 7.3.1 Exact Duplicate Detection (HASH_MATCH rule)

```sql
-- O(log n) with B-tree index, latency <1ms
-- Finds if the exact same photo was submitted before

SELECT e.report_id, r.report_number, r.reporter_id, r.status
FROM eg_sdcrs_report_evidence e
JOIN eg_sdcrs_report r ON e.report_id = r.id
WHERE e.tenant_id = :tenantId
  AND e.image_hash_hex = :submittedHashHex
  AND e.report_id != :currentReportId
  AND r.status NOT IN ('AUTO_REJECTED', 'REJECTED')
  AND e.created_time > :thirtyDaysAgo
ORDER BY e.created_time DESC
LIMIT 5;
```

#### 7.3.2 Near-Duplicate Detection (IMAGE_SIMILARITY rule)

```sql
-- Uses bit_count() for Hamming distance on pHash
-- PostgreSQL: bit_count(a # b) where # is XOR
-- Hamming distance ≤ 10 means ~85% visual similarity

SELECT e.report_id, r.report_number, r.reporter_id,
       bit_count(e.image_phash # :submittedPHash) AS hamming_distance
FROM eg_sdcrs_report_evidence e
JOIN eg_sdcrs_report r ON e.report_id = r.id
WHERE e.tenant_id = :tenantId
  AND e.evidence_type = 'DOG_PHOTO'
  AND e.report_id != :currentReportId
  AND e.image_phash IS NOT NULL
  AND bit_count(e.image_phash # :submittedPHash) <= 10
  AND r.status NOT IN ('AUTO_REJECTED', 'REJECTED')
  AND e.created_time > :sevenDaysAgo
ORDER BY hamming_distance ASC
LIMIT 10;
```

#### 7.3.3 Geo-Temporal Duplicate Detection (GEO_CLUSTER rule)

```sql
-- Finds reports from DIFFERENT reporters at same location within 24 hours
-- Uses PostGIS ST_DWithin for spatial query with GiST index

SELECT e.report_id, r.report_number, r.reporter_id,
       ST_Distance(e.gps_location::geography,
                   ST_MakePoint(:lon, :lat)::geography) AS distance_meters
FROM eg_sdcrs_report_evidence e
JOIN eg_sdcrs_report r ON e.report_id = r.id
WHERE e.tenant_id = :tenantId
  AND e.evidence_type = 'DOG_PHOTO'
  AND e.report_id != :currentReportId
  AND r.reporter_id != :currentReporterId        -- Different reporter
  AND ST_DWithin(
        e.gps_location::geography,
        ST_MakePoint(:lon, :lat)::geography,
        50                                        -- 50 meters radius
      )
  AND e.created_time > :twentyFourHoursAgo
  AND r.status NOT IN ('AUTO_REJECTED', 'REJECTED', 'DUPLICATE')
ORDER BY distance_meters ASC
LIMIT 5;
```

#### 7.3.4 Device Collusion Detection (DEVICE_SHARING rule)

```sql
-- Finds multiple unique reporters using the same device

SELECT e.device_id, COUNT(DISTINCT r.reporter_id) AS unique_reporters,
       ARRAY_AGG(DISTINCT r.reporter_id) AS reporter_ids
FROM eg_sdcrs_report_evidence e
JOIN eg_sdcrs_report r ON e.report_id = r.id
WHERE e.tenant_id = :tenantId
  AND e.device_id = :submittedDeviceId
  AND e.device_id IS NOT NULL
  AND e.created_time > :sevenDaysAgo
GROUP BY e.device_id
HAVING COUNT(DISTINCT r.reporter_id) >= 2;
```

### 7.4 Query Performance Estimates

| Check Type | Index Used | Complexity | Expected Latency |
|------------|-----------|------------|------------------|
| Exact hash match | B-tree on `image_hash_hex` | O(log n) | <1ms |
| pHash similarity | B-tree + bit_count filter | O(n) filtered | 5-20ms |
| Geo proximity | GiST spatial on `gps_location` | O(log n) | 2-5ms |
| Device lookup | B-tree on `device_id` | O(log n) | <1ms |
| **Total fraud evaluation** | | | **~10-30ms** |

### 7.5 SDCRS Service Evidence Enrichment

The SDCRS Service computes and persists evidence metadata during report creation:

```java
// In SDCRSService.create() - called before persisting report
private void enrichEvidenceMetadata(DogReport report) {
    for (Evidence evidence : report.getEvidences()) {
        // 1. Fetch file from FileStore
        FileStoreResponse file = fileStoreService.get(evidence.getFileStoreId());

        // 2. Compute perceptual hash (64-bit pHash)
        long pHash = imageHashService.computePHash(file.getBytes());
        evidence.setImagePHash(pHash);

        // 3. Compute SHA-256 for exact match
        String sha256 = DigestUtils.sha256Hex(file.getBytes());
        evidence.setImageHashHex(sha256);

        // 4. Extract EXIF GPS if present
        ExifData exif = exifService.extract(file.getBytes());
        if (exif != null && exif.hasGps()) {
            evidence.setGpsLatitude(exif.getLatitude());
            evidence.setGpsLongitude(exif.getLongitude());
            evidence.setCaptureTimestamp(exif.getTimestamp());
            evidence.setExifPresent(true);
        }

        // 5. Store device info
        evidence.setDeviceId(report.getDeviceInfo().getDeviceId());
        evidence.setDeviceModel(report.getDeviceInfo().getModel());
    }
}
```

### 7.6 Fraud Detection Service Query Implementation

```java
// In InternalRuleEvaluator - queries evidence table for duplicate detection
@Service
public class DuplicateDetectionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Check for exact duplicate (same image hash)
     */
    public Optional<DuplicateMatch> findExactDuplicate(
            String tenantId, String imageHashHex, String excludeReportId) {

        String sql = """
            SELECT e.report_id, r.report_number, r.reporter_id
            FROM eg_sdcrs_report_evidence e
            JOIN eg_sdcrs_report r ON e.report_id = r.id
            WHERE e.tenant_id = ?
              AND e.image_hash_hex = ?
              AND e.report_id != ?
              AND r.status NOT IN ('AUTO_REJECTED', 'REJECTED')
              AND e.created_time > ?
            ORDER BY e.created_time DESC
            LIMIT 1
            """;

        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        return jdbcTemplate.query(sql,
            new Object[]{tenantId, imageHashHex, excludeReportId, thirtyDaysAgo},
            rs -> rs.next() ? Optional.of(new DuplicateMatch(
                rs.getString("report_id"),
                rs.getString("report_number"),
                rs.getString("reporter_id"),
                DuplicateType.EXACT
            )) : Optional.empty()
        );
    }

    /**
     * Check for near-duplicate using pHash Hamming distance
     */
    public List<DuplicateMatch> findSimilarImages(
            String tenantId, long pHash, String excludeReportId, int maxHammingDistance) {

        String sql = """
            SELECT e.report_id, r.report_number, r.reporter_id,
                   bit_count(e.image_phash # ?::bigint) AS hamming_distance
            FROM eg_sdcrs_report_evidence e
            JOIN eg_sdcrs_report r ON e.report_id = r.id
            WHERE e.tenant_id = ?
              AND e.evidence_type = 'DOG_PHOTO'
              AND e.report_id != ?
              AND e.image_phash IS NOT NULL
              AND bit_count(e.image_phash # ?::bigint) <= ?
              AND r.status NOT IN ('AUTO_REJECTED', 'REJECTED')
              AND e.created_time > ?
            ORDER BY hamming_distance ASC
            LIMIT 10
            """;

        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);

        return jdbcTemplate.query(sql,
            new Object[]{pHash, tenantId, excludeReportId, pHash, maxHammingDistance, sevenDaysAgo},
            (rs, rowNum) -> new DuplicateMatch(
                rs.getString("report_id"),
                rs.getString("report_number"),
                rs.getString("reporter_id"),
                DuplicateType.SIMILAR,
                rs.getInt("hamming_distance")
            )
        );
    }
}
```

---

### 7.7 Fraud Service Tables

```sql
-- Fraud check requests and results
CREATE TABLE eg_fraud_check (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    module_code VARCHAR(32) NOT NULL,
    business_service VARCHAR(64),
    application_id VARCHAR(128) NOT NULL,
    applicant_id VARCHAR(64) NOT NULL,
    applicant_type VARCHAR(32),
    check_type VARCHAR(16) NOT NULL,  -- QUICK, FULL, DEEP
    priority VARCHAR(16) DEFAULT 'NORMAL',
    status VARCHAR(32) NOT NULL,  -- PENDING, PROCESSING, COMPLETED, FAILED
    overall_score INTEGER,
    risk_level VARCHAR(16),
    flag_count INTEGER DEFAULT 0,
    recommendation VARCHAR(32),
    processing_time_ms INTEGER,
    rules_evaluated INTEGER,
    rules_passed INTEGER,
    rules_failed INTEGER,
    image_hash VARCHAR(128),
    content_hash VARCHAR(128),
    additional_data JSONB,
    created_by VARCHAR(64) NOT NULL,
    created_time BIGINT NOT NULL,
    last_modified_by VARCHAR(64),
    last_modified_time BIGINT
);

CREATE INDEX idx_fraud_check_tenant ON eg_fraud_check(tenant_id);
CREATE INDEX idx_fraud_check_module ON eg_fraud_check(module_code);
CREATE INDEX idx_fraud_check_application ON eg_fraud_check(application_id);
CREATE INDEX idx_fraud_check_applicant ON eg_fraud_check(applicant_id);
CREATE INDEX idx_fraud_check_status ON eg_fraud_check(status);
CREATE INDEX idx_fraud_check_created ON eg_fraud_check(created_time);

-- Fraud flags raised during checks
CREATE TABLE eg_fraud_flag (
    id VARCHAR(64) PRIMARY KEY,
    fraud_check_id VARCHAR(64) NOT NULL REFERENCES eg_fraud_check(id),
    tenant_id VARCHAR(64) NOT NULL,
    module_code VARCHAR(32) NOT NULL,
    application_id VARCHAR(128) NOT NULL,
    applicant_id VARCHAR(64) NOT NULL,
    rule_id VARCHAR(32) NOT NULL,
    rule_code VARCHAR(64) NOT NULL,
    category VARCHAR(8) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL,  -- OPEN, UNDER_REVIEW, RESOLVED, DISMISSED
    score INTEGER,
    details JSONB,
    recommended_action VARCHAR(32),
    auto_action VARCHAR(32),
    resolution VARCHAR(32),
    resolution_reason TEXT,
    resolver_id VARCHAR(64),
    resolved_time BIGINT,
    linked_flag_ids VARCHAR(256),
    linked_application_ids VARCHAR(512),
    created_by VARCHAR(64) NOT NULL,
    created_time BIGINT NOT NULL,
    last_modified_by VARCHAR(64),
    last_modified_time BIGINT
);

CREATE INDEX idx_fraud_flag_tenant ON eg_fraud_flag(tenant_id);
CREATE INDEX idx_fraud_flag_module ON eg_fraud_flag(module_code);
CREATE INDEX idx_fraud_flag_application ON eg_fraud_flag(application_id);
CREATE INDEX idx_fraud_flag_applicant ON eg_fraud_flag(applicant_id);
CREATE INDEX idx_fraud_flag_status ON eg_fraud_flag(status);
CREATE INDEX idx_fraud_flag_severity ON eg_fraud_flag(severity);
CREATE INDEX idx_fraud_flag_category ON eg_fraud_flag(category);
CREATE INDEX idx_fraud_flag_created ON eg_fraud_flag(created_time);

-- Applicant risk profiles
CREATE TABLE eg_applicant_risk_profile (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    applicant_id VARCHAR(64) NOT NULL UNIQUE,
    risk_score INTEGER DEFAULT 0,
    risk_level VARCHAR(16) DEFAULT 'LOW',
    total_applications INTEGER DEFAULT 0,
    flagged_applications INTEGER DEFAULT 0,
    confirmed_frauds INTEGER DEFAULT 0,
    false_positives INTEGER DEFAULT 0,
    penalty_level INTEGER DEFAULT 0,
    penalty_status VARCHAR(32),
    penalty_expiry BIGINT,
    last_application_time BIGINT,
    last_fraud_time BIGINT,
    additional_data JSONB,
    created_time BIGINT NOT NULL,
    last_modified_time BIGINT
);

CREATE UNIQUE INDEX idx_risk_profile_applicant ON eg_applicant_risk_profile(tenant_id, applicant_id);
CREATE INDEX idx_risk_profile_score ON eg_applicant_risk_profile(risk_score);
CREATE INDEX idx_risk_profile_level ON eg_applicant_risk_profile(risk_level);

-- Image hashes for duplicate detection
CREATE TABLE eg_image_hash (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    module_code VARCHAR(32) NOT NULL,
    application_id VARCHAR(128) NOT NULL,
    file_store_id VARCHAR(64) NOT NULL,
    purpose VARCHAR(32),  -- DOG_PHOTO, SELFIE, EVIDENCE
    hash_algorithm VARCHAR(16) NOT NULL,  -- pHash, dHash, MD5
    hash_value VARCHAR(256) NOT NULL,
    hash_bits BYTEA,  -- Binary hash for efficient comparison
    created_time BIGINT NOT NULL
);

CREATE INDEX idx_image_hash_tenant ON eg_image_hash(tenant_id);
CREATE INDEX idx_image_hash_module ON eg_image_hash(module_code);
CREATE INDEX idx_image_hash_value ON eg_image_hash(hash_value);
CREATE INDEX idx_image_hash_created ON eg_image_hash(created_time);
```

### 7.2 Elasticsearch Index for Hash Similarity

```json
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
      "tenantId": { "type": "keyword" },
      "moduleCode": { "type": "keyword" },
      "applicationId": { "type": "keyword" },
      "fileStoreId": { "type": "keyword" },
      "purpose": { "type": "keyword" },
      "hashVector": {
        "type": "dense_vector",
        "dims": 64,
        "index": true,
        "similarity": "cosine"
      },
      "createdTime": { "type": "date", "format": "epoch_millis" }
    }
  }
}
```

---

## 8. Integration Patterns

### 8.1 Synchronous Check (Blocking)

For high-severity rules that must block submission:

```
┌─────────┐         ┌──────────────┐         ┌────────────────┐
│  SDCRS  │         │   Gateway    │         │ Fraud Service  │
└────┬────┘         └──────┬───────┘         └───────┬────────┘
     │                     │                         │
     │  POST /_create      │                         │
     │────────────────────►│                         │
     │                     │  POST /_check (QUICK)   │
     │                     │────────────────────────►│
     │                     │                         │
     │                     │   FraudCheckResult      │
     │                     │◄────────────────────────│
     │                     │                         │
     │   if CRITICAL:      │                         │
     │   reject            │                         │
     │◄────────────────────│                         │
     │                     │                         │
     │   else: continue    │                         │
     │   with workflow     │                         │
```

### 8.2 Asynchronous Check (Non-Blocking)

For detailed analysis that can run post-submission:

```
┌─────────┐       ┌───────┐       ┌────────────────┐       ┌──────────┐
│  SDCRS  │       │ Kafka │       │ Fraud Service  │       │ Workflow │
└────┬────┘       └───┬───┘       └───────┬────────┘       └────┬─────┘
     │                │                   │                     │
     │  save-sdcrs-   │                   │                     │
     │  service       │                   │                     │
     │───────────────►│                   │                     │
     │                │                   │                     │
     │                │  fraud-check-     │                     │
     │                │  request          │                     │
     │                │──────────────────►│                     │
     │                │                   │                     │
     │                │                   │ (async analysis)    │
     │                │                   │                     │
     │                │  fraud-flag-      │                     │
     │                │  created          │                     │
     │                │◄──────────────────│                     │
     │                │                   │                     │
     │                │                   │  if HIGH/CRITICAL:  │
     │                │                   │  update workflow    │
     │                │                   │─────────────────────►
```

### 8.3 Domain Service Integration Code

```java
// In SDCRS Service - Create endpoint
@PostMapping("/_create")
public ResponseEntity<DogReportResponse> create(@RequestBody DogReportRequest request) {

    // 1. Quick synchronous fraud check (blocking critical issues)
    FraudCheckRequest fraudCheck = buildFraudCheckRequest(request, "QUICK");
    FraudCheckResult quickResult = fraudDetectionService.check(fraudCheck);

    if (quickResult.getRiskLevel() == RiskLevel.CRITICAL) {
        // Block submission immediately
        throw new FraudDetectedException(quickResult.getFlags());
    }

    // 2. Proceed with normal creation
    DogReport report = dogReportService.create(request);

    // 3. Trigger async deep fraud check
    FraudCheckRequest deepCheck = buildFraudCheckRequest(request, "FULL");
    kafkaTemplate.send("fraud-check-request", report.getId(), deepCheck);

    return ResponseEntity.ok(new DogReportResponse(report));
}
```

---

## 9. Fraud Flag Lifecycle

```
                                    ┌─────────────────┐
                                    │   Rule Engine   │
                                    │   evaluates     │
                                    └────────┬────────┘
                                             │
                                             ▼
                              ┌──────────────────────────┐
                              │          OPEN            │
                              │   (Flag just created)    │
                              └────────────┬─────────────┘
                                           │
                    ┌──────────────────────┼──────────────────────┐
                    │                      │                      │
                    ▼                      ▼                      ▼
         ┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
         │   AUTO_RESOLVED  │   │   UNDER_REVIEW   │   │    ESCALATED     │
         │  (Auto-action)   │   │ (Human assigned) │   │ (To higher auth) │
         └────────┬─────────┘   └────────┬─────────┘   └────────┬─────────┘
                  │                      │                      │
                  │                      ▼                      │
                  │           ┌──────────────────┐              │
                  │           │   Resolution     │              │
                  │           │   Decision       │◄─────────────┘
                  │           └────────┬─────────┘
                  │                    │
                  │     ┌──────────────┼──────────────┐
                  │     │              │              │
                  ▼     ▼              ▼              ▼
         ┌─────────────────┐  ┌─────────────┐  ┌─────────────┐
         │ FALSE_POSITIVE  │  │TRUE_POSITIVE│  │INCONCLUSIVE │
         │ (Allow app)     │  │ (Take action│  │ (Monitor)   │
         └─────────────────┘  └─────────────┘  └─────────────┘
```

---

## 10. Dashboard & Analytics

### 10.1 Fraud Metrics for DSS

| Metric ID | Name | Aggregation | Drill-down |
|-----------|------|-------------|------------|
| `FRAUD_TOTAL_CHECKS` | Total Fraud Checks | Count | Module, Tenant, Date |
| `FRAUD_FLAG_COUNT` | Flags Raised | Count | Category, Severity, Rule |
| `FRAUD_FLAG_RATE` | Flag Rate (%) | Percentage | Module, Time Period |
| `FRAUD_RESOLUTION_TIME` | Avg Resolution Time | Average | Severity, Resolver |
| `FRAUD_FALSE_POSITIVE_RATE` | False Positive Rate | Percentage | Rule, Category |
| `FRAUD_CONFIRMED_RATE` | Confirmed Fraud Rate | Percentage | Module, Applicant Type |
| `FRAUD_BLOCKED_COUNT` | Auto-Blocked Submissions | Count | Rule, Date |
| `FRAUD_RISK_DISTRIBUTION` | Risk Level Distribution | Distribution | Module, Date |

### 10.2 Real-time Alerts

| Alert | Condition | Channel | Recipients |
|-------|-----------|---------|------------|
| Spike in flags | >3x normal rate in 1 hour | SMS, Email | State Admin |
| Critical flag | Severity = CRITICAL | SMS | District Admin |
| Collusion detected | Category = COL | Email | State Admin, Fraud Team |
| High false positive | Rule FP rate >50% | Email | System Admin |

---

## 11. SDCRS-Specific Rules

### 11.1 Module-Specific Configuration

```json
{
  "tenantId": "ncr",
  "moduleName": "FRAUD-DETECTION",
  "ModuleRules": {
    "SDCRS": [
      {
        "id": "SDCRS-001",
        "code": "DOG_PHOTO_SELFIE_TIME_GAP",
        "name": "Photo-Selfie Time Gap Too Large",
        "description": "Dog photo and selfie timestamps differ by more than 10 minutes",
        "category": "TMP",
        "severity": "MEDIUM",
        "condition": {
          "type": "TIMESTAMP_DIFF",
          "field1": "evidences[purpose=DOG_PHOTO].metadata.timestamp",
          "field2": "evidences[purpose=SELFIE].metadata.timestamp",
          "maxDiffMinutes": 10
        }
      },
      {
        "id": "SDCRS-002",
        "code": "AGGRESSIVE_DOG_NO_PHOTO",
        "name": "Aggressive Dog Without Adequate Evidence",
        "description": "Dog marked as aggressive but photo quality/angle is poor",
        "category": "EVD",
        "severity": "LOW",
        "condition": {
          "type": "CUSTOM",
          "expression": "additionalData.isAggressive == true && imageQualityScore < 0.5"
        }
      },
      {
        "id": "SDCRS-003",
        "code": "GPS_PHOTO_SELFIE_MISMATCH",
        "name": "GPS Mismatch Between Photos",
        "description": "Dog photo GPS and selfie GPS are too far apart",
        "category": "LOC",
        "severity": "HIGH",
        "condition": {
          "type": "GEO_DISTANCE",
          "point1": "evidences[purpose=DOG_PHOTO].metadata.gps",
          "point2": "evidences[purpose=SELFIE].metadata.gps",
          "maxDistanceMeters": 500
        }
      },
      {
        "id": "SDCRS-004",
        "code": "MULTIPLE_DOGS_SAME_LOCATION",
        "name": "Multiple Reports from Same Location",
        "description": "Multiple dog reports from nearly identical GPS within 24 hours",
        "category": "DUP",
        "severity": "MEDIUM",
        "condition": {
          "type": "GEO_CLUSTER",
          "field": "locationData",
          "radiusMeters": 50,
          "windowHours": 24,
          "minCount": 3
        }
      },
      {
        "id": "SDCRS-005",
        "code": "SCHOOL_HOURS_VIOLATION",
        "name": "Report Outside School Hours",
        "description": "Teacher submitting report outside typical school hours",
        "category": "TMP",
        "severity": "LOW",
        "condition": {
          "type": "TIME_WINDOW",
          "field": "createdTime",
          "allowedWindows": [
            { "start": "07:00", "end": "18:00", "days": ["MON", "TUE", "WED", "THU", "FRI"] }
          ]
        },
        "action": {
          "type": "FLAG",
          "autoReject": false
        }
      },
      {
        "id": "SDCRS-006",
        "code": "MONTHLY_CAP_APPROACHING",
        "name": "Monthly Submission Cap Warning",
        "description": "Teacher approaching monthly submission limit",
        "category": "VEL",
        "severity": "LOW",
        "condition": {
          "type": "AGGREGATE_COUNT",
          "field": "applicantId",
          "periodDays": 30,
          "threshold": 15,
          "capValue": 20
        },
        "action": {
          "type": "NOTIFY",
          "notifyApplicant": true
        }
      },
      {
        "id": "SDCRS-007",
        "code": "DEVICE_SHARED_MULTIPLE_TEACHERS",
        "name": "Device Used by Multiple Teachers",
        "description": "Same device ID used by different teacher accounts",
        "category": "COL",
        "severity": "HIGH",
        "condition": {
          "type": "DEVICE_SHARING",
          "field": "evidences[*].metadata.deviceId",
          "minUniqueUsers": 2,
          "windowDays": 7
        }
      },
      {
        "id": "SDCRS-008",
        "code": "DOG_PHOTO_DUPLICATE_EXACT",
        "name": "Exact Duplicate Dog Photo",
        "description": "Dog photo is byte-for-byte identical to previous submission",
        "category": "DUP",
        "severity": "CRITICAL",
        "condition": {
          "type": "HASH_MATCH",
          "field": "evidences[purpose=DOG_PHOTO].fileStoreId",
          "algorithm": "SHA256",
          "threshold": 1.0
        },
        "action": {
          "type": "AUTO_REJECT",
          "rejectionReason": "DUPLICATE_EVIDENCE"
        }
      }
    ]
  }
}
```

### 11.2 Integration with SDCRS Workflow

The Fraud Detection Service integrates with SDCRS workflow transitions:

| Workflow State | Fraud Check | Action |
|----------------|-------------|--------|
| `null → PENDING_VALIDATION` | QUICK sync check | Block if CRITICAL |
| `PENDING_VALIDATION` | FULL async check | Flags stored on report |
| `PENDING_VERIFICATION` | Verifier sees flags | Manual review |
| `VERIFIED → ASSIGNED` | No additional check | - |
| `CAPTURED → RESOLVED` | Capture photo check | Validate resolution evidence |

---

## 12. External AI/ML Service Integration

### 12.1 Architecture for External Validators

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         FRAUD DETECTION SERVICE                                  │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────┐           │
│  │                    RULE EVALUATOR ENGINE                          │           │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐   │           │
│  │  │  Internal   │  │  External   │  │  Hybrid                 │   │           │
│  │  │  Validators │  │  Validators │  │  (Internal + External)  │   │           │
│  │  └─────────────┘  └──────┬──────┘  └─────────────────────────┘   │           │
│  └──────────────────────────┼───────────────────────────────────────┘           │
│                             │                                                    │
│                             ▼                                                    │
│  ┌──────────────────────────────────────────────────────────────────┐           │
│  │              EXTERNAL SERVICE ORCHESTRATOR                        │           │
│  │  ┌──────────┐  ┌───────────────┐  ┌─────────────┐  ┌──────────┐  │           │
│  │  │ Circuit  │  │ Retry with    │  │ Timeout     │  │ Response │  │           │
│  │  │ Breaker  │  │ Backoff       │  │ Handler     │  │ Cache    │  │           │
│  │  └──────────┘  └───────────────┘  └─────────────┘  └──────────┘  │           │
│  └──────────────────────────┬───────────────────────────────────────┘           │
│                             │                                                    │
└─────────────────────────────┼────────────────────────────────────────────────────┘
                              │
           ┌──────────────────┼──────────────────┬──────────────────┐
           │                  │                  │                  │
           ▼                  ▼                  ▼                  ▼
    ┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
    │ Dog Breed   │   │ Object      │   │ Face        │   │ Anomaly     │
    │ Classifier  │   │ Detection   │   │ Matching    │   │ Detection   │
    │ (AWS/GCP)   │   │ (YOLO API)  │   │ (Azure)     │   │ (Custom ML) │
    └─────────────┘   └─────────────┘   └─────────────┘   └─────────────┘
```

### 12.2 External Validator Interface

```java
/**
 * Interface for external validation services (AI/ML models, third-party APIs)
 */
public interface ExternalValidator {

    /**
     * Unique identifier for this validator
     */
    String getValidatorId();

    /**
     * Check if this validator is available (circuit breaker status)
     */
    boolean isAvailable();

    /**
     * Synchronous validation (with timeout)
     */
    ExternalValidationResult validate(ExternalValidationRequest request, long timeoutMs);

    /**
     * Asynchronous validation (returns CompletableFuture)
     */
    CompletableFuture<ExternalValidationResult> validateAsync(ExternalValidationRequest request);

    /**
     * Get cached result if available
     */
    Optional<ExternalValidationResult> getCachedResult(String cacheKey);
}

/**
 * Request payload for external validation
 */
@Data
public class ExternalValidationRequest {
    private String requestId;
    private String applicationId;
    private String validatorId;
    private String inputType;          // IMAGE, TEXT, METADATA, COMBINED
    private List<String> fileStoreIds; // Images to analyze
    private Map<String, Object> metadata;
    private Map<String, Object> parameters;  // Validator-specific params
}

/**
 * Response from external validation
 */
@Data
public class ExternalValidationResult {
    private String requestId;
    private String validatorId;
    private boolean success;
    private String errorCode;
    private String errorMessage;
    private double confidence;          // 0.0 to 1.0
    private Map<String, Object> predictions;
    private Map<String, Object> metadata;
    private long processingTimeMs;
    private boolean fromCache;
}
```

### 12.3 MDMS Configuration for External Services

#### ExternalValidators.json

```json
{
  "tenantId": "ncr",
  "moduleName": "FRAUD-DETECTION",
  "ExternalValidators": [
    {
      "id": "DOG_BREED_CLASSIFIER",
      "name": "Dog Breed Classification AI",
      "description": "Identifies dog breed from photo using CNN model",
      "type": "AI_MODEL",
      "enabled": true,
      "endpoint": {
        "url": "${AI_SERVICE_URL}/api/v1/classify/dog-breed",
        "method": "POST",
        "authType": "API_KEY",
        "authHeader": "X-API-Key",
        "authSecretKey": "AI_SERVICE_API_KEY"
      },
      "inputMapping": {
        "imageField": "evidences[purpose=DOG_PHOTO].fileStoreId",
        "imageFormat": "BASE64"
      },
      "outputMapping": {
        "breedField": "predictions.breed",
        "confidenceField": "predictions.confidence",
        "isDogField": "predictions.is_dog"
      },
      "timeout": {
        "connectMs": 5000,
        "readMs": 30000
      },
      "retry": {
        "maxAttempts": 2,
        "backoffMs": 1000,
        "backoffMultiplier": 2.0
      },
      "circuitBreaker": {
        "failureThreshold": 5,
        "resetTimeoutMs": 60000
      },
      "cache": {
        "enabled": true,
        "ttlSeconds": 86400,
        "keyFields": ["imageHash"]
      },
      "fallback": {
        "action": "SKIP_RULE",
        "logLevel": "WARN"
      }
    },
    {
      "id": "OBJECT_DETECTOR",
      "name": "Object Detection (YOLO)",
      "description": "Detects objects in image - verifies dog presence and count",
      "type": "AI_MODEL",
      "enabled": true,
      "endpoint": {
        "url": "${YOLO_SERVICE_URL}/detect",
        "method": "POST",
        "authType": "BEARER_TOKEN",
        "authSecretKey": "YOLO_SERVICE_TOKEN"
      },
      "inputMapping": {
        "imageField": "evidences[purpose=DOG_PHOTO].fileStoreId",
        "imageFormat": "URL",
        "parameters": {
          "classes": ["dog", "person", "cat"],
          "minConfidence": 0.5
        }
      },
      "outputMapping": {
        "detectionsField": "detections",
        "dogCountField": "detections[class=dog].count",
        "personPresentField": "detections[class=person].exists"
      },
      "timeout": {
        "connectMs": 3000,
        "readMs": 15000
      },
      "async": true,
      "priority": "NORMAL"
    },
    {
      "id": "FACE_MATCHER",
      "name": "Face Recognition & Matching",
      "description": "Compares selfie with registered user photo",
      "type": "AI_MODEL",
      "enabled": true,
      "endpoint": {
        "url": "${FACE_API_URL}/api/verify",
        "method": "POST",
        "authType": "OAUTH2",
        "tokenEndpoint": "${FACE_API_URL}/oauth/token",
        "clientIdKey": "FACE_API_CLIENT_ID",
        "clientSecretKey": "FACE_API_CLIENT_SECRET"
      },
      "inputMapping": {
        "sourceImage": "evidences[purpose=SELFIE].fileStoreId",
        "targetImage": "applicant.profilePhotoId",
        "threshold": 0.85
      },
      "outputMapping": {
        "matchField": "result.match",
        "similarityField": "result.similarity",
        "facesDetectedField": "result.faces_detected"
      },
      "timeout": {
        "connectMs": 5000,
        "readMs": 20000
      }
    },
    {
      "id": "IMAGE_QUALITY_ANALYZER",
      "name": "Image Quality Assessment",
      "description": "Analyzes image blur, lighting, resolution",
      "type": "AI_MODEL",
      "enabled": true,
      "endpoint": {
        "url": "${IQA_SERVICE_URL}/analyze",
        "method": "POST",
        "authType": "BASIC",
        "usernameKey": "IQA_USERNAME",
        "passwordKey": "IQA_PASSWORD"
      },
      "inputMapping": {
        "imageField": "evidences[*].fileStoreId",
        "checkTypes": ["blur", "exposure", "resolution", "noise"]
      },
      "outputMapping": {
        "overallQuality": "quality.overall_score",
        "blurScore": "quality.blur_score",
        "exposureScore": "quality.exposure_score",
        "isAcceptable": "quality.acceptable"
      },
      "timeout": {
        "connectMs": 3000,
        "readMs": 10000
      },
      "cache": {
        "enabled": true,
        "ttlSeconds": 3600
      }
    },
    {
      "id": "ANOMALY_DETECTOR",
      "name": "Submission Anomaly Detection",
      "description": "ML model to detect unusual submission patterns",
      "type": "AI_MODEL",
      "enabled": true,
      "endpoint": {
        "url": "${ANOMALY_SERVICE_URL}/predict",
        "method": "POST",
        "authType": "API_KEY",
        "authHeader": "Authorization",
        "authSecretKey": "ANOMALY_API_KEY"
      },
      "inputMapping": {
        "features": {
          "submissionHour": "createdTime.hour",
          "dayOfWeek": "createdTime.dayOfWeek",
          "gpsLat": "locationData.latitude",
          "gpsLon": "locationData.longitude",
          "recentSubmissionCount": "applicant.recentCount",
          "avgTimeBetweenSubmissions": "applicant.avgIntervalMinutes"
        }
      },
      "outputMapping": {
        "anomalyScore": "prediction.anomaly_score",
        "isAnomaly": "prediction.is_anomaly",
        "anomalyType": "prediction.anomaly_type"
      },
      "timeout": {
        "connectMs": 3000,
        "readMs": 5000
      }
    }
  ]
}
```

### 12.4 AI-Enhanced Fraud Rules

```json
{
  "tenantId": "ncr",
  "moduleName": "FRAUD-DETECTION",
  "AIFraudRules": [
    {
      "id": "AI-001",
      "code": "NO_DOG_IN_PHOTO",
      "name": "No Dog Detected in Photo",
      "description": "AI object detection found no dog in the submitted photo",
      "category": "EVD",
      "severity": "CRITICAL",
      "enabled": true,
      "condition": {
        "type": "EXTERNAL_VALIDATOR",
        "validatorId": "OBJECT_DETECTOR",
        "checkExpression": "predictions.dogCount == 0",
        "minConfidence": 0.8
      },
      "action": {
        "type": "AUTO_REJECT",
        "rejectionReason": "NO_DOG_DETECTED",
        "notifyApplicant": true
      }
    },
    {
      "id": "AI-002",
      "code": "DOG_COUNT_MISMATCH",
      "name": "Dog Count Mismatch",
      "description": "Reported dog count doesn't match AI detection",
      "category": "EVD",
      "severity": "MEDIUM",
      "enabled": true,
      "condition": {
        "type": "EXTERNAL_VALIDATOR",
        "validatorId": "OBJECT_DETECTOR",
        "checkExpression": "abs(predictions.dogCount - additionalData.dogCount) > 1",
        "minConfidence": 0.7
      },
      "action": {
        "type": "FLAG",
        "autoReject": false,
        "notifyVerifier": true
      }
    },
    {
      "id": "AI-003",
      "code": "SELFIE_FACE_MISMATCH",
      "name": "Selfie Doesn't Match Profile Photo",
      "description": "Face in selfie doesn't match registered user photo",
      "category": "IDN",
      "severity": "CRITICAL",
      "enabled": true,
      "condition": {
        "type": "EXTERNAL_VALIDATOR",
        "validatorId": "FACE_MATCHER",
        "checkExpression": "predictions.match == false && predictions.similarity < 0.7",
        "minConfidence": 0.9
      },
      "action": {
        "type": "FLAG",
        "autoReject": false,
        "escalate": true,
        "escalateToRole": "DISTRICT_ADMIN"
      }
    },
    {
      "id": "AI-004",
      "code": "LOW_IMAGE_QUALITY",
      "name": "Poor Image Quality",
      "description": "Submitted image is blurry, poorly lit, or low resolution",
      "category": "EVD",
      "severity": "LOW",
      "enabled": true,
      "condition": {
        "type": "EXTERNAL_VALIDATOR",
        "validatorId": "IMAGE_QUALITY_ANALYZER",
        "checkExpression": "predictions.overallQuality < 0.4 || predictions.isAcceptable == false"
      },
      "action": {
        "type": "FLAG",
        "autoReject": false,
        "notifyApplicant": true,
        "message": "Please submit a clearer photo"
      }
    },
    {
      "id": "AI-005",
      "code": "ANOMALOUS_SUBMISSION_PATTERN",
      "name": "Anomalous Submission Detected",
      "description": "ML model detected unusual submission pattern",
      "category": "VEL",
      "severity": "HIGH",
      "enabled": true,
      "condition": {
        "type": "EXTERNAL_VALIDATOR",
        "validatorId": "ANOMALY_DETECTOR",
        "checkExpression": "predictions.isAnomaly == true && predictions.anomalyScore > 0.85"
      },
      "action": {
        "type": "FLAG",
        "autoReject": false,
        "escalate": true
      }
    },
    {
      "id": "AI-006",
      "code": "PHOTO_NOT_DOG_RELATED",
      "name": "Photo Contains No Animals",
      "description": "Photo appears to be unrelated to dog sighting",
      "category": "EVD",
      "severity": "HIGH",
      "enabled": true,
      "condition": {
        "type": "EXTERNAL_VALIDATOR",
        "validatorId": "OBJECT_DETECTOR",
        "checkExpression": "predictions.dogCount == 0 && predictions.catCount == 0",
        "minConfidence": 0.85
      },
      "action": {
        "type": "FLAG",
        "autoReject": false,
        "notifyVerifier": true
      }
    }
  ]
}
```

### 12.5 External Service Orchestrator Implementation

```java
@Service
@Slf4j
public class ExternalServiceOrchestrator {

    private final Map<String, ExternalValidator> validators;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;

    /**
     * Execute external validation with resilience patterns
     */
    public ExternalValidationResult executeValidation(
            String validatorId,
            ExternalValidationRequest request) {

        ExternalValidator validator = validators.get(validatorId);
        if (validator == null) {
            throw new ValidatorNotFoundException(validatorId);
        }

        // 1. Check cache first
        String cacheKey = buildCacheKey(validatorId, request);
        Optional<ExternalValidationResult> cached = validator.getCachedResult(cacheKey);
        if (cached.isPresent()) {
            log.debug("Cache hit for validator: {}", validatorId);
            meterRegistry.counter("fraud.external.cache.hit", "validator", validatorId).increment();
            return cached.get();
        }

        // 2. Check circuit breaker
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(validatorId);
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            log.warn("Circuit breaker OPEN for validator: {}", validatorId);
            meterRegistry.counter("fraud.external.circuit.open", "validator", validatorId).increment();
            return buildFallbackResult(validatorId, request, "CIRCUIT_BREAKER_OPEN");
        }

        // 3. Execute with timeout and retry
        try {
            Supplier<ExternalValidationResult> decorated = CircuitBreaker
                .decorateSupplier(circuitBreaker,
                    () -> validator.validate(request, getTimeout(validatorId)));

            ExternalValidationResult result = Retry.of(validatorId, getRetryConfig(validatorId))
                .executeSupplier(decorated);

            // 4. Cache successful result
            if (result.isSuccess()) {
                cacheResult(cacheKey, result, validatorId);
            }

            meterRegistry.timer("fraud.external.latency", "validator", validatorId)
                .record(result.getProcessingTimeMs(), TimeUnit.MILLISECONDS);

            return result;

        } catch (Exception e) {
            log.error("External validation failed for {}: {}", validatorId, e.getMessage());
            meterRegistry.counter("fraud.external.error", "validator", validatorId).increment();
            return buildFallbackResult(validatorId, request, e.getMessage());
        }
    }

    /**
     * Execute multiple validators in parallel
     */
    public CompletableFuture<Map<String, ExternalValidationResult>> executeParallel(
            List<String> validatorIds,
            ExternalValidationRequest request) {

        List<CompletableFuture<ExternalValidationResult>> futures = validatorIds.stream()
            .map(id -> CompletableFuture.supplyAsync(
                () -> executeValidation(id, request),
                executorService))
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                Map<String, ExternalValidationResult> results = new HashMap<>();
                for (int i = 0; i < validatorIds.size(); i++) {
                    results.put(validatorIds.get(i), futures.get(i).join());
                }
                return results;
            });
    }
}
```

### 12.6 Async Processing Flow for External AI Calls

```
┌──────────────┐     ┌─────────────────────────────────────────────────────────────┐
│   SDCRS      │     │               FRAUD DETECTION SERVICE                       │
│   Service    │     │                                                             │
└──────┬───────┘     │  ┌─────────────────┐    ┌──────────────────────────────┐   │
       │             │  │ Quick Check     │    │ Deep Check (Async)           │   │
       │ _create     │  │ (Sync, <500ms)  │    │ (Background, <30s)           │   │
       │             │  │                 │    │                              │   │
       ▼             │  │ • Hash match    │    │ • AI Object Detection        │   │
  ┌─────────┐        │  │ • Velocity      │    │ • Face Matching              │   │
  │ Request │────────┼──► • GPS boundary  │    │ • Anomaly Detection          │   │
  └─────────┘        │  │ • Daily limit   │    │ • Image Quality Analysis     │   │
       │             │  │                 │    │ • Breed Classification       │   │
       │             │  └────────┬────────┘    └──────────────┬───────────────┘   │
       │             │           │                            │                   │
       │             │           ▼                            │                   │
       │             │     ┌──────────┐                       │                   │
       │             │     │ALLOW or  │                       │                   │
       │◄────────────┼─────│REJECT    │                       │                   │
       │             │     └──────────┘                       │                   │
       │             │                                        │                   │
       │             │                         ┌──────────────▼───────────────┐   │
       │             │                         │     Kafka: fraud-check-      │   │
       │             │                         │     deep-result              │   │
       │             │                         └──────────────┬───────────────┘   │
       │             │                                        │                   │
       │             └────────────────────────────────────────┼───────────────────┘
       │                                                      │
       │                                                      ▼
       │                                              ┌───────────────┐
       │                                              │ If HIGH/      │
       │                                              │ CRITICAL flag │
       │◄─────────────────────────────────────────────│ Update report │
       │                                              │ workflow      │
                                                      └───────────────┘
```

### 12.7 Fallback Strategies

| Strategy | When to Use | Behavior |
|----------|-------------|----------|
| `SKIP_RULE` | Non-critical AI rules | Proceed without this validation |
| `USE_CACHED` | Recent cache available | Use last known result (stale) |
| `DEGRADE_TO_MANUAL` | Critical validations | Flag for human review |
| `QUEUE_FOR_RETRY` | Temporary outage | Persist and retry later |
| `APPLY_DEFAULT` | Conservative approach | Assume suspicious (flag) |

### 12.8 Monitoring & Observability

```yaml
# Prometheus metrics for external AI services
fraud_external_request_total:
  type: counter
  labels: [validator_id, status]
  help: Total external validation requests

fraud_external_latency_seconds:
  type: histogram
  labels: [validator_id]
  buckets: [0.1, 0.5, 1, 2, 5, 10, 30]
  help: External validation latency

fraud_external_circuit_state:
  type: gauge
  labels: [validator_id]
  values: {CLOSED: 0, HALF_OPEN: 1, OPEN: 2}
  help: Circuit breaker state per validator

fraud_external_cache_hit_ratio:
  type: gauge
  labels: [validator_id]
  help: Cache hit ratio for external validations
```

---

## 13. Implementation Phases

### Phase 1: Core Infrastructure (Week 1-2)
- Database schema setup
- Basic API endpoints
- Kafka integration
- MDMS configuration framework

### Phase 2: Rule Engine (Week 3-4)
- Standard rules (STD-001 to STD-010)
- Velocity checking
- Geospatial validation
- Temporal checks

### Phase 3: Duplicate Detection (Week 5-6)
- Image hash computation (pHash)
- Elasticsearch hash index
- Similarity search
- Duplicate linking

### Phase 4: SDCRS Integration (Week 7-8)
- SDCRS-specific rules
- Workflow integration
- Verifier UI for flags
- Notification setup

### Phase 5: Analytics & Monitoring (Week 9-10)
- DSS dashboard charts
- Real-time alerts
- Applicant risk profiles
- Admin reporting

---

*Document Version: 1.0*
*Last Updated: December 2025*
