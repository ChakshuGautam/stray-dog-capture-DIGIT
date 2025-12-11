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
| Process payout | Create Payment Demand | Create | Demand/Payment |
| Send notification | Send Notification | Send | Notification |
| View dashboard | View Dashboard | View | Dashboard |
| Generate report | Generate Report | Generate | Report |

### 1.2 Identified Services and Operations

**New Custom Service:**

| Service | Operations | Description |
|---------|------------|-------------|
| **Dog Report Registry** | Create, Update, Search, Verify, Approve, Reject, Resolve, Close | Core registry for stray dog reports (includes evidence as part of the report) |

**Reusing Existing DIGIT Services:**

| Existing DIGIT Service | Operations Used | Purpose in SDCRS |
|------------------------|-----------------|------------------|
| **File Store Service** | Upload, Download, Delete | Photo and selfie storage |
| **Expense Service** | Create Bill, Update, Search | Bill creation for teacher payouts |
| **IFMS Adapter** | Create PI, Update Status | Payment Instruction to treasury/JIT |
| **Dashboard Backend (DSS)** | Aggregate, Query | Analytics, dashboards, and reporting |

> **Note:** Following DIGIT platform patterns, only the Dog Report Registry is a new service. Evidence (photos, selfies) are stored via File Store Service with references in the report. **Payouts use the DIGIT-native Expense Service + IFMS Adapter pattern** for direct bank transfers to teachers via JIT (Just-in-Time) treasury integration. Analytics use the Dashboard Backend (DSS).

---

## 2. DIGIT Reusable Services

Based on the SDCRS requirements, the following DIGIT core services will be reused:

| DIGIT Service | Purpose in SDCRS | Required |
|---------------|------------------|----------|
| **User Service** | Teacher, Verifier, MC Officer authentication | Yes |
| **Role Service** | Role-based access control | Yes |
| **MDMS Service** | Reference data (report types, payout rates, status codes) | Yes |
| **Workflow Service** | Dog report state management | Yes |
| **File Store Service** | Photo and selfie storage (evidence management) | Yes |
| **Location Service** | GPS validation, tenant boundary check | Yes |
| **Notification Service** | SMS/Email alerts to teachers | Yes |
| **Persister Service** | Async database writes via Kafka | Yes |
| **Indexer Service** | Elasticsearch indexing for search/analytics | Yes |
| **Localization Service** | Multi-language support | Yes |
| **Encryption Service** | PII data protection (teacher details) | Yes |
| **Expense Service** | Bill creation for teacher payouts | Yes |
| **IFMS Adapter** | Payment Instruction to treasury/JIT for disbursement | Yes |
| **URL Shortening Service** | Generate short tracking URLs for reports | Yes |
| **PDF Service** | Report/certificate generation | Optional |
| **Dashboard Backend (DSS)** | Analytics dashboards for all user roles | Yes |

> **Architecture Decision:** By leveraging existing DIGIT services for file storage, payments, and dashboards, SDCRS only requires one new custom service (Dog Report Registry). This reduces development effort and ensures consistency with the DIGIT platform.

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

> **Config File:** [`03-configs/workflow/BusinessService.json`](03-configs/workflow/BusinessService.json)

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

#### 4.1.1 Report Types

> **Config File:** [`03-configs/mdms/SDCRS/ReportType.json`](03-configs/mdms/SDCRS/ReportType.json)

| Code | Name | Priority |
|------|------|----------|
| STRAY_DOG_AGGRESSIVE | Aggressive Stray Dog | HIGH |
| STRAY_DOG_INJURED | Injured Stray Dog | HIGH |
| STRAY_DOG_PACK | Stray Dog Pack (3+) | MEDIUM |
| STRAY_DOG_STANDARD | Standard Stray Dog | LOW |

#### 4.1.2 Payout Configuration

> **Config File:** [`03-configs/mdms/SDCRS/PayoutConfig.json`](03-configs/mdms/SDCRS/PayoutConfig.json)

| Parameter | Value |
|-----------|-------|
| Amount Per Report | 500 DJF |
| Minimum Photos | 2 |
| Max Daily Reports | 5 |
| Monthly Payout Cap | 5,000 DJF |

> **Note:** Payouts are processed through DIGIT's Collection/Billing Service. When a dog report reaches "CAPTURED/RESOLVED" status, a demand is created for the teacher and payment is processed via the existing payment workflow.

#### 4.1.3 Rejection Reasons

> **Config File:** [`03-configs/mdms/SDCRS/RejectionReason.json`](03-configs/mdms/SDCRS/RejectionReason.json)

