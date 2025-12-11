# Product Requirements Document
## Stray Dog Capture & Reporting System (SDCRS)

---

| Document Info | |
|---------------|---|
| **Version** | 1.2 |
| **Last Updated** | December 2025 |
| **Status** | Draft |
| **Owner** | [State Education Department / eGov Team] |
| **Stakeholders** | Education Dept, Municipal Corporation, AICOE, Treasury |

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Problem Statement](#2-problem-statement)
3. [Goals & Success Metrics](#3-goals--success-metrics)
4. [User Personas](#4-user-personas)
5. [Scope](#5-scope)
6. [Functional Requirements](#6-functional-requirements)
7. [Non-Functional Requirements](#7-non-functional-requirements)
8. [System Architecture Overview](#8-system-architecture-overview)
9. [Data Model](#9-data-model)
10. [Security & Privacy](#10-security--privacy)
11. [Integration Requirements](#11-integration-requirements)
12. [Rollout Plan](#12-rollout-plan)
13. [Risks & Mitigations](#13-risks--mitigations)
14. [Appendix](#14-appendix)

---

## 1. Executive Summary

### 1.1 Purpose

The Stray Dog Capture & Reporting System (SDCRS) is a state-wide civic initiative that leverages the existing teacher workforce as a distributed reporting network for stray dog sightings. Teachers report sightings through a mobile application with photo evidence and location data. Verified applications are routed to Municipal Corporations for action, and teachers receive incentive-based payouts only after the Municipal Corporation successfully captures or resolves the incident.

### 1.2 Key Value Propositions

- **For the State**: Scalable, low-cost data collection on stray dog population and hotspots
- **For Municipal Corporations**: Prioritized, verified incident queue with precise location data for field operations
- **For Teachers**: Additional income opportunity through a simple reporting mechanism
- **For Citizens**: Improved stray dog management leading to safer public spaces

### 1.3 High-Level Solution

A mobile-first application for teachers to submit geo-tagged photo evidence of stray dogs, supported by:
- Automated duplicate detection using image hashing
- Human verification workflow for flagged submissions
- Payout system tied to successful capture/resolution by Municipal Corporation
- Operational dashboards for Municipal Corporation field teams
- Administrative dashboards for program monitoring and fraud detection

---

## 2. Problem Statement

### 2.1 Current Challenges

1. **Lack of reliable data**: No systematic mechanism to identify stray dog hotspots across the state
2. **Reactive approach**: Municipal Corporations respond to complaints rather than proactively managing stray dog populations
3. **Resource inefficiency**: Field teams operate without optimized routes or prioritized incident queues
4. **No accountability**: No way to track if reported incidents are addressed

### 2.2 Opportunity

The state has approximately 400,000 teachers distributed across all districts and blocks. This existing workforce, already on government payroll with verified identities, can serve as a crowdsourced reporting network with minimal onboarding friction.

---

## 3. Goals & Success Metrics

### 3.1 Primary Goals

| Goal | Description |
|------|-------------|
| **G1** | Enable systematic, verifiable reporting of stray dog sightings statewide |
| **G2** | Reduce fraudulent/duplicate submissions to <5% of total volume |
| **G3** | Achieve >15% teacher participation within 6 months of launch |
| **G4** | Provide actionable data to Municipal Corporations for field operations |

### 3.2 Success Metrics

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| Teacher adoption rate | 15% (60,000 teachers) | Unique active submitters / Total teachers |
| Daily submission volume | 100,000+ | Backend analytics |
| Verification turnaround | <24 hours | Time from submission to decision |
| Fraud/duplicate rate | <5% | Rejected submissions / Total submissions |
| Payout disbursement accuracy | 99.9% | Reconciliation audits |
| Municipal Corp action rate | >80% of verified incidents | Downstream status tracking |

---

## 4. User Personas

### 4.1 Teacher (Reporter)

| Attribute | Details |
|-----------|---------|
| **Who** | Government school teacher, ages 25-55 |
| **Tech proficiency** | Basic to moderate; comfortable with WhatsApp, UPI apps |
| **Device** | Android smartphone (mid-range), variable network connectivity |
| **Motivation** | Earn additional income (₹2,000-5,000/month potential) |
| **Pain points** | Complex apps, slow uploads, unclear payout status |
| **Key needs** | Simple flow, offline support, transparent tracking |

### 4.2 Verifier (Backend Operator)

| Attribute | Details |
|-----------|---------|
| **Who** | District-level data entry operator or dedicated verification staff |
| **Tech proficiency** | Moderate; works on desktop/laptop |
| **Motivation** | Clear daily targets, efficient queue management |
| **Pain points** | Overwhelming volume, unclear duplicate cases |
| **Key needs** | Side-by-side image comparison, quick actions, SLA tracking |

### 4.3 Municipal Corporation Field Officer

| Attribute | Details |
|-----------|---------|
| **Who** | Animal control staff, field veterinarians |
| **Tech proficiency** | Basic; uses feature phone or basic smartphone |
| **Motivation** | Clear task assignments, optimized routes |
| **Pain points** | Incorrect locations, outdated information |
| **Key needs** | Accurate GPS, dog condition info, simple status updates |

### 4.4 District Administrator

| Attribute | Details |
|-----------|---------|
| **Who** | District Education Officer or designated program coordinator |
| **Tech proficiency** | Moderate |
| **Motivation** | Meet program targets, identify underperforming blocks |
| **Pain points** | Lack of visibility, manual reporting |
| **Key needs** | Aggregated dashboards, anomaly alerts, exportable reports |

### 4.5 State Administrator (AICOE)

| Attribute | Details |
|-----------|---------|
| **Who** | State-level program director, policy team |
| **Tech proficiency** | Moderate to high |
| **Motivation** | Program success, budget optimization, fraud prevention |
| **Pain points** | Statewide visibility, payout reconciliation |
| **Key needs** | Real-time analytics, fraud metrics, budget tracking |

---

## 5. Scope

### 5.1 In Scope (v1.0)

| Category | Features |
|----------|----------|
| **Submission** | Photo upload (dog + selfie), GPS capture, incident details |
| **Verification** | Duplicate detection (ML), human review queue, approve/reject workflow |
| **Payouts** | Fixed payout per successful capture, integration with treasury/DBT |
| **Dashboards** | Teacher app dashboard, verifier console, admin analytics, Municipal Corp queue |
| **Notifications** | Submission status updates, payout confirmations |
| **Offline support** | Local queue with auto-retry |

### 5.2 Out of Scope (v1.0)

| Feature | Rationale |
|---------|-----------|
| Geographic restrictions | Policy decision: no boundary limits for v1 |
| Capture tracking (post-report) | Municipal Corp's internal workflow; not tracked in SDCRS |
| Teacher training module | Separate initiative; may integrate later |
| Public reporting (non-teachers) | Phase 2 consideration |
| Route optimization for field teams | Phase 3; requires integration with Municipal Corp systems |

### 5.3 Future Considerations (v2.0+)

- Vehicle routing optimization for Municipal Corp
- Public-facing reporting portal
- Integration with animal shelter management systems
- Vaccination/sterilization tracking
- AI-based dog identification (unique dog tracking)

---

## 6. Functional Requirements

### 6.1 Teacher Mobile Application

#### 6.1.1 Authentication

| ID | Requirement | Priority |
|----|-------------|----------|
| AUTH-01 | Teachers log in via OTP sent to registered mobile number (from HRMS) | P0 |
| AUTH-02 | Profile auto-populated from teacher database (name, school, district, block) | P0 |
| AUTH-03 | Session validity: 30 days with refresh token | P1 |
| AUTH-04 | Support for eKYC via Aadhaar for bank account linkage (payout) | P1 |

#### 6.1.2 Submission Flow

| ID | Requirement | Priority |
|----|-------------|----------|
| SUB-01 | Capture/upload photo of stray dog | P0 |
| SUB-02 | Capture/upload selfie at same location (for verification) | P0 |
| SUB-03 | Auto-extract GPS coordinates from image EXIF data | P0 |
| SUB-04 | Flag submission if GPS is missing or appears spoofed | P0 |
| SUB-04a | Auto-reject submission if GPS coordinates are outside valid tenant boundary | P0 |
| SUB-05 | Capture timestamp automatically; reject if >48 hours old | P0 |
| SUB-06 | Allow manual location adjustment via map pin (with audit log) | P1 |
| SUB-07 | Dog condition tags: `normal`, `injured`, `aggressive`, `with puppies`, `collared` | P0 |
| SUB-08 | Optional free-text notes field (max 200 characters) | P2 |
| SUB-09 | Generate unique Incident ID upon successful submission | P0 |
| SUB-10 | Show confirmation screen with estimated verification timeline | P0 |

#### 6.1.3 Offline Support

| ID | Requirement | Priority |
|----|-------------|----------|
| OFF-01 | If upload fails, store submission in local queue | P0 |
| OFF-02 | Auto-retry upload every 5 minutes when connectivity detected | P0 |
| OFF-03 | Show "Pending Upload" status for queued submissions | P0 |
| OFF-04 | Auto-expire local submissions after 48 hours | P0 |
| OFF-05 | Allow manual retry trigger by user | P1 |

#### 6.1.4 Teacher Dashboard

| ID | Requirement | Priority |
|----|-------------|----------|
| TDASH-01 | List of all submissions with status (`Pending`, `Under Review`, `Verified`, `Rejected`) | P0 |
| TDASH-02 | Payout history with transaction IDs | P0 |
| TDASH-03 | Notification center for status updates | P0 |
| TDASH-06 | Generate shareable permalink for any submission | P0 |

#### 6.1.5 Shareable Permalink (Public View)

| ID | Requirement | Priority |
|----|-------------|----------|
| PERM-01 | Display Incident ID and workflow status | P0 |
| PERM-02 | Display submission date/time | P0 |
| PERM-03 | Display general location (district/block only, no precise GPS) | P0 |
| PERM-04 | Display payout amount (once processed) | P1 |
| PERM-05 | DO NOT display dog photo, selfie, or precise coordinates | P0 |
| PERM-06 | DO NOT display any teacher PII | P0 |

---

### 6.2 Verification Workflow

#### 6.2.1 Automated Duplicate Detection

| ID | Requirement | Priority |
|----|-------------|----------|
| DUP-01 | Compute perceptual hash (pHash) for each dog image | P0 |
| DUP-02 | Compare against all submissions within 7-day window | P0 |
| DUP-03 | Flag as potential duplicate if pHash similarity >90% | P0 |
| DUP-04 | Cluster submissions by GPS proximity (<500m) + time window (<24h) | P0 |
| DUP-05 | Auto-reject if exact hash match found (same image) | P1 |
| DUP-06 | Route flagged submissions to human verification queue | P0 |

#### 6.2.2 Human Verification Queue

| ID | Requirement | Priority |
|----|-------------|----------|
| VER-01 | Display dog photo, selfie, map location, and EXIF metadata | P0 |
| VER-02 | Show potential duplicate matches side-by-side | P0 |
| VER-03 | Actions: `Approve`, `Reject`, `Mark as Duplicate`, `Escalate` | P0 |
| VER-04 | Require rejection reason from predefined list | P0 |
| VER-05 | Track verifier ID and timestamp for audit | P0 |
| VER-06 | SLA indicator: highlight submissions approaching 24-hour limit | P1 |
| VER-07 | Bulk actions for obvious duplicates (same cluster) | P2 |

#### 6.2.3 Fraud Detection Flags

| ID | Requirement | Priority |
|----|-------------|----------|
| FRD-01 | Flag if selfie GPS >500m from dog photo GPS | P0 |
| FRD-02 | Flag if EXIF timestamp >48 hours before upload | P0 |
| FRD-03 | Flag if >10 submissions from same user in <1 hour | P0 |
| FRD-04 | Flag if device GPS is disabled or mock location detected | P1 |
| FRD-05 | Maintain fraud score per teacher based on rejection history | P1 |

---

### 6.3 Payouts

#### 6.3.1 Payout Processing

| ID | Requirement | Priority |
|----|-------------|----------|
| PAY-01 | Fixed payout of ₹500 per successfully captured/resolved incident | P0 |
| PAY-01a | Monthly payout cap: ₹5,000 per teacher | P0 |
| PAY-01b | Calculate payout weekly based on successfully captured/resolved incidents | P0 |
| PAY-02 | Process payouts via UPI (see [Appendix 14.4](#144-ncr-treasuryifms-status) for rationale) | P0 |
| PAY-03 | Update teacher dashboard upon successful disbursement | P0 |
| PAY-04 | Handle failed transactions with retry mechanism | P0 |
| PAY-05 | Maintain full audit trail of payout calculations | P0 |

#### 6.3.2 Penalty System

| Offense Level | Condition | Action |
|---------------|-----------|--------|
| Warning | 1st rejected submission | No payout for that submission; warning displayed |
| Cooldown | 3 rejections in 30 days | 7-day submission block |
| Suspension | 5 rejections in 30 days | 30-day suspension from program |
| Ban | 10+ lifetime rejections | Permanent ban; escalation to district admin |

---

### 6.4 Administrative Dashboards

#### 6.4.1 Operations Dashboard (Municipal Corporation)

| ID | Requirement | Priority |
|----|-------------|----------|
| MCD-01 | Map view with verified incident markers | P0 |
| MCD-02 | Heatmap overlay showing incident density | P1 |
| MCD-03 | Incident queue with filters (date, severity, area, status) | P0 |
| MCD-04 | Incident details: dog photo, GPS, condition tags, timestamp | P0 |
| MCD-05 | Status update: `Assigned`, `In Progress`, `Resolved`, `Unable to Locate` | P0 |
| MCD-06 | DO NOT display teacher identity or selfie | P0 |
| MCD-07 | Export incident list as CSV | P1 |

#### 6.4.2 District Admin Dashboard

| ID | Requirement | Priority |
|----|-------------|----------|
| DAD-01 | Submission volume trends (daily, weekly, monthly) | P0 |
| DAD-02 | Verification funnel: submitted → verified → rejected → duplicate | P0 |
| DAD-03 | Teacher participation rate by block | P0 |
| DAD-04 | Top performing blocks by submission volume | P1 |
| DAD-05 | Fraud indicators: rejection rate by teacher, GPS spoofing incidents | P0 |
| DAD-06 | Payout summary by block | P1 |
| DAD-07 | Export reports as PDF/CSV | P1 |

#### 6.4.3 State Admin Dashboard (AICOE)

| ID | Requirement | Priority |
|----|-------------|----------|
| SAD-01 | Statewide submission and verification metrics | P0 |
| SAD-02 | District-wise comparison (participation, fraud rate, payout) | P0 |
| SAD-03 | Budget utilization: total payouts vs. allocated budget | P0 |
| SAD-04 | Fraud analytics: patterns, high-risk users, anomaly detection | P0 |
| SAD-05 | Municipal Corp action rate by district | P1 |
| SAD-06 | Real-time alerts for unusual activity spikes | P2 |
| SAD-07 | Configurable parameters: payout rates, caps, penalty thresholds | P0 |

---

### 6.5 Notifications

| ID | Requirement | Priority |
|----|-------------|----------|
| NOT-01 | Push notification on submission received | P0 |
| NOT-02 | Push notification on verification complete (approved/rejected) | P0 |
| NOT-03 | Push notification on payout processed | P0 |
| NOT-04 | Push notification on penalty applied (cooldown, suspension) | P0 |
| NOT-05 | In-app notification center with history | P0 |
| NOT-06 | SMS fallback for critical notifications (payout, suspension) | P1 |

---

## 7. Non-Functional Requirements

### 7.1 Performance

| Metric | Requirement |
|--------|-------------|
| API response time | <500ms (P95) |
| Image upload time | <10 seconds for 5MB image on 3G |
| Dashboard load time | <3 seconds |
| Concurrent users | Support 60,000 simultaneous users |
| Peak load | 300,000 submissions/day (5 photos × 60K active users) |

### 7.2 Scalability

| Component | Approach |
|-----------|----------|
| Image storage | Cloud object storage (S3-compatible) with CDN |
| Database | Horizontally scalable (PostgreSQL with read replicas or distributed DB) |
| Processing queue | Message queue (RabbitMQ/Kafka) for async duplicate detection |
| API layer | Containerized microservices with auto-scaling |

### 7.3 Availability

| Metric | Requirement |
|--------|-------------|
| Uptime | 99.5% (excluding scheduled maintenance) |
| Maintenance window | Sundays 2 AM - 6 AM |
| Disaster recovery | RTO: 4 hours, RPO: 1 hour |
| Backup frequency | Daily full backup, hourly incremental |

### 7.4 Compatibility

| Platform | Requirement |
|----------|-------------|
| Android | Version 8.0 (Oreo) and above |
| iOS | Version 13.0 and above (if applicable) |
| Web (Admin) | Chrome 90+, Firefox 88+, Edge 90+ |
| Network | Functional on 2G/3G with graceful degradation |

### 7.5 Localization

| Requirement | Details |
|-------------|---------|
| Languages | English + State regional language(s) |
| Date/time | IST (Asia/Kolkata) |
| Currency | INR (₹) |

---

## 8. System Architecture Overview

### 8.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENTS                                         │
├─────────────────┬─────────────────┬─────────────────┬───────────────────────┤
│  Teacher App    │  Verifier Web   │  Admin Web      │  Municipal Corp Web   │
│  (Android/iOS)  │  Console        │  Dashboard      │  Dashboard            │
└────────┬────────┴────────┬────────┴────────┬────────┴──────────┬────────────┘
         │                 │                 │                   │
         └─────────────────┴────────┬────────┴───────────────────┘
                                    │
                          ┌─────────▼─────────┐
                          │   API Gateway     │
                          │   (Auth, Rate     │
                          │    Limiting)      │
                          └─────────┬─────────┘
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         │                          │                          │
┌────────▼────────┐      ┌──────────▼──────────┐    ┌──────────▼────────┐
│  Submission     │      │  Verification       │    │  Payout           │
│  Service        │      │  Service            │    │  Service          │
└────────┬────────┘      └──────────┬──────────┘    └──────────┬────────┘
         │                          │                          │
         │               ┌──────────▼──────────┐               │
         │               │  Duplicate Detection│               │
         │               │  (ML Pipeline)      │               │
         │               └──────────┬──────────┘               │
         │                          │                          │
         └──────────────────────────┼──────────────────────────┘
                                    │
                          ┌─────────▼─────────┐
                          │   Message Queue   │
                          │   (Async Jobs)    │
                          └─────────┬─────────┘
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         │                          │                          │
┌────────▼────────┐      ┌──────────▼──────────┐    ┌─────────▼─────────┐
│  PostgreSQL     │      │  Object Storage    │    │  Redis            │
│  (Primary DB)   │      │  (Images)          │    │  (Cache/Sessions) │
└─────────────────┘      └────────────────────┘    └───────────────────┘
```

### 8.2 Key Components

| Component | Technology Options | Purpose |
|-----------|-------------------|---------|
| Mobile App | React Native / Flutter | Cross-platform teacher application |
| API Gateway | Kong / AWS API Gateway | Authentication, rate limiting, routing |
| Backend Services | Node.js / Python (FastAPI) | Business logic microservices |
| Database | PostgreSQL | Primary data store |
| Object Storage | AWS S3 / MinIO | Image storage |
| CDN | CloudFront / Cloudflare | Image delivery, static assets |
| Message Queue | RabbitMQ / AWS SQS | Async job processing |
| Cache | Redis | Session management, caching |
| ML Pipeline | Python (OpenCV, imagehash) | Duplicate detection |
| Monitoring | Prometheus + Grafana | System observability |

---

## 9. Data Model

### 9.1 Core Entities

#### Teachers

| Field | Type | Description |
|-------|------|-------------|
| teacher_id | UUID | Primary key |
| hrms_id | String | Reference to HRMS system |
| name | String | Full name |
| mobile | String | Registered mobile number |
| school_id | UUID | FK to Schools |
| district_id | UUID | FK to Districts |
| block_id | UUID | FK to Blocks |
| bank_account | Encrypted String | For payout (optional) |
| aadhaar_verified | Boolean | eKYC status |
| status | Enum | `active`, `suspended`, `banned` |
| fraud_score | Integer | Cumulative fraud indicator |
| created_at | Timestamp | Registration date |

#### Incidents

| Field | Type | Description |
|-------|------|-------------|
| incident_id | UUID | Primary key |
| teacher_id | UUID | FK to Teachers |
| dog_image_url | String | S3 path to dog photo |
| selfie_image_url | String | S3 path to selfie |
| dog_image_hash | String | pHash for duplicate detection |
| latitude | Decimal | GPS latitude |
| longitude | Decimal | GPS longitude |
| capture_timestamp | Timestamp | From EXIF data |
| upload_timestamp | Timestamp | Server receipt time |
| condition_tags | Array | `normal`, `injured`, `aggressive`, etc. |
| notes | String | Optional free-text |
| status | Enum | `pending`, `under_review`, `verified`, `rejected`, `duplicate` |
| rejection_reason | String | If rejected |
| verified_by | UUID | FK to Verifiers |
| verified_at | Timestamp | Verification time |
| payout_amount_awarded | Decimal | Payout for this incident (₹500 if captured/resolved) |
| payout_amount | Decimal | ₹ amount |
| payout_status | Enum | `pending`, `processed`, `failed` |
| created_at | Timestamp | Record creation |

#### Verifications

| Field | Type | Description |
|-------|------|-------------|
| verification_id | UUID | Primary key |
| incident_id | UUID | FK to Incidents |
| verifier_id | UUID | FK to Verifiers |
| action | Enum | `approve`, `reject`, `duplicate`, `escalate` |
| reason | String | Rejection/escalation reason |
| duplicate_of | UUID | FK to Incidents (if duplicate) |
| created_at | Timestamp | Verification time |

#### Payouts

| Field | Type | Description |
|-------|------|-------------|
| payout_id | UUID | Primary key |
| teacher_id | UUID | FK to Teachers |
| period_start | Date | Payout period start |
| period_end | Date | Payout period end |
| incident_count | Integer | Number of resolved incidents in period |
| gross_amount | Decimal | Before any deductions |
| net_amount | Decimal | Final payout amount |
| transaction_id | String | Treasury/bank reference |
| status | Enum | `calculated`, `submitted`, `processed`, `failed` |
| processed_at | Timestamp | Disbursement time |
| created_at | Timestamp | Record creation |

#### Municipal Corp Actions

| Field | Type | Description |
|-------|------|-------------|
| action_id | UUID | Primary key |
| incident_id | UUID | FK to Incidents |
| assigned_to | String | Field officer ID |
| status | Enum | `assigned`, `in_progress`, `resolved`, `unable_to_locate` |
| resolution_notes | String | Outcome details |
| resolved_at | Timestamp | Resolution time |
| created_at | Timestamp | Assignment time |

---

## 10. Security & Privacy

### 10.1 Authentication & Authorization

| Mechanism | Details |
|-----------|---------|
| Teacher Auth | OTP-based login via registered mobile |
| Admin Auth | SSO integration with state portal (SAML/OAuth) |
| API Security | JWT tokens with 1-hour expiry, refresh tokens |
| Role-Based Access | Teacher, Verifier, District Admin, State Admin, Municipal Corp |

### 10.2 Data Protection

| Data Type | Protection Measure |
|-----------|-------------------|
| Teacher PII | Encrypted at rest (AES-256) |
| Selfie images | Stored in separate encrypted bucket; access logged |
| Bank details | Encrypted; accessible only to payout service |
| GPS coordinates | Not exposed in public permalinks |
| API traffic | TLS 1.3 encryption |

### 10.3 Privacy Compliance

| Requirement | Implementation |
|-------------|----------------|
| Data minimization | Collect only necessary data |
| Selfie retention | Delete 90 days post-verification |
| Right to access | Teachers can download their data |
| Audit logging | All admin actions logged with user ID and timestamp |
| Public dashboard | No PII, no images, no precise locations |

### 10.4 Fraud Prevention

| Control | Description |
|---------|-------------|
| GPS validation | Cross-check EXIF GPS with device GPS |
| Mock location detection | Flag submissions from rooted devices with mock GPS |
| Rate limiting | Max 20 submissions per hour per user |
| Image hashing | Detect resubmission of same images |
| Behavioral analysis | Flag unusual patterns (bulk submissions, odd hours) |

---

## 11. Integration Requirements

### 11.1 Upstream Integrations

| System | Integration Type | Data Exchanged |
|--------|-----------------|----------------|
| Teacher HRMS | API (read) | Teacher details, mobile numbers, school mapping |
| Aadhaar eKYC | API | Identity verification for bank linkage |
| State Auth (SSO) | SAML/OAuth | Admin user authentication |

### 11.2 Downstream Integrations

| System | Integration Type | Data Exchanged |
|--------|-----------------|----------------|
| **UPI Payout** | UPI API | Teacher payouts via UPI (VPA/mobile linked accounts) |
| Municipal Corp System | API / CSV export | Verified incidents (location, photo, condition) |
| SMS Gateway | API | OTP, critical notifications |
| Push Notification | Firebase / APNs | In-app notifications |

> **📝 Note: UPI-Based Payout Decision**
>
> The system uses **UPI for teacher payouts** instead of direct Treasury/IFMS integration. This decision is based on the current state of JIT (Just-In-Time) API availability in NCR states. See [Appendix 14.4](#144-ncr-treasuryifms-status) for detailed assessment.

### 11.3 API Specifications

All APIs follow RESTful conventions with JSON payloads. Key endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/otp/request` | POST | Request OTP for login |
| `/auth/otp/verify` | POST | Verify OTP, receive JWT |
| `/incidents` | POST | Submit new incident |
| `/incidents/{id}` | GET | Get incident details |
| `/incidents/{id}/status` | GET | Public permalink status |
| `/teacher/dashboard` | GET | Teacher dashboard data |
| `/verification/queue` | GET | Verifier queue |
| `/verification/{id}` | POST | Submit verification decision |
| `/admin/metrics` | GET | Admin dashboard metrics |
| `/municipal/incidents` | GET | Municipal Corp incident queue |

Full OpenAPI specification to be provided separately.

---

## 12. Rollout Plan

### 12.1 Phased Approach

| Phase | Scope | Duration | Success Criteria |
|-------|-------|----------|------------------|
| **Phase 0: Internal Testing** | Dev team, 50 test users | 2 weeks | All flows functional, no critical bugs |
| **Phase 1: Pilot** | 2 districts, ~5,000 teachers | 4 weeks | 500+ submissions, <10% rejection rate, payout cycle complete |
| **Phase 2: Expansion** | 10 districts, ~50,000 teachers | 6 weeks | 10,000+ daily submissions, verification SLA met |
| **Phase 3: Statewide** | All districts, 400,000 teachers | 8 weeks | 15% participation, <5% fraud rate |
| **Phase 4: Optimization** | Full scale with advanced features | Ongoing | Route optimization, AI improvements, advanced analytics |

### 12.2 Pilot District Selection Criteria

- Mix of urban and rural blocks
- Reliable network connectivity
- Cooperative District Education Officer
- Active Municipal Corporation
- Moderate stray dog population (for meaningful data)

### 12.3 Launch Checklist

- [ ] Infrastructure provisioned and load-tested
- [ ] Teacher database synced from HRMS
- [ ] Municipal Corp users onboarded and trained
- [ ] Verifier team trained and staffed
- [ ] Payout integration tested with treasury
- [ ] Help desk / support channel established
- [ ] Monitoring and alerting configured
- [ ] Rollback plan documented

---

## 13. Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Low teacher adoption | Medium | High | Clear payout incentives, district-level awareness campaigns, peer influence |
| High fraud rate | Medium | High | ML detection, progressive penalties, random audits |
| Infrastructure overload at scale | Medium | High | Load testing, auto-scaling, CDN for images |
| Payout delays causing trust issues | Low | High | Buffer in payout timeline, proactive communication |
| Municipal Corp not acting on incidents | Medium | Medium | Dashboard visibility, SLA tracking, escalation to state |
| Network issues in rural areas | High | Medium | Offline support, retry mechanism, low-bandwidth optimization |
| Privacy breach (selfie leak) | Low | High | Encryption, access controls, 90-day deletion, audit logs |
| Duplicate detection false positives | Medium | Medium | Human review for flagged cases, appeal mechanism |

---

## 14. Appendix

### 14.1 Glossary

| Term | Definition |
|------|------------|
| AICOE | AI Center of Excellence (State-level tech body) |
| DBT | Direct Benefit Transfer |
| EXIF | Exchangeable Image File Format (image metadata) |
| HRMS | Human Resource Management System |
| pHash | Perceptual Hash (image fingerprinting algorithm) |
| PII | Personally Identifiable Information |

### 14.2 References

- State Teacher HRMS API Documentation
- Treasury DBT Integration Guidelines
- Aadhaar eKYC Technical Specifications
- Municipal Corporation Data Standards

### 14.3 Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 0.1 | Dec 2025 | [Author] | Initial draft |
| 1.0 | Dec 2025 | [Author] | Incorporated policy decisions, finalized scope |
| 1.1 | Dec 2025 | [Author] | Removed gamification/points system; simplified to fixed payout model |
| 1.2 | Dec 2025 | [Author] | Added UPI payout decision rationale (NCR Treasury/IFMS status) |

### 14.4 NCR Treasury/IFMS Status

> **Decision: Going with UPI for payouts**

The following assessment of Treasury/IFMS systems in NCR states informed the decision to use UPI-based payouts instead of direct treasury integration:

| State/UT | IFMS System | JIT APIs Available? | Notes |
|----------|-------------|---------------------|-------|
| **Delhi** | CFMS (Delhi Treasury) | ❌ No | Delhi uses CFMS; no public JIT APIs. Payments via DDO (Drawing & Disbursing Officer) bills |
| **Haryana** | IFMS Haryana | ❌ No | Legacy system; API modernization in progress. Manual bill processing |
| **Uttar Pradesh** | IFMIS UP | ⚠️ Limited | JIT module exists for select schemes (DBT). Not API-ready for external integration |
| **Rajasthan** | IFMS Rajasthan | ⚠️ Limited | JIT available for specific welfare schemes. Custom integration requires state approval |

**Key Findings:**

1. **No standardized JIT API** exists across NCR states for external system integration
2. **Custom integrations** would require separate MOUs and development for each state
3. **UPI provides universal coverage** - all teachers have mobile-linked bank accounts
4. **Faster deployment** - UPI integration is standardized and well-documented
5. **Audit trail** - UPI provides transaction IDs for reconciliation

**Recommendation:** Use **UPI-based payouts** via a payment gateway (e.g., Razorpay, PayU, or NPCI direct) for teacher incentive disbursement. This approach:
- Works across all NCR states without individual treasury integrations
- Leverages existing teacher mobile numbers (already in HRMS)
- Provides instant settlement with transaction tracking
- Reduces operational complexity and time-to-market

---

## Approval

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Product Owner | | | |
| Tech Lead | | | |
| State Program Director | | | |
| Municipal Corp Representative | | | |

---

*End of Document*
