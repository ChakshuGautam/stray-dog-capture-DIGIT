# SDCRS Design Output #5: Sequence Diagrams

This document contains sequence diagrams for the Stray Dog Capture & Reporting System (SDCRS), following the same pattern as [DIGIT Complaints Resolution System](https://docs.digit.org/complaints-resolution/design/architecture/sequence-diagrams).

## PlantUML Source Files

| Diagram | File | Description |
|---------|------|-------------|
| Create Report | [`SDCRS_Create.puml`](./05-sequence-diagrams/SDCRS_Create.puml) | Backend creation flow with sync fraud validation |
| Update Report | [`SDCRS_Update.puml`](./05-sequence-diagrams/SDCRS_Update.puml) | Update/workflow transition flow |
| Search Reports | [`SDCRS_Search.puml`](./05-sequence-diagrams/SDCRS_Search.puml) | Search flow |
| Track Report | [`SDCRS_Track.puml`](./05-sequence-diagrams/SDCRS_Track.puml) | Public tracking flow (anonymous access) |
| Async Fraud (HTTP) | [`SDCRS_AsyncValidation.puml`](./05-sequence-diagrams/SDCRS_AsyncValidation.puml) | AI/ML fraud detection via HTTP |
| Async Fraud (Kafka) | [`SDCRS_AsyncValidationKafka.puml`](./05-sequence-diagrams/SDCRS_AsyncValidationKafka.puml) | Event-driven fraud detection via Kafka |
| Payout Processing | [`SDCRS_Payout.puml`](./05-sequence-diagrams/SDCRS_Payout.puml) | Payout flow after capture |

---

## Create Report Flow

```mermaid
sequenceDiagram
    autonumber
    participant UI as ReportUI
    participant GW as Gateway
    participant US as User
    participant SS as SDCRSService
    participant FDS as FraudDetectionService
    participant MDMS as MDMS
    participant IDG as IDGen
    participant FS as FileStore
    participant WF as Workflow
    participant KF as Kafka
    participant PS as Persister
    participant IX as Indexer
    participant DB as PostgreSQL
    participant ES as ElasticSearch

    UI->>GW: Call Report _create API with request payload<br/>& auth token. Includes photo & selfie fileStoreIds.
    GW->>US: Validate auth token in the request payload
    US-->>GW: User info

    alt Token valid
        GW->>GW: Enrich UserInfo in request payload
        GW->>SS: Route request to relevant service
    else Token expired or invalid
        GW-->>UI: 401 Unauthorized
    end

    SS->>MDMS: Request service specific master data<br/>for the tenant (ServiceType, PayoutConfig)
    MDMS-->>SS: masters

    SS->>SS: Validate if master data codes sent in the<br/>request payload match the response from MDMS

    alt Master data code(s) invalid
        SS-->>UI: 400 error
    end

    SS->>SS: Validate request payload (business logic)<br/>to ensure data integrity

    SS->>FS: Fetch files for photo & selfie
    FS-->>SS: File bytes and metadata

    Note over SS: Evidence Enrichment (computed once at submission)

    SS->>SS: Compute evidence metadata:<br/>- pHash (64-bit perceptual hash)<br/>- SHA-256 hex (exact match)<br/>- Extract EXIF GPS & timestamp<br/>- Capture device info

    SS->>SS: Build FraudEvaluationRequest with<br/>location, evidence hashes, applicant data

    Note over SS,FDS: Real-time fraud validation (INTERNAL rules only)

    SS->>FDS: POST /fraud/v1/_evaluateSync<br/>(GPS, timestamp, duplicate, velocity checks)
    FDS->>MDMS: Fetch FraudRules (ruleType=INTERNAL)
    MDMS-->>FDS: enabled fraud rules

    Note over FDS,DB: Database queries for cross-reporter duplicate detection

    FDS->>DB: Query eg_sdcrs_report_evidence:<br/>SELECT WHERE image_hash_hex = ?<br/>(exact duplicate check, O(log n), <1ms)
    DB-->>FDS: matching reports (if any)

    FDS->>DB: Query eg_sdcrs_report_evidence:<br/>SELECT WHERE bit_count(phash # ?) <= 10<br/>(near-duplicate check, 5-20ms)
    DB-->>FDS: similar images with hamming distance

    FDS->>DB: Query eg_sdcrs_report_evidence:<br/>SELECT WHERE ST_DWithin(location, ?, 50m)<br/>AND reporter_id != ?<br/>(geo-temporal duplicate, 2-5ms)
    DB-->>FDS: nearby reports from other reporters

    FDS->>DB: Query eg_sdcrs_report_evidence:<br/>SELECT device_id, COUNT(DISTINCT reporter_id)<br/>(device sharing check, <1ms)
    DB-->>FDS: device collusion results

    FDS->>FDS: Evaluate remaining INTERNAL rules:<br/>- GEO_DISTANCE (EXIF vs submitted)<br/>- GEO_BOUNDARY (within tenant)<br/>- TIMESTAMP_AGE (photo freshness)<br/>- VELOCITY (daily submission limit)
    FDS-->>SS: FraudEvaluationResponse<br/>(passed, riskLevel, primaryAction)

    alt primaryAction = AUTO_REJECT
        SS-->>UI: 400 error - Fraud detected<br/>(GPS_MISMATCH, DUPLICATE, etc.)
    end

    SS->>IDG: Generate ID(s) for report based on<br/>format defined in masters (DJ-SDCRS-YYYY-NNNNNN)
    IDG-->>SS: List of IDs

    SS->>SS: Enrich request payload with ID,<br/>fraud score, evidence hashes, & audit details

    SS->>US: Retrieve reporter (teacher) user details
    US-->>SS: user
    SS->>SS: Enrich user details (name, phone, school)

    SS->>WF: Search for business service
    WF-->>SS: business service

    SS->>SS: Create new workflow process instance<br/>& enrich with action from request payload

    SS->>WF: Call transition API to trigger workflow<br/>(action: SUBMIT, status: PENDING_VALIDATION)
    WF-->>SS: workflow status

    SS->>SS: Enrich workflow status in request payload

    SS->>KF: Push report payload to save topic

    SS-->>UI: Return report created response<br/>(reportNumber, status: PENDING_VALIDATION)

    KF->>PS: Read payload
    PS->>DB: Persist data as per configuration<br/>(eg_sdcrs_report + eg_sdcrs_report_evidence tables)

    KF->>IX: Read payload
    IX->>ES: Push data to indexes as per configuration

    KF->>SS: Trigger async validation<br/>(sdcrs-async-validation topic)
```

---

## Update Report Flow

```mermaid
sequenceDiagram
    autonumber
    participant UI as ReportUI
    participant GW as Gateway
    participant US as User
    participant SS as SDCRSService
    participant MDMS as MDMS
    participant FS as FileStore
    participant WF as Workflow
    participant KF as Kafka
    participant PS as Persister
    participant IX as Indexer
    participant DB as PostgreSQL
    participant ES as ElasticSearch

    UI->>GW: Call Report _update API with request payload<br/>& auth token. Used for verification, assignment,<br/>and resolution updates.
    GW->>US: Validate auth token in the request payload
    US-->>GW: User info

    alt Token valid
        GW->>GW: Enrich UserInfo in request payload
        GW->>SS: Route request to relevant service
    else Token expired or invalid
        GW-->>UI: 401 Unauthorized
    end

    SS->>MDMS: Request service specific master data<br/>for the tenant
    MDMS-->>SS: masters

    SS->>SS: Validate if master data codes sent in the<br/>request payload match the response from MDMS

    alt Master data code(s) invalid
        SS-->>UI: 400 error
    end

    SS->>SS: Validate request payload (business logic)<br/>to ensure data integrity

    SS->>DB: Fetch existing report by reportNumber
    DB-->>SS: existing report

    alt Report not found
        SS-->>UI: 404 Not Found
    end

    SS->>SS: Validate user has permission for<br/>requested action on current status

    alt User not authorized for action
        SS-->>UI: 403 Forbidden
    end

    alt Action requires file upload (MARK_CAPTURED)
        SS->>FS: Validate capture photo fileStoreId exists
        FS-->>SS: File metadata
    end

    SS->>SS: Enrich request payload with audit details

    SS->>US: Retrieve actor user details
    US-->>SS: user
    SS->>SS: Enrich user details

    SS->>WF: Call transition API based on<br/>action in the request payload
    WF-->>SS: workflow status

    SS->>SS: Enrich workflow status in request payload

    alt Action is MARK_CAPTURED
        SS->>SS: Set payoutEligible = true<br/>Calculate payout amount from config
    end

    alt Action is ASSIGN_MC
        SS->>US: Validate assignee exists and has MC_OFFICER role
        US-->>SS: assignee details
        SS->>SS: Enrich assignment details
    end

    SS->>KF: Push report payload to update topic

    SS-->>UI: Return report updated response

    KF->>PS: Read payload
    PS->>DB: Persist data as per configuration

    KF->>IX: Read payload
    IX->>ES: Update ES doc (idempotency) in index

    alt Status changed to CAPTURED
        KF->>SS: Trigger payout processing<br/>(sdcrs-payout-trigger topic)
    end

    KF->>SS: Trigger notification<br/>(sdcrs-notification topic)
```

---

## Search Reports Flow

```mermaid
sequenceDiagram
    autonumber
    participant UI as ReportUI
    participant GW as Gateway
    participant US as User
    participant SS as SDCRSService
    participant DB as PostgreSQL

    UI->>GW: Call Report _search API with search criteria<br/>& auth token.
    GW->>US: Validate auth token in the request payload
    US-->>GW: User info

    alt Token valid
        GW->>GW: Enrich UserInfo in request payload
        GW->>SS: Route request to relevant service
    else Token expired or invalid
        GW-->>UI: 401 Unauthorized
    end

    SS->>SS: Validate request payload (business logic)<br/>to ensure data integrity

    alt Invalid Search Params
        SS-->>UI: 400 Invalid Search
    end

    SS->>SS: Retrieve user info from request<br/>payload & prep for query to DB

    SS->>SS: Apply role-based filtering:<br/>TEACHER: own reports only<br/>VERIFIER: reports in PENDING_VERIFICATION<br/>MC_OFFICER: assigned reports<br/>MC_SUPERVISOR: reports in ward/district

    SS->>DB: Query database with search criteria<br/>(status, reporterId, assignedTo, locality, dateRange)
    DB-->>SS: rows

    SS->>SS: Format & prep response payload<br/>(apply pagination, sort by createdTime DESC)

    SS-->>UI: Return responses as a list
```

---

## Track Report Flow (Public/Anonymous)

```mermaid
sequenceDiagram
    autonumber
    participant UI as Browser/SMS Link
    participant GW as Gateway
    participant SS as SDCRSService
    participant LOC as Localization
    participant DB as PostgreSQL

    Note over UI,GW: No authentication required<br/>(endpoint whitelisted in gateway)

    UI->>GW: POST /sdcrs-services/v1/report/_track<br/>(reportNumber OR trackingId)

    GW->>GW: Check endpoint whitelist<br/>(egov-open-endpoints-whitelist)

    alt Endpoint NOT whitelisted
        GW-->>UI: 401 Unauthorized
    else Endpoint IS whitelisted (bypass auth)
        GW->>SS: Route request to SDCRS service<br/>(no UserInfo enrichment)
    end

    SS->>SS: Validate request payload<br/>(must have reportNumber OR trackingId)

    alt Neither identifier provided
        SS-->>UI: 400 Bad Request
    end

    alt Search by reportNumber
        SS->>DB: Query by report_number
    else Search by trackingId
        SS->>DB: Query by tracking_id
    end

    DB-->>SS: Report record (if found)

    alt Report not found
        SS-->>UI: 404 Not Found
    end

    SS->>SS: Sanitize response<br/>(remove PII: reporter name, phone,<br/>assigned officer details)

    SS->>LOC: Get status descriptions<br/>(localized text for status codes)
    LOC-->>SS: Localized messages

    SS->>SS: Build timeline from workflow history

    SS->>SS: Calculate estimated resolution<br/>(based on SLA and current status)

    SS-->>UI: Return sanitized tracking response
```

**Key Features:**
- **No Authentication Required**: Endpoint whitelisted in gateway (`egov-open-endpoints-whitelist`)
- **Dual Lookup**: Can search by `reportNumber` (DJ-SDCRS-2024-000123) or `trackingId` (ABC123)
- **PII Sanitization**: Response excludes reporter name, phone, and officer details
- **Localized Status**: Status descriptions fetched from Localization Service

---

## Async Fraud Validation Flow

This flow is triggered asynchronously after report creation to run AI/ML-based external validations.

```mermaid
sequenceDiagram
    autonumber
    participant KF as Kafka
    participant SS as SDCRSService
    participant FDS as FraudDetectionService
    participant MDMS as MDMS
    participant EXT as External AI/ML APIs
    participant WF as Workflow
    participant PS as Persister
    participant NS as NotificationService
    participant DB as PostgreSQL
    participant TCH as Teacher

    Note over KF,SS: Triggered after report persisted<br/>(sdcrs-async-validation topic)

    KF->>SS: Consume async validation message<br/>(reportId, tenantId)

    SS->>DB: Fetch report details with evidence
    DB-->>SS: report (with fileStoreIds, location, etc.)

    SS->>SS: Build FraudEvaluationRequest<br/>(include all evidence data)

    Note over SS,FDS: Async fraud validation (EXTERNAL rules - AI/ML)

    SS->>FDS: POST /fraud/v1/_evaluateAsync<br/>(AI/ML validations)

    FDS->>MDMS: Fetch FraudRules (ruleType=EXTERNAL)<br/>& ExternalValidators config
    MDMS-->>FDS: enabled rules & validator configs

    loop For each EXTERNAL rule
        FDS->>FDS: Get validator config for rule.validatorId

        alt Validator: DOG_BREED_CLASSIFIER
            FDS->>EXT: POST to AI breed classifier API<br/>(photo URL)
            EXT-->>FDS: {is_dog: true, breed: "stray", confidence: 0.92}
        else Validator: OBJECT_DETECTOR
            FDS->>EXT: POST to YOLO object detection API<br/>(photo URL)
            EXT-->>FDS: {dog_count: 1, person_present: false}
        else Validator: IMAGE_QUALITY_ANALYZER
            FDS->>EXT: POST to IQA API<br/>(photo URL)
            EXT-->>FDS: {blur_score: 0.1, acceptable: true}
        else Validator: GPS_SPOOFING_DETECTOR
            FDS->>EXT: POST to GPS spoofing detector<br/>(device info, location history)
            EXT-->>FDS: {is_spoofed: false, confidence: 0.95}
        end

        FDS->>FDS: Build RuleResult from API response
    end

    FDS->>FDS: Aggregate results, calculate total score<br/>Determine overallRisk & primaryAction

    FDS-->>SS: FraudEvaluationResponse<br/>(ruleResults[], totalScore, primaryAction)

    SS->>SS: Update report with fraud assessment<br/>(fraudScore, fraudRisk, ruleResults)

    alt primaryAction = AUTO_REJECT
        SS->>WF: Transition to AUTO_REJECTED<br/>(action: AUTO_VALIDATE_FAIL)
        WF-->>SS: workflow status

        SS->>KF: Push update to persister topic
        KF->>PS: Read payload
        PS->>DB: Update report status & fraud details

        SS->>NS: Notify teacher (rejection reason)
        NS->>TCH: SMS: Report rejected - {reason}

    else primaryAction = MANUAL_REVIEW
        SS->>SS: Flag for manual verification<br/>(add to verifier queue with HIGH priority)

        SS->>KF: Push update to persister topic
        KF->>PS: Read payload
        PS->>DB: Update report with manual review flag

    else primaryAction = APPROVE
        SS->>WF: Transition to PENDING_VERIFICATION<br/>(action: AUTO_VALIDATE_PASS)
        WF-->>SS: workflow status

        SS->>KF: Push update to persister topic
        KF->>PS: Read payload
        PS->>DB: Update report status

        SS->>NS: Notify verifier (new report for review)
    end
```

**Key Features:**
- **Async Processing**: Runs after response returned to user (non-blocking)
- **External AI/ML Validators**: Calls configured external APIs from MDMS
- **Circuit Breaker**: FraudDetectionService uses Resilience4j for fault tolerance
- **Fallback Handling**: If external API fails, applies fallback action (SKIP_RULE, DEGRADE_TO_MANUAL)
- **Configurable Rules**: All rules loaded from MDMS (FRAUD-DETECTION/FraudRules.json)

---

## Async Fraud Validation Flow (Kafka Event-Driven)

This is an alternative fully event-driven pattern using Kafka for complete decoupling between SDCRSService and FraudDetectionService.

```mermaid
sequenceDiagram
    autonumber
    participant KF as Kafka
    participant SS as SDCRSService
    participant FDS as FraudDetectionService
    participant MDMS as MDMS
    participant EXT as External AI/ML APIs
    participant WF as Workflow
    participant PS as Persister
    participant NS as NotificationService
    participant DB as PostgreSQL
    participant TCH as Teacher

    Note over KF,SS: Phase 1: SDCRSService publishes evaluation request

    KF->>SS: Consume from sdcrs-async-validation topic<br/>(reportId, tenantId)

    SS->>DB: Fetch report details with evidence
    DB-->>SS: report (with fileStoreIds, location, etc.)

    SS->>SS: Build FraudEvaluationRequest<br/>(businessId, sourceModule=SDCRS,<br/>location, evidence, applicant data)

    SS->>KF: Publish to fraud-evaluation-request topic<br/>(FraudEvaluationRequest payload)

    Note over SS: SDCRSService continues with other work<br/>(fully async, no blocking)

    Note over KF,FDS: Phase 2: FraudDetectionService processes request

    KF->>FDS: Consume from fraud-evaluation-request topic

    FDS->>MDMS: Fetch FraudRules (ruleType=EXTERNAL)<br/>& ExternalValidators config
    MDMS-->>FDS: enabled rules & validator configs

    loop For each EXTERNAL rule
        FDS->>FDS: Get validator config for rule.validatorId

        alt Validator: DOG_BREED_CLASSIFIER
            FDS->>EXT: POST to AI breed classifier API
            EXT-->>FDS: {is_dog: true, breed: "stray", confidence: 0.92}
        else Validator: OBJECT_DETECTOR
            FDS->>EXT: POST to YOLO object detection API
            EXT-->>FDS: {dog_count: 1, person_present: false}
        else Validator: IMAGE_QUALITY_ANALYZER
            FDS->>EXT: POST to IQA API
            EXT-->>FDS: {blur_score: 0.1, acceptable: true}
        else Validator: GPS_SPOOFING_DETECTOR
            FDS->>EXT: POST to GPS spoofing detector
            EXT-->>FDS: {is_spoofed: false, confidence: 0.95}
        end

        FDS->>FDS: Build RuleResult from API response
    end

    FDS->>FDS: Aggregate results, calculate total score<br/>Determine overallRisk & primaryAction

    FDS->>KF: Publish to fraud-evaluation-result topic<br/>(businessId, passed, totalScore,<br/>primaryAction, ruleResults[])

    Note over KF,SS: Phase 3: SDCRSService processes result

    KF->>SS: Consume from fraud-evaluation-result topic<br/>(FraudEvaluationResponse)

    SS->>DB: Fetch report by businessId
    DB-->>SS: report

    SS->>SS: Update report with fraud assessment<br/>(fraudScore, fraudRisk, ruleResults)

    alt primaryAction = AUTO_REJECT
        SS->>WF: Transition to AUTO_REJECTED<br/>(action: AUTO_VALIDATE_FAIL)
        WF-->>SS: workflow status

        SS->>KF: Push update to persister topic
        KF->>PS: Read payload
        PS->>DB: Update report status & fraud details

        SS->>NS: Notify teacher (rejection reason)
        NS->>TCH: SMS: Report rejected - {reason}

    else primaryAction = MANUAL_REVIEW
        SS->>SS: Flag for manual verification<br/>(HIGH priority in verifier queue)

        SS->>KF: Push update to persister topic
        KF->>PS: Read payload
        PS->>DB: Update report with manual review flag

    else primaryAction = APPROVE
        SS->>WF: Transition to PENDING_VERIFICATION<br/>(action: AUTO_VALIDATE_PASS)
        WF-->>SS: workflow status

        SS->>KF: Push update to persister topic
        KF->>PS: Read payload
        PS->>DB: Update report status

        SS->>NS: Notify verifier (new report for review)
    end
```

**Kafka Topics:**

| Topic | Producer | Consumer | Payload |
|-------|----------|----------|---------|
| `sdcrs-async-validation` | SDCRSService (after create) | SDCRSService | reportId, tenantId |
| `fraud-evaluation-request` | SDCRSService | FraudDetectionService | FraudEvaluationRequest |
| `fraud-evaluation-result` | FraudDetectionService | SDCRSService, other modules | FraudEvaluationResponse |

**Key Advantages of Kafka Pattern:**
- **Full Decoupling**: FraudDetectionService has no direct dependency on calling services
- **Multi-Consumer**: Multiple modules (SDCRS, TL, PGR) can publish to same request topic
- **Retry & DLQ**: Kafka provides built-in retry and dead-letter queue handling
- **Backpressure**: Consumer controls processing rate
- **Audit Trail**: All fraud evaluations logged in Kafka for compliance

---

## Payout Processing Flow

```mermaid
sequenceDiagram
    autonumber
    participant KF as Kafka
    participant SS as SDCRSService
    participant US as User
    participant MDMS as MDMS
    participant BS as BillingService
    participant WF as Workflow
    participant PS as Persister
    participant NS as NotificationService
    participant DB as PostgreSQL
    participant TCH as Teacher

    Note over KF,SS: Triggered when report status changes to CAPTURED

    KF->>SS: Consume payout trigger message<br/>(reportId, teacherId, amount)

    SS->>DB: Fetch report details
    DB-->>SS: report (with teacherId, resolution details)

    SS->>MDMS: Fetch payout configuration<br/>(amount, monthly cap, daily limit)
    MDMS-->>SS: PayoutConfig

    SS->>US: Retrieve teacher details<br/>(bankAccount, upiId)
    US-->>SS: teacher payment details

    SS->>DB: Query teacher's monthly payout total<br/>(current month, status=COMPLETED)
    DB-->>SS: currentMonthTotal

    SS->>SS: Calculate remaining monthly allowance<br/>(cap - currentMonthTotal)

    alt Monthly cap exceeded
        SS->>SS: Set payoutStatus = CAP_EXCEEDED
        SS->>KF: Push update to persister topic
        KF->>PS: Read payload
        PS->>DB: Update payoutStatus in eg_sdcrs_report

        SS->>NS: Notify teacher (cap exceeded)
        NS->>TCH: SMS: Monthly payout cap reached

        Note over SS: End flow - no payment
    else Within monthly cap
        SS->>BS: POST /billing-service/demand/_create<br/>(taxHeadCode: SDCRS_PAYOUT, amount: 500)
        BS-->>SS: demandId

        SS->>SS: Update payoutDemandId, payoutStatus=PENDING

        SS->>KF: Push update to persister topic
        KF->>PS: Read payload
        PS->>DB: Update payout details in eg_sdcrs_report

        Note over BS: Async payment processing via Collection Service

        BS->>BS: Process payment to teacher's account
        BS->>KF: Publish payment-completed event

        KF->>SS: Consume payment completion event

        SS->>SS: Update payoutStatus = COMPLETED

        SS->>WF: Transition to RESOLVED status
        WF-->>SS: workflow status

        SS->>KF: Push final update to persister topic
        KF->>PS: Read payload
        PS->>DB: Update final status in eg_sdcrs_report

        SS->>NS: Notify teacher (payout completed)
        NS->>TCH: SMS: Rs.500 credited for report {reportNumber}
    end
```

---

## Workflow Actions Reference

| Action | Current Status | Next Status | Actor |
|--------|----------------|-------------|-------|
| SUBMIT | null | PENDING_VALIDATION | Teacher |
| AUTO_VALIDATE_PASS | PENDING_VALIDATION | PENDING_VERIFICATION | System |
| AUTO_VALIDATE_FAIL | PENDING_VALIDATION | AUTO_REJECTED | System |
| VERIFY | PENDING_VERIFICATION | VERIFIED | Verifier |
| REJECT | PENDING_VERIFICATION | REJECTED | Verifier |
| MARK_DUPLICATE | PENDING_VERIFICATION | DUPLICATE | Verifier |
| ASSIGN_MC | VERIFIED | ASSIGNED | MC_Supervisor |
| START_FIELD_VISIT | ASSIGNED | IN_PROGRESS | MC_Officer |
| MARK_CAPTURED | IN_PROGRESS | CAPTURED | MC_Officer |
| MARK_UNABLE_TO_LOCATE | IN_PROGRESS | UNABLE_TO_LOCATE | MC_Officer |
| PROCESS_PAYOUT | CAPTURED | RESOLVED | System |

---

## Services Reference

| Service | Purpose |
|---------|---------|
| ReportUI | Mobile app or web interface |
| Gateway | API gateway with auth validation |
| User | User service for authentication & user details |
| SDCRSService | Core SDCRS business logic |
| **FraudDetectionService** | **Generic fraud detection (reusable across DIGIT modules)** |
| MDMS | Master data (ServiceType, PayoutConfig, FraudRules) |
| IDGen | Report number generation |
| FileStore | Photo/selfie storage & retrieval |
| Workflow | State machine management |
| Kafka | Message broker for async operations |
| Persister | Async database writes |
| Indexer | Elasticsearch indexing |
| BillingService | Payout/demand management |
| NotificationService | SMS/Email notifications |
| External AI/ML APIs | Dog breed classifier, object detector, image quality, GPS spoofing |
| PostgreSQL | Primary database |
| ElasticSearch | Search index |

---

## Database Tables Reference

| Table | Populated By | Queried By | Purpose |
|-------|--------------|------------|---------|
| `eg_sdcrs_report` | Persister (via Kafka) | SDCRSService, FraudDetectionService | Main report records |
| `eg_sdcrs_report_evidence` | Persister (via Kafka) | **FraudDetectionService** | Evidence metadata with hashes, GPS, timestamps for duplicate detection |

### Evidence Table Query Patterns (Fraud Detection)

| Query Type | SQL Pattern | Index | Latency |
|------------|-------------|-------|---------|
| Exact duplicate | `WHERE image_hash_hex = ?` | B-tree | <1ms |
| Near-duplicate (pHash) | `WHERE bit_count(image_phash # ?) <= 10` | B-tree + filter | 5-20ms |
| Geo-temporal | `WHERE ST_DWithin(gps_location, ?, 50m)` | GiST spatial | 2-5ms |
| Device collusion | `WHERE device_id = ? GROUP BY HAVING COUNT(DISTINCT reporter_id) >= 2` | B-tree | <1ms |

> **Note**: Evidence metadata (pHash, SHA-256, EXIF GPS) is computed **once** by SDCRSService at submission time and stored in `eg_sdcrs_report_evidence`. The Fraud Detection Service queries this table but does not write to it.

---

## Generating PNG Images

To generate PNG images from PlantUML source files:

```bash
# Using PlantUML JAR
java -jar plantuml.jar sequence-diagrams/SDCRS_Create.puml

# Using Docker
docker run -v $(pwd):/data plantuml/plantuml sequence-diagrams/*.puml

# Using online server
# Upload .puml files to https://www.plantuml.com/plantuml/
```

---

*Document Version: 2.0*
*Last Updated: December 2025*
*Pattern Reference: [DIGIT Complaints Resolution Sequence Diagrams](https://docs.digit.org/complaints-resolution/design/architecture/sequence-diagrams)*
