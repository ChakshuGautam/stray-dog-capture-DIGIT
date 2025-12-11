# SDCRS Design Outputs Checklist

## Architecture Decision Required

**Three implementation options have been documented for evaluation:**

| Option | Approach | Development Time | Flexibility |
|--------|----------|------------------|-------------|
| **A** | Use CCRS directly (config only) | ~2-3 weeks | Limited |
| **B** | Custom service inspired by CCRS | ~6-8 weeks | Full |
| **C** | Fork CCRS and modify data model | ~4-5 weeks | High |

### Option A: CCRS Direct Configuration
- Use existing PGR/CCRS service
- Configuration changes only (ServiceDefs, Workflow, MDMS)
- Store dog-specific data in `additionalDetail` JSONB
- Reuse PGR UI with customizations
- **Documentation:** [`04a-ccrs-direct-configuration.md`](./04a-ccrs-direct-configuration.md)
- **Config Files:** [`04a-ccrs/`](./04a-ccrs/) folder

### Option B: Custom Service (CCRS-Inspired)
- Build new `sdcrs-services` module
- Custom database schema with first-class columns
- Domain-specific API endpoints
- Full control over data model and queries
- **Documentation:** [`04b-ccrs-inspired-custom-service.md`](./04b-ccrs-inspired-custom-service.md)
- **Config Files:** [`04b-no-ccrs/`](./04b-no-ccrs/) folder

### Option C: Fork CCRS with Modified Schema
- Fork CCRS/PGR repository
- Add first-class columns to existing `eg_pgr_service_v2` table
- Retain PGR API structure with extended fields
- Balance between proven code and custom data model
- **Documentation:** [`04c-ccrs-fork-modified.md`](./04c-ccrs-fork-modified.md)
- **Config Files:** [`04c-ccrs-fork/`](./04c-ccrs-fork/) folder

### Decision Criteria

| Factor | Favor Option A | Favor Option B | Favor Option C |
|--------|----------------|----------------|----------------|
| Timeline | Tight deadline | Flexible timeline | Moderate timeline |
| Team expertise | Limited DIGIT exp | Strong DIGIT exp | Moderate DIGIT exp |
| Query complexity | Simple filters | Complex analytics | Complex analytics |
| Future extensions | Minimal | Significant | Moderate |
| Maintenance | Prefer platform | Accept custom code | Accept fork maintenance |
| Upstream updates | Automatic | N/A | Manual merge required |

---

## Status Overview

| # | Design Output | Status | Files |
|---|---------------|--------|-------|
| 1 | Process Workflow | ✅ Completed | [`01-process-workflow.md`](./01-process-workflow.md) |
| 2 | User Stories | ✅ Completed | [`02-user-stories.md`](./02-user-stories.md) |
| 3 | Service Design | ✅ Completed | [`03-service-design.md`](./03-service-design.md) |
| 4a | Option A: CCRS Direct | ✅ Completed | [`04a-ccrs-direct-configuration.md`](./04a-ccrs-direct-configuration.md), [`04a-ccrs/`](./04a-ccrs/) |
| 4b | Option B: Custom Service | ✅ Completed | [`04b-ccrs-inspired-custom-service.md`](./04b-ccrs-inspired-custom-service.md), [`04b-no-ccrs/`](./04b-no-ccrs/) |
| 4c | Option C: CCRS Fork | ✅ Completed | [`04c-ccrs-fork-modified.md`](./04c-ccrs-fork-modified.md), [`04c-ccrs-fork/`](./04c-ccrs-fork/) |
| 5 | Sequence Diagrams | ✅ Completed | [`05-sequence-diagrams.md`](./05-sequence-diagrams.md), [`05-sequence-diagrams/`](./05-sequence-diagrams/) |
| 6 | UI Wireframes | ✅ Completed | Embedded in user stories ([`02-user-stories/`](./02-user-stories/)) |
| 7 | Scale Management | ✅ Completed | [`07-scale-management.md`](./07-scale-management.md) |
| 8 | Fraud Detection Service | ✅ Completed | [`08-fraud-detection-service.md`](./08-fraud-detection-service.md) |

### Configuration Folders

| Folder | Option | Contents |
|--------|--------|----------|
| [`04a-ccrs/`](./04a-ccrs/) | A | ServiceDefs, Workflow, Roles, JSONB schema, Templates, **Fraud Detection MDMS** |
| [`04b-no-ccrs/`](./04b-no-ccrs/) | B | MDMS configs, Persister, Indexer, DB schema, Templates, **Fraud Detection MDMS + DB** |
| [`04c-ccrs-fork/`](./04c-ccrs-fork/) | C | DB migration, Modified persister, MDMS configs, Templates, **Fraud Detection MDMS + DB** |

---

## Completed Design Outputs

