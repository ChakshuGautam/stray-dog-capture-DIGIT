# Design Feasibility Review

## SDCRS - Stray Dog Capture & Reporting System

---

## Overview

This document reviews each user story from Design Output #2 against the service design (Design Output #3), the Fraud Detection Service (Design Output #8), and DIGIT platform capabilities to assess implementation feasibility.

### Feasibility Ratings

| Rating | Description |
|--------|-------------|
| **✅ High** | Fully supported by existing DIGIT/SDCRS services; minimal custom code |
| **🟡 Medium** | Requires custom development within normal DIGIT patterns |
| **🔴 Low** | Complex; requires significant new development or external integrations |
| **❓ Needs Clarification** | Requirements need more definition before assessment |

### Key Services Available

| Service | Capabilities |
|---------|-------------|
| **Dog Report Registry** | Core CRUD, workflow, evidence references |
| **Fraud Detection Service** | pHash duplicate detection, velocity checking, geospatial validation, pattern detection, collusion analysis, real-time alerts |
| **DIGIT Core Services** | File Store, Workflow, Collection, Notification, DSS, Location, User |

---

## 1. Teacher (Reporter) Stories

| User Story | Feasibility |
|------------|-------------|
| **T-AUTH-01**: Log in using OTP sent to registered mobile number | ✅ **High** - DIGIT User Service supports OTP-based authentication out of the box |
| **T-AUTH-02**: Profile auto-populated from HRMS database | 🟡 **Medium** - Requires integration with external HRMS system; DIGIT User Service can store profile data but initial sync needs custom integration layer |
| **T-AUTH-03**: Link bank account via Aadhaar eKYC | 🔴 **Low** - Requires integration with UIDAI/DigiLocker eKYC APIs; not a standard DIGIT service. May need to use DBT (Direct Benefit Transfer) integration patterns |
| **T-SUB-01**: Capture and upload photo of stray dog | ✅ **High** - DIGIT File Store Service handles image uploads; standard DIGIT pattern |
| **T-SUB-02**: Capture selfie at same location | ✅ **High** - Same as T-SUB-01; File Store Service |
| **T-SUB-03**: GPS coordinates auto-extracted from photos | 🟡 **Medium** - EXIF extraction must be done in custom SDCRS service; not a standard DIGIT feature. JavaScript libraries (exif-js) or backend processing needed |
| **T-SUB-04**: Tag dog's condition (normal, injured, aggressive, collared) | ✅ **High** - MDMS-driven dropdown; ReportType.json already defined |
| **T-SUB-05**: Add optional notes about sighting | ✅ **High** - Simple text field in Dog Report Registry |
| **T-SUB-06**: Receive unique Application ID upon submission | ✅ **High** - DIGIT IDGen Service; configuration already defined |
| **T-OFF-01**: Submission saved locally if upload fails | 🟡 **Medium** - Requires PWA/service worker implementation in frontend; not a backend DIGIT feature. DIGIT Studio supports offline but needs configuration |
| **T-OFF-02**: Auto-retry uploads when connectivity restored | 🟡 **Medium** - Same as T-OFF-01; frontend PWA responsibility |
| **T-OFF-03**: See "Pending Upload" status for queued submissions | 🟡 **Medium** - Frontend state management; requires UI implementation |
| **T-STAT-01**: View status of all submissions | ✅ **High** - Dog Report Registry search API with reporter filter |
| **T-STAT-02**: Receive SMS notifications on status change | ✅ **High** - DIGIT Notification Service; workflow triggers configured |
| **T-STAT-03**: See rejection reason if rejected | ✅ **High** - Stored in rejectionReason field; MDMS codes for display |
| **T-STAT-04**: Generate shareable public link for submission | ✅ **High** - URL Shortening Service integration already designed; public `_track` API defined |
| **T-PAY-01**: View total earnings and pending payouts | ✅ **High** - DIGIT Collection/Billing Service provides payment search |
| **T-PAY-02**: View payout history with transaction IDs | ✅ **High** - Collection/Billing Service maintains transaction history |
| **T-PAY-03**: Receive SMS when payout processed | ✅ **High** - Notification Service triggered on payment state change |

