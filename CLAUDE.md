# SDCRS - Stray Dog Capture & Reporting System

## Project Overview

This repository contains the design artifacts for the **Stray Dog Capture & Reporting System (SDCRS)**, a DIGIT platform module being developed for a state government in India. The system leverages teachers as a distributed reporting network for stray dog sightings, with payouts tied to successful capture/resolution by Municipal Corporation officers.

### Key Business Rule

**Teachers receive payout via UPI ONLY after Municipal Corporation successfully captures/resolves the reported dog.** No payout is issued for rejected, duplicate, or "unable to locate" outcomes.

---

## Repository Structure

```
stray-dog-capture/
├── CLAUDE.md                         # This file - project guidance
├── stray-dog-capture-system-prd.md   # Product Requirements Document
├── generate_workflow.py              # Python script to generate workflow diagrams
│
├── design-outputs/                   # DIGIT Design Guide deliverables
│   ├── 00-design-checklist.md        # Design checklist tracking
│   ├── 01-process-workflow.md        # Process workflow documentation (original)
│   ├── 01-process-workflow-upi.md    # Process workflow with UPI payout states
│   ├── 02-user-stories.md            # User stories summary
│   ├── 02-user-stories/              # Detailed user stories by role
│   │   ├── teacher/
│   │   ├── verifier/
│   │   ├── mc-officer/
│   │   ├── district-admin/
│   │   ├── state-admin/
│   │   └── system/
│   ├── 03-service-design.md          # Service design (registries, APIs, MDMS)
│   ├── 03-configs/                   # All configuration files (consolidated)
│   │   ├── database/                 # PostgreSQL schemas
│   │   ├── elasticsearch/            # Index templates
│   │   ├── expense/                  # Expense/billing configs
│   │   ├── ifms/                     # JIT/Treasury adapter configs
│   │   ├── indexer/                  # DIGIT indexer configs
│   │   ├── mdms/                     # Master data configs
│   │   │   ├── SDCRS/
│   │   │   ├── DSS/
│   │   │   ├── ACCESSCONTROL-*/
│   │   │   └── FRAUD-DETECTION/
│   │   ├── persister/                # DIGIT persister configs
│   │   ├── upi/                      # UPI payout adapter configs (NEW)
│   │   └── workflow/                 # Workflow BusinessService configs
│   │
│   ├── 04a-ccrs/                     # Approach A: Direct CCRS configuration
│   ├── 04a-ccrs-direct-configuration.md
│   ├── 04b-no-ccrs/                  # Approach B: Custom service (RECOMMENDED)
│   │   ├── sdcrs-services/           # Full Spring Boot implementation
│   │   ├── fraud-detection-service/  # Fraud detection microservice
│   │   ├── upi-payout-adapter/       # UPI Payout Adapter (Razorpay) (NEW)
│   │   ├── sdcrs-integration/        # SDCRS-UPI integration code (NEW)
│   │   ├── configs/
│   │   ├── data/
│   │   └── db/
│   ├── 04b-ccrs-inspired-custom-service.md
│   ├── 04c-ccrs-fork/                # Approach C: Fork PGR services
│   ├── 04c-ccrs-fork-modified.md
│   │
│   ├── 05-sequence-diagrams.md       # Sequence diagrams documentation
│   ├── 05-sequence-diagrams/         # PlantUML sequence diagrams
│   │   ├── SDCRS_Create.puml
│   │   ├── SDCRS_Update.puml
│   │   ├── SDCRS_Search.puml
│   │   ├── SDCRS_Track.puml
│   │   ├── SDCRS_Payout.puml         # Original billing-based payout
│   │   ├── SDCRS_Payout_UPI.puml     # UPI payout sequence (NEW)
│   │   ├── SDCRS_AsyncValidation.puml
│   │   └── SDCRS_AsyncValidationKafka.puml
│   │
│   ├── 06-workflow/                  # Workflow visualizations
│   │   ├── sdcrs-workflow-viz.ts     # XState visualization (original)
│   │   ├── sdcrs-workflow-viz-upi.ts # XState visualization with UPI (NEW)
│   │   ├── sdcrs-workflow-machine.ts
│   │   ├── SDCRS_Swimlane_UPI.puml   # Swimlane diagram (NEW)
│   │   └── SDCRS_StateMachine_UPI.puml # State machine diagram (NEW)
│   │
│   ├── 07-scale-management.md        # Scale and performance design
│   ├── 08-fraud-detection-service.md # Fraud detection design
│   │
│   ├── assets/                       # Generated diagram images
│   │   ├── SDCRS_Swimlane_UPI.png    # Swimlane with 6 lanes (NEW)
│   │   ├── SDCRS_StateMachine_UPI.png # State machine with UPI states (NEW)
│   │   └── SDCRS_Payout_UPI.png      # UPI payout sequence (NEW)
│   │
│   └── dashboard-designs/            # Dashboard mockups
│
└── 01-old/                           # Archived/legacy configuration files
```

