# T-PAY-03: Cumulative Earnings Dashboard

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** see my cumulative earnings from the program,
**So that** I can track my total contribution and rewards.

---

## Description

Teachers should see a summary of their total earnings and contribution to the SDCRS program. This includes total money earned, number of successful captures, and key statistics. This motivates continued participation and provides a sense of accomplishment.

---

## Acceptance Criteria

### Functional

- [ ] Earnings summary displayed on dashboard/home screen
- [ ] Total earnings amount prominently shown
- [ ] Number of successful captures displayed
- [ ] Current month vs previous month comparison (optional)
- [ ] Quick link to payout history
- [ ] Quick link to submit new report
- [ ] Stats refresh on app open
- [ ] Show last payout date and amount
- [ ] Earnings visible even when offline (cached)

### Dashboard Metrics

| Metric | Description |
|--------|-------------|
| Total Earned | Sum of all completed payouts |
| Successful Captures | Count of dogs captured from submissions |
| Pending Payouts | Amount currently processing |
| This Month | Earnings in current calendar month |
| Total Submissions | All submissions (including rejected) |
| Success Rate | % of submissions that led to capture |

---

## UI/UX Requirements (PWA)

### Home Dashboard

```
┌─────────────────────────────────┐
│  ☰  SDCRS              👤       │
├─────────────────────────────────┤
│                                 │
│  Good morning, Ahmed!           │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  💰 Your Earnings               │
│  ┌───────────────────────────┐  │
│  │                           │  │
│  │  Total Earned             │  │
│  │  2,500 DJF               │  │
│  │                           │  │
│  │  ┌─────────┬─────────┐   │  │
│  │  │ 🐕 5    │ 💵 500   │   │  │
│  │  │Captures │This Month│   │  │
│  │  └─────────┴─────────┘   │  │
│  │                           │  │
│  │  Last payout: Dec 10      │  │
│  │                   [View →]│  │
│  └───────────────────────────┘  │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  📊 Your Activity               │
│  ┌───────────────────────────┐  │
│  │                           │  │
│  │  Total Submissions: 8     │  │
│  │  Success Rate: 62%        │  │
│  │                           │  │
│  │  ● Captured: 5            │  │
│  │  ● Unable to Locate: 1    │  │
│  │  ● Rejected: 2            │  │
│  │                           │  │
│  └───────────────────────────┘  │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  [  📸 Report a Stray Dog  ]    │
│                                 │
└─────────────────────────────────┘
```

### Compact Earnings Card (Alternative)

```
┌─────────────────────────────────┐
│  💰 2,500 DJF earned            │
│  5 dogs captured · [History →]  │
└─────────────────────────────────┘
```

### Detailed Stats Screen

```
┌─────────────────────────────────┐
│  ← My Statistics                │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐  │
│  │      💰 Total Earned       │  │
│  │        2,500 DJF          │  │
│  └───────────────────────────┘  │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Earnings Breakdown             │
│                                 │
│  Dec 2024        1,000 DJF      │
│  ████████░░░░░░░░ 2 captures    │
│                                 │
│  Nov 2024        1,500 DJF      │
│  ████████████░░░░ 3 captures    │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Submission Statistics          │
│                                 │
│  Total Submitted        8       │
│  Verified              6        │
│  Captured              5        │
│  Unable to Locate      1        │
│  Rejected              2        │
│                                 │
│  Success Rate          62%      │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Bank Account                   │
│  ****4567 · BCD Djibouti        │
│  [Update Bank Details →]        │
│                                 │
└─────────────────────────────────┘
```

### New User - No Earnings Yet

```
┌─────────────────────────────────┐
│  💰 Your Earnings               │
│  ┌───────────────────────────┐  │
│  │                           │  │
│  │  Start earning today!     │  │
│  │                           │  │
│  │  Report stray dogs in     │  │
│  │  your area and earn       │  │
│  │  500 DJF for each dog     │  │
│  │  successfully captured.   │  │
│  │                           │  │
│  │  [  Get Started  ]        │  │
│  │                           │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Cache earnings data for offline display
- [ ] Show "Last updated" timestamp
- [ ] Pull-to-refresh to update stats
- [ ] Skeleton loading for stats
- [ ] Animate number changes on refresh
- [ ] Show trend indicators (up/down from last month)
- [ ] Deep link to detailed stats from home card

---

## Technical Notes

### Earnings Summary API

```typescript
interface EarningsSummary {
  totalEarned: number;
  totalCaptures: number;
  pendingAmount: number;
  thisMonth: {
    earned: number;
    captures: number;
  };
  lastPayout: {
    amount: number;
    date: string;
    applicationId: string;
  } | null;
  submissions: {
    total: number;
    verified: number;
    captured: number;
    unableToLocate: number;
    rejected: number;
  };
  successRate: number; // percentage
  monthlyBreakdown: MonthlyEarnings[];
}