---

## 2. System (Automated) Stories

| System Behavior | Feasibility |
|-----------------|-------------|
| **S-VAL-01**: Validate GPS coordinates present and flag if missing/spoofed | 🟡 **Medium** - Custom validation logic in SDCRS service; GPS spoofing detection requires heuristics (velocity checks, consistency with network location) |
| **S-VAL-02**: Validate GPS within tenant boundary using egov-location | ✅ **High** - DIGIT Location Service provides `/tenant/_search` with lat/long; standard pattern |
| **S-VAL-03**: Validate photo timestamp within 48 hours | 🟡 **Medium** - EXIF timestamp extraction and comparison; custom logic in SDCRS service |
| **S-VAL-04**: Compute perceptual hash (pHash) for dog image | 🟡 **Medium** - Fraud Detection Service provides Duplicate Detector component with pHash/SimHash computation using imagehash library |
| **S-VAL-05**: Compare pHash against past 7 days submissions | 🟡 **Medium** - Fraud Detection Service includes Elasticsearch index (sdcrs-image-hashes) with hamming distance scoring for similarity search |
| **S-VAL-06**: Flag submissions where selfie GPS >500m from dog photo GPS | ✅ **High** - Fraud Detection Service Geospatial Validator already performs this check |
| **S-VAL-07**: Flag if >10 submissions from same user in <1 hour | 🟡 **Medium** - Rate limiting logic; can query recent submissions by user and timestamp |
| **S-REJ-01**: Auto-reject submissions with missing GPS | ✅ **High** - Simple null check in validation workflow |
| **S-REJ-02**: Auto-reject GPS outside tenant boundary | ✅ **High** - Uses Location Service response |
| **S-REJ-03**: Auto-reject timestamps >48 hours old | 🟡 **Medium** - Requires reliable EXIF extraction first (see S-VAL-03) |
| **S-REJ-04**: Auto-reject exact hash matches | 🟡 **Medium** - Fraud Detection Service rule SDCRS-008 handles exact duplicate auto-rejection with BLOCK action |
| **S-RTE-01**: Route valid submissions to verification queue | ✅ **High** - DIGIT Workflow Service; standard state transition |
| **S-RTE-02**: Flag potential duplicates (pHash >90% similarity) | 🟡 **Medium** - Fraud Detection Service rule STD-006 flags near-duplicates (similarity >90%) with FLAG action for manual review |
| **S-RTE-03**: Cluster submissions by GPS proximity and time | 🟡 **Medium** - Custom logic; geospatial clustering can use PostGIS or Elasticsearch geo queries |
| **S-MC-01**: Route verified applications to MC queue by jurisdiction | ✅ **High** - Workflow routing based on locality code; MDMS can map locality to MC zone |
| **S-PAY-01**: Award ₹500 payout for captured/resolved incident | ✅ **High** - DIGIT Collection Service demand creation; amount from MDMS PayoutConfig |
| **S-PAY-02**: Enforce monthly cap of ₹5,000 per teacher | 🟡 **Medium** - Requires aggregating teacher payouts before demand creation; custom business logic |
| **S-PAY-03**: Generate weekly payout files for treasury/DBT integration | 🔴 **Low** - Requires DBT file format knowledge; may need integration with state treasury system |
| **S-NOT-01**: Send push notification on auto-rejection | 🟡 **Medium** - DIGIT Notification Service supports SMS/email; push notifications need Firebase/APNs integration which may not be standard |
| **S-NOT-02**: Include rejection reason in notification | ✅ **High** - MDMS localized rejection reason in notification template |

---

## 3. Verifier Stories

