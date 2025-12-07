# Design Output #1: Process Workflow

## SDCRS - Stray Dog Capture & Reporting System

---

## Overview

This document captures the end-to-end process workflow for the Stray Dog Capture & Reporting System, following the DIGIT Design Guide methodology.

**Key Business Rule:** Teachers receive payout ONLY after Municipal Corporation successfully captures/resolves the reported dog.

---

## Process Workflow Diagram

![SDCRS Process Workflow](../assets/sdcrs-workflow.png)

---

## Swimlanes & Roles

| Lane | Role | Description |
|------|------|-------------|
| **Teacher** | Reporter | Submits stray dog sighting applications, views status updates, receives payouts |
| **System** | Automated | Validates submissions, routes to queues, processes payouts, sends automated notifications |
| **Verifier** | Backend Operator | Reviews evidence, compares duplicates, approves/rejects applications, sends manual notifications |
| **MC Officer** | Municipal Corporation | Views verified applications, conducts field visits, marks outcomes, sends manual notifications |

---

## Process Flow Description

### 1. Submission Phase (Teacher → System)

1. **Teacher** starts the process and submits an application with:
   - Photo of stray dog
   - Selfie at the location
   - GPS coordinates (auto-captured)

2. **System** validates the submission:
   - GPS presence and authenticity
   - GPS within valid tenant boundary (using `egov-location` service)
   - Timestamp within 48 hours
   - Image hash computation for duplicate detection

### 2. Validation Decision (System)

| Outcome | Path |
|---------|------|
| **Invalid** (missing GPS, outside boundary, old timestamp, duplicate image) | Auto Reject → System sends notification → Teacher views updated status |
| **Valid** | Route to Verification Queue → Verifier review |

### 3. Verification Phase (Verifier)

1. **Verifier** reviews evidence and compares with potential duplicates
2. Decision gateway:

| Decision | Path |
|----------|------|
| **Approve** | Approve Application → Route to MC Queue |
| **Reject/Duplicate** | Reject → Verifier sends notification → Teacher views updated status |

### 4. Field Action Phase (MC Officer)

1. **MC Officer** views verified applications in their jurisdiction
2. Conducts field visit and takes action
3. Outcome gateway:

| Outcome | Path |
|---------|------|
| **Success** | Mark Captured/Resolved → Award Points & Process Payout → MC sends notification → Teacher receives payout → views updated status |
| **Failure** | Mark Unable to Locate → MC sends notification → Teacher views updated status (NO PAYOUT) |

---

## Notification Ownership

| Scenario | Notification Sender | Type |
|----------|---------------------|------|
| Auto-reject (invalid submission) | System | Automated |
| Verifier rejection/duplicate | Verifier | Manual |
| MC successful capture | MC Officer | Manual |
| MC unable to locate | MC Officer | Manual |

---

## Status States

| Status | Description | Trigger |
|--------|-------------|---------|
| `Pending` | Awaiting system validation | Submission received |
| `Under Review` | In verifier queue | Passed auto-validation |
| `Verified` | Approved by verifier | Verifier approval |
| `Rejected` | Rejected by system or verifier | Failed validation or manual rejection |
| `Duplicate` | Marked as duplicate of another submission | Verifier action |
| `Assigned` | Assigned to MC Officer | Routed to MC queue |
| `In Progress` | MC Officer conducting field visit | MC status update |
| `Captured/Resolved` | Dog successfully captured | MC marks success → triggers payout |
| `Unable to Locate` | Dog not found at location | MC marks failure → no payout |

---

## Payout Trigger

**Critical:** Payout is triggered ONLY when:
1. Application is verified by Verifier (approved)
2. AND MC Officer marks it as "Captured/Resolved"

No payout is issued for:
- Auto-rejected submissions
- Verifier-rejected submissions
- Duplicate submissions
- "Unable to Locate" outcomes

---

## Source Files

- **Workflow Generator:** `generate_workflow.py`
- **Workflow Diagram:** `assets/sdcrs-workflow.png`

---

*Document Version: 1.0*
*Last Updated: December 2024*
*Status: Approved*
