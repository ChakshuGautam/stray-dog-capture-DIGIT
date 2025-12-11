# State Administrator User Stories

## Overview
State Administrators (SA) have the highest level of access within the SDCRS platform for Bihar. They oversee all 38 districts, manage statewide budgets, configure system parameters, monitor for fraud patterns across the state, and receive real-time alerts for critical events.

## Role Permissions
- Full access to all district data
- Configure system-wide parameters (payout rates, thresholds)
- Manage state-level budget allocations
- Access comprehensive fraud analytics
- Generate statewide reports
- Receive and manage critical alerts

## User Stories

| ID | Title | Priority | Status |
|----|-------|----------|--------|
| [SA-01](./SA-01.md) | Statewide Submission and Verification Metrics | High | Complete |
| [SA-02](./SA-02.md) | Compare Districts | High | Complete |
| [SA-03](./SA-03.md) | Budget Utilization | Critical | Complete |
| [SA-04](./SA-04.md) | Fraud Analytics | Critical | Complete |
| [SA-05](./SA-05.md) | MC Action Rates by District | High | Complete |
| [SA-06](./SA-06.md) | Configure System Parameters | Critical | Complete |
| [SA-07](./SA-07.md) | Real-time Alerts for Unusual Activity Spikes | Critical | Complete |

## Story Dependencies

```
SA-01 (Statewide Metrics)
├── All DA-02 instances (district metrics roll up)
├── S-VAL (Validation) - statewide submission data
└── S-NOT (Notifications) - KPI alerts

SA-02 (Compare Districts)
├── SA-01 (Metrics) - comparison data source
├── DA-02 (all districts) - district-level data
└── Statistical analysis algorithms

SA-03 (Budget Utilization)
├── S-PTS (Payout Service) - payment data
├── SA-06 (Configuration) - payout rates
└── Financial forecasting models

SA-04 (Fraud Analytics)
├── All DA-06 instances (district fraud data)
├── S-VAL (Validation) - anomaly detection
├── Machine learning models
└── SA-07 (Alerts) - fraud alerts

SA-05 (MC Action Rates)
├── All DA-03 instances (district MC data)
├── MC-CAP, MC-UTL (resolution data)
└── S-RTE (Routing) - assignment data

SA-06 (Configure System Parameters)
├── Maker-checker workflow
├── All system modules (receive config)
└── SA-07 (Alerts) - configuration change alerts

SA-07 (Real-time Alerts)
├── All SA stories (alert sources)
├── All DA stories (district alerts escalate)
├── WebSocket infrastructure
└── S-NOT (Notifications) - delivery
```

## Integration Points

### With District Admin Module
- Aggregates data from all 38 districts
- Pushes configuration changes
- Escalates alerts from district level
- Provides comparative analytics

### With System Module
- Configures all system parameters
- Receives system health alerts
- Manages notification templates
- Controls routing rules

### With Financial Systems
- Budget allocation management
- Payout rate configuration
- Financial reporting
- Audit trail maintenance

### With External Systems
- Government reporting systems
- Financial disbursement systems
- Analytics/BI platforms
- Audit systems

## Key Metrics Tracked

| Metric | State Target | Source |
|--------|--------------|--------|
| Overall Verification Rate | 85% | SA-01 |
| Average Resolution Time | < 5 days | SA-05 |
| Budget Utilization | 80-95% | SA-03 |
| Fraud Rate | < 0.5% | SA-04 |
| Alert Response Time | < 15 min (critical) | SA-07 |
| System Uptime | 99.9% | SA-06 |

## Bihar District Coverage

The State Admin oversees all 38 districts of Bihar:

| Division | Districts |
|----------|-----------|
| Patna | Patna, Nalanda, Bhojpur, Buxar, Rohtas, Kaimur, Arwal, Aurangabad, Gaya, Nawada, Jehanabad |
| Tirhut | Muzaffarpur, Vaishali, Sitamarhi, Sheohar, East Champaran, West Champaran |
| Darbhanga | Darbhanga, Madhubani, Samastipur |
| Kosi | Saharsa, Supaul, Madhepura |
| Purnia | Purnia, Katihar, Araria, Kishanganj |
| Bhagalpur | Bhagalpur, Banka |
| Munger | Munger, Lakhisarai, Sheikhpura, Jamui, Khagaria, Begusarai |
| Saran | Saran, Siwan, Gopalganj |

## Navigation Structure
```
State Admin Portal
├── Dashboard (Home)
│   ├── Statewide Metrics (SA-01)
│   └── Alert Summary (SA-07 preview)
├── Analytics
│   ├── District Comparison (SA-02)
│   ├── Fraud Analytics (SA-04)
│   └── MC Action Rates (SA-05)
├── Budget
│   └── Budget Utilization (SA-03)
├── Administration
│   └── System Configuration (SA-06)
├── Alerts
│   └── Real-time Alerts (SA-07)
└── Reports
    └── Statewide Reports
```

## Security & Audit
- All configuration changes require maker-checker approval
- Comprehensive audit trail for all admin actions
- Role-based access control within State Admin team
- Sensitive operations require additional authentication
- Data export requires authorization
