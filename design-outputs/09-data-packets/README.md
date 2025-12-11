# SDCRS Data Packets - Complete Flow Documentation

This folder contains JSON examples of every data packet exchanged in the **Stray Dog Capture & Reporting System (SDCRS)** - from teacher login through payment completion.

## Purpose

These data packets serve as:
1. **API Contract Documentation** - Exact request/response structures for each interaction
2. **Integration Guide** - Clear sender/receiver for each packet
3. **Testing Reference** - Sample data for integration testing
4. **Onboarding Resource** - Understand the complete flow at any stage

## Packet Structure

Each JSON file contains:

```json
{
  "_meta": {
    "stage": "Folder name (e.g., 02-report-submission)",
    "step": "Sequential number within stage",
    "description": "What this packet represents",
    "sender": "Component sending the request",
    "receiver": "Component receiving the request",
    "trigger": "What triggers this packet",
    "previousPacket": "Link to previous packet file",
    "nextPacket": "Link to next packet file",
    "verified": true/false,
    "source": "Source file if verified",
    "assumption": "Description if not verified"
  },
  "request": { /* API request structure */ },
  "response": { /* API response structure */ }
}
```

---

## Complete Flow Index

### 01-teacher-login (SSO Authentication)
| # | File | Description | Verified |
|---|------|-------------|----------|
| 1 | [01-sso-auth-url-request.json](01-teacher-login/01-sso-auth-url-request.json) | App requests HRMS SSO auth URL | ⚠️ Assumed |
| 2 | [02-sso-auth-url-response.json](01-teacher-login/02-sso-auth-url-response.json) | SDCRS returns authorization URL with PKCE | ⚠️ Assumed |
| 3 | [03-hrms-login-page.json](01-teacher-login/03-hrms-login-page.json) | User redirected to HRMS login | ⚠️ Assumed |
| 4 | [04-hrms-user-credentials.json](01-teacher-login/04-hrms-user-credentials.json) | User enters HRMS credentials | ⚠️ Assumed |
| 5 | [05-hrms-auth-response.json](01-teacher-login/05-hrms-auth-response.json) | HRMS validates and redirects with code | ⚠️ Assumed |
| 6 | [06-token-exchange-request.json](01-teacher-login/06-token-exchange-request.json) | App exchanges code for tokens | ⚠️ Assumed |
| 7 | [07-token-exchange-response.json](01-teacher-login/07-token-exchange-response.json) | HRMS returns access/refresh tokens | ⚠️ Assumed |
| 8 | [08-user-info-request.json](01-teacher-login/08-user-info-request.json) | App fetches user profile from HRMS | ⚠️ Assumed |
| 9 | [09-user-info-response.json](01-teacher-login/09-user-info-response.json) | HRMS returns teacher profile | ⚠️ Assumed |
| 10 | [10-digit-user-check.json](01-teacher-login/10-digit-user-check.json) | Check if user exists in DIGIT | ✅ Verified |
| 11 | [11-digit-user-create.json](01-teacher-login/11-digit-user-create.json) | Create/update user in DIGIT | ✅ Verified |
| 12 | [12-digit-token-generation.json](01-teacher-login/12-digit-token-generation.json) | Generate DIGIT JWT token | ✅ Verified |
| 13 | [13-login-complete-app-state.json](01-teacher-login/13-login-complete-app-state.json) | Final app state after login | ⚠️ Assumed |