### 1. Process Workflow (Completed)
- **File:** [`01-process-workflow.md`](./01-process-workflow.md)
- **Contents:**
  - Complete workflow state diagram
  - State transitions with actions and roles
  - Business rules for each transition
  - DIGIT workflow configuration (JSON)
  - SLA definitions per state

### 2. User Stories (Completed)
- **File:** [`02-user-stories.md`](./02-user-stories.md)
- **Contents:**
  - Epic breakdown by user role
  - Citizen user stories (report, track, feedback)
  - Ward Officer user stories (verify, assign, monitor)
  - Capture Team user stories (accept, capture, update)
  - Shelter Staff user stories (intake, treat, release)
  - Administrator user stories (configure, reports, manage)
  - Acceptance criteria for each story

### 3. Service Design (Completed)
- **File:** [`03-service-design.md`](./03-service-design.md)
- **Contents:**
  - System architecture overview
  - Service dependencies (DIGIT platform services)
  - Data models (DogReport, Location, Evidence, etc.)
  - MDMS configuration specifications
  - Kafka topics for persistence
  - Indexer configuration for search
  - Notification templates
  - **Public Tracking API** (Section 10) - Anonymous access for report tracking
    - Dual identifiers: Report Number + Tracking ID + Short URL
    - IDGen configuration for tracking ID (`[A-Z]{3}[0-9]{3}`)
    - URL Shortener integration
    - Sanitized response (no PII exposure)
  - **Gateway Whitelist Configuration** (Section 11)
    - `egov-open-endpoints-whitelist` for `_track` endpoint
    - Rate limiting configuration (100 req/min per IP)
  - **Process Performance Indicators** (Section 12)
    - Report volume metrics (total, by status, by type)
    - Time-based SLA metrics (avg time to resolution, SLA compliance)
    - Resolution & outcome metrics (capture rate, rejection rate)
    - Payout metrics (total payouts, pending, avg time to payout)
    - User performance metrics (teacher, verifier, MC officer)
    - Quality & fraud prevention metrics
    - Geographic distribution metrics
    - Dashboard hierarchy by role
    - DSS configuration for charts

### 4a. Option A: CCRS Direct Configuration (Completed)
- **Documentation:** [`04a-ccrs-direct-configuration.md`](./04a-ccrs-direct-configuration.md)
- **Config Folder:** [`04a-ccrs/`](./04a-ccrs/)
- **Config Files:**
  - [`ServiceDefs.json`](./04a-ccrs/data/ncr/RAINMAKER-PGR/ServiceDefs.json) - Stray dog complaint types
  - [`SdcrsWorkflowConfig.json`](./04a-ccrs/data/ncr/Workflow/SdcrsWorkflowConfig.json) - SDCRS workflow states
  - [`roles.json`](./04a-ccrs/data/ncr/ACCESSCONTROL-ROLES/roles.json) - Role definitions
  - [`additionalDetail-schema.json`](./04a-ccrs/configs/additionalDetail-schema.json) - JSONB schema reference
  - [`notification-templates.json`](./04a-ccrs/configs/notification-templates.json) - SMS templates
  - [`role-mapping.json`](./04a-ccrs/configs/role-mapping.json) - SDCRS ↔ PGR role mapping
- **Fraud Detection MDMS:** [`04a-ccrs/data/ncr/FRAUD-DETECTION/`](./04a-ccrs/data/ncr/FRAUD-DETECTION/)
  - `FraudRules.json` - All rules (34 total) with `ruleType: INTERNAL|EXTERNAL`
  - `ExternalValidators.json` - External service configurations
  - `RiskScoreConfig.json`, `ApplicantPenalties.json`

### 4b. Option B: Custom Service (Completed)
- **Documentation:** [`04b-ccrs-inspired-custom-service.md`](./04b-ccrs-inspired-custom-service.md)
- **Config Folder:** [`04b-no-ccrs/`](./04b-no-ccrs/)
- **Config Files:**
  - [`ServiceType.json`](./04b-no-ccrs/data/ncr/SDCRS/ServiceType.json) - Dog report types
  - [`PayoutConfig.json`](./04b-no-ccrs/data/ncr/SDCRS/PayoutConfig.json) - Payout configuration
  - [`RejectionReason.json`](./04b-no-ccrs/data/ncr/SDCRS/RejectionReason.json) - Rejection reasons
  - [`ResolutionType.json`](./04b-no-ccrs/data/ncr/SDCRS/ResolutionType.json) - Resolution outcomes
  - [`SdcrsWorkflowConfig.json`](./04b-no-ccrs/data/ncr/Workflow/SdcrsWorkflowConfig.json) - SDCRS workflow
  - [`roles.json`](./04b-no-ccrs/data/ncr/ACCESSCONTROL-ROLES/roles.json) - Role definitions
  - [`sdcrs-persister.yml`](./04b-no-ccrs/configs/sdcrs-persister.yml) - Persister configuration
  - [`sdcrs-indexer.yml`](./04b-no-ccrs/configs/sdcrs-indexer.yml) - Indexer configuration
  - [`kafka-topics.json`](./04b-no-ccrs/configs/kafka-topics.json) - Kafka topic definitions
  - [`notification-templates.json`](./04b-no-ccrs/configs/notification-templates.json) - SMS templates
  - [`V1__create_sdcrs_tables.sql`](./04b-no-ccrs/db/V1__create_sdcrs_tables.sql) - Database schema

