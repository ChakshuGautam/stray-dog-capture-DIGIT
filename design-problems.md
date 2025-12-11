# Initial Design Problems & Solutions

This document addresses the key technical challenges identified during SDCRS design and their solutions.

---

## Problem 1: Public Sharable Permalinks

**Challenge:** Reports need public tracking URLs that work without authentication while protecting PII.

### DIGIT Platform Mechanism

DIGIT's Zuul API Gateway provides two whitelist mechanisms for public access (verified from [AuthPreCheckFilter.java](https://github.com/egovernments/DIGIT-OSS/blob/master/core-services/zuul/src/main/java/org/egov/filters/pre/AuthPreCheckFilter.java)):

**1. Open Endpoints Whitelist** - Complete authentication bypass:
```java
if (openEndpointsWhitelist.contains(getRequestURI())) {
    setShouldDoAuth(false);  // Skip auth entirely
    return null;
}
```

**2. Mixed Mode Endpoints** - Anonymous access when no token provided:
```java
if (authToken == null && mixedModeEndpointsWhitelist.contains(getRequestURI())) {
    setShouldDoAuth(false);
    setAnonymousUser();  // Assign system user for anonymous access
}
```

### The Challenges

**1. Whitelist is infrastructure config:**
- Defined in `application.properties` (not MDMS)
- Requires DevOps change + gateway restart for each new public endpoint

**2. Internal endpoints expose too much data:**
- Standard `/_search` returns full report object
- Would expose: reporter PII, officer details, internal audit trail, fraud scores
- Simply whitelisting an existing endpoint is a **data leak**

### Solution Options

Two approaches to expose public tracking:

#### Option A: Custom Service Endpoint (Recommended for Option B architecture)

Build a dedicated `/_track` endpoint in the SDCRS service that returns sanitized data.

| Component | Implementation |
|-----------|----------------|
| **Custom Track API** | New `/_track` endpoint in SDCRS service |
| **Sanitized Response** | Returns only: status, timestamps, location (city-level), resolution type |
| **Gateway Whitelist** | Add to `egov.open.endpoints.whitelist` property |

#### Option B: Zuul Gateway Filters (For CCRS/PGR-based architecture)

Use Zuul pre/post filters to transform requests and sanitize responses at the gateway level.

| Component | Implementation |
|-----------|----------------|
| **Route Aliasing** | Map `/ccrs/v1/report/_track` → PGR's `/_search` |
| **Auth Bypass Filter** | `CCRSAuthBypassFilter` sets `skipAuth=true` for `/_track` |
| **Request Transform Filter** | `CCRSTrackRequestFilter` converts tracking request to PGR search format |
| **Response Sanitization Filter** | `CCRSTrackResponseFilter` strips PII from PGR response |