---

## Design Methodology

This project follows the **DIGIT Design Guide** methodology for building government services:

| # | Design Output | Status |
|---|---------------|--------|
| 1 | Process Workflow (swimlane diagrams) | ✅ Complete (with UPI) |
| 2 | User Stories (role-based, Gherkin format) | ✅ Complete |
| 3 | Service Design (registries, workflows, MDMS, access control) | ✅ Complete |
| 4 | API Specifications (OpenAPI/YAML) | ✅ Complete |
| 5 | Sequence Diagrams | ✅ Complete (with UPI) |
| 6 | UI Wireframes | ⏳ Pending |
| 7 | Scale Management | ✅ Complete |
| 8 | Fraud Detection | ✅ Complete |

---

## Service Architecture

### Custom Services (3 total)

| Service | Description |
|---------|-------------|
| **SDCRS Service** | Core registry for stray dog reports - includes evidence references, workflow state, assignment, resolution |
| **Fraud Detection Service** | Risk scoring, duplicate detection, penalty management |
| **UPI Payout Adapter** | Razorpay X integration for UPI beneficiary payouts |

### Reused DIGIT Services

| DIGIT Service | Purpose in SDCRS |
|---------------|------------------|
| User Service | Teacher, Verifier, MC Officer authentication |
| Workflow Service | Dog report state management |
| File Store Service | Photo and selfie storage |
| Location Service | GPS validation, tenant boundary check |
| Dashboard Backend (DSS) | Analytics dashboards |
| Notification Service | SMS/Email alerts |
| Persister Service | Async database writes via Kafka |
| Indexer Service | Elasticsearch indexing |
| MDMS Service | Reference data (report types, payout config) |
| Encryption Service | PII data protection |

---

## Workflow States (with UPI Payout)

The dog report lifecycle follows this state machine with async UPI payout:

```
null (Start)
  └── SUBMIT → PENDING_VALIDATION
                  ├── AUTO_VALIDATE_FAIL → AUTO_REJECTED (terminal, no payout)
                  └── AUTO_VALIDATE_PASS → PENDING_VERIFICATION
                                              ├── REJECT → REJECTED (terminal, no payout)
                                              ├── MARK_DUPLICATE → DUPLICATE (terminal, no payout)
                                              └── VERIFY → VERIFIED
                                                            └── ASSIGN → ASSIGNED
                                                                          └── START_FIELD_VISIT → IN_PROGRESS
                                                                                                    ├── MARK_UNABLE_TO_LOCATE → UNABLE_TO_LOCATE (terminal, no payout)
                                                                                                    └── MARK_CAPTURED → CAPTURED
                                                                                                                          └── INITIATE_PAYOUT → PAYOUT_PENDING
                                                                                                                                                   ├── PAYOUT_SUCCESS → RESOLVED (payout complete)
                                                                                                                                                   └── PAYOUT_FAILED → PAYOUT_FAILED
                                                                                                                                                                         ├── RETRY_PAYOUT → PAYOUT_PENDING
                                                                                                                                                                         └── MANUAL_RESOLVE → RESOLVED
```

