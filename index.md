# Stray Dog Capture & Reporting System

## Initial Problem Statement
The assignment involves building an eGov software solution for a school-mandated stray dog capture program. The system will enable teachers to report dog captures through a workflow that includes uploading an image of the dog, taking a selfie at the capture location for verification, and sharing a permalink that displays the current workflow state. Key features include geolocation tagging, gamification with awards based on captures, and a payout mechanism tied to the number of dogs reported. The platform requires two dashboards—one for downstream dog capture tracking and another for viewing all incidents—along with a notification system. Duplicate photo submissions will be flagged for human verification. The system must also support operation without SSO while still allowing self sign-on through AICOE, with all data exportable via permalinks. From a scale perspective, the solution needs to handle peak loads of approximately 400K teachers (representing 15% of the population) submitting up to 5 photos per day. The technical scope includes building out the data pipeline and necessary configurations.

## PRD

### Personas
See [PRD Section 4](./prd.md#4-user-personas) for detailed persona definitions:
- Teacher (Reporter)
- Verifier (Backend Operator)
- MC Field Officer
- District Administrator
- State Administrator

### Document
Based on the above, a [detailed PRD](./prd.md) was generated through Claude. Which was then refined through manual edits.

## Swimlanes
The PRD allowed me to generate [swimlanes](./swimlanes.md) for the various actors in the system.
![Workflow Diagram](./design-outputs/assets/SDCRS_Swimlane_UPI.png)

Generated with PlantUML from [SDCRS_Swimlane_UPI.puml](./design-outputs/06-workflow/SDCRS_Swimlane_UPI.puml).

## User Stories
PRD was expanded to single line [user stories](./design-outputs/02-user-stories.md) to help define the problem space. Expanded further into [detailed user stories](./design-outputs/02-user-stories/) (68 stories across 6 roles).

## Metrics and Performance Indicators
See [Service Design - Section 12](./design-outputs/03-service-design.md#12-process-performance-indicators-ppis) for complete PPI definitions covering:
- Report volume metrics
- Time-based SLA metrics
- Resolution & outcome metrics
- Payout metrics
- User performance metrics
- Quality & fraud prevention metrics

## Dashboards
1. [Design Considerations](./design-outputs/dashboard-designs/README.md)
2. HTML Mockups (Live):
   - [Teacher Dashboard](https://dashboard-designs-fawn.vercel.app/01-teacher-dashboard.html)
   - [Verifier Dashboard](https://dashboard-designs-fawn.vercel.app/02-verifier-dashboard.html)
   - [MC Officer Dashboard](https://dashboard-designs-fawn.vercel.app/03-mc-officer-dashboard.html)
   - [MC Supervisor Dashboard](https://dashboard-designs-fawn.vercel.app/04-mc-supervisor-dashboard.html)
   - [District Admin Dashboard](https://dashboard-designs-fawn.vercel.app/05-district-admin-dashboard.html)
   - [State Admin Dashboard](https://dashboard-designs-fawn.vercel.app/06-state-admin-dashboard.html)

## Initial Design Problems
See [Design Problems & Solutions](./design-problems.md) for:
1. Public Sharable Permalinks
2. Scale
3. Payment to users

## Additional Problem Statements from User Stories
1. Linking an external SSO - HRMS system → [HRMS-SSO Integration](./design-outputs/HRMS-SSO-DIGIT-integration.md)
2. Defining Fraud → [Fraud Detection Service](./design-outputs/08-fraud-detection-service.md)
3. Detecting Fraud → [Fraud Detection Service](./design-outputs/08-fraud-detection-service.md)

All addressed in [Design Problems](./design-problems.md).

## Solution
1. [DIGIT Platform overview and reusable services](./solution-architecture.md#digit-platform-what-it-provides)
2. [Gaps requiring custom development](./solution-architecture.md#gaps-custom-development-required)

### Architectural Options
1. [Solution A - Use CCRS](./design-outputs/04a-ccrs-direct-configuration.md)
2. [Solution B - Build new service(s) on DIGIT](./design-outputs/04b-ccrs-inspired-custom-service.md) (Recommended)
3. [Solution C - Fork CCRS and modify](./design-outputs/04c-ccrs-fork-modified.md)

[Summary Table & Decision Criteria](./solution-architecture.md#architectural-options)

### Service Design for Solution B - No CCRS
- [Service Design Document](./design-outputs/03-service-design.md)
- [API Specification (OpenAPI)](./design-outputs/04b-no-ccrs/06-api-specification.yaml)
- [Implementation Code](./design-outputs/04b-no-ccrs/sdcrs-services/)

### Sequence Diagrams
- [Sequence Diagrams Index](./design-outputs/05-sequence-diagrams.md)
- [Create Flow](./design-outputs/05-sequence-diagrams/SDCRS_Create.puml)
- [Update Flow](./design-outputs/05-sequence-diagrams/SDCRS_Update.puml)
- [Search Flow](./design-outputs/05-sequence-diagrams/SDCRS_Search.puml)
- [Track Flow](./design-outputs/05-sequence-diagrams/SDCRS_Track.puml)
- [UPI Payout Flow](./design-outputs/05-sequence-diagrams/SDCRS_Payout_UPI.puml)

### Workflow Configuration
- [State Machine Diagram](./design-outputs/06-workflow/SDCRS_StateMachine_UPI.puml)
- [JSON Configuration](./design-outputs/03-configs/workflow/BusinessService-with-UPI.json)

### Fraud Detection
1. [Design Considerations](./design-outputs/08-fraud-detection-service.md#overview)
2. [Design - HLD and LLD](./design-outputs/08-fraud-detection-service.md)
3. Spec (MDMS):
   - [FraudRules.json](./design-outputs/03-configs/mdms/FRAUD-DETECTION/FraudRules.json)
   - [ExternalValidators.json](./design-outputs/03-configs/mdms/FRAUD-DETECTION/ExternalValidators.json)
   - [RiskScoreConfig.json](./design-outputs/03-configs/mdms/FRAUD-DETECTION/RiskScoreConfig.json)
4. [Sample Implementation](./design-outputs/04b-no-ccrs/fraud-detection-service/)

### Public Link
See [Service Design - Section 10](./design-outputs/03-service-design.md#10-public-tracking-api):
1. Option A - Dual identifier (Report Number + Tracking ID)
2. Option B - URL Shortener integration

### Scale
1. [Target Scale Definition](./design-outputs/07-scale-management.md#1-load-profile-analysis)
2. [Hotspots Analysis](./design-outputs/07-scale-management.md#2-hotspot-1-photo-upload--processing)
3. [Mitigation Strategies](./design-outputs/07-scale-management.md#12-infrastructure-recommendations)

### Dashboards
[ES Index Design](./design-outputs/03-configs/elasticsearch/)

## Software Development Plan
1. [Development Phases & Deliverables](./development-plan.md)
2. [POCs Required](./development-plan.md#pocs-required)
3. [Team Requirements](./development-plan.md#team-requirements)

---

## Quick Reference

| Design Output | Document |
|---------------|----------|
| Process Workflow | [01-process-workflow-upi.md](./design-outputs/01-process-workflow-upi.md) |
| User Stories | [02-user-stories.md](./design-outputs/02-user-stories.md) |
| Service Design | [03-service-design.md](./design-outputs/03-service-design.md) |
| Option A | [04a-ccrs-direct-configuration.md](./design-outputs/04a-ccrs-direct-configuration.md) |
| Option B | [04b-ccrs-inspired-custom-service.md](./design-outputs/04b-ccrs-inspired-custom-service.md) |
| Option C | [04c-ccrs-fork-modified.md](./design-outputs/04c-ccrs-fork-modified.md) |
| Sequence Diagrams | [05-sequence-diagrams.md](./design-outputs/05-sequence-diagrams.md) |
| Scale Management | [07-scale-management.md](./design-outputs/07-scale-management.md) |
| Fraud Detection | [08-fraud-detection-service.md](./design-outputs/08-fraud-detection-service.md) |
| HRMS SSO Integration | [HRMS-SSO-DIGIT-integration.md](./design-outputs/HRMS-SSO-DIGIT-integration.md) |
| Design Checklist | [00-design-checklist.md](./design-outputs/00-design-checklist.md) |