```
┌─────────────────────────────────────────────────────────────┐
│                        Zuul Gateway                          │
│  ┌────────────┐   ┌────────────┐   ┌────────────────────┐  │
│  │ AuthBypass │ → │  Request   │ → │ Route to PGR/_search│  │
│  │  Filter    │   │ Transform  │   │                     │  │
│  └────────────┘   └────────────┘   └────────────────────┘  │
│                                              │               │
│                                              ▼               │
│                                    ┌────────────────────┐   │
│                                    │ Response Sanitize  │   │
│                                    │ (strip PII)        │   │
│                                    └────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

**Full Implementation:** [Zuul Gateway Configuration](./design-outputs/04a-ccrs/07-zuul-gateway-configuration.md)

### Public vs Internal Response Comparison

| Field | `/_search` (Internal) | `/_track` (Public) |
|-------|----------------------|-------------------|
| Report Number | ✅ | ✅ |
| Status | ✅ | ✅ |
| Created/Updated Time | ✅ | ✅ |
| Location (GPS) | ✅ | ❌ (city only) |
| Dog Photo | ✅ | ✅ |
| Reporter Name/Phone | ✅ | ❌ |
| Reporter UPI ID | ✅ | ❌ |
| Verifier Details | ✅ | ❌ |
| MC Officer Details | ✅ | ❌ |
| Fraud Score | ✅ | ❌ |
| Internal Notes | ✅ | ❌ |
| Audit Trail | ✅ | ❌ |

### Gateway Configuration Required (Both Options)

```properties
# Add to application.properties or environment config
egov.open.endpoints.whitelist=/user/oauth/token,/user-otp/v1/_send,...,/sdcrs-services/v1/report/_track
```

**Documentation:** [Service Design - Section 10](./design-outputs/03-service-design.md#10-public-tracking-api)

---

## Problem 2: Scale

**Challenge:** Handle 400K potential reporters submitting up to 5 reports/day with 2 photos each.

### Target Scale (10% active teachers)

| Metric | Value |
|--------|-------|
| Peak Reports/Day | 200,000 |
| Peak RPS | 7.4/sec |
| Photo Uploads/Day | 400,000 |
| Monthly Storage | 4.8TB |

### Identified Hotspots

| # | Hotspot | Key Metric | Mitigation |
|---|---------|------------|------------|
| 1 | Photo Upload & Processing | 14.8 uploads/sec | [Async processing, pre-signed URLs, CDN](./design-outputs/07-scale-management.md#2-hotspot-1-photo-upload--processing) |
| 2 | Duplicate Detection | 18M hash comparisons | [Bloom filter + Redis cache](./design-outputs/07-scale-management.md#3-hotspot-2-duplicate-detection) |
| 3 | Verification Queue | 1,400/verifier/day | [Auto-verify 40% low-risk](./design-outputs/07-scale-management.md#4-hotspot-3-verification-queue-bottleneck) |
| 4 | Kafka Consumer Lag | 60.8 msg/sec | [Partition scaling, consumer auto-scaling](./design-outputs/07-scale-management.md#5-hotspot-4-kafka-consumer-lag) |
| 5 | Database Write Contention | 29.4 writes/sec | [PgBouncer, read replicas](./design-outputs/07-scale-management.md#6-hotspot-5-database-write-contention) |
| 6 | Elasticsearch Indexing | 7.4 docs/sec | [Bulk indexing, refresh tuning](./design-outputs/07-scale-management.md#7-hotspot-6-elasticsearch-indexing-lag) |
| 7 | Public Tracking API Abuse | 1M+ requests/day | [Rate limiting, CDN caching](./design-outputs/07-scale-management.md#8-hotspot-7-public-tracking-api-abuse) |
| 8 | Payout Processing Spikes | 50K payouts/day | [Batch processing, daily windows](./design-outputs/07-scale-management.md#9-hotspot-8-payout-processing-spikes) |
| 9 | Notification Gateway | 780K SMS/day | [Push priority, bulk provider](./design-outputs/07-scale-management.md#10-hotspot-9-notification-gateway-limits) |
| 10 | Auto-Validation | 256ms/validation | [Async pipeline, geo-cache](./design-outputs/07-scale-management.md#11-hotspot-10-auto-validation-bottleneck) |

### Infrastructure Summary

| Component | Sizing for 200K/day |
|-----------|---------------------|
| SDCRS Pods | 5 pods × 2 vCPU, 4GB RAM |
| PostgreSQL | db.r5.xlarge + 2 read replicas |
| Elasticsearch | 3 nodes × 4 vCPU, 8GB heap |
| Redis | 2GB cluster |
| Kafka | 3 brokers, 36 partitions |
| Object Storage | 5TB/month, CDN-enabled |
| **Monthly Cost (est)** | **$3,000-5,000** |

**Full Documentation:** [Scale Management Guide](./design-outputs/07-scale-management.md) - includes detailed calculations, mitigation code, and capacity planning for Y1-Y5.

---

## Problem 3: Payment to Users

**Challenge:** Teachers need fast, reliable payouts only after successful capture. DIGIT's existing payment infrastructure (Mukta/IFMS/JIT) is not available in NCR states.

### Platform Limitation

DIGIT provides payment modules like **Mukta** (wage disbursement) and **IFMS adapters** for treasury integration, but these require:
- State treasury JIT (Just-In-Time) API availability
- Integration with state-specific IFMS systems

**NCR Assessment:** Delhi, Haryana, UP, and Rajasthan lack public JIT APIs or use proprietary treasury systems (e.g., Delhi CFMS). See [PRD Appendix 14.4](./prd.md#144-ncr-treasuryifms-status) for detailed state-by-state analysis.

### Solution: UPI-Based Payouts

| Component | Implementation |
|-----------|----------------|
| **UPI Payout Adapter** | Custom Razorpay X integration (bypasses treasury) |
| **Async Processing** | Kafka-based workflow with webhooks |
| **Retry Mechanism** | Max 3 retries with manual override |
| **Monthly Cap** | ₹5,000 per teacher, system-enforced |

### Why UPI?

| Advantage | Detail |
|-----------|--------|
| Universal coverage | All teachers have mobile-linked bank accounts |
| Faster deployment | Standardized APIs, well-documented |
| No treasury dependency | Works regardless of state IFMS status |
| Audit trail | UPI provides UTR for reconciliation |

**Documentation:**
- [PRD: NCR Treasury/IFMS Status](./prd.md#144-ncr-treasuryifms-status)
- [UPI Payout Sequence](./design-outputs/05-sequence-diagrams/SDCRS_Payout_UPI.puml)
- [UPI Adapter Config](./design-outputs/03-configs/upi/)

---

## Problem 4: External SSO - HRMS Integration

**Challenge:** Teachers already exist in State HRMS; avoid duplicate identity management and leverage existing UPI payment details.

### Solution Overview

| Component | Implementation |
|-----------|----------------|
| **OAuth2 SSO** | Teachers login via State HRMS credentials (Employee ID + password/OTP) |
| **PKCE Flow** | Secure mobile app OAuth2 with code challenge |
| **Auto-Provisioning** | DIGIT user created/updated on first login from HRMS claims |
| **UPI Sync** | Payment details sourced from HRMS salary records |
| **Token Management** | 7-day access token with silent refresh |

### Data Flow

```
State HRMS (Source of Truth)
    │
    │ SSO Login
    ▼
