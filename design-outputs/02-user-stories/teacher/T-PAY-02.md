# T-PAY-02: View Payout History

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** view my payout history,
**So that** I can track all my earnings from the program.

---

## Description

Teachers should have access to a complete history of all payouts received through the SDCRS program. This includes completed payouts, pending payouts, and any failed transactions. The history serves as a record for personal tracking and can be referenced for any payment disputes.

---

## Acceptance Criteria

### Functional

- [ ] Payout history accessible from profile/settings menu
- [ ] List shows all payouts in reverse chronological order
- [ ] Each entry shows: amount, date, status, Application ID
- [ ] Filter by status: All, Completed, Processing, Failed
- [ ] Filter by date range
- [ ] Tap on entry shows full details
- [ ] Show transaction reference for completed payouts
- [ ] Export option for records (optional v2)
- [ ] Empty state message if no payouts yet
- [ ] Pagination for large histories

### Payout Status Display

| Status | Display | Color |
|--------|---------|-------|
| Processing | 🔄 Processing | Blue |
| Completed | ✓ Credited | Green |
| Failed | ⚠️ Failed | Red |

---

## UI/UX Requirements (PWA)

### Payout History Screen

```
┌─────────────────────────────────┐
│  ← Payout History               │
├─────────────────────────────────┤
│                                 │
│  Total Earned: 2,500 DJF        │
│                                 │
│  [All] [Completed] [Processing] │
│                                 │
│  December 2026                  │
│  ─────────────────────────────  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 💰 500 DJF                 │  │
│  │ ✓ Credited                │  │
│  │ Dec 10, 2026               │  │
│  │ SDCRS-20261207-A1B2C      │  │
│  │                      →    │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 💰 500 DJF                 │  │
│  │ 🔄 Processing              │  │
│  │ Dec 9, 2026                │  │
│  │ SDCRS-20261206-X7Y8Z      │  │
│  │                      →    │  │
│  └───────────────────────────┘  │
│                                 │
│  November 2026                  │
│  ─────────────────────────────  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 💰 500 DJF                 │  │
│  │ ✓ Credited                │  │
│  │ Nov 28, 2026               │  │
│  │ SDCRS-20261125-M3N4P      │  │
│  │                      →    │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 💰 500 DJF                 │  │
│  │ ✓ Credited                │  │
│  │ Nov 15, 2026               │  │
│  │ SDCRS-20261112-Q5R6S      │  │
│  │                      →    │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 💰 500 DJF                 │  │
│  │ ✓ Credited                │  │
│  │ Nov 5, 2026                │  │
│  │ SDCRS-20261102-T7U8V      │  │
│  │                      →    │  │
│  └───────────────────────────┘  │
│                                 │
│  Load More...                   │
│                                 │
└─────────────────────────────────┘
```

### Payout Detail View

```
┌─────────────────────────────────┐
│  ← Payout Details               │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐  │
│  │         💰                 │  │
│  │       500 DJF              │  │
│  │      ✓ Credited            │  │
│  └───────────────────────────┘  │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Application ID                 │
│  SDCRS-20261207-A1B2C      [📋] │
│                                 │
│  Transaction Reference          │
│  TXN-2026120812345         [📋] │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Payment Details                │
│                                 │
│  Amount          500 DJF        │
│  Status          Credited       │
│  Initiated       Dec 9, 2026    │
│  Completed       Dec 10, 2026   │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Bank Account                   │
│                                 │
│  Account         ****4567       │
│  Bank            BCD Djibouti   │
│  Name            Ahmed Hassan   │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  [  View Application  ]         │
│                                 │
└─────────────────────────────────┘
```

### Empty State

