# T-STAT-01: View Submission Status

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** view the current status of my submission,
**So that** I know where it is in the review process.

---

## Description

Teachers should be able to track their submissions through the entire lifecycle - from submission to verification to MC capture to payout. A clear status timeline helps set expectations and reduces the need for support inquiries. The status should update in real-time when the teacher views the application.

---

## Acceptance Criteria

### Functional

- [ ] "My Applications" screen shows list of all submissions
- [ ] Each submission displays current status prominently
- [ ] Status includes both state and timestamp of last update
- [ ] Clicking a submission opens detailed status view
- [ ] Status timeline shows all state transitions
- [ ] Pull-to-refresh updates status from server
- [ ] Local pending uploads shown separately from server submissions
- [ ] Filter submissions by status (All, Pending, Approved, Captured, Paid)
- [ ] Search submissions by Application ID

### Status Lifecycle

| Status | Description | Color |
|--------|-------------|-------|
| Pending Verification | Awaiting verifier review | 🟡 Yellow |
| Verified | Approved by verifier | 🟢 Green |
| Rejected | Rejected by verifier | 🔴 Red |
| Assigned to MC | MC Officer assigned | 🔵 Blue |
| Capture Attempted | MC visited location | 🟡 Yellow |
| Captured | Dog successfully captured | 🟢 Green |
| Unable to Locate | Dog not found | 🟠 Orange |
| Payout Initiated | Payment processing | 🔵 Blue |
| Payout Complete | Payment received | 🟢 Green |

---

## UI/UX Requirements (PWA)

### My Applications List

```
┌─────────────────────────────────┐
│  ← My Applications              │
├─────────────────────────────────┤
│                                 │
│  🔍 Search by ID...             │
│                                 │
│  [All] [Pending] [Done] [Paid]  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ SDCRS-20261207-A1B2C      │  │
│  │ 🟢 Captured                │  │
│  │ 📍 Central Block           │  │
│  │ 🕐 Dec 7, 2026             │  │
│  │                      →    │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ SDCRS-20261206-X7Y8Z      │  │
│  │ 🟡 Pending Verification    │  │
│  │ 📍 North Block             │  │
│  │ 🕐 Dec 6, 2026             │  │
│  │                      →    │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ SDCRS-20261205-P3Q4R      │  │
│  │ 🔴 Rejected                │  │
│  │ 📍 East Block              │  │
│  │ 🕐 Dec 5, 2026             │  │
│  │                      →    │  │
│  └───────────────────────────┘  │
│                                 │
│  Load More...                   │
│                                 │
└─────────────────────────────────┘
```

### Detailed Status View

```
┌─────────────────────────────────┐
│  ← Application Details          │
├─────────────────────────────────┤
│                                 │
│  SDCRS-20261207-A1B2C      [📋] │
│                                 │
│  ┌───────────────────────────┐  │
│  │      [Dog Photo]          │  │
│  │                           │  │
│  └───────────────────────────┘  │
│                                 │
│  📍 Central Block, Djibouti     │
│  🏷️ Injured, Collared           │
│  📅 Dec 7, 2026, 2:45 PM        │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Current Status                 │
│  ┌───────────────────────────┐  │
│  │  🟢 CAPTURED               │  │
│  │  Dec 8, 2026, 10:30 AM    │  │
│  └───────────────────────────┘  │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Status Timeline                │
│                                 │
│  ● Captured                     │
│  │ Dec 8, 10:30 AM              │
│  │                              │
│  ● Assigned to MC Officer       │
│  │ Dec 7, 4:00 PM               │
│  │                              │
│  ● Verified                     │
│  │ Dec 7, 3:15 PM               │
│  │                              │
│  ● Submitted                    │
│    Dec 7, 2:45 PM               │
│                                 │
│  [  Share Status Link  ]        │
│                                 │
└─────────────────────────────────┘
```

### Rejected Status View

