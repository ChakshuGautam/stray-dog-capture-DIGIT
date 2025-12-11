# Option C: CCRS Fork with Modified Data Model

This folder contains configuration files for implementing SDCRS by **forking CCRS/PGR and modifying its data model** to add first-class columns for dog-specific data.

## Approach

Fork the existing PGR (Public Grievance Redressal) / CCRS (Citizen Complaint Resolution System) service and:
- Add database migration for SDCRS-specific columns
- Extend entity models with new fields
- Keep existing PGR API structure
- Add custom validation and payout services

## Folder Structure

```
04c-ccrs-fork/
├── data/                           # MDMS data (follows egov-mdms-data structure)
│   └── ncr/                        # Tenant: NCR (National Capital Region)
│       ├── SDCRS/                  # SDCRS module masters
│       │   ├── ServiceType.json
│       │   ├── PayoutConfig.json
│       │   ├── RejectionReason.json
│       │   └── ResolutionType.json
│       ├── ACCESSCONTROL-ROLES/
│       │   └── roles.json
│       └── Workflow/
│           └── SdcrsWorkflowConfig.json
├── db/                             # Database migrations
│   └── V2__add_sdcrs_columns.sql
├── configs/                        # Modified service configurations
│   ├── pgr-services-persister-modified.yml
│   ├── pgr-services-indexer-modified.yml
│   └── notification-templates.json
└── README.md                       # This file
```

## Key Differences from Options A & B

| Aspect | Option A | Option B | Option C |
|--------|----------|----------|----------|
| **Base** | Use CCRS as-is | Build new service | Fork CCRS |
| **Schema** | JSONB only | Custom tables | Modified PGR table |
| **API** | PGR endpoints | Custom endpoints | PGR endpoints |
| **Code** | Config only | Full implementation | Schema + entities |

## Database Migration

The key change is adding first-class columns to the existing `eg_pgr_service_v2` table:

```sql
-- Dog details
ALTER TABLE eg_pgr_service_v2 ADD COLUMN dog_count INTEGER;
ALTER TABLE eg_pgr_service_v2 ADD COLUMN is_aggressive BOOLEAN;

-- Evidence
ALTER TABLE eg_pgr_service_v2 ADD COLUMN photo_file_store_id VARCHAR(64);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN image_hash VARCHAR(256);

-- Payout
ALTER TABLE eg_pgr_service_v2 ADD COLUMN payout_eligible BOOLEAN;
ALTER TABLE eg_pgr_service_v2 ADD COLUMN payout_amount DECIMAL(10, 2);
ALTER TABLE eg_pgr_service_v2 ADD COLUMN payout_status VARCHAR(32);
```

See [`db/V2__add_sdcrs_columns.sql`](./db/V2__add_sdcrs_columns.sql) for full migration.

## Fork & Merge Strategy

```bash
# Initial setup
git clone https://github.com/egovernments/DIGIT-Works.git sdcrs-fork
git checkout -b sdcrs-customization

# Periodic sync with upstream
git fetch upstream
git merge upstream/master
# Resolve conflicts in modified files
```

## Pros

- Inherits battle-tested PGR code
- First-class columns for efficient queries
- Faster than building from scratch
- Familiar PGR API structure

## Cons

- Must manage merge conflicts with upstream
- Fork maintenance overhead
- Testing burden for modified code paths

---

*See [`04c-ccrs-fork-modified.md`](../04c-ccrs-fork-modified.md) for full documentation*
