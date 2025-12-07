# District Administrator User Stories

## Overview
District Administrators (DA) manage and oversee SDCRS operations within their assigned district in Bihar. They have visibility into all activities, can manage verifiers and MC officers, monitor performance metrics, and handle escalations within their jurisdiction.

## Role Permissions
- View all district-level metrics and dashboards
- Manage verifier and MC officer accounts within district
- Access fraud detection reports for district
- Handle escalated cases
- Generate district-level reports

## User Stories

| ID | Title | Priority | Status |
|----|-------|----------|--------|
| [DA-01](./DA-01.md) | District Dashboard Overview | High | Complete |
| [DA-02](./DA-02.md) | View Submission and Verification Metrics | High | Complete |
| [DA-03](./DA-03.md) | Monitor MC Action Rates and Pending Cases | High | Complete |
| [DA-04](./DA-04.md) | View Teacher Leaderboard for District | Medium | Complete |
| [DA-05](./DA-05.md) | Manage Verifiers | Medium | Complete |
| [DA-06](./DA-06.md) | Fraud Detection Dashboard | High | Complete |
| [DA-07](./DA-07.md) | Generate District Reports | Medium | Complete |

## Story Dependencies

```
DA-01 (Dashboard)
├── DA-02 (Metrics) - summary metrics feed dashboard
├── DA-03 (MC Actions) - pending cases widget
├── DA-04 (Leaderboard) - top teachers preview
└── DA-06 (Fraud) - alert indicators

DA-02 (Metrics)
├── S-VAL (Validation) - submission counts
├── V-APP/V-REJ (Verification) - approval rates
└── S-NOT (Notifications) - alert thresholds

DA-03 (MC Actions)
├── MC-CAP (Capture) - resolution data
├── MC-UTL (UTL) - escalation handling
└── S-RTE (Routing) - case assignments

DA-05 (Manage Verifiers)
├── V-REV (Review Queue) - workload visibility
└── S-NOT (Notifications) - account notifications

DA-06 (Fraud Detection)
├── SA-04 (State Fraud Analytics) - pattern data
├── S-VAL (Validation) - anomaly flags
└── S-NOT (Notifications) - fraud alerts
```

## Integration Points

### With Teacher Module
- View teacher submission statistics
- Access teacher penalty/suspension status
- See teacher payout history

### With Verifier Module
- Manage verifier accounts
- Monitor verification queues
- Track verifier performance

### With MC Officer Module
- Monitor pending case backlog
- Track resolution rates
- View field officer performance

### With System Module
- Receive automated alerts
- Access routing information
- View notification history

### With State Admin Module
- Data rolls up to state level
- Receive configuration changes
- Follow state-defined thresholds

## Key Metrics Tracked

| Metric | Target | Source |
|--------|--------|--------|
| Verification SLA | 48 hours | DA-02 |
| MC Resolution Rate | 80% | DA-03 |
| Fraud Detection Rate | < 2% | DA-06 |
| Teacher Participation | 60% | DA-04 |
| Report Generation | Monthly | DA-07 |

## Navigation Structure
```
District Admin Portal
├── Dashboard (DA-01)
├── Metrics & Analytics
│   ├── Submissions (DA-02)
│   ├── MC Actions (DA-03)
│   └── Leaderboard (DA-04)
├── User Management
│   └── Verifiers (DA-05)
├── Fraud Detection (DA-06)
└── Reports (DA-07)
```