### Terminal States

| State | Payout? | Description |
|-------|---------|-------------|
| AUTO_REJECTED | No | Failed auto-validation |
| REJECTED | No | Verifier rejection |
| DUPLICATE | No | Already reported |
| UNABLE_TO_LOCATE | No | Dog not found |
| PAYOUT_FAILED | No (pending) | UPI payment failed, can retry |
| RESOLVED | **Yes** (₹500) | Capture confirmed + UPI payout complete |

---

## UPI Payout Integration (NEW)

### Architecture

```
SDCRS Service                      UPI Payout Adapter              Razorpay X
     │                                    │                            │
     │ (CAPTURED)                         │                            │
     │──── Kafka: upi-payout-create ─────▶│                            │
     │                                    │── POST /contacts ─────────▶│
     │                                    │◀─ contact_id ──────────────│
     │                                    │── POST /fund_accounts ────▶│
     │                                    │◀─ fund_account_id ─────────│
     │                                    │── POST /payouts ──────────▶│
     │                                    │◀─ payout_id (queued) ──────│
     │                                    │                            │
     │                                    │              (UPI transfer)│
     │                                    │                            │
     │                                    │◀─ webhook: payout.processed│
     │◀─── Kafka: sdcrs-payout-callback ──│                            │
     │                                    │                            │
     │ (RESOLVED)                         │                            │
```

### Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `upi-payout-create` | SDCRS | UPI Adapter | Trigger payout |
| `upi-payout-status` | Webhook | UPI Adapter | Status processing |
| `upi-payout-persist` | UPI Adapter | Persister | Save payout records |
| `sdcrs-payout-callback` | UPI Adapter | SDCRS | Notify payout result |

### Key Files

| File | Purpose |
|------|---------|
| `04b-no-ccrs/upi-payout-adapter/` | Complete Spring Boot UPI adapter |
| `04b-no-ccrs/sdcrs-integration/` | SDCRS ↔ UPI adapter integration code |
| `03-configs/upi/upi-adapter-config.json` | UPI adapter MDMS config |
| `03-configs/upi/persister-config.json` | UPI payout persister config |
| `03-configs/workflow/BusinessService-with-UPI.json` | Workflow with UPI states |

---

## User Roles

| Role | Code | Description |
|------|------|-------------|
| Teacher | `TEACHER` | Reports stray dog sightings, receives payouts via UPI |
| Verifier | `VERIFIER` | Reviews evidence, approves/rejects submissions |
| MC Officer | `MC_OFFICER` | Conducts field visits, captures dogs |
| MC Supervisor | `MC_SUPERVISOR` | Supervises MC Officers, assigns work |
| District Admin | `DISTRICT_ADMIN` | District-level reporting and analytics |
| State Admin | `STATE_ADMIN` | Statewide program management, payout retry |
| System | `SYSTEM` | Automated operations (validation, payout trigger) |

---

## Key Technical Decisions

1. **Single Registry Pattern**: Only `Dog Report Registry` is custom; everything else uses existing DIGIT services
2. **Evidence via File Store**: Photos stored in DIGIT File Store Service with references in report
3. **UPI Payouts via Custom Adapter**: Real-time UPI payments using Razorpay X (not DIGIT Collection Service)
4. **Multi-tenant Architecture**: Designed for `dj` tenant with configurable boundaries
5. **Duplicate Detection**: Image hashing (pHash) with 90% similarity threshold
6. **Async Payout Processing**: Kafka-based integration with webhook callbacks

---

## MDMS Configuration

### Module: `SDCRS`

| Master | Purpose |
|--------|---------|
| `ReportType.json` | Dog report types (aggressive, injured, pack, standard) |
| `PayoutConfig.json` | Payout amounts, caps, minimum requirements |
| `RejectionReason.json` | Auto and manual rejection reasons |
| `ResolutionType.json` | MC resolution outcomes |