```
┌─────────────────────────────────┐
│  ← Payout History               │
├─────────────────────────────────┤
│                                 │
│                                 │
│           💰                    │
│                                 │
│    No payouts yet               │
│                                 │
│    Submit your first stray      │
│    dog report to earn rewards   │
│    when the dog is captured.    │
│                                 │
│    [  Submit a Report  ]        │
│                                 │
│                                 │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Pull-to-refresh for latest payouts
- [ ] Skeleton loading while fetching
- [ ] Group payouts by month for easy scanning
- [ ] Infinite scroll pagination
- [ ] Cache history for offline viewing
- [ ] Deep link to payout from notification
- [ ] Copy transaction reference to clipboard

---

## Technical Notes

### Payout History API

```typescript
interface PayoutHistoryResponse {
  payouts: Payout[];
  summary: {
    totalEarned: number;
    totalCompleted: number;
    totalProcessing: number;
    totalFailed: number;
  };
  pagination: {
    page: number;
    limit: number;
    total: number;
    hasMore: boolean;
  };
}
```

### Fetch Payout History

```javascript
async function fetchPayoutHistory(filters = {}) {
  const token = await getAuthToken();
  const params = new URLSearchParams({
    page: filters.page || 1,
    limit: filters.limit || 20,
    status: filters.status || '',
    fromDate: filters.fromDate || '',
    toDate: filters.toDate || ''
  });

  const response = await fetch(`/api/payouts/history?${params}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  const data = await response.json();

  // Cache for offline
  await cachePayoutHistory(data.payouts);

  return data;
}
```

### Group Payouts by Month

```javascript
function groupPayoutsByMonth(payouts) {
  const groups = {};

  payouts.forEach(payout => {
    const date = new Date(payout.completedAt || payout.initiatedAt);
    const monthKey = date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long'
    });

    if (!groups[monthKey]) {
      groups[monthKey] = [];
    }
    groups[monthKey].push(payout);
  });

  return Object.entries(groups).map(([month, items]) => ({
    month,
    payouts: items
  }));
}
```

### Payout History Component

```jsx
function PayoutHistory() {
  const [payouts, setPayouts] = useState([]);
  const [filter, setFilter] = useState('all');
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState(null);

  useEffect(() => {
    loadPayouts();
  }, [filter]);

  const loadPayouts = async () => {
    setLoading(true);
    const data = await fetchPayoutHistory({ status: filter });
    setPayouts(data.payouts);
    setSummary(data.summary);
    setLoading(false);
  };

  const groupedPayouts = groupPayoutsByMonth(payouts);

  return (
    <div className="payout-history">
      <div className="summary-card">
        <h2>Total Earned</h2>
        <span className="amount">{formatCurrency(summary?.totalEarned)}</span>
      </div>

      <div className="filters">
        {['all', 'completed', 'processing'].map(f => (
          <button
            key={f}
            className={filter === f ? 'active' : ''}
            onClick={() => setFilter(f)}
          >
            {f.charAt(0).toUpperCase() + f.slice(1)}
          </button>
        ))}
      </div>

      {loading ? (
        <PayoutSkeleton />
      ) : payouts.length === 0 ? (
        <EmptyState />
      ) : (
        groupedPayouts.map(group => (
          <div key={group.month} className="month-group">
            <h3>{group.month}</h3>
            {group.payouts.map(payout => (
              <PayoutCard key={payout.payoutId} payout={payout} />
            ))}
          </div>
        ))
      )}
    </div>
  );
}
```

### Cache Payout History

```javascript
async function cachePayoutHistory(payouts) {
  const db = await openDB('sdcrs-cache', 1, {
    upgrade(db) {
      if (!db.objectStoreNames.contains('payouts')) {
        db.createObjectStore('payouts', { keyPath: 'payoutId' });
      }
    }
  });

  const tx = db.transaction('payouts', 'readwrite');
  for (const payout of payouts) {
    await tx.store.put(payout);
  }
  await tx.done;
}

async function getCachedPayoutHistory() {
  const db = await openDB('sdcrs-cache', 1);
  const payouts = await db.getAll('payouts');
  return payouts.sort((a, b) =>
    new Date(b.initiatedAt) - new Date(a.initiatedAt)
  );
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Payout API | Backend | Fetch history |
| IndexedDB | Browser | Offline cache |
| idb | Library | IndexedDB wrapper |

---

## Related Stories

- [T-PAY-01](./T-PAY-01.md) - Payout notification
- [T-PAY-03](./T-PAY-03.md) - Cumulative earnings
- [T-AUTH-03](./T-AUTH-03.md) - Bank account verification