### 02-report-submission (Creating a Report)
| # | File | Description | Verified |
|---|------|-------------|----------|
| 1 | [01-photo-capture.json](02-report-submission/01-photo-capture.json) | Camera capture with EXIF data | ⚠️ Assumed |
| 2 | [02-selfie-capture.json](02-report-submission/02-selfie-capture.json) | Teacher selfie for verification | ⚠️ Assumed |
| 3 | [03-file-upload-photo.json](02-report-submission/03-file-upload-photo.json) | Upload photo to File Store | ✅ Verified |
| 4 | [04-file-upload-selfie.json](02-report-submission/04-file-upload-selfie.json) | Upload selfie to File Store | ✅ Verified |
| 5 | [05-report-create-request.json](02-report-submission/05-report-create-request.json) | Create report API call | ✅ Verified |
| 6 | [06-report-create-response.json](02-report-submission/06-report-create-response.json) | Report created response | ✅ Verified |
| 7 | [07-kafka-persister-message.json](02-report-submission/07-kafka-persister-message.json) | Kafka message to persister | ✅ Verified |
| 8 | [08-workflow-create.json](02-report-submission/08-workflow-create.json) | Workflow state creation | ✅ Verified |
| 9 | [09-kafka-indexer-message.json](02-report-submission/09-kafka-indexer-message.json) | Kafka message to indexer | ✅ Verified |
| 10 | [10-kafka-notification-message.json](02-report-submission/10-kafka-notification-message.json) | Kafka notification trigger | ⚠️ Assumed |
| 11 | [11-sms-notification.json](02-report-submission/11-sms-notification.json) | SMS sent to teacher | ⚠️ Assumed |

### 03-auto-validation (System Validation)
| # | File | Description | Verified |
|---|------|-------------|----------|
| 1 | [01-validation-trigger.json](03-auto-validation/01-validation-trigger.json) | Validation trigger event | ✅ Verified |
| 2 | [02-gps-validation.json](03-auto-validation/02-gps-validation.json) | GPS vs EXIF coordinate check | ✅ Verified |
| 3 | [03-timestamp-validation.json](03-auto-validation/03-timestamp-validation.json) | Photo age validation (48h max) | ✅ Verified |
| 4 | [04-boundary-validation.json](03-auto-validation/04-boundary-validation.json) | Location within tenant boundary | ✅ Verified |
| 5 | [05-duplicate-check.json](03-auto-validation/05-duplicate-check.json) | Image hash comparison (pHash) | ✅ Verified |
| 6 | [06-fraud-score-request.json](03-auto-validation/06-fraud-score-request.json) | Fraud evaluation request with SpEL context | ✅ Verified |
| 7 | [07-fraud-score-response.json](03-auto-validation/07-fraud-score-response.json) | Fraud response with SpEL rule results | ✅ Verified |
| 8 | [08-validation-result-pass.json](03-auto-validation/08-validation-result-pass.json) | All checks passed | ✅ Verified |
| 9 | [09-validation-result-fail.json](03-auto-validation/09-validation-result-fail.json) | Auto-rejection on failure | ✅ Verified |

### 04-verification (Manual Review)
| # | File | Description | Verified |
|---|------|-------------|----------|
| 1 | [01-verifier-inbox.json](04-verification/01-verifier-inbox.json) | Verifier views pending reports | ✅ Verified |
| 2 | [02-report-detail-view.json](04-verification/02-report-detail-view.json) | Verifier opens report details | ✅ Verified |
| 3 | [03-verify-action.json](04-verification/03-verify-action.json) | Verifier approves report | ✅ Verified |
| 4 | [04-reject-action.json](04-verification/04-reject-action.json) | Verifier rejects report | ✅ Verified |
| 5 | [05-mark-duplicate-action.json](04-verification/05-mark-duplicate-action.json) | Verifier marks as duplicate | ✅ Verified |
| 6 | [06-rejection-notification.json](04-verification/06-rejection-notification.json) | SMS to teacher on rejection | ⚠️ Assumed |

### 05-assignment (Supervisor Assignment)
| # | File | Description | Verified |
|---|------|-------------|----------|
| 1 | [01-supervisor-inbox.json](05-assignment/01-supervisor-inbox.json) | Supervisor views verified reports | ✅ Verified |
| 2 | [02-available-officers.json](05-assignment/02-available-officers.json) | Fetch MC Officers for assignment | ✅ Verified |
| 3 | [03-assign-action.json](05-assignment/03-assign-action.json) | Assign report to officer | ✅ Verified |
| 4 | [04-officer-notification.json](05-assignment/04-officer-notification.json) | SMS to assigned officer | ⚠️ Assumed |