| User Story | Feasibility |
|------------|-------------|
| **V-REV-01**: View dog photo, selfie, map location, EXIF side-by-side | 🟡 **Medium** - UI component development; data available from Dog Report Registry and File Store |
| **V-REV-02**: View potential duplicate matches alongside current submission | 🟡 **Medium** - Fraud Detection Service stores duplicateMatches array in FraudCheckResult; UI component needed to display |
| **V-REV-03**: View fraud flags (GPS mismatch, timestamp issues) | 🟡 **Medium** - Flags stored in additionalDetails JSONB; UI to display them |
| **V-REV-04**: SLA indicator for 24-hour verification limit | ✅ **High** - DIGIT Workflow SLA tracking provides this data |
| **V-APP-01**: Approve valid submissions with single click | ✅ **High** - Workflow action VERIFY; simple API call |
| **V-APP-02**: Approved applications auto-routed to MC queue | ✅ **High** - Workflow state transition VERIFIED → ASSIGNED |
| **V-REJ-01**: Reject with predefined reason list | ✅ **High** - MDMS RejectionReason.json provides list; stored on report |
| **V-REJ-02**: Mark as duplicate and link to original | 🟡 **Medium** - Requires `linkedReportId` field addition; UI for selection |
| **V-REJ-03**: Escalate ambiguous cases to senior verifier | 🟡 **Medium** - Requires additional workflow state or escalation flag; role-based routing |
| **V-REJ-04**: Bulk actions on obvious duplicates in cluster | 🟡 **Medium** - Batch update API; requires cluster identification first |
| **V-NOT-01**: Notify teacher on rejection/duplicate | ✅ **High** - Notification Service triggered by workflow transition |
| **V-NOT-02**: Log verifier ID and timestamp for audit | ✅ **High** - DIGIT audit fields (lastModifiedBy, lastModifiedTime) standard |

---

## 4. MC Officer Stories

| User Story | Feasibility |
|------------|-------------|
| **MC-VIEW-01**: Map view with verified incident markers | 🟡 **Medium** - Frontend map component (Leaflet/Mapbox); data from Dog Report search API with geo filter |
| **MC-VIEW-02**: Heatmap overlay showing incident density | 🟡 **Medium** - Elasticsearch aggregations or PostGIS; visualization library needed |
| **MC-VIEW-03**: Filter queue by date, severity, area, status | ✅ **High** - Dog Report search API supports these filters |
| **MC-VIEW-04**: View incident details (photo, GPS, tags, timestamp) | ✅ **High** - Dog Report Registry provides all fields |
| **MC-VIEW-05**: Teacher identity/selfie hidden from MC view | ✅ **High** - Role-based field masking in API response; DIGIT encryption service for PII |
| **MC-FLD-01**: Update status to "In Progress" | ✅ **High** - Workflow action START_FIELD_VISIT |
| **MC-FLD-02**: Navigate to incident using GPS | 🟡 **Medium** - Deep link to Google Maps/native maps app; UI implementation |
| **MC-CAP-01**: Mark incident as "Captured/Resolved" | ✅ **High** - Workflow action MARK_CAPTURED |
| **MC-CAP-02**: Add resolution notes | ✅ **High** - resolution_notes field in Dog Report |
| **MC-UTL-01**: Mark as "Unable to Locate" | ✅ **High** - Workflow action MARK_UNABLE_TO_LOCATE |
| **MC-UTL-02**: Add notes explaining why not found | ✅ **High** - Same resolution_notes field |
| **MC-NOT-01**: Notify teacher on capture/resolve | ✅ **High** - Notification triggered on CAPTURED/RESOLVED state |
| **MC-NOT-02**: Notify teacher on "Unable to Locate" | ✅ **High** - Notification triggered on UNABLE_TO_LOCATE state |

---

## 5. District Administrator Stories

| User Story | Feasibility |
|------------|-------------|
| **DA-01**: Dashboard with key metrics | ✅ **High** - DIGIT DSS (Dashboard Backend); PPIs already defined |
| **DA-02**: Submission volume trends and verification funnel | ✅ **High** - DSS time-series and funnel charts |
| **DA-03**: MC action rates and pending cases | ✅ **High** - DSS metrics; data from Dog Report aggregations |
| **DA-04**: Top-performing blocks by volume/resolution | ✅ **High** - DSS leaderboard/ranking charts |
| **DA-05**: Manage verifier accounts and performance | 🟡 **Medium** - DIGIT User Service for account management; custom metrics view for performance |
| **DA-06**: Fraud indicators and suspicious patterns | ✅ **High** - Fraud Detection Service provides real-time fraud flags, risk scores, and pattern detection; DSS aggregates FraudCheckResult data |
| **DA-07**: Generate and export reports | ✅ **High** - DIGIT PDF Service for report generation; DSS export functionality |

