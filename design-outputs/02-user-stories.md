# Design Output #2: User Stories

## SDCRS - Stray Dog Capture & Reporting System

---

## Overview

This document elaborates the activities from the Process Workflow (Design Output #1) as user stories following the format:

> *"As a [persona], I want to [action], so that [benefit]."*

### Key Business Rule
**Teachers receive payout ONLY after MC successfully captures or resolves the reported dog - NOT upon submission or verification.**

### Detailed Story Documentation
Each story ID links to a detailed specification including acceptance criteria, UI/UX mockups, and technical implementation. Click on any story ID to view the full details.

---

## 1. Teacher (Reporter)

### 1.1 Authentication

| ID | User Story |
|----|------------|
| [T-AUTH-01](./user-stories/teacher/T-AUTH-01.md) | As a **Teacher**, I want to log in using OTP sent to my registered mobile number, so that I can securely access the application without remembering passwords. |
| [T-AUTH-02](./user-stories/teacher/T-AUTH-02.md) | As a **Teacher**, I want my profile to be auto-populated from the HRMS database, so that I don't have to manually enter my details. |
| [T-AUTH-03](./user-stories/teacher/T-AUTH-03.md) | As a **Teacher**, I want to link my bank account via Aadhaar eKYC, so that I can receive payouts directly. |

### 1.2 Submit Application (Photo+Selfie+GPS)

| ID | User Story |
|----|------------|
| [T-SUB-01](./user-stories/teacher/T-SUB-01.md) | As a **Teacher**, I want to capture and upload a photo of a stray dog, so that I can report its presence. |
| [T-SUB-02](./user-stories/teacher/T-SUB-02.md) | As a **Teacher**, I want to capture a selfie at the same location, so that my physical presence can be verified. |
| [T-SUB-03](./user-stories/teacher/T-SUB-03.md) | As a **Teacher**, I want GPS coordinates to be automatically extracted from my photos, so that the exact location is recorded without manual entry. |
| [T-SUB-04](./user-stories/teacher/T-SUB-04.md) | As a **Teacher**, I want to tag the dog's condition (normal, injured, aggressive, with puppies, collared), so that responders know what to expect. |
| [T-SUB-05](./user-stories/teacher/T-SUB-05.md) | As a **Teacher**, I want to add optional notes about the sighting, so that I can provide additional context. |
| [T-SUB-06](./user-stories/teacher/T-SUB-06.md) | As a **Teacher**, I want to receive a unique Application ID upon submission, so that I can track my report. |

### 1.3 Offline Support

| ID | User Story |
|----|------------|
| [T-OFF-01](./user-stories/teacher/T-OFF-01.md) | As a **Teacher**, I want my submission to be saved locally if upload fails, so that I don't lose my work in areas with poor connectivity. |
| [T-OFF-02](./user-stories/teacher/T-OFF-02.md) | As a **Teacher**, I want the app to automatically retry uploads when connectivity is restored, so that I don't have to manually resubmit. |
| [T-OFF-03](./user-stories/teacher/T-OFF-03.md) | As a **Teacher**, I want to see "Pending Upload" status for queued submissions, so that I know which reports haven't been sent yet. |

### 1.4 View Updated Status

| ID | User Story |
|----|------------|
| [T-STAT-01](./user-stories/teacher/T-STAT-01.md) | As a **Teacher**, I want to see the current status of all my submissions (Pending, Under Review, Verified, Rejected, Captured, Unable to Locate), so that I can track progress. |
| [T-STAT-02](./user-stories/teacher/T-STAT-02.md) | As a **Teacher**, I want to receive SMS notifications when my application status changes, so that I'm informed promptly. |
| [T-STAT-03](./user-stories/teacher/T-STAT-03.md) | As a **Teacher**, I want to see the rejection reason if my application was rejected, so that I can avoid similar issues in future submissions. |
| [T-STAT-04](./user-stories/teacher/T-STAT-04.md) | As a **Teacher**, I want to generate a shareable public link for my submission, so that I can share the status with others. |

### 1.5 Receive Payout

| ID | User Story |
|----|------------|
| [T-PAY-01](./user-stories/teacher/T-PAY-01.md) | As a **Teacher**, I want to see my total earnings and pending payouts, so that I understand my earnings. |
| [T-PAY-02](./user-stories/teacher/T-PAY-02.md) | As a **Teacher**, I want to see my payout history with transaction IDs, so that I can reconcile with my bank statements. |
| [T-PAY-03](./user-stories/teacher/T-PAY-03.md) | As a **Teacher**, I want to receive SMS notifications when payouts are processed, so that I know when money has been transferred. |

---

## 2. System (Automated)

### 2.1 Validate (GPS+Timestamp+Hash+Boundary)

| ID | System Behavior |
|----|-----------------|
| [S-VAL-01](./user-stories/system/S-VAL-01.md) | The **System** shall validate that GPS coordinates are present in the submission and flag if missing or potentially spoofed. |
| [S-VAL-02](./user-stories/system/S-VAL-02.md) | The **System** shall validate that GPS coordinates fall within a valid tenant boundary using `egov-location` service (`/tenant/_search` with lat/long). |
| [S-VAL-03](./user-stories/system/S-VAL-03.md) | The **System** shall validate that the photo timestamp is within 48 hours of upload time. |
| S-VAL-04 | The **System** shall compute a perceptual hash (pHash) for the dog image for duplicate detection. |
| S-VAL-05 | The **System** shall compare the pHash against submissions from the past 7 days. |
| S-VAL-06 | The **System** shall flag submissions where selfie GPS is >500m from dog photo GPS. |
| S-VAL-07 | The **System** shall flag if >10 submissions from the same user occur in <1 hour. |

### 2.2 Auto Reject

| ID | System Behavior |
|----|-----------------|
| [S-REJ-01](./user-stories/system/S-REJ-01.md) | The **System** shall auto-reject submissions with missing GPS data. |
| [S-REJ-02](./user-stories/system/S-REJ-02.md) | The **System** shall auto-reject submissions with GPS coordinates outside any valid tenant boundary. |
| S-REJ-03 | The **System** shall auto-reject submissions with timestamps >48 hours old. |
| S-REJ-04 | The **System** shall auto-reject exact hash matches (same image resubmitted). |

### 2.3 Route to Verification Queue

| ID | System Behavior |
|----|-----------------|
| [S-RTE-01](./user-stories/system/S-RTE-01.md) | The **System** shall route valid submissions to the human verification queue. |
| S-RTE-02 | The **System** shall flag potential duplicates (pHash similarity >90%) for verifier attention. |
| S-RTE-03 | The **System** shall cluster submissions by GPS proximity (<500m) and time window (<24h). |

### 2.4 Route to MC Queue

| ID | System Behavior |
|----|-----------------|
| [S-MC-01](./user-stories/system/S-MC-01.md) | The **System** shall route verified/approved applications to the Municipal Corporation queue based on geographic jurisdiction. |

### 2.5 Process Payout

| ID | System Behavior |
|----|-----------------|
| [S-PAY-01](./user-stories/system/S-PAY-01.md) | The **System** shall award ₹500 fixed payout for each successfully captured/resolved incident. |
| S-PAY-02 | The **System** shall enforce monthly payout cap of ₹5,000 per teacher. |
| S-PAY-03 | The **System** shall generate weekly payout files for treasury/DBT integration. |

### 2.6 Send Notification (Automated)

| ID | System Behavior |
|----|-----------------|
| S-NOT-01 | The **System** shall send push notification to Teacher when submission is auto-rejected. |
| S-NOT-02 | The **System** shall include rejection reason in the notification. |

---

## 3. Verifier

### 3.1 Review Evidence & Compare Duplicates

| ID | User Story |
|----|------------|
| [V-REV-01](./user-stories/verifier/V-REV-01.md) | As a **Verifier**, I want to see the dog photo, selfie, map location, and EXIF metadata side-by-side, so that I can assess the submission's validity. |
| [V-REV-02](./user-stories/verifier/V-REV-02.md) | As a **Verifier**, I want to see potential duplicate matches displayed alongside the current submission, so that I can identify resubmissions. |
| [V-REV-03](./user-stories/verifier/V-REV-03.md) | As a **Verifier**, I want to see fraud flags (GPS mismatch, timestamp issues, high submission rate), so that I can prioritize suspicious cases. |
| [V-REV-04](./user-stories/verifier/V-REV-04.md) | As a **Verifier**, I want to see an SLA indicator highlighting submissions approaching the 24-hour verification limit, so that I can meet turnaround targets. |

### 3.2 Approve Application

| ID | User Story |
|----|------------|
| [V-APP-01](./user-stories/verifier/V-APP-01.md) | As a **Verifier**, I want to approve valid submissions with a single click, so that I can process the queue efficiently. |
| [V-APP-02](./user-stories/verifier/V-APP-02.md) | As a **Verifier**, I want approved applications to be automatically routed to the Municipal Corporation queue, so that field action can begin. |

### 3.3 Reject/Duplicate

| ID | User Story |
|----|------------|
| [V-REJ-01](./user-stories/verifier/V-REJ-01.md) | As a **Verifier**, I want to reject invalid submissions and select a reason from a predefined list, so that rejection reasons are consistent and trackable. |
| [V-REJ-02](./user-stories/verifier/V-REJ-02.md) | As a **Verifier**, I want to mark submissions as duplicates and link them to the original submission, so that duplicate tracking is maintained. |
| [V-REJ-03](./user-stories/verifier/V-REJ-03.md) | As a **Verifier**, I want to escalate ambiguous cases to a senior verifier, so that edge cases are handled appropriately. |
| [V-REJ-04](./user-stories/verifier/V-REJ-04.md) | As a **Verifier**, I want to perform bulk actions on obvious duplicates within the same cluster, so that I can clear the queue faster. |

### 3.4 Send Notification

| ID | User Story |
|----|------------|
| [V-NOT-01](./user-stories/verifier/V-NOT-01.md) | As a **Verifier**, I want the system to notify the Teacher when I reject or mark their submission as duplicate, so that they are informed of the decision. |
| [V-NOT-02](./user-stories/verifier/V-NOT-02.md) | As a **Verifier**, I want my verifier ID and timestamp to be logged for audit purposes, so that there is accountability for decisions. |

---

## 4. MC Officer (Municipal Corporation)

[View MC Officer Module Index](./user-stories/mc-officer/index.md)

### 4.1 View Verified Applications

| ID | User Story |
|----|------------|
| [MC-VIEW-01](./user-stories/mc-officer/MC-VIEW-01.md) | As an **MC Officer**, I want to see a map view with verified incident markers in my jurisdiction, so that I can visualize where stray dogs have been reported. |
| [MC-VIEW-02](./user-stories/mc-officer/MC-VIEW-02.md) | As an **MC Officer**, I want to see a heatmap overlay showing incident density, so that I can identify hotspot areas. |
| [MC-VIEW-03](./user-stories/mc-officer/MC-VIEW-03.md) | As an **MC Officer**, I want to filter the incident queue by date, severity, area, and status, so that I can prioritize my work. |
| [MC-VIEW-04](./user-stories/mc-officer/MC-VIEW-04.md) | As an **MC Officer**, I want to see incident details including dog photo, GPS coordinates, condition tags, and timestamp, so that I can prepare for field visits. |
| [MC-VIEW-05](./user-stories/mc-officer/MC-VIEW-05.md) | As an **MC Officer**, I want the Teacher's identity and selfie to be hidden from my view, so that reporter privacy is protected. |

### 4.2 Field Visit & Take Action

| ID | User Story |
|----|------------|
| [MC-FLD-01](./user-stories/mc-officer/MC-FLD-01.md) | As an **MC Officer**, I want to update the incident status to "In Progress" when I begin a field visit, so that the system reflects my activity. |
| [MC-FLD-02](./user-stories/mc-officer/MC-FLD-02.md) | As an **MC Officer**, I want to navigate to the incident location using the provided GPS coordinates, so that I can find the reported dog. |

### 4.3 Mark Captured/Resolved

| ID | User Story |
|----|------------|
| [MC-CAP-01](./user-stories/mc-officer/MC-CAP-01.md) | As an **MC Officer**, I want to mark an incident as "Captured" or "Resolved" when the dog has been successfully handled, so that the case can be closed and payout triggered. |
| [MC-CAP-02](./user-stories/mc-officer/MC-CAP-02.md) | As an **MC Officer**, I want to add resolution notes describing the outcome, so that there is a record of what was done. |

### 4.4 Mark Unable to Locate

| ID | User Story |
|----|------------|
| [MC-UTL-01](./user-stories/mc-officer/MC-UTL-01.md) | As an **MC Officer**, I want to mark an incident as "Unable to Locate" if the dog cannot be found, so that the case is closed appropriately. |
| [MC-UTL-02](./user-stories/mc-officer/MC-UTL-02.md) | As an **MC Officer**, I want to add notes explaining why the dog could not be located, so that there is context for the outcome. |

### 4.5 Send Notification

| ID | User Story |
|----|------------|
| [MC-NOT-01](./user-stories/mc-officer/MC-NOT-01.md) | As an **MC Officer**, I want the Teacher to be notified when I successfully capture/resolve an incident, so that they know their report led to action and payout is coming. |
| [MC-NOT-02](./user-stories/mc-officer/MC-NOT-02.md) | As an **MC Officer**, I want the Teacher to be notified when I mark an incident as "Unable to Locate", so that they know the outcome (no payout in this case). |

---

## 5. District Administrator

[View District Admin Module Index](./user-stories/district-admin/index.md)

| ID | User Story | Details |
|----|------------|---------|
| [DA-01](./user-stories/district-admin/DA-01.md) | As a **District Administrator**, I want to see a comprehensive dashboard with key metrics, so that I can monitor program activity at a glance. | Dashboard Overview |
| [DA-02](./user-stories/district-admin/DA-02.md) | As a **District Administrator**, I want to see submission volume trends and verification funnel metrics, so that I can identify bottlenecks. | Submission & Verification Metrics |
| [DA-03](./user-stories/district-admin/DA-03.md) | As a **District Administrator**, I want to monitor MC action rates and pending cases, so that I can ensure downstream accountability. | MC Action Monitoring |
| [DA-04](./user-stories/district-admin/DA-04.md) | As a **District Administrator**, I want to see top-performing blocks by submission volume and resolution rate, so that I can identify successful areas. | Performance Metrics |
| [DA-05](./user-stories/district-admin/DA-05.md) | As a **District Administrator**, I want to manage verifier accounts and monitor their performance, so that verification quality is maintained. | Verifier Management |
| [DA-06](./user-stories/district-admin/DA-06.md) | As a **District Administrator**, I want to see fraud indicators and suspicious patterns, so that I can identify and address misuse. | Fraud Detection Dashboard |
| [DA-07](./user-stories/district-admin/DA-07.md) | As a **District Administrator**, I want to generate and export reports, so that I can share them in meetings and with stakeholders. | Report Generation |

---

## 6. State Administrator (AICOE)

[View State Admin Module Index](./user-stories/state-admin/index.md)

| ID | User Story | Details |
|----|------------|---------|
| [SA-01](./user-stories/state-admin/SA-01.md) | As a **State Administrator**, I want to see statewide submission and verification metrics with KPI tracking, so that I can assess overall program health. | Statewide Metrics Dashboard |
| [SA-02](./user-stories/state-admin/SA-02.md) | As a **State Administrator**, I want to compare districts by participation rate, fraud rate, and payout, so that I can identify best and worst performers. | District Comparison |
| [SA-03](./user-stories/state-admin/SA-03.md) | As a **State Administrator**, I want to see budget utilization with projections and alerts, so that I can manage program finances effectively. | Budget Utilization |
| [SA-04](./user-stories/state-admin/SA-04.md) | As a **State Administrator**, I want to see fraud analytics with pattern detection, network visualization, and anomaly alerts, so that I can prevent misuse at scale. | Fraud Analytics |
| [SA-05](./user-stories/state-admin/SA-05.md) | As a **State Administrator**, I want to see MC action rates and response times by district, so that I can ensure downstream accountability across the state. | MC Action Rates by District |
| [SA-06](./user-stories/state-admin/SA-06.md) | As a **State Administrator**, I want to configure system parameters with maker-checker approval workflow, so that I can adjust program rules safely. | System Configuration |
| [SA-07](./user-stories/state-admin/SA-07.md) | As a **State Administrator**, I want to receive real-time alerts for unusual activity spikes and critical events, so that I can respond to potential issues quickly. | Real-time Alerts |

---

## Payout Structure

| Resolution Type | Teacher Payout | Condition |
|-----------------|----------------|-----------|
| Captured/Resolved | ₹500 | Dog successfully captured/resolved by MC |
| Unable to Locate | ₹0 | Dog not found at location |
| Rejected | ₹0 | Invalid or fraudulent submission |

**Monthly Cap**: ₹5,000 per teacher
**Fixed Rate**: ₹500 per successfully resolved incident

---

## Story Statistics Summary

| Category | Stories | Priority |
|----------|---------|----------|
| Teacher | 17 | Authentication, Submission, Offline, Status, Payout |
| System | 12 | Validation, Auto-Reject, Routing, Payouts, Notifications |
| Verifier | 12 | Review, Approve, Reject, Notifications |
| MC Officer | 13 | View, Field, Capture, UTL, Notifications |
| District Admin | 7 | Dashboard, Metrics, Performance, Fraud, Reports |
| State Admin | 7 | Statewide Metrics, Comparison, Budget, Fraud, Config, Alerts |

---

## Acceptance Criteria Notes

Each linked story contains detailed acceptance criteria including:
- Functional requirements with Given/When/Then format
- Non-functional requirements (performance, security, accessibility)
- UI/UX mockups in ASCII format
- Technical implementation with code samples
- API endpoint specifications
- Dependencies and related stories

---

## Related Documents

- [Design Output #1: Process Workflow](./01-process-workflow.md)
- [Product Requirements Document](../PRD.md)

---