| Code | Name | Auto Rejection |
|------|------|---------------|
| INVALID_GPS | Invalid GPS Coordinates | Yes |
| OUTSIDE_BOUNDARY | Outside Tenant Boundary | Yes |
| STALE_TIMESTAMP | Stale Timestamp (48h+) | Yes |
| DUPLICATE_IMAGE | Duplicate Image | Yes |
| POOR_IMAGE_QUALITY | Poor Image Quality | No |
| NOT_A_DOG | Not a Dog | No |
| DOMESTIC_DOG | Domestic Dog | No |
| FRAUDULENT_SUBMISSION | Fraudulent Submission | No |

#### 4.1.4 Resolution Types

> **Config File:** [`03-configs/mdms/SDCRS/ResolutionType.json`](03-configs/mdms/SDCRS/ResolutionType.json)

| Code | Name | Triggers Payout |
|------|------|-----------------|
| CAPTURED | Captured | Yes |
| RELOCATED | Relocated | Yes |
| STERILIZED_RELEASED | Sterilized and Released | Yes |
| UNABLE_TO_LOCATE | Unable to Locate | No |
| ALREADY_RESOLVED | Already Resolved | No |

---

## 5. Roles & Access Control Configuration

### 5.1 Roles

> **Config File:** [`03-configs/mdms/ACCESSCONTROL-ROLES/roles.json`](03-configs/mdms/ACCESSCONTROL-ROLES/roles.json)

| Code | Name | Description |
|------|------|-------------|
| TEACHER | Teacher | School teacher who reports stray dog sightings |
| VERIFIER | Verifier | Backend operator who verifies submitted reports |
| MC_OFFICER | MC Officer | Municipal Corporation officer who captures stray dogs |
| MC_SUPERVISOR | MC Supervisor | Supervisor for MC Officers |
| DISTRICT_ADMIN | District Admin | District level administrator |
| STATE_ADMIN | State Admin | State level administrator |
| SYSTEM | System | Automated system operations |

### 5.2 Actions

> **Config File:** [`03-configs/mdms/ACCESSCONTROL-ACTIONS-TEST/actions-test.json`](03-configs/mdms/ACCESSCONTROL-ACTIONS-TEST/actions-test.json)

| ID | Name | URL |
|----|------|-----|
| 2001 | Create Dog Report | /sdcrs-services/v1/report/_create |
| 2002 | Update Dog Report | /sdcrs-services/v1/report/_update |
| 2003 | Search Dog Reports | /sdcrs-services/v1/report/_search |
| 2004 | Verify Dog Report | /sdcrs-services/v1/report/_verify |
| 2005 | Reject Dog Report | /sdcrs-services/v1/report/_reject |
| 2006 | Resolve Dog Report | /sdcrs-services/v1/report/_resolve |
| 2007 | Upload Evidence | /sdcrs-services/v1/evidence/_upload |
| 2008 | Search Evidence | /sdcrs-services/v1/evidence/_search |
| 2009 | Compare Duplicates | /sdcrs-services/v1/evidence/_compare |
| 2010 | Create Payout | /sdcrs-services/v1/payout/_create |
| 2011 | Search Payouts | /sdcrs-services/v1/payout/_search |
| 2012 | Process Payout | /sdcrs-services/v1/payout/_process |
| 2013 | Get Dashboard | /sdcrs-services/v1/dashboard/_get |
| 2017 | Get Analytics | /sdcrs-services/v1/analytics/_get |
| 2018 | Generate Report | /sdcrs-services/v1/report/_generate |
| 2019 | Assign MC Officer | /sdcrs-services/v1/assignment/_assign |
| 2020 | Get Assignments | /sdcrs-services/v1/assignment/_search |

### 5.3 Role-Action Mapping

> **Config File:** [`03-configs/mdms/ACCESSCONTROL-ROLEACTIONS/roleactions.json`](03-configs/mdms/ACCESSCONTROL-ROLEACTIONS/roleactions.json)

Maps roles to actions (by action ID). See Section 5.4 for the visual summary matrix.

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
| Get Dashboard | | X | X | X | X | X | |
| Get Analytics | | | | X | X | X | |
| Generate Report | | | | | X | X | |
| Assign MC Officer | | | | X | | | X |
| Get Assignments | | | X | X | | | |

> **Note:** Dashboards and Analytics are served by DIGIT's Dashboard Backend (DSS). The actions above are for custom SDCRS endpoints; DSS has its own role-action mappings.

---

## 6. Persister Configuration

### 6.1 Dog Report Persister

> **Config File:** [`03-configs/persister/sdcrs-persister.yml`](03-configs/persister/sdcrs-persister.yml)

| Topic | Operation | Table |
|-------|-----------|-------|
| `save-sdcrs-report` | INSERT | `eg_sdcrs_report` |
| `update-sdcrs-report` | UPDATE | `eg_sdcrs_report` |

### 6.2 Payout Integration with Expense Service + IFMS Adapter