interface MonthlyEarnings {
  month: string; // "2024-12"
  label: string; // "December 2024"
  earned: number;
  captures: number;
}
```

### Fetch Earnings Summary

```javascript
async function fetchEarningsSummary() {
  const token = await getAuthToken();

  const response = await fetch('/api/teacher/earnings-summary', {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  const data = await response.json();

  // Cache for offline
  await cacheEarningsSummary(data);

  return data;
}
```

### Cache Earnings Summary

```javascript
async function cacheEarningsSummary(summary) {
  const db = await openDB('sdcrs-cache', 1, {
    upgrade(db) {
      if (!db.objectStoreNames.contains('summary')) {
        db.createObjectStore('summary');
      }
    }
  });

  await db.put('summary', {
    ...summary,
    cachedAt: Date.now()
  }, 'earnings');
}

async function getCachedEarningsSummary() {
  const db = await openDB('sdcrs-cache', 1);
  return db.get('summary', 'earnings');
}
```

### Earnings Dashboard Component

```jsx
function EarningsDashboard() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isOffline, setIsOffline] = useState(!navigator.onLine);

  useEffect(() => {
    loadSummary();

    // Listen for online/offline
    window.addEventListener('online', loadSummary);
    window.addEventListener('offline', () => setIsOffline(true));

    return () => {
      window.removeEventListener('online', loadSummary);
      window.removeEventListener('offline', () => setIsOffline(true));
    };
  }, []);

  const loadSummary = async () => {
    setLoading(true);

    try {
      if (navigator.onLine) {
        const data = await fetchEarningsSummary();
        setSummary(data);
        setIsOffline(false);
      } else {
        const cached = await getCachedEarningsSummary();
        setSummary(cached);
        setIsOffline(true);
      }
    } catch (error) {
      // Fall back to cache
      const cached = await getCachedEarningsSummary();
      setSummary(cached);
    }

    setLoading(false);
  };

  if (loading) return <EarningsSkeleton />;
  if (!summary) return <NoEarningsState />;

  return (
    <div className="earnings-dashboard">
      {isOffline && (
        <div className="offline-badge">Viewing cached data</div>
      )}

      <div className="total-earned">
        <span className="label">Total Earned</span>
        <AnimatedNumber value={summary.totalEarned} />
        <span className="currency">DJF</span>
      </div>

      <div className="stats-row">
        <div className="stat">
          <span className="value">{summary.totalCaptures}</span>
          <span className="label">Captures</span>
        </div>
        <div className="stat">
          <span className="value">{summary.thisMonth.earned}</span>
          <span className="label">This Month</span>
        </div>
      </div>

      {summary.lastPayout && (
        <div className="last-payout">
          Last payout: {formatDate(summary.lastPayout.date)}
          <button onClick={() => navigate('/payouts')}>View →</button>
        </div>
      )}
    </div>
  );
}
```

### Success Rate Calculation

```javascript
function calculateSuccessRate(submissions) {
  const { total, captured } = submissions;

  if (total === 0) return 0;

  // Success = captured / (total - rejected for invalid reasons)
  return Math.round((captured / total) * 100);
}
```

### Animated Number Component

```jsx
function AnimatedNumber({ value, duration = 1000 }) {
  const [displayValue, setDisplayValue] = useState(0);

  useEffect(() => {
    const startValue = displayValue;
    const startTime = Date.now();

    const animate = () => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(elapsed / duration, 1);

      // Ease out quad
      const eased = 1 - (1 - progress) * (1 - progress);
      const current = Math.round(startValue + (value - startValue) * eased);

      setDisplayValue(current);

      if (progress < 1) {
        requestAnimationFrame(animate);
      }
    };

    requestAnimationFrame(animate);
  }, [value]);

  return (
    <span className="animated-number">
      {displayValue.toLocaleString()}
    </span>
  );
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Earnings API | Backend | Summary data |
| IndexedDB | Browser | Offline cache |
| idb | Library | IndexedDB wrapper |

---

## Related Stories

- [T-PAY-01](./T-PAY-01.md) - Payout notification
- [T-PAY-02](./T-PAY-02.md) - View payout history
- [T-AUTH-03](./T-AUTH-03.md) - Bank account verification
- [T-STAT-01](./T-STAT-01.md) - View submission status