DIGIT egov-user (Synced Copy)
    │
    │ userInfo.additionalDetails.upiId
    ▼
UPI Payout Adapter → Razorpay → Teacher's UPI
```

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| HRMS is source of truth | Teachers already registered; UPI verified for salary |
| No separate SDCRS registration | Reduces friction, prevents duplicate accounts |
| UPI from HRMS | Already validated for salary disbursement |
| District → Tenant mapping | HRMS district code maps to DIGIT tenantId |

**Full Documentation:** [HRMS-SSO-DIGIT Integration](./design-outputs/HRMS-SSO-DIGIT-integration.md) - includes OAuth2 sequence diagrams, token flows, MDMS configuration, and implementation checklist.

---

## Problem 5: Defining & Detecting Fraud

**Challenge:** Prevent gaming of the payout system through duplicate/fake submissions.

### Solution

| Mechanism | Implementation |
|-----------|----------------|
| **GPS Validation** | EXIF metadata vs submitted location |
| **Boundary Check** | Location Service tenant validation |
| **Image Hashing** | pHash with 90% similarity threshold |
| **Timestamp Check** | 48-hour EXIF timestamp validation |
| **Risk Scoring** | Configurable thresholds |
| **Penalties** | Progressive: warning → cooldown → suspension → ban |

**Documentation:** [Fraud Detection Service](./design-outputs/08-fraud-detection-service.md)

---

## Summary

| Problem | Solution Document |
|---------|-------------------|
| Public Permalinks | [Service Design §10](./design-outputs/03-service-design.md#10-public-tracking-api) |
| Scale | [Scale Management](./design-outputs/07-scale-management.md) |
| Payments | [UPI Adapter](./design-outputs/03-configs/upi/) |
| HRMS SSO | [Service Design §2](./design-outputs/03-service-design.md) |
| Fraud | [Fraud Detection](./design-outputs/08-fraud-detection-service.md) |