---

## 6. State Administrator Stories

| User Story | Feasibility |
|------------|-------------|
| **SA-01**: Statewide metrics with KPI tracking | ✅ **High** - DSS with state-level aggregation |
| **SA-02**: District comparison by participation, fraud, payout | ✅ **High** - DSS comparison charts |
| **SA-03**: Budget utilization with projections and alerts | 🟡 **Medium** - Requires budget tracking system; alerts need custom threshold configuration |
| **SA-04**: Fraud analytics with pattern detection | 🟡 **Medium** - Fraud Detection Service includes Pattern Detector and Network Analysis (collusion detection); DSS visualization for basic analytics; advanced network viz may need Phase 2 |
| **SA-05**: MC action rates by district | ✅ **High** - DSS with district grouping |
| **SA-06**: Configure system parameters with maker-checker | 🟡 **Medium** - MDMS for config; maker-checker requires workflow for config changes |
| **SA-07**: Real-time alerts for unusual activity | 🟡 **Medium** - Fraud Detection Service provides real-time alerts via Kafka topic (sdcrs.fraud.alerts) with DIGIT Notification Service integration for SMS/email |

---

## Summary

### Feasibility Distribution

| Rating | Count | Percentage |
|--------|-------|------------|
| ✅ High | 46 | 67% |
| 🟡 Medium | 21 | 30% |
| 🔴 Low | 2 | 3% |

### High-Risk Items (🔴 Low Feasibility)

| ID | Story | Risk Mitigation |
|----|-------|-----------------|
| T-AUTH-03 | Aadhaar eKYC bank linking | **Alternative**: Manual bank account entry with later eKYC verification; or partner with CSC (Common Service Centre) for eKYC |
| S-PAY-03 | DBT/treasury file generation | **Requires**: State treasury file format specification; may need payment gateway partnership |

### Items Addressed by Fraud Detection Service

The following items were previously 🔴 Low but are now 🟡 Medium due to the Fraud Detection Service:

| ID | Story | Now Supported By |
|----|-------|------------------|
| S-VAL-04/05 | pHash generation and comparison | Duplicate Detector with imagehash library + Elasticsearch similarity index |
| S-REJ-04 | Auto-reject exact hash matches | Rule SDCRS-008 with BLOCK action |
| S-RTE-02 | Flag near-duplicates (>90% similarity) | Rule STD-006 with FLAG action |
| V-REV-02 | View duplicate matches | FraudCheckResult.duplicateMatches array |
| SA-04 | Fraud pattern detection | Pattern Detector + Network Analysis components |
| SA-07 | Real-time anomaly alerts | Kafka topic sdcrs.fraud.alerts + Notification Service |

### Recommendations

1. **Phase 1 (MVP)**: Implement all High feasibility items + critical Medium items (GPS extraction, offline support, Fraud Detection Service core)
2. **Phase 2**: Advanced fraud analytics, network visualization, collusion detection refinement
3. **Phase 3**: Aadhaar eKYC, DBT integration

### Dependencies on External Systems

| External System | Stories Affected | Integration Complexity |
|-----------------|------------------|----------------------|
| State HRMS | T-AUTH-02 | Medium - API integration |
| UIDAI/DigiLocker | T-AUTH-03 | High - Certification required |
| State Treasury/DBT | S-PAY-03 | High - File format, banking APIs |
| Firebase/APNs | S-NOT-01 | Medium - Push notification setup |

---

*Document Version: 1.2*
*Last Updated: December 2026*
*Revision Notes: Updated feasibility ratings to reflect Fraud Detection Service (Design Output #8) capabilities; S-VAL-06 upgraded to High (Geospatial Validator)*