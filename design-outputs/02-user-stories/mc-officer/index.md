# MC Officer User Stories

[← Back to User Stories Master](../user-stories-master.md)

## Overview

MC (Municipal Corporation) Officers are field personnel responsible for responding to verified stray dog incidents. They receive assignments, navigate to locations, and capture/resolve reported dogs.

**Key Privacy Constraint**: MC Officers cannot see teacher personal information (name, phone, email) to protect teacher identity.

## Stories Index

### MC-VIEW: View Assigned Incidents
| Story ID | Title | Priority | Status |
|----------|-------|----------|--------|
| [MC-VIEW-01](./MC-VIEW-01.md) | View Assigned Incidents on Map | High | Draft |
| [MC-VIEW-02](./MC-VIEW-02.md) | Heatmap Overlay for Incident Density | Medium | Draft |
| [MC-VIEW-03](./MC-VIEW-03.md) | Filter Incident Queue | Medium | Draft |
| [MC-VIEW-04](./MC-VIEW-04.md) | View Incident Details | High | Draft |
| [MC-VIEW-05](./MC-VIEW-05.md) | Teacher Privacy Protection | High | Draft |

### MC-FLD: Field Operations
| Story ID | Title | Priority | Status |
|----------|-------|----------|--------|
| [MC-FLD-01](./MC-FLD-01.md) | Update Status to "In Progress" | High | Draft |
| [MC-FLD-02](./MC-FLD-02.md) | Navigate to Incident Location | High | Draft |

### MC-CAP: Capture & Resolution
| Story ID | Title | Priority | Status |
|----------|-------|----------|--------|
| [MC-CAP-01](./MC-CAP-01.md) | Mark Captured/Resolved | Critical | Draft |
| [MC-CAP-02](./MC-CAP-02.md) | Add Resolution Notes and Photo | High | Draft |

### MC-UTL: Unable to Locate
| Story ID | Title | Priority | Status |
|----------|-------|----------|--------|
| [MC-UTL-01](./MC-UTL-01.md) | Mark Unable to Locate | High | Draft |
| [MC-UTL-02](./MC-UTL-02.md) | Add UTL Notes | High | Draft |

### MC-NOT: Notifications (System-Triggered)
| Story ID | Title | Priority | Status |
|----------|-------|----------|--------|
| [MC-NOT-01](./MC-NOT-01.md) | Notify Teacher on Capture | Critical | Draft |
| [MC-NOT-02](./MC-NOT-02.md) | Notify Teacher on UTL | High | Draft |

## Workflow Summary

```
┌─────────────────────────────────────────────────────────────────┐
│                     MC Officer Workflow                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. VIEW ASSIGNMENTS                                            │
│     ├── Map view with pins (MC-VIEW-01)                        │
│     ├── Heatmap overlay (MC-VIEW-02)                           │
│     ├── Filter queue (MC-VIEW-03)                              │
│     └── View details (MC-VIEW-04)                              │
│         └── Privacy filtered (MC-VIEW-05)                      │
│                                                                 │
│  2. RESPOND TO INCIDENT                                         │
│     ├── Mark "In Progress" (MC-FLD-01)                         │
│     └── Navigate to location (MC-FLD-02)                       │
│                                                                 │
│  3. RESOLUTION (One of the following)                          │
│     │                                                          │
│     ├── SUCCESS: Dog Captured/Resolved                         │
│     │   ├── Mark captured (MC-CAP-01) ──► TRIGGERS PAYOUT     │
│     │   ├── Add notes/photo (MC-CAP-02)                       │
│     │   └── System notifies teacher (MC-NOT-01)               │
│     │                                                          │
│     └── FAILURE: Unable to Locate                              │
│         ├── Mark UTL (MC-UTL-01) ──► NO PAYOUT                │
│         ├── Add UTL notes (MC-UTL-02)                         │
│         └── System notifies teacher (MC-NOT-02)               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Key Business Rules

1. **Payout Trigger**: Teacher payout is ONLY triggered when MC marks incident as "Captured" or "Resolved" (MC-CAP-01)
2. **No Payout for UTL**: If MC marks "Unable to Locate", NO payout is processed for the teacher
3. **Privacy Protection**: MC Officers cannot see teacher PII - only anonymized reporter ID
4. **Response Time Tracking**: Time from assignment to resolution is tracked for performance metrics
5. **Offline Support**: All field operations work offline and sync when connected

## Technical Components

### Shared Services
- `IncidentService` - Fetch and update incidents
- `NavigationService` - Deep linking to navigation apps
- `OfflineQueueService` - Queue actions for offline sync
- `PrivacyFilterService` - Filter PII from responses

### State Management
- `useFilterStore` - Incident filter state (Zustand)
- `useIncidentStore` - Current incident state

### Key Dependencies
- MapLibre GL - Maps and heatmaps
- idb - IndexedDB for offline storage
- Web Speech API - Voice input for notes

## Related Actor Stories

- [Teacher Stories](../teacher/index.md) - Report submitters
- [Verifier Stories](../verifier/index.md) - Report validators
- [System Stories](../system/index.md) - Automated processes
- [Admin Stories](../admin/index.md) - System administrators