### Module: `FRAUD-DETECTION`

| Master | Purpose |
|--------|---------|
| `FraudRules.json` | Rule definitions with weights |
| `RiskScoreConfig.json` | Risk thresholds and actions |
| `ApplicantPenalties.json` | Penalty escalation config |
| `ExternalValidators.json` | External API integrations |

---

## API Endpoints

### SDCRS Service

```
POST   /sdcrs-services/v1/report/_create     # Submit new report
PUT    /sdcrs-services/v1/report/_update     # Update report (workflow actions)
POST   /sdcrs-services/v1/report/_search     # Search reports
POST   /sdcrs-services/v1/report/_track      # Track report by number
```

### UPI Payout Adapter

```
POST   /payout/v1/_create                    # Create payout (via Kafka preferred)
POST   /payout/v1/_search                    # Search payouts
POST   /payout/v1/_retry                     # Retry failed payout
GET    /payout/v1/{id}                       # Get payout by ID
POST   /webhook/v1/razorpay                  # Razorpay webhook endpoint
```

---

## Development Commands

### Generate Workflow Diagrams

```bash
cd design-outputs

# Generate PNG from PlantUML
plantuml -tpng 06-workflow/SDCRS_Swimlane_UPI.puml -o ../assets
plantuml -tpng 06-workflow/SDCRS_StateMachine_UPI.puml -o ../assets
plantuml -tpng 05-sequence-diagrams/SDCRS_Payout_UPI.puml -o ../assets
```

### XState Visualization

Copy contents of `06-workflow/sdcrs-workflow-viz-upi.ts` to:
**https://stately.ai/viz**

---

## Key Documents to Read

1. **Start here**: `stray-dog-capture-system-prd.md` - Full product requirements
2. **Process flow**: `design-outputs/01-process-workflow-upi.md` - Swimlane workflow with UPI
3. **Technical design**: `design-outputs/03-service-design.md` - Service architecture, MDMS, access control
4. **UPI Integration**: `design-outputs/04b-no-ccrs/sdcrs-integration/INTEGRATION.md` - UPI adapter integration
5. **User stories**: `design-outputs/02-user-stories.md` or drill into `design-outputs/02-user-stories/<role>/`
6. **Fraud detection**: `design-outputs/08-fraud-detection-service.md`

---

## Payout Logic

| Condition | Result |
|-----------|--------|
| MC marks "Captured" | System triggers UPI payout → PAYOUT_PENDING |
| UPI payment successful | ₹500 credited via UPI → RESOLVED |
| UPI payment failed | Can retry (max 3) or manual resolve |
| MC marks "Unable to Locate" | No payout |
| Verifier rejects | No payout |
| System auto-rejects | No payout |
| Marked as duplicate | No payout |

**Monthly cap**: ₹5,000 per teacher
**Maximum daily reports**: 5
**Payout amount**: ₹500 per successful capture

---

## Fraud Prevention

- GPS validation against EXIF data
- Tenant boundary checking via Location Service
- Image hash comparison for duplicates (pHash, 90% threshold)
- 48-hour timestamp validation
- Risk scoring with configurable thresholds
- Progressive penalty system (warning → cooldown → suspension → ban)

---

## Implementation Approaches

| Approach | Location | Description | Recommended? |
|----------|----------|-------------|--------------|
| **A** | `04a-ccrs/` | Configure existing CCRS/PGR service | No |
| **B** | `04b-no-ccrs/` | Build custom SDCRS service | **Yes** |
| **C** | `04c-ccrs-fork/` | Fork and modify PGR service | No |

**Approach B** is recommended because:
- Full control over domain model
- Clean separation from PGR/complaint handling
- Easier to add SDCRS-specific features (fraud detection, UPI payouts)
- No dependency on CCRS release cycles

---

*Document Version: 2.0*
*Last Updated: December 2025*
*Status: Updated with UPI Payout Integration*