### 06-field-visit (MC Officer Field Work)
| # | File | Description | Verified |
|---|------|-------------|----------|
| 1 | [01-officer-app-inbox.json](06-field-visit/01-officer-app-inbox.json) | Officer views assigned reports | ✅ Verified |
| 2 | [02-report-detail-with-location.json](06-field-visit/02-report-detail-with-location.json) | Report with map/directions | ✅ Verified |
| 3 | [03-start-field-visit.json](06-field-visit/03-start-field-visit.json) | Officer starts field visit | ✅ Verified |
| 4 | [04-field-visit-in-progress.json](06-field-visit/04-field-visit-in-progress.json) | App state during visit | ⚠️ Assumed |
| 5 | [05-mark-captured.json](06-field-visit/05-mark-captured.json) | Officer marks dog captured | ✅ Verified |
| 6 | [06-mark-unable-to-locate.json](06-field-visit/06-mark-unable-to-locate.json) | Officer cannot find dog | ✅ Verified |
| 7 | [07-unable-to-locate-notification.json](06-field-visit/07-unable-to-locate-notification.json) | SMS to teacher (no payout) | ⚠️ Assumed |

### 07-payout-initiation (Triggering Payment)
| # | File | Description | Verified |
|---|------|-------------|----------|
| 1 | [01-payout-trigger.json](07-payout-initiation/01-payout-trigger.json) | Kafka message to UPI Adapter | ✅ Verified |
| 2 | [02-initiate-payout-workflow.json](07-payout-initiation/02-initiate-payout-workflow.json) | Workflow → PAYOUT_PENDING | ✅ Verified |
| 3 | [03-payout-pending-notification.json](07-payout-initiation/03-payout-pending-notification.json) | SMS to teacher (processing) | ⚠️ Assumed |

### 08-razorpay-integration (UPI Payment)
| # | File | Description | Verified |
|---|------|-------------|----------|
| 1 | [01-create-contact.json](08-razorpay-integration/01-create-contact.json) | Create Razorpay contact | ✅ Verified |
| 2 | [02-create-fund-account.json](08-razorpay-integration/02-create-fund-account.json) | Create UPI fund account | ✅ Verified |
| 3 | [03-create-payout.json](08-razorpay-integration/03-create-payout.json) | Create payout request | ✅ Verified |
| 4 | [04-payout-queued-response.json](08-razorpay-integration/04-payout-queued-response.json) | Razorpay returns queued | ✅ Verified |
| 5 | [05-persist-payout-record.json](08-razorpay-integration/05-persist-payout-record.json) | Save payout to DB via Kafka | ✅ Verified |
| 6 | [06-webhook-payout-processing.json](08-razorpay-integration/06-webhook-payout-processing.json) | Webhook: processing status | ✅ Verified |
| 7 | [07-webhook-payout-processed.json](08-razorpay-integration/07-webhook-payout-processed.json) | Webhook: success with UTR | ✅ Verified |
| 8 | [08-webhook-payout-failed.json](08-razorpay-integration/08-webhook-payout-failed.json) | Webhook: payment failed | ✅ Verified |