### 4c. Option C: CCRS Fork with Modified Schema (Completed)
- **Documentation:** [`04c-ccrs-fork-modified.md`](./04c-ccrs-fork-modified.md)
- **Config Folder:** [`04c-ccrs-fork/`](./04c-ccrs-fork/)
- **Config Files:**
  - [`V2__add_sdcrs_columns.sql`](./04c-ccrs-fork/db/V2__add_sdcrs_columns.sql) - Schema migration for PGR table
  - [`ServiceType.json`](./04c-ccrs-fork/data/ncr/SDCRS/ServiceType.json) - Dog report types
  - [`PayoutConfig.json`](./04c-ccrs-fork/data/ncr/SDCRS/PayoutConfig.json) - Payout configuration
  - [`RejectionReason.json`](./04c-ccrs-fork/data/ncr/SDCRS/RejectionReason.json) - Rejection reasons
  - [`ResolutionType.json`](./04c-ccrs-fork/data/ncr/SDCRS/ResolutionType.json) - Resolution outcomes
  - [`SdcrsWorkflowConfig.json`](./04c-ccrs-fork/data/ncr/Workflow/SdcrsWorkflowConfig.json) - SDCRS workflow
  - [`roles.json`](./04c-ccrs-fork/data/ncr/ACCESSCONTROL-ROLES/roles.json) - Role definitions
  - [`notification-templates.json`](./04c-ccrs-fork/configs/notification-templates.json) - SMS templates
- **Fraud Detection MDMS:** [`04c-ccrs-fork/data/ncr/FRAUD-DETECTION/`](./04c-ccrs-fork/data/ncr/FRAUD-DETECTION/)
  - `FraudRules.json` - All rules (34 total) with `ruleType: INTERNAL|EXTERNAL`
  - `ExternalValidators.json` - External service configurations
  - `RiskScoreConfig.json`, `ApplicantPenalties.json`
- **Fraud Detection DB:** [`04c-ccrs-fork/db/V2__create_fraud_detection_tables.sql`](./04c-ccrs-fork/db/V2__create_fraud_detection_tables.sql)

### 5. Sequence Diagrams (Completed)
- **File:** [`05-sequence-diagrams.md`](./05-sequence-diagrams.md)
- **PlantUML Sources:** [`05-sequence-diagrams/`](./05-sequence-diagrams/) folder
- **Contents (matching DIGIT CCRS pattern):**
  - [`SDCRS_Create.puml`](./05-sequence-diagrams/SDCRS_Create.puml) - Backend creation flow (38 steps)
    - Gateway auth validation, MDMS validation
    - FileStore + EXIF extraction, GPS validation
    - Image hash duplicate detection
    - IDGen for report number + tracking ID, User enrichment
    - URL Shortener integration for tracking URL
    - Workflow transition, Kafka async persistence
  - [`SDCRS_Update.puml`](./05-sequence-diagrams/SDCRS_Update.puml) - Update/workflow transition flow (37 steps)
    - Verification, assignment, resolution actions
    - Role-based permission validation
    - Capture photo validation, payout triggering
  - [`SDCRS_Search.puml`](./05-sequence-diagrams/SDCRS_Search.puml) - Search flow (14 steps)
    - Role-based filtering (Teacher/Verifier/MC_Officer)
    - Direct DB query with pagination
  - [`SDCRS_Track.puml`](./05-sequence-diagrams/SDCRS_Track.puml) - **Public tracking flow (15 steps, anonymous access)**
    - Gateway whitelist bypass (no auth required)
    - Dual lookup: reportNumber OR trackingId
    - PII sanitization (no reporter/officer details)
    - Localized status descriptions
  - [`SDCRS_Payout.puml`](./05-sequence-diagrams/SDCRS_Payout.puml) - Payout processing flow (33 steps)
    - Monthly cap validation
    - Billing Service demand creation
    - Payment completion handling

---

## All Design Outputs Complete

All design collaterals have been completed:

- ✅ Process Workflow
- ✅ User Stories (with embedded wireframes)
- ✅ Service Design
- ✅ Option A: CCRS Direct Configuration
- ✅ Option B: Custom Service Implementation
- ✅ Option C: CCRS Fork with Modified Schema
- ✅ Sequence Diagrams
- ✅ UI Wireframes (embedded in user stories)
- ✅ XState Workflow Machine ([`06-workflow/`](./06-workflow/))
- ✅ OpenAPI Specification (Option B only: [`04b-no-ccrs/06-api-specification.yaml`](./04b-no-ccrs/06-api-specification.yaml))
- ✅ Zuul Gateway Configuration (Option A only: [`04a-ccrs/07-zuul-gateway-configuration.md`](./04a-ccrs/07-zuul-gateway-configuration.md))
- ✅ Scale Management Guide ([`07-scale-management.md`](./07-scale-management.md))
- ✅ Fraud Detection Service ([`08-fraud-detection-service.md`](./08-fraud-detection-service.md))
- ✅ Fraud Detection OpenAPI Specification ([`04b-no-ccrs/09-fraud-detection-api-specification.yaml`](./04b-no-ccrs/09-fraud-detection-api-specification.yaml))

### 8. Fraud Detection Service (Completed)
- **File:** [`08-fraud-detection-service.md`](./08-fraud-detection-service.md)
- **Contents:**
  - Reusable fraud detection service design
  - Configurable rule engine with MDMS-driven rules
  - Single `FraudRules.json` with `ruleType` field for all 34 rules
  - Internal rules (22): threshold-based, pattern matching, duplicate detection
  - External rules (12): API-based checks (object detection, face matching, etc.)
  - Duplicate detection via perceptual hashing (pHash)
  - Applicant risk profiling and penalty system
  - API specifications for sync/async fraud checks
  - Database schema for flags, checks, and risk profiles
  - Kafka integration patterns
  - Dashboard metrics and real-time alerts
  - **External Service Integration** (Section 12)
    - Pluggable external validators for API-based checks
    - Circuit breaker, retry, and caching patterns
    - Support for object detection, face matching, anomaly detection
    - Async processing flow for slow external calls
- **Config Files (All Options):**
  - `FraudRules.json` - All 34 rules with `ruleType` field (`INTERNAL` or `EXTERNAL`)
    - 10 standard internal rules (STD-001 to STD-010)
    - 12 SDCRS-specific internal rules (SDCRS-001 to SDCRS-012)
    - 12 external API-based rules (EXT-001 to EXT-012)
  - `ExternalValidators.json` - External service configuration
  - `RiskScoreConfig.json` - Risk scoring thresholds
  - `ApplicantPenalties.json` - Penalty escalation
- **Config Locations:**
  - Option A: [`04a-ccrs/data/ncr/FRAUD-DETECTION/`](./04a-ccrs/data/ncr/FRAUD-DETECTION/)
  - Option B: [`04b-no-ccrs/data/ncr/FRAUD-DETECTION/`](./04b-no-ccrs/data/ncr/FRAUD-DETECTION/)
  - Option C: [`04c-ccrs-fork/data/ncr/FRAUD-DETECTION/`](./04c-ccrs-fork/data/ncr/FRAUD-DETECTION/)
- **Database Schema:** (Options B & C)
  - [`04b-no-ccrs/db/V2__create_fraud_detection_tables.sql`](./04b-no-ccrs/db/V2__create_fraud_detection_tables.sql)
  - [`04c-ccrs-fork/db/V2__create_fraud_detection_tables.sql`](./04c-ccrs-fork/db/V2__create_fraud_detection_tables.sql)
- **OpenAPI Specification:**
  - [`09-fraud-detection-api-specification.yaml`](./04b-no-ccrs/09-fraud-detection-api-specification.yaml) - Complete API specification

---

## Reference Documents

- **PRD:** [`stray-dog-capture-system-prd.md`](../stray-dog-capture-system-prd.md)
- **CLAUDE.md:** [`CLAUDE.md`](../CLAUDE.md) - Project documentation for AI assistants
- **Reference Implementation:** [`../Citizen-Complaint-Resolution-System/`](../../Citizen-Complaint-Resolution-System/) (PGR/CCRS pattern)

---

## Next Steps

1. **Make architecture decision** (Option A vs Option B vs Option C)
2. Begin implementation based on chosen approach:
   - **Option A:** Configure CCRS/PGR + Zuul gateway filters
   - **Option B:** Deploy `sdcrs-services` from [`04b-no-ccrs/sdcrs-services/`](./04b-no-ccrs/sdcrs-services/)
   - **Option C:** Fork CCRS, apply [`V2__add_sdcrs_columns.sql`](./04c-ccrs-fork/db/V2__add_sdcrs_columns.sql), update entities
3. Set up infrastructure:
   - Kafka topics
   - PostgreSQL schema
   - Elasticsearch indexes
4. Configure MDMS masters
5. Deploy and test workflow transitions
6. Integrate with DIGIT services (User, Workflow, FileStore, Billing)