```
┌─────────────────────────────────┐
│  ← Application Details          │
├─────────────────────────────────┤
│                                 │
│  SDCRS-20261205-P3Q4R      [📋] │
│                                 │
│  Current Status                 │
│  ┌───────────────────────────┐  │
│  │  🔴 REJECTED               │  │
│  │  Dec 5, 2026, 5:00 PM     │  │
│  └───────────────────────────┘  │
│                                 │
│  Rejection Reason:              │
│  ┌───────────────────────────┐  │
│  │ Photo does not clearly     │  │
│  │ show a stray dog. Please   │  │
│  │ ensure the dog is visible  │  │
│  │ in the frame.              │  │
│  └───────────────────────────┘  │
│                                 │
│  You can submit a new report    │
│  with a clearer photo.          │
│                                 │
│  [  Submit New Report  ]        │
│                                 │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Pull-to-refresh for status updates
- [ ] Skeleton loading states while fetching
- [ ] Cache last known status in IndexedDB
- [ ] Offline indicator when viewing cached data
- [ ] Deep linking to specific application by ID
- [ ] Infinite scroll or pagination for large lists
- [ ] Haptic feedback on status refresh

---

## Technical Notes

### Status API Response

```typescript
interface ApplicationStatus {
  applicationId: string;
  currentStatus: {
    code: string;
    label: string;
    timestamp: string;
  };
  timeline: StatusEvent[];
  location: {
    latitude: number;
    longitude: number;
    address: string;
    block: string;
  };
  conditionTags: string[];
  photos: {
    dog: string; // URL
    selfie: string; // URL
  };
  rejectionReason?: string;
  mcOfficer?: {
    name: string;
    phone: string;
  };
  payout?: {
    status: string;
    amount: number;
    transactionId?: string;
  };
}

interface StatusEvent {
  status: string;
  timestamp: string;
  actor?: string;
  notes?: string;
}
```

### Fetch Applications

```javascript
async function fetchMyApplications(filters = {}) {
  const token = await getAuthToken();
  const params = new URLSearchParams({
    page: filters.page || 1,
    limit: filters.limit || 20,
    status: filters.status || '',
    search: filters.search || ''
  });

  const response = await fetch(`/api/applications/my?${params}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) throw new Error('Failed to fetch applications');

  const data = await response.json();

  // Cache for offline viewing
  await cacheApplications(data.applications);

  return data;
}
```

### Status Caching

```javascript
// Cache applications for offline viewing
async function cacheApplications(applications) {
  const db = await openDB('sdcrs-cache', 1, {
    upgrade(db) {
      db.createObjectStore('applications', { keyPath: 'applicationId' });
    }
  });

  const tx = db.transaction('applications', 'readwrite');
  for (const app of applications) {
    await tx.store.put(app);
  }
  await tx.done;
}

// Get cached applications when offline
async function getCachedApplications() {
  const db = await openDB('sdcrs-cache', 1);
  return db.getAll('applications');
}
```

### Status Timeline Component

```jsx
function StatusTimeline({ events }) {
  return (
    <div className="timeline">
      {events.map((event, index) => (
        <div key={index} className="timeline-item">
          <div className={`timeline-dot ${getStatusColor(event.status)}`} />
          {index < events.length - 1 && <div className="timeline-line" />}
          <div className="timeline-content">
            <div className="timeline-status">{event.status}</div>
            <div className="timeline-time">
              {formatDateTime(event.timestamp)}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
```

### Deep Linking

```javascript
// Handle deep links to application
function setupDeepLinking() {
  // Handle URL on app load
  const urlParams = new URLSearchParams(window.location.search);
  const appId = urlParams.get('applicationId');

  if (appId) {
    navigateToApplicationDetail(appId);
  }
}

// Generate shareable link
function getApplicationLink(applicationId) {
  return `${window.location.origin}/status/${applicationId}`;
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Application API | Backend | Fetch status data |
| IndexedDB | Browser | Offline caching |
| idb | Library | IndexedDB wrapper |

---

## Related Stories

- [T-SUB-06](./T-SUB-06.md) - Receive Application ID
- [T-STAT-02](./T-STAT-02.md) - Receive SMS notifications
- [T-STAT-04](./T-STAT-04.md) - Shareable public link
- [T-PAY-01](./T-PAY-01.md) - Payout notification