SDCRS uses the DIGIT-native **Expense Service + IFMS Adapter** pattern for teacher payouts. This enables direct bank transfers via JIT (Just-in-Time) treasury integration.

#### Payment Flow

```
Dog CAPTURED → Create Expense Bill → IFMS Adapter creates Payment Instruction (PI) →
PI pushed to JIT/Treasury → Direct Bank Transfer → Status updated to RESOLVED
```

#### Components

| Component | Purpose | GitHub Reference |
|-----------|---------|------------------|
| **Expense Service** | Creates bills with payee (teacher) and amount details | [DIGIT-Works/backend/expense](https://github.com/egovernments/DIGIT-Works/tree/master/backend/expense) |
| **IFMS Adapter** | Converts bills to Payment Instructions (PI) and posts to JIT | [DIGIT-Works/reference-adapters/ifms-adapter](https://github.com/egovernments/DIGIT-Works/tree/master/reference-adapters/ifms-adapter) |

#### Payment Instruction (PI) States

| Status | Description |
|--------|-------------|
| `INITIATED` | PI created and pushed to JIT |
| `INPROCESS` | JIT processing the payment |
| `COMPLETED` | Bank transfer successful |
| `REJECTED` | Payment failed (can create revised PI) |

#### Expense Bill Schema

> **Config File:** [`03-configs/expense/expense-bill-config.json`](03-configs/expense/expense-bill-config.json)

The expense bill links the dog report to the teacher beneficiary:

| Field | Value |
|-------|-------|
| `businessService` | `SDCRS` |
| `referenceId` | Dog Report ID |
| `payee.identifier` | Teacher ID |
| `payee.type` | `INDIVIDUAL` |
| `billDetails.lineItems.headCode` | `SDCRS_PAYOUT` |
| `billDetails.lineItems.amount` | 500 (from PayoutConfig) |

#### IFMS Adapter Configuration

> **Config File:** [`03-configs/ifms/ifms-adapter-config.json`](03-configs/ifms/ifms-adapter-config.json)

Configures the JIT integration with scheme codes and treasury endpoints.

### 6.3 Indexer Configuration

> **Config File:** [`03-configs/indexer/sdcrs-indexer.yml`](03-configs/indexer/sdcrs-indexer.yml)

The Indexer Service listens to Kafka topic `sdcrs-report-indexer` and indexes data to Elasticsearch index `sdcrs-report-index` for search and analytics (DSS dashboards).

**Computed Fields for SLA Analytics:**
- `timeToVerification` - Time from submission to verification
- `timeToAssignment` - Time from submission to MC assignment
- `timeToFieldVisit` - Time from assignment to field visit start
- `timeToResolution` - Total time from submission to resolution

### 6.4 Elasticsearch Index Template

> **Config File:** [`03-configs/elasticsearch/sdcrs-report-index-template.json`](03-configs/elasticsearch/sdcrs-report-index-template.json)

Defines field mappings for the `sdcrs-report-index`. Key features:
- `geo_point` type for location-based queries
- Lowercase normalizer for case-insensitive text search
- `epoch_millis` date format for timestamps
- Long type for computed SLA fields

---

## 7. Kafka Topics

| Topic Name | Producer | Consumer | Purpose |
|------------|----------|----------|---------|
| `save-sdcrs-report` | sdcrs-services | persister | Save new dog report |
| `update-sdcrs-report` | sdcrs-services | persister | Update dog report |
| `sdcrs-report-indexer` | sdcrs-services | indexer | Index for search |
| `sdcrs-notification` | sdcrs-services | notification | Trigger notifications |
| `sdcrs-workflow-transition` | sdcrs-services | workflow | Workflow state changes |
| `expense-bill-create` | sdcrs-services | expense-service | Create expense bill on capture |
| `expense-bill-update` | expense-service | persister | Persist bill updates |
| `ifms-pi-create` | expense-service | ifms-adapter | Create Payment Instruction |
| `ifms-pi-status-update` | ifms-adapter | sdcrs-services | PI status callback |

> **Note:** Payout processing uses DIGIT's **Expense Service + IFMS Adapter** Kafka topics. When a dog is captured, an expense bill is created which triggers the IFMS Adapter to create a Payment Instruction (PI) for JIT treasury integration.

---

## 8. Database Schema

### 8.1 Dog Report Table

> **Config File:** [`03-configs/database/eg_sdcrs_report.sql`](03-configs/database/eg_sdcrs_report.sql)

**Table:** `eg_sdcrs_report`

| Field Group | Key Columns |
|-------------|-------------|
| Identity | `id`, `tenant_id`, `report_number`, `tracking_id` |
| Location | `latitude`, `longitude`, `ward_code`, `district_code` |
| Dog Details | `dog_description`, `dog_count`, `is_aggressive` |
| Evidence | `photo_file_store_id`, `selfie_file_store_id`, `image_hash` |
| Workflow | `status`, `verifier_id`, `assigned_mc_id`, `resolution_type` |
| Payout | `payout_status`, `payout_amount`, `payout_demand_id` |
| Fraud | `fraud_flags` (JSONB), `penalty_type`, `sla_breached` |

**Performance Indexes:** tenant, status, reporter, MC, verifier, locality, ward, district, created time, image hash, tracking ID, report type, and composite indexes for common query patterns.

### 8.2 Payout Management

> **Note:** Payout data is managed by DIGIT's Collection/Billing Service. The `eg_sdcrs_report` table only stores a reference (`payout_demand_id`) to the demand created in the Collection Service. All payment tracking, transaction history, and reconciliation are handled by the existing DIGIT payment infrastructure.

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
│ Dog Report    │         │ User Service  │         │ Access Control│
│ Registry      │◄───────►│               │◄───────►│    Service    │
│ (NEW SERVICE) │         │ - Auth        │         │  - Roles      │
│  - Reports    │         │ - Profile     │         │  - Actions    │
│  - Evidence   │         │               │         │               │
│    refs       │         └───────────────┘         └───────────────┘
└───────────────┘                 │
        │                         │
        ├─────────────────────────┼─────────────────────────────┐
        │                         │                             │
        ▼                         ▼                             ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│   Workflow    │         │  File Store   │         │   Location    │
│   Service     │         │   Service     │         │   Service     │
│   (DIGIT)     │         │   (DIGIT)     │         │   (DIGIT)     │
│ - States      │         │ - Photos      │         │ - GPS Valid   │
│ - Actions     │         │ - Selfies     │         │ - Boundary    │
└───────────────┘         └───────────────┘         └───────────────┘
        │                         │                             │
        └─────────────────────────┼─────────────────────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────────┐
        │                         │                             │
        ▼                         ▼                             ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│   Expense     │         │   Dashboard   │         │     Kafka     │
│   Service     │         │    Backend    │         │    Topics     │
│   (DIGIT)     │         │   (DSS)       │         │               │
│ - Bills       │         │ - Analytics   │         └───────────────┘
│ - Payments    │         │ - Reports     │                 │
└───────────────┘         └───────────────┘                 │
        │
        ▼
┌───────────────┐
│ IFMS Adapter  │
│   (DIGIT)     │
│ - Payment     │
│   Instruction │
│ - JIT/Treasury│
└───────────────┘
                                                            │
        ┌───────────────────────────────────────────────────┤
        │                         │                         │
        ▼                         ▼                         ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│   Persister   │         │    Indexer    │         │ Notification  │
│   Service     │         │   Service     │         │   Service     │
│   (DIGIT)     │         │   (DIGIT)     │         │   (DIGIT)     │
│ - PostgreSQL  │         │ - Elastic     │         │ - SMS         │
│ - CRUD        │         │ - Search      │         │ - Email       │
└───────────────┘         └───────────────┘         └───────────────┘
```

**Architecture Highlights:**
- **Only 1 new service** (Dog Report Registry) - follows DIGIT Registry pattern
- **Evidence** stored via File Store Service with references in Dog Report
- **Payouts** processed via **Expense Service + IFMS Adapter** (JIT/Treasury integration)
- **Direct bank transfers** to teachers via Payment Instructions (PI)
- **Dashboards** served by DSS (Dashboard Backend)
- All other services are existing DIGIT platform services

---

## 10. Public Tracking API (Anonymous Access)

### 10.1 Overview

SDCRS provides a **public tracking endpoint** that allows anyone to check the status of a dog report without authentication. This enables:
- Teachers to share tracking links via SMS with community members
- Citizens to check report status without creating an account
- Transparency in the capture/resolution process

### 10.2 Tracking Identifiers

Each dog report has three identifiers for tracking:

| Identifier | Format | Purpose | Example |
|------------|--------|---------|---------|
| **Report Number** | `DJ-SDCRS-YYYY-NNNNNN` | Formal reference, used in official communications | `DJ-SDCRS-2024-000123` |
| **Tracking ID** | `[A-Z]{3}[0-9]{3}` | Short 6-character ID for easy sharing | `ABC123` |
| **Tracking URL** | `https://dj.gov.in/t/{shortCode}` | Clickable short URL via URL Shortener | `https://dj.gov.in/t/xY7kM` |

### 10.3 IDGen Configuration for Tracking ID

```json
{
  "idgen": {
    "formats": [
      {
        "idName": "sdcrs.reportnumber",
        "format": "DJ-SDCRS-[fy:yyyy]-[SEQ_SDCRS_REPORT]",
        "tenantId": "dj"
      },
      {
        "idName": "sdcrs.trackingid",
        "format": "[A-Z]{3}[0-9]{3}",
        "tenantId": "dj"
      }
    ]
  }
}
```

### 10.4 URL Shortener Integration

When a report is created, SDCRS calls the DIGIT URL Shortening Service to generate a short tracking URL:

```bash
POST /egov-url-shortening/shortener

{
  "url": "https://dj.gov.in/sdcrs/track?id=DJ-SDCRS-2024-000123"
}

Response:
{
  "shortenedUrl": "https://dj.gov.in/t/xY7kM"
}
```

The shortened URL is stored in `tracking_url` column and sent to the teacher via SMS.

### 10.5 Track API Endpoint

```
POST /sdcrs-services/v1/report/_track
```

**Request (by Report Number):**
```json
{
  "RequestInfo": {},
  "reportNumber": "DJ-SDCRS-2024-000123"
}
```

**Request (by Tracking ID):**
```json
{
  "RequestInfo": {},
  "trackingId": "ABC123"
}
```

**Response (Sanitized - No PII):**
```json
{
  "ResponseInfo": {
    "apiId": "sdcrs-services",
    "ver": "1.0",
    "ts": 1702310400000,
    "status": "successful"
  },
  "reportNumber": "DJ-SDCRS-2024-000123",
  "trackingId": "ABC123",
  "trackingUrl": "https://dj.gov.in/t/xY7kM",
  "status": "ASSIGNED",
  "statusDescription": "Assigned to Municipal Corporation team",
  "reportType": "STRAY_DOG_AGGRESSIVE",
  "locality": "Ward 5, Djibouti City",
  "createdDate": "2024-12-10",
  "lastUpdated": "2024-12-11",
  "timeline": [
    {
      "status": "PENDING_VALIDATION",
      "timestamp": 1702300400000,
      "description": "Report submitted"
    },
    {
      "status": "PENDING_VERIFICATION",
      "timestamp": 1702304000000,
      "description": "Auto-validation passed"
    },
    {
      "status": "VERIFIED",
      "timestamp": 1702306000000,
      "description": "Report verified by operator"
    },
    {
      "status": "ASSIGNED",
      "timestamp": 1702308000000,
      "description": "Assigned to Municipal Corporation team"
    }
  ],
  "estimatedResolution": "Within 48 hours"
}
```

**Security Notes:**
- Response does NOT include reporter details (name, phone, ID)
- Response does NOT include assigned officer details
- Response does NOT include photo/evidence URLs
- Only status, locality, and timeline are exposed

### 10.6 SMS Notification with Tracking

When a report is successfully submitted, the teacher receives:

```
Your dog report DJ-SDCRS-2024-000123 has been submitted.

Track status: https://dj.gov.in/t/xY7kM
or use ID: ABC123

You will be notified when the dog is captured.
```

---

## 11. Gateway Whitelist Configuration

### 11.1 Overview

The `_track` endpoint is configured as an **open endpoint** in the DIGIT API Gateway, allowing anonymous access without authentication. This is achieved through gateway whitelisting.

### 11.2 Whitelist Configuration

Add the following to the gateway configuration (`egov-gateway` values):

```yaml
egov-open-endpoints-whitelist: |
  /sdcrs-services/v1/report/_track,
  /egov-url-shortening/shortener,
  /localization/messages/v1/_search,
  /mdms-v2/v1/_search
```

**Explanation:**
| Endpoint | Purpose |
|----------|---------|
| `/sdcrs-services/v1/report/_track` | Public report tracking |
| `/egov-url-shortening/shortener` | URL shortening service (used by _track) |
| `/localization/messages/v1/_search` | UI text localization |
| `/mdms-v2/v1/_search` | Master data for status descriptions |

### 11.3 Rate Limiting Configuration

To prevent DDoS attacks and abuse of the public endpoint, configure rate limiting:

```yaml
zuul:
  ratelimit:
    enabled: true
    repository: BUCKET4J_HAZELCAST
    policy-list:
      sdcrs-track:
        - limit: 100
          refresh-interval: 60
          type:
            - url=/sdcrs-services/v1/report/_track
            - origin
```

**Rate Limit Rules:**
| Parameter | Value | Description |
|-----------|-------|-------------|
| `limit` | 100 | Max requests per window |
| `refresh-interval` | 60 | Window size in seconds |
| `type` | url + origin | Limit per IP per endpoint |

This allows 100 requests per minute per IP address.

### 11.4 Security Considerations

| Risk | Mitigation |
|------|------------|
| Enumeration attack | Rate limiting + random tracking IDs |
| PII exposure | Sanitized response (no reporter/officer details) |
| DDoS attack | Rate limiting + origin-based throttling |
| Data scraping | Response contains minimal public information |

### 11.5 Action NOT in Role-Action Mapping

> **Important:** The `_track` endpoint is NOT added to `ACCESSCONTROL-ACTIONS-TEST` or `ACCESSCONTROL-ROLEACTIONS`. Since it's whitelisted in the gateway, it bypasses the role-action authorization entirely. This is intentional for anonymous access.

---

## 12. Process Performance Indicators (PPIs)

This section defines the key metrics that administrators can use to monitor system performance, identify bottlenecks, and drive improvements. All metrics should be viewable across time periods (daily, weekly, monthly, quarterly, yearly) and administrative hierarchies (ward, zone, district, state).

### 12.1 Report Volume Metrics

| Metric | Description | Formula | Drill-down Dimensions |
|--------|-------------|---------|----------------------|
| **Total Reports** | Number of reports submitted | COUNT(reports) | Time, Location, Report Type |
| **Reports by Status** | Distribution across workflow states | COUNT(reports) GROUP BY status | Time, Location |
| **Reports by Type** | Distribution by dog type (aggressive, injured, pack, standard) | COUNT(reports) GROUP BY report_type | Time, Location |
| **New Reports** | Reports submitted in the period | COUNT(reports WHERE created_time IN period) | Time, Location |
| **Daily Report Rate** | Average reports per day | SUM(reports) / days_in_period | Location |
| **Reports by Channel** | Reports by submission channel (mobile app, web) | COUNT(reports) GROUP BY channel | Time, Location |

### 12.2 Status Distribution Metrics

| Metric | Description | Formula |
|--------|-------------|---------|
| **Pending Validation** | Reports awaiting auto-validation | COUNT(status = 'PENDING_VALIDATION') |
| **Pending Verification** | Reports in verifier queue | COUNT(status = 'PENDING_VERIFICATION') |
| **Verified** | Reports verified, awaiting assignment | COUNT(status = 'VERIFIED') |
| **Assigned** | Reports assigned to MC officers | COUNT(status = 'ASSIGNED') |
| **In Progress** | Field visits underway | COUNT(status = 'IN_PROGRESS') |
| **Captured** | Dogs captured, payout pending | COUNT(status = 'CAPTURED') |
| **Resolved** | Successfully completed with payout | COUNT(status = 'RESOLVED') |
| **Rejected** | Rejected by verifier | COUNT(status = 'REJECTED') |
| **Auto-Rejected** | Failed auto-validation | COUNT(status = 'AUTO_REJECTED') |
| **Duplicate** | Marked as duplicate | COUNT(status = 'DUPLICATE') |
| **Unable to Locate** | Dog not found at location | COUNT(status = 'UNABLE_TO_LOCATE') |

### 12.3 Time-Based (SLA) Metrics

| Metric | Description | Formula | SLA Target |
|--------|-------------|---------|------------|
| **Avg. Time to Verification** | Submission to verification complete | AVG(verified_time - created_time) | < 4 hours |
| **Avg. Time to Assignment** | Submission to MC assignment | AVG(assigned_time - created_time) | < 8 hours |
| **Avg. Time to Field Visit** | Assignment to field visit start | AVG(field_visit_time - assigned_time) | < 24 hours |
| **Avg. Time to Resolution** | Submission to final resolution | AVG(resolved_time - created_time) | < 72 hours |
| **Avg. Time in Queue** | Time spent in each status | AVG(exit_time - entry_time) per status | Varies |
| **SLA Compliance Rate** | % reports resolved within SLA | COUNT(resolved_within_sla) / COUNT(resolved) | > 90% |
| **SLA Breach Rate** | % reports exceeding SLA | COUNT(breached_sla) / COUNT(total) | < 10% |
| **Ageing Analysis** | Reports by age bucket | COUNT by (0-24h, 24-48h, 48-72h, >72h) | - |

### 12.4 Resolution & Outcome Metrics

| Metric | Description | Formula |
|--------|-------------|---------|
| **Resolution Rate** | % reports reaching final state | COUNT(terminal_states) / COUNT(total) |
| **Capture Success Rate** | % of assigned reports resulting in capture | COUNT(CAPTURED + RESOLVED) / COUNT(ASSIGNED) |
| **Unable to Locate Rate** | % dogs not found | COUNT(UNABLE_TO_LOCATE) / COUNT(IN_PROGRESS) |
| **Rejection Rate** | % reports rejected | COUNT(REJECTED + AUTO_REJECTED) / COUNT(total) |
| **Duplicate Rate** | % marked as duplicate | COUNT(DUPLICATE) / COUNT(total) |
| **Auto-Rejection Rate** | % failed auto-validation | COUNT(AUTO_REJECTED) / COUNT(total) |
| **First-Visit Resolution** | % resolved on first field visit | COUNT(resolved_first_visit) / COUNT(field_visits) |

### 12.5 Payout Metrics

| Metric | Description | Formula |
|--------|-------------|---------|
| **Total Payouts** | Total amount disbursed | SUM(payout_amount) |
| **Payout Count** | Number of payouts processed | COUNT(payout_status = 'COMPLETED') |
| **Average Payout per Report** | Average payout amount | AVG(payout_amount) |
| **Payouts by Status** | Distribution (pending, processing, completed, failed) | COUNT GROUP BY payout_status |
| **Pending Payout Amount** | Amount awaiting disbursement | SUM(payout_amount WHERE payout_status = 'PENDING') |
| **Avg. Time to Payout** | Capture to payout completion | AVG(payout_time - captured_time) |
| **Teachers at Monthly Cap** | Teachers who hit ₹5,000 cap | COUNT(monthly_earnings >= 5000) |
| **Payout Failure Rate** | % of failed payout attempts | COUNT(payout_failed) / COUNT(payout_attempted) |

### 12.6 User Performance Metrics

#### Teacher (Reporter) Metrics

| Metric | Description | Formula |
|--------|-------------|---------|
| **Active Teachers** | Teachers with at least 1 report in period | COUNT(DISTINCT reporter_id) |
| **Reports per Teacher** | Average reports per teacher | COUNT(reports) / COUNT(DISTINCT reporter_id) |
| **Top Reporters** | Teachers with highest successful captures | RANK BY COUNT(RESOLVED) |
| **Teacher Acceptance Rate** | % of teacher reports verified | COUNT(VERIFIED) / COUNT(submitted) per teacher |
| **Teacher Earnings** | Total/average earnings | SUM/AVG(payout_amount) per teacher |
| **Suspended Teachers** | Teachers with fraud penalties | COUNT(status = 'SUSPENDED') |

#### Verifier Metrics

| Metric | Description | Formula |
|--------|-------------|---------|
| **Verifier Workload** | Reports verified per verifier | COUNT(verified) GROUP BY verifier_id |
| **Avg. Verification Time** | Time to verify per verifier | AVG(verified_time - assigned_time) per verifier |
| **Verification Accuracy** | % of verified reports resolved | COUNT(RESOLVED) / COUNT(VERIFIED) per verifier |
| **Pending per Verifier** | Queue depth per verifier | COUNT(PENDING_VERIFICATION) per verifier |

#### MC Officer Metrics

| Metric | Description | Formula |
|--------|-------------|---------|
| **MC Officer Workload** | Reports assigned per officer | COUNT(assigned) GROUP BY mc_officer_id |
| **Capture Rate per Officer** | % captures per officer | COUNT(CAPTURED) / COUNT(ASSIGNED) per officer |
| **Avg. Field Visit Duration** | Time from start to completion | AVG(resolution_time - field_visit_start) per officer |
| **Unable to Locate Rate** | % UTL per officer | COUNT(UNABLE_TO_LOCATE) / COUNT(IN_PROGRESS) per officer |
| **Daily Completions** | Reports resolved per day per officer | COUNT(resolved) / days per officer |

### 12.7 Quality & Fraud Prevention Metrics

| Metric | Description | Formula |
|--------|-------------|---------|
| **Duplicate Detection Rate** | % caught by image hash matching | COUNT(DUPLICATE) / COUNT(submitted) |
| **GPS Validation Failure Rate** | % failed GPS/boundary check | COUNT(gps_failed) / COUNT(submitted) |
| **EXIF Validation Failure Rate** | % failed photo timestamp check | COUNT(exif_failed) / COUNT(submitted) |
| **Fraud Alerts** | Reports flagged for suspicious activity | COUNT(fraud_flag = true) |
| **Penalty Actions** | Warnings, suspensions, bans issued | COUNT GROUP BY penalty_type |
| **Reopened Reports** | Reports returned for re-investigation | COUNT(reopened = true) |
| **Escalations** | Reports escalated to supervisors | COUNT(escalated = true) |

### 12.8 Geographic Distribution Metrics

| Metric | Description | Grouping |
|--------|-------------|----------|
| **Reports by Ward** | Distribution across wards | GROUP BY ward_code |
| **Reports by Zone** | Distribution across zones | GROUP BY zone_code |
| **Reports by District** | Distribution across districts | GROUP BY district_code |
| **Hotspot Analysis** | Locations with high report density | GROUP BY locality, HAVING COUNT > threshold |
| **Coverage Map** | Areas with/without reports | Geographic visualization |
| **Resolution Time by Area** | SLA performance by location | AVG(resolution_time) GROUP BY locality |

### 12.9 Trend & Comparison Metrics

| Metric | Description | Comparison |
|--------|-------------|------------|
| **Week-over-Week Growth** | Change in report volume | (this_week - last_week) / last_week |
| **Month-over-Month Trend** | Monthly volume comparison | (this_month - last_month) / last_month |
| **YoY Comparison** | Year-over-year metrics | this_year vs last_year |
| **Target vs Actual** | Performance against targets | actual / target × 100% |
| **Benchmark Comparison** | Performance vs other districts | district_metric / state_avg |

### 12.10 Dashboard Hierarchy

| Role | Dashboard Focus | Key Metrics |
|------|-----------------|-------------|
| **Teacher** | Personal performance | My reports, earnings, status distribution |
| **Verifier** | Queue management | Pending queue, verification rate, accuracy |
| **MC Officer** | Field operations | Assigned work, capture rate, daily targets |
| **MC Supervisor** | Team performance | Team workload, SLA compliance, escalations |
| **District Admin** | District overview | Volume, resolution rates, top performers, hotspots |
| **State Admin** | Statewide analytics | District comparison, trends, budget utilization, fraud metrics |

### 12.11 DSS Configuration

The Dashboard Backend (DSS) requires two types of configuration: **MasterChartConfig** (defines aggregation queries) and **DashboardConfig** (groups charts into dashboards).

#### 12.11.1 Master Chart Configuration (`MasterChartConfig.json`)

> **Config File:** [`03-configs/mdms/DSS/MasterChartConfig.json`](03-configs/mdms/DSS/MasterChartConfig.json)

Defines all chart queries with Elasticsearch aggregations. Charts included:

| Chart ID | Type | Description |
|----------|------|-------------|
| `sdcrsTotalReports` | metric | Total report count with trend insight |
| `sdcrsResolvedReports` | metric | Count of resolved reports |
| `sdcrsCaptureRate` | metric | Percentage of captured vs assigned |
| `sdcrsAvgResolutionTime` | metric | Average time to resolution (hours) |
| `sdcrsReportsByStatus` | pie | Distribution by workflow status |
| `sdcrsReportsByType` | pie | Distribution by report type |
| `sdcrsDailyReportTrend` | line | Daily submission trend |
| `sdcrsReportsByDistrict` | bar | Geographic distribution |
| `sdcrsTotalPayouts` | metric | Total payout amount |
| `sdcrsPendingPayouts` | metric | Pending payout count |
| `sdcrsPayoutsByDistrict` | bar | Payouts by district |
| `sdcrsMonthlyPayoutTrend` | line | Monthly payout trend |
| `sdcrsTopEarners` | table | Top 10 earning reporters |
| `sdcrsAvgTimeByStage` | bar | Avg time per workflow stage |
| `sdcrsAgingReport` | table | Open reports by age bucket |
| `sdcrsVerifierPerformance` | table | Verifier stats |
| `sdcrsMcOfficerPerformance` | table | MC Officer capture stats |
| `sdcrsFraudMetrics` | metric | Fraud flag count |

#### 12.11.2 Dashboard Configuration (`DashboardConfig.json`)

> **Config File:** [`03-configs/mdms/DSS/DashboardConfig.json`](03-configs/mdms/DSS/DashboardConfig.json)

Groups charts into role-specific dashboards:

| Dashboard ID | Name | Purpose |
|--------------|------|---------|
| `sdcrs-overview` | SDCRS Overview | Summary metrics, status/type distribution, daily trends, geographic |
| `sdcrs-payout` | SDCRS Payout | Payout totals, pending, district breakdown, monthly trend, top earners |
| `sdcrs-sla` | SDCRS SLA | SLA compliance rate, breached reports, avg time by stage, aging report |
| `sdcrs-performance` | SDCRS Performance | Top reporters, verifier stats, MC officer capture rates |

#### 12.11.3 Role-Dashboard Mapping (`RoleDashboardMapping.json`)

> **Config File:** [`03-configs/mdms/DSS/RoleDashboardMapping.json`](03-configs/mdms/DSS/RoleDashboardMapping.json)

Maps user roles to visible dashboards:

| Role | Dashboards |
|------|------------|
| TEACHER | Overview |
| VERIFIER | Overview, SLA |
| MC_OFFICER | Overview |
| MC_SUPERVISOR | Overview, SLA, Performance |
| DISTRICT_ADMIN | Overview, Payout, SLA, Performance |
| STATE_ADMIN | Overview, Payout, SLA, Performance |

---

## 13. Next Steps

1. **Design Output #4:** Create detailed OpenAPI/Swagger specifications for all SDCRS APIs
2. **Design Output #5:** Develop sequence diagrams for key user flows ✅ (Completed)
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

*Document Version: 1.2*
*Last Updated: December 2025*
*Status: Draft - Simplified to single Dog Report Registry*
*Change: Updated to use DIGIT-native Expense Service + IFMS Adapter for payouts (JIT/Treasury integration)*
