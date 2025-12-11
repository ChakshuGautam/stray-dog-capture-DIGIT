# Option B: Custom Service (CCRS-Inspired)

This folder contains configuration files for implementing SDCRS as a **custom service** inspired by CCRS/PGR patterns but with full domain control.

## Approach

Build a dedicated `sdcrs-services` module with:
- Custom database schema with first-class columns
- Domain-specific API endpoints
- Full control over data model and queries
- Follows DIGIT architectural patterns

## Folder Structure

```
04b-no-ccrs/
├── data/                           # MDMS data (follows egov-mdms-data structure)
│   └── ncr/                        # Tenant: NCR (National Capital Region)
│       ├── SDCRS/                  # SDCRS module masters
│       │   ├── ServiceType.json
│       │   ├── PayoutConfig.json
│       │   ├── RejectionReason.json
│       │   └── ResolutionType.json
│       ├── ACCESSCONTROL-ROLES/
│       │   └── roles.json
│       ├── ACCESSCONTROL-ACTIONS-TEST/
│       │   └── actions-test.json
│       ├── ACCESSCONTROL-ROLEACTIONS/
│       │   └── roleactions.json
│       └── Workflow/
│           └── SdcrsWorkflowConfig.json
├── configs/                        # Service configurations
│   ├── sdcrs-persister.yml
│   ├── sdcrs-indexer.yml
│   ├── kafka-topics.json
│   └── notification-templates.json
├── db/                             # Database migrations
│   └── V1__create_sdcrs_tables.sql
├── sdcrs-services/                 # Java service source code
│   └── src/main/...
├── 06-api-specification.yaml       # OpenAPI spec
└── README.md                       # This file
```

## MDMS Configuration

Following the [egov-mdms-data](https://github.com/egovernments/egov-mdms-data) structure:

| Module | File | Purpose |
|--------|------|---------|
| SDCRS | `ServiceType.json` | Dog report types (aggressive, injured, pack, standard) |
| SDCRS | `PayoutConfig.json` | Payout amounts, caps, requirements |
| SDCRS | `RejectionReason.json` | Auto and manual rejection reasons |
| SDCRS | `ResolutionType.json` | MC resolution outcomes |
| ACCESSCONTROL-ROLES | `roles.json` | SDCRS user roles |
| ACCESSCONTROL-ACTIONS-TEST | `actions-test.json` | API action definitions |
| ACCESSCONTROL-ROLEACTIONS | `roleactions.json` | Role-to-action mappings |
| Workflow | `SdcrsWorkflowConfig.json` | Workflow states and transitions |

## Infrastructure Configs

| File | Purpose | Deploy To |
|------|---------|-----------|
| `sdcrs-persister.yml` | Async DB write config | Persister Service |
| `sdcrs-indexer.yml` | Elasticsearch indexing | Indexer Service |
| `kafka-topics.json` | Kafka topic definitions | Kafka Admin |
| `notification-templates.json` | SMS/Email templates | Notification Service |

## API Endpoints

Custom domain-specific endpoints:

```
POST /sdcrs-services/v1/report/_create    # Submit report
POST /sdcrs-services/v1/report/_update    # Update report
POST /sdcrs-services/v1/report/_search    # Search reports
POST /sdcrs-services/v1/report/_count     # Count reports
GET  /sdcrs-services/v1/report/_track     # Public tracking (no auth)
```

## Database Schema

Key tables:
- `eg_sdcrs_report` - Main report table with first-class columns
- `eg_sdcrs_report_audit` - State change history

First-class columns enable:
- Efficient SQL queries on dog details, location, status
- Geo-spatial queries with PostGIS
- Fast filtering by assigned officer, school, district

## Deployment

1. Load MDMS data from `data/ncr/` to egov-mdms-data repository
2. Apply configs from `configs/` to respective services
3. Run DB migration from `db/`
4. Deploy `sdcrs-services` Java application

## Pros

- Full control over data model
- Optimized queries with indexed columns
- Clean domain-specific API
- Easier schema evolution via migrations

## Cons

- More development effort (~6-8 weeks)
- Full service implementation required
- Custom UI needed
- Additional maintenance burden

---

*See [`04b-ccrs-inspired-custom-service.md`](../04b-ccrs-inspired-custom-service.md) for full documentation*
