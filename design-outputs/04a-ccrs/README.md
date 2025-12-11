# Option A: CCRS Direct Configuration

This folder contains configuration files for implementing SDCRS by **using CCRS/PGR directly** with configuration-only changes.

## Approach

Use the existing PGR (Public Grievance Redressal) / CCRS (Citizen Complaint Resolution System) service with:
- Custom ServiceDefs for stray dog report types
- Custom SDCRS workflow
- Dog-specific data stored in `additionalDetail` JSONB

## Folder Structure

```
04a-ccrs/
├── data/                           # MDMS data (follows egov-mdms-data structure)
│   └── ncr/                        # Tenant: NCR (National Capital Region)
│       ├── RAINMAKER-PGR/          # PGR module masters
│       │   └── ServiceDefs.json
│       ├── ACCESSCONTROL-ROLES/
│       │   └── roles.json
│       └── Workflow/
│           └── SdcrsWorkflowConfig.json
├── configs/                        # Service configurations and reference docs
│   ├── notification-templates.json
│   ├── role-mapping.json
│   └── additionalDetail-schema.json
├── [07-zuul-gateway-configuration.md](./07-zuul-gateway-configuration.md)
└── README.md                       # This file
```

## MDMS Configuration

Following the [egov-mdms-data](https://github.com/egovernments/egov-mdms-data) structure:

| Module | File | Purpose |
|--------|------|---------|
| RAINMAKER-PGR | `ServiceDefs.json` | Stray dog complaint types (aggressive, injured, pack, standard) |
| ACCESSCONTROL-ROLES | `roles.json` | SDCRS user roles |
| Workflow | `SdcrsWorkflowConfig.json` | Workflow states and transitions |

## Service Configurations

| File | Purpose | Notes |
|------|---------|-------|
| `notification-templates.json` | SMS/Email templates | Deploy to Notification Service |
| `role-mapping.json` | SDCRS ↔ PGR role mapping | Reference document |
| `additionalDetail-schema.json` | JSONB schema for dog data | Reference for validation |

## Role Mapping (SDCRS to PGR)

Since this option uses PGR directly, SDCRS roles map to existing PGR roles:

| SDCRS Role | PGR Role | Description |
|------------|----------|-------------|
| TEACHER | CITIZEN | Teachers report as citizens |
| VERIFIER | GRO | Verifiers use GRO permissions |
| MC_OFFICER | PGR_LME | MC Officers are field workers |
| MC_SUPERVISOR | GRO | Supervisors have GRO-level access |
| DISTRICT_ADMIN | PGR_VIEWER | Read-only access |
| STATE_ADMIN | PGR_VIEWER | Read-only access |

## API Endpoints

Uses existing PGR endpoints:

```
POST /pgr-services/v2/request/_create   # Submit report
POST /pgr-services/v2/request/_update   # Update/transition
POST /pgr-services/v2/request/_search   # Search reports
POST /pgr-services/v2/request/_count    # Count reports
```

## additionalDetail Schema

Dog-specific data is stored in PGR's `additionalDetail` JSONB field:

```json
{
  "dogDetails": {
    "description": "Brown dog near school gate",
    "count": 1,
    "isAggressive": true,
    "breed": "Mixed",
    "estimatedAge": "Adult"
  },
  "evidence": {
    "photoFileStoreId": "uuid",
    "selfieFileStoreId": "uuid",
    "imageHash": "phash-value"
  },
  "validation": {
    "gpsValid": true,
    "boundaryValid": true,
    "autoValidationResult": "PASS"
  },
  "payout": {
    "eligible": true,
    "amount": 500,
    "currency": "INR"
  }
}
```

## Deployment

1. Load MDMS data from `data/ncr/` to egov-mdms-data repository
2. Load workflow configuration via workflow service bootstrap
3. Configure notification templates in Notification Service
4. Build minimal custom services for:
   - Auto-validation (GPS, boundary, timestamp, duplicates)
   - Payout trigger (Collection Service integration)

## Pros

- Faster implementation
- Uses battle-tested PGR code
- Lower maintenance burden
- Automatic platform updates

## Cons

- Less flexibility in data model
- JSONB queries may be slower
- Still requires custom validation/payout services
- UI customization needed for dog-specific fields

---

*See [`04a-ccrs-direct-configuration.md`](../04a-ccrs-direct-configuration.md) for full documentation*
