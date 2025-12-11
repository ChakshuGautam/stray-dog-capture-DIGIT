# Solution Architecture

This document describes how SDCRS leverages the DIGIT platform and identifies gaps requiring custom development.

---

## DIGIT Platform: What It Provides

SDCRS reuses the following DIGIT core services:

| DIGIT Service | Purpose in SDCRS |
|---------------|------------------|
| **User Service** | Teacher, Verifier, MC Officer authentication |
| **Role Service** | Role-based access control |
| **MDMS Service** | Reference data (report types, payout rates) |
| **Workflow Service** | Dog report state management |
| **File Store Service** | Photo and selfie storage |
| **Location Service** | GPS validation, tenant boundary check |
| **Notification Service** | SMS/Email alerts |
| **Persister Service** | Async database writes via Kafka |
| **Indexer Service** | Elasticsearch indexing |
| **Encryption Service** | PII data protection |
| **DSS (Dashboard Backend)** | Analytics dashboards |
| **URL Shortening Service** | Public tracking URLs |

---

## Gaps (Custom Development Required)

| Gap | Custom Component | Rationale |
|-----|------------------|-----------|
| **Dog Report Registry** | `sdcrs-services` | Domain-specific entity with first-class columns |
| **Fraud Detection** | `fraud-detection-service` | Configurable rule engine + ML integration |
| **UPI Payouts** | `upi-payout-adapter` | Real-time UPI via Razorpay X (not treasury) |
| **Duplicate Detection** | Embedded in SDCRS | Perceptual image hashing (pHash) |

---

## Architectural Options

Three implementation approaches evaluated:

| Option | Approach | Dev Time | Flexibility |
|--------|----------|----------|-------------|
| **A** | Use CCRS/PGR directly (config only) | 2-3 weeks | Limited |
| **B** | Custom SDCRS service on DIGIT | 6-8 weeks | Full |
| **C** | Fork CCRS and modify schema | 4-5 weeks | High |

### Decision Criteria

| Factor | Favor A | Favor B | Favor C |
|--------|---------|---------|---------|
| Timeline | Tight | Flexible | Moderate |
| Team expertise | Limited DIGIT | Strong DIGIT | Moderate |
| Query complexity | Simple | Complex analytics | Complex |
| Future extensions | Moderate | Significant | Moderate |
| Upstream updates | Automatic | N/A | Manual merge |

### Recommendation: Option B (Custom Service)

**Rationale:**
- Full control over domain model
- Clean separation from PGR/complaint handling
- Easier to add SDCRS-specific features
- No dependency on CCRS release cycles
- Better query performance with first-class columns

---

## Option Documentation

| Option | Documentation | Config Folder |
|--------|---------------|---------------|
| A | [04a-ccrs-direct-configuration.md](./design-outputs/04a-ccrs-direct-configuration.md) | [04a-ccrs/](./design-outputs/04a-ccrs/) |
| B | [04b-ccrs-inspired-custom-service.md](./design-outputs/04b-ccrs-inspired-custom-service.md) | [04b-no-ccrs/](./design-outputs/04b-no-ccrs/) |
| C | [04c-ccrs-fork-modified.md](./design-outputs/04c-ccrs-fork-modified.md) | [04c-ccrs-fork/](./design-outputs/04c-ccrs-fork/) |

---

## Service Design Details

For complete service design including:
- Activity to Service Mapping
- Registry Definitions
- Workflow Configuration
- MDMS Specifications
- Access Control Mappings
- API Endpoints

See: [Service Design Document](./design-outputs/03-service-design.md)

---

## Sequence Diagrams

| Flow | Document |
|------|----------|
| Report Creation | [SDCRS_Create.puml](./design-outputs/05-sequence-diagrams/SDCRS_Create.puml) |
| Workflow Update | [SDCRS_Update.puml](./design-outputs/05-sequence-diagrams/SDCRS_Update.puml) |
| Search | [SDCRS_Search.puml](./design-outputs/05-sequence-diagrams/SDCRS_Search.puml) |
| Public Tracking | [SDCRS_Track.puml](./design-outputs/05-sequence-diagrams/SDCRS_Track.puml) |
| UPI Payout | [SDCRS_Payout_UPI.puml](./design-outputs/05-sequence-diagrams/SDCRS_Payout_UPI.puml) |
| Async Validation | [SDCRS_AsyncValidation.puml](./design-outputs/05-sequence-diagrams/SDCRS_AsyncValidation.puml) |

Full index: [Sequence Diagrams](./design-outputs/05-sequence-diagrams.md)

---

## Configuration Files

All configurations consolidated in [03-configs/](./design-outputs/03-configs/):

| Folder | Contents |
|--------|----------|
| `database/` | PostgreSQL schema |
| `elasticsearch/` | Index templates |
| `mdms/` | SDCRS, Fraud Detection, Access Control, DSS |
| `persister/` | Kafka → PostgreSQL mapping |
| `indexer/` | Kafka → Elasticsearch mapping |
| `workflow/` | BusinessService.json |
| `upi/` | UPI Payout adapter config |

---

## API Specifications

- [SDCRS API (OpenAPI)](./design-outputs/04b-no-ccrs/06-api-specification.yaml)
- [Fraud Detection API (OpenAPI)](./design-outputs/04b-no-ccrs/09-fraud-detection-api-specification.yaml)