### 09-payout-completion (Final Settlement)
| # | File | Description | Verified |
|---|------|-------------|----------|
| 1 | [01-payout-success-callback.json](09-payout-completion/01-payout-success-callback.json) | Kafka callback: success | ✅ Verified |
| 2 | [02-update-report-resolved.json](09-payout-completion/02-update-report-resolved.json) | Workflow → RESOLVED | ✅ Verified |
| 3 | [03-payout-success-notification.json](09-payout-completion/03-payout-success-notification.json) | SMS to teacher (₹500 credited) | ⚠️ Assumed |
| 4 | [04-payout-failed-callback.json](09-payout-completion/04-payout-failed-callback.json) | Kafka callback: failure | ✅ Verified |
| 5 | [05-update-report-payout-failed.json](09-payout-completion/05-update-report-payout-failed.json) | Workflow → PAYOUT_FAILED | ✅ Verified |
| 6 | [06-payout-failed-notification.json](09-payout-completion/06-payout-failed-notification.json) | SMS to teacher (failed) | ⚠️ Assumed |
| 7 | [07-retry-payout-request.json](09-payout-completion/07-retry-payout-request.json) | Admin/system retries payout | ✅ Verified |
| 8 | [08-retry-payout-response.json](09-payout-completion/08-retry-payout-response.json) | Back to PAYOUT_PENDING | ✅ Verified |
| 9 | [09-manual-resolve.json](09-payout-completion/09-manual-resolve.json) | Admin manual resolution | ✅ Verified |

### 10-search-and-track (Query APIs)
| # | File | Description | Verified |
|---|------|-------------|----------|
| 1 | [01-teacher-my-reports.json](10-search-and-track/01-teacher-my-reports.json) | Teacher views own reports | ✅ Verified |
| 2 | [02-teacher-report-detail.json](10-search-and-track/02-teacher-report-detail.json) | Teacher views report detail | ✅ Verified |
| 3 | [03-public-track-by-id.json](10-search-and-track/03-public-track-by-id.json) | Public tracking by ID | ✅ Verified |
| 4 | [04-admin-search-by-filters.json](10-search-and-track/04-admin-search-by-filters.json) | Admin multi-filter search | ✅ Verified |
| 5 | [05-elasticsearch-search.json](10-search-and-track/05-elasticsearch-search.json) | ES aggregation for DSS | ⚠️ Assumed |

### 11-edge-cases (Error Scenarios)
| # | File | Description | Verified |
|---|------|-------------|----------|
| 1 | [01-daily-limit-exceeded.json](11-edge-cases/01-daily-limit-exceeded.json) | 6th report blocked (limit=5) | ⚠️ Assumed |
| 2 | [02-monthly-cap-reached.json](11-edge-cases/02-monthly-cap-reached.json) | ₹5000 monthly cap reached | ⚠️ Assumed |
| 3 | [03-fraud-high-risk-block.json](11-edge-cases/03-fraud-high-risk-block.json) | High fraud score auto-reject | ✅ Verified |
| 4 | [04-teacher-suspended.json](11-edge-cases/04-teacher-suspended.json) | Suspended teacher blocked | ⚠️ Assumed |
| 5 | [05-outside-tenant-boundary.json](11-edge-cases/05-outside-tenant-boundary.json) | GPS outside service area | ✅ Verified |
| 6 | [06-reassign-report.json](11-edge-cases/06-reassign-report.json) | Supervisor reassigns officer | ✅ Verified |
| 7 | [07-stale-photo-rejection.json](11-edge-cases/07-stale-photo-rejection.json) | Photo older than 48 hours | ✅ Verified |
| 8 | [08-razorpay-low-balance.json](11-edge-cases/08-razorpay-low-balance.json) | RazorpayX low balance queue | ⚠️ Assumed |

---

## Verification Status Legend

| Symbol | Meaning |
|--------|---------|
| ✅ Verified | Structure validated from existing codebase |
| ⚠️ Assumed | Structure based on design docs/patterns |

---

## Verified Sources

The following source files were used to validate packet structures:

### Java Models
- `DogReport.java` - Core report entity with all fields
- `Evidence.java` - Photo evidence with imageHash (pHash)
- `Location.java` - GPS coordinates with validation result
- `Payout.java` - UPI payout entity
- `FundAccount.java` - UPI VPA details
- `PayoutCallback.java` - Kafka callback model
- `DogReportSearchCriteria.java` - Search filter fields

### Configuration Files
- `BusinessService-with-UPI.json` - Complete workflow states and transitions
- `sdcrs-persister-config.json` - Kafka → PostgreSQL mapping
- `sdcrs-indexer-config.json` - Kafka → Elasticsearch mapping
- `ReportType.json` - Service types (Aggressive, Injured, Pack, Standard)
- `PayoutConfig.json` - Payout amounts, limits, caps
- `RejectionReason.json` - Rejection codes
- `upi-adapter-config.json` - Razorpay integration config

---

## Assumptions Documentation

### SSO/Authentication (01-teacher-login)
1. **HRMS SSO Integration**: Assumes OAuth2/OIDC with PKCE flow
2. **Token Structure**: JWT with standard claims
3. **User Provisioning**: Auto-create DIGIT user on first login

### Notifications
1. **SMS Templates**: Assumed template IDs (e.g., `SDCRS_CREATE_TEACHER`)
2. **Kafka Topics**: `egov.core.notification.sms` for all SMS
3. **Push Notifications**: Optional, not core to flow

### Fraud Detection (SpEL-Based Rules)
1. **Rule Engine**: Spring Expression Language (SpEL) for configurable rules
2. **Condition Types**: 16 types including NULL_CHECK, VELOCITY, GEO_BOUNDARY, CUSTOM
3. **CUSTOM Rules**: SpEL expressions like `#additionalData['dogCount'] > 10`
4. **External Validators**: ML predictions evaluated via `checkExpression` (e.g., `dogCount == 0`)
5. **SpEL Variables**: `#applicantId`, `#deviceId`, `#latitude`, `#longitude`, `#evidenceCount`, `#additionalData`, `#predictions`
6. **Risk Thresholds**: LOW (≤25), MEDIUM (26-50), HIGH (51-75), CRITICAL (>75)
7. **Auto-Reject**: Score > 80 triggers auto-rejection
8. **Perceptual Hash**: Using pHash with 90% similarity threshold for IMAGE_SIMILARITY type
9. **Reference**: See [08-fraud-detection-service.md Section 2.5](../08-fraud-detection-service.md#25-spel-expression-evaluator)

### Razorpay Integration
1. **API Version**: Razorpay X current API
2. **Webhook Events**: payout.processing, payout.processed, payout.failed
3. **Amount Format**: Paise (₹500 = 50000 paise)
4. **Idempotency**: Reference ID used for deduplication

### Limits and Caps
1. **Daily Report Limit**: 5 reports per teacher
2. **Monthly Payout Cap**: ₹5,000 per teacher
3. **Photo Age Limit**: 48 hours maximum
4. **Payout Retry Limit**: 3 attempts

---

## Usage

### For Developers
Use these packets to understand:
- Request/response structure for each API
- Kafka message formats for async operations
- Workflow state transitions

### For Testers
Use these packets as:
- Sample payloads for API testing
- Expected response validation
- Edge case scenario references

### For Product/Business
Use these packets to:
- Trace data through the system
- Understand what happens at each stage
- Identify notification touchpoints

---

## Related Documents

| Document | Path | Description |
|----------|------|-------------|
| PRD | [../prd.md](../prd.md) | Product requirements |
| Process Workflow | [../01-process-workflow-upi.md](../01-process-workflow-upi.md) | Swimlane diagrams |
| Service Design | [../03-service-design.md](../03-service-design.md) | Technical architecture |
| Fraud Detection Service | [../08-fraud-detection-service.md](../08-fraud-detection-service.md) | SpEL rules, condition types, risk scoring |
| Workflow Config | [../03-configs/workflow/BusinessService-with-UPI.json](../03-configs/workflow/BusinessService-with-UPI.json) | State machine |
| Sequence Diagrams | [../05-sequence-diagrams/](../05-sequence-diagrams/) | PlantUML sequences |

---

*Generated: December 2024*
*Total Packets: 74 files across 11 stages*
