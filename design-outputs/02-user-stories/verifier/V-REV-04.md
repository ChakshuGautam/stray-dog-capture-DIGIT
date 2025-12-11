# V-REV-04: SLA Indicator

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Verifier
**I want to** see an SLA indicator highlighting submissions approaching the 24-hour verification limit
**So that** I can meet turnaround targets and prioritize urgent reviews.

---

## Description

Each submission has a 24-hour SLA (Service Level Agreement) from when it enters the verification queue. Verifiers need clear visual indicators to prioritize submissions approaching or breaching SLA to maintain program quality metrics.

---

## Acceptance Criteria

### SLA Display

- [ ] SLA countdown visible on each queue item
- [ ] Color-coded urgency indicator
- [ ] Time remaining shown in hours/minutes
- [ ] SLA breached items highlighted prominently
- [ ] Queue sortable by SLA urgency
- [ ] Dashboard shows SLA compliance metrics

### SLA Status Levels

| Status | Time Remaining | Color | Action |
|--------|----------------|-------|--------|
| On Track | >8 hours | 🟢 Green | Normal priority |
| Attention | 4-8 hours | 🟡 Yellow | Review soon |
| Urgent | 1-4 hours | 🟠 Orange | Priority review |
| Critical | <1 hour | 🔴 Red | Immediate action |
| Breached | 0 or negative | ⚫ Black | Escalate |

### SLA Notifications

- [ ] Warning at 4 hours remaining
- [ ] Alert at 1 hour remaining
- [ ] Escalation at breach
- [ ] Manager notified of breaches
- [ ] Daily SLA summary report

---

## UI/UX Requirements

### Queue List with SLA Indicators

```
┌─────────────────────────────────────────────────────────────┐
│  📋 Verification Queue                    Sort: [SLA ▼]     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ⚫ SLA BREACHED (2)                                        │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ⚫ SDC-2026-001220  │  Block: Ambouli                 │  │
│  │    SLA: -2h 15m     │  Flags: 1   Priority: ●●●○○    │  │
│  │    ESCALATED        │  [Review Now]                   │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ⚫ SDC-2026-001218  │  Block: Balbala                 │  │
│  │    SLA: -45m        │  Flags: 0   Priority: ●●○○○    │  │
│  │    ESCALATED        │  [Review Now]                   │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  🔴 CRITICAL - <1 HOUR (3)                                 │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ 🔴 SDC-2026-001225  │  Block: Boulaos                 │  │
│  │    SLA: 42 minutes  │  Flags: 2   Priority: ●●●●○    │  │
│  │                     │  [Review Now]                   │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  🟠 URGENT - 1-4 HOURS (5)                                 │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ 🟠 SDC-2026-001230  │  Block: Arhiba                  │  │
│  │    SLA: 2h 15m      │  Flags: 0   Priority: ●●●○○    │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ 🟠 SDC-2026-001232  │  Block: Quartier 7             │  │
│  │    SLA: 3h 40m      │  Flags: 1   Priority: ●●○○○    │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  🟡 ATTENTION - 4-8 HOURS (8)                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ 🟡 SDC-2026-001235  │  Block: PK13                    │  │
│  │    SLA: 5h 20m      │  Flags: 0   Priority: ●●○○○    │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  🟢 ON TRACK (15)                                          │
│  [Show All...]                                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### SLA Badge on Review Screen

```
┌─────────────────────────────────────────────────────────────┐
│  Application: SDC-2026-001234                               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                                                     │    │
│  │  🟠 SLA: 2 hours 15 minutes remaining              │    │
│  │  ━━━━━━━━━━━━━━━━━━━━━━●━━━━━━━                    │    │
│  │  Submitted 21h 45m ago      Deadline in 2h 15m     │    │
│  │                                                     │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                             │
│  [Rest of review screen...]                                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### SLA Dashboard Widget

```
┌─────────────────────────────────────────────────────────────┐
│  📊 SLA Compliance - Today                                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Overall Compliance: 94.2%                                  │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━●━━━━        │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                                                     │    │
│  │    Reviewed: 127         Within SLA: 120           │    │
│  │    Breached: 7           Avg Time: 6.2h            │    │
│  │                                                     │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                             │
│  Queue Status:                                              │
│  ├─ ⚫ Breached: 2                                         │
│  ├─ 🔴 Critical: 3                                         │
│  ├─ 🟠 Urgent: 5                                           │
│  ├─ 🟡 Attention: 8                                        │
│  └─ 🟢 On Track: 15                                        │
│                                                             │
│  [View Detailed Report]                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### SLA Breach Alert

```
┌─────────────────────────────────────────────────────────────┐
│  🚨 SLA BREACH ALERT                                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  2 submissions have breached their 24-hour SLA.             │
│                                                             │
│  These have been automatically escalated to the             │
│  Senior Verifier.                                           │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ SDC-2026-001220  │  Breached by: 2h 15m              │  │
│  │ SDC-2026-001218  │  Breached by: 45m                 │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  Please prioritize reviewing critical items to              │
│  prevent further breaches.                                  │
│                                                             │
│  Current Queue:                                             │
│  • 3 items have <1 hour remaining                          │
│  • 5 items have <4 hours remaining                         │
│                                                             │
│  [  View Critical Queue  ]    [  Dismiss  ]                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### SLA Calculation

```javascript
class SLAService {
  constructor() {
    this.SLA_HOURS = 24;
    this.WARNING_THRESHOLD = 4; // hours
    this.URGENT_THRESHOLD = 1; // hours
  }

  calculateSLAStatus(queueEntry) {
    const deadline = new Date(queueEntry.timestamps.slaDeadline);
    const now = new Date();
    const remainingMs = deadline - now;
    const remainingHours = remainingMs / (1000 * 60 * 60);

    let status, color, priority;

    if (remainingHours < 0) {
      status = 'breached';
      color = 'black';
      priority = 0;
    } else if (remainingHours < 1) {
      status = 'critical';
      color = 'red';
      priority = 1;
    } else if (remainingHours < 4) {
      status = 'urgent';
      color = 'orange';
      priority = 2;
    } else if (remainingHours < 8) {
      status = 'attention';
      color = 'yellow';
      priority = 3;
    } else {
      status = 'on_track';
      color = 'green';
      priority = 4;
    }

    return {
      status,
      color,
      priority,
      deadline: deadline.toISOString(),
      remainingMs,
      remainingHours,
      remainingFormatted: this.formatRemaining(remainingMs),
      progressPercent: this.calculateProgress(queueEntry),
      isBreached: remainingHours < 0
    };
  }

  formatRemaining(ms) {
    const isNegative = ms < 0;
    const absMs = Math.abs(ms);

    const hours = Math.floor(absMs / (1000 * 60 * 60));
    const minutes = Math.floor((absMs % (1000 * 60 * 60)) / (1000 * 60));

    const timeStr = hours > 0
      ? `${hours}h ${minutes}m`
      : `${minutes}m`;

    return isNegative ? `-${timeStr}` : timeStr;
  }

  calculateProgress(queueEntry) {
    const queued = new Date(queueEntry.timestamps.queued);
    const deadline = new Date(queueEntry.timestamps.slaDeadline);
    const now = new Date();

    const total = deadline - queued;
    const elapsed = now - queued;

    return Math.min(Math.max((elapsed / total) * 100, 0), 100);
  }
}
```

### Queue Sorting by SLA

```javascript
async function getQueueSortedBySLA(verifierId) {
  const slaService = new SLAService();

  // Fetch queue entries
  const entries = await db.verifierQueue.find({
    assignedVerifierId: verifierId,
    status: 'pending'
  }).toArray();

  // Calculate SLA status for each
  const withSLA = entries.map(entry => ({
    ...entry,
    sla: slaService.calculateSLAStatus(entry)
  }));

  // Sort by SLA priority (breached first, then critical, etc.)
  withSLA.sort((a, b) => {
    // First by SLA priority (lower = more urgent)
    if (a.sla.priority !== b.sla.priority) {
      return a.sla.priority - b.sla.priority;
    }
    // Then by remaining time (less = more urgent)
    return a.sla.remainingMs - b.sla.remainingMs;
  });

  // Group by SLA status
  const grouped = {
    breached: withSLA.filter(e => e.sla.status === 'breached'),
    critical: withSLA.filter(e => e.sla.status === 'critical'),
    urgent: withSLA.filter(e => e.sla.status === 'urgent'),
    attention: withSLA.filter(e => e.sla.status === 'attention'),
    on_track: withSLA.filter(e => e.sla.status === 'on_track')
  };

  return {
    items: withSLA,
    grouped,
    summary: {
      total: entries.length,
      breached: grouped.breached.length,
      critical: grouped.critical.length,
      urgent: grouped.urgent.length,
      attention: grouped.attention.length,
      onTrack: grouped.on_track.length
    }
  };
}
```

### SLA Monitoring Cron Job

```javascript
// Run every 15 minutes
async function monitorSLAStatus() {
  const slaService = new SLAService();
  const now = new Date();

  // Find items approaching SLA breach
  const warningThreshold = new Date(now.getTime() + 4 * 60 * 60 * 1000);
  const urgentThreshold = new Date(now.getTime() + 1 * 60 * 60 * 1000);

  // Items entering warning zone
  const enteringWarning = await db.verifierQueue.find({
    status: 'pending',
    'timestamps.slaDeadline': { $lte: warningThreshold, $gt: urgentThreshold },
    'notifications.warningSet': { $ne: true }
  }).toArray();

  for (const entry of enteringWarning) {
    await sendSLAWarningNotification(entry);
    await db.verifierQueue.updateOne(
      { queueId: entry.queueId },
      { $set: { 'notifications.warningSet': true } }
    );
  }

  // Items entering critical zone
  const enteringCritical = await db.verifierQueue.find({
    status: 'pending',
    'timestamps.slaDeadline': { $lte: urgentThreshold, $gt: now },
    'notifications.criticalSent': { $ne: true }
  }).toArray();

  for (const entry of enteringCritical) {
    await sendSLACriticalNotification(entry);
    await db.verifierQueue.updateOne(
      { queueId: entry.queueId },
      { $set: { 'notifications.criticalSent': true } }
    );
  }

  // Items that have breached
  const breached = await db.verifierQueue.find({
    status: 'pending',
    'timestamps.slaDeadline': { $lte: now },
    'notifications.breachHandled': { $ne: true }
  }).toArray();

  for (const entry of breached) {
    await handleSLABreach(entry);
    await db.verifierQueue.updateOne(
      { queueId: entry.queueId },
      { $set: { 'notifications.breachHandled': true } }
    );
  }

  // Log SLA status
  await logSLASnapshot();
}

async function handleSLABreach(entry) {
  // 1. Escalate to senior verifier
  const seniorVerifier = await getSeniorVerifier(entry.tenantId);

  await db.verifierQueue.updateOne(
    { queueId: entry.queueId },
    {
      $set: {
        escalatedTo: seniorVerifier.id,
        escalatedAt: new Date().toISOString(),
        escalationReason: 'SLA_BREACH'
      }
    }
  );

  // 2. Notify current verifier
  await notificationService.create({
    userId: entry.assignedVerifierId,
    type: 'sla_breach',
    title: 'SLA Breach',
    body: `${entry.applicationId} has breached its 24-hour SLA`,
    priority: 'high'
  });

  // 3. Notify senior verifier
  await notificationService.create({
    userId: seniorVerifier.id,
    type: 'sla_escalation',
    title: 'SLA Breach Escalation',
    body: `${entry.applicationId} needs immediate review`,
    priority: 'high'
  });

  // 4. Log for reporting
  await auditLog.create({
    type: 'SLA_BREACH',
    applicationId: entry.applicationId,
    data: {
      originalVerifier: entry.assignedVerifierId,
      escalatedTo: seniorVerifier.id,
      slaDeadline: entry.timestamps.slaDeadline,
      breachedAt: new Date().toISOString()
    }
  });
}
```

### SLA Progress Component

```javascript
function SLAIndicator({ sla }) {
  const getStatusEmoji = () => {
    switch (sla.status) {
      case 'breached': return '⚫';
      case 'critical': return '🔴';
      case 'urgent': return '🟠';
      case 'attention': return '🟡';
      default: return '🟢';
    }
  };

  const getStatusLabel = () => {
    if (sla.isBreached) {
      return `BREACHED by ${sla.remainingFormatted.replace('-', '')}`;
    }
    return `${sla.remainingFormatted} remaining`;
  };

  return (
    <div className={`sla-indicator sla-${sla.status}`}>
      <div className="sla-header">
        <span className="sla-emoji">{getStatusEmoji()}</span>
        <span className="sla-label">SLA: {getStatusLabel()}</span>
      </div>

      <div className="sla-progress-bar">
        <div
          className="sla-progress-fill"
          style={{
            width: `${sla.progressPercent}%`,
            backgroundColor: getProgressColor(sla.progressPercent)
          }}
        />
        <div
          className="sla-progress-marker"
          style={{ left: `${sla.progressPercent}%` }}
        />
      </div>

      <div className="sla-timeline">
        <span>Queued</span>
        <span>Deadline {sla.isBreached ? '(passed)' : ''}</span>
      </div>
    </div>
  );
}

function getProgressColor(percent) {
  if (percent >= 100) return '#000000'; // Breached
  if (percent >= 96) return '#EF4444'; // Critical (<1h)
  if (percent >= 83) return '#F97316'; // Urgent (1-4h)
  if (percent >= 67) return '#EAB308'; // Attention (4-8h)
  return '#22C55E'; // On track
}
```

### SLA Dashboard Stats

```javascript
async function getSLADashboardStats(tenantId, date = new Date()) {
  const startOfDay = new Date(date);
  startOfDay.setHours(0, 0, 0, 0);
  const endOfDay = new Date(date);
  endOfDay.setHours(23, 59, 59, 999);

  // Completed today
  const completed = await db.verifierQueue.find({
    tenantId,
    'timestamps.completedAt': { $gte: startOfDay, $lte: endOfDay }
  }).toArray();

  // Calculate SLA compliance
  let withinSLA = 0;
  let totalReviewTime = 0;

  for (const entry of completed) {
    const queued = new Date(entry.timestamps.queued);
    const completedAt = new Date(entry.timestamps.completedAt);
    const reviewTime = (completedAt - queued) / (1000 * 60 * 60); // hours

    totalReviewTime += reviewTime;

    if (reviewTime <= 24) {
      withinSLA++;
    }
  }

  // Current queue status
  const pendingQueue = await db.verifierQueue.find({
    tenantId,
    status: 'pending'
  }).toArray();

  const slaService = new SLAService();
  const queueStatus = {
    breached: 0,
    critical: 0,
    urgent: 0,
    attention: 0,
    on_track: 0
  };

  for (const entry of pendingQueue) {
    const status = slaService.calculateSLAStatus(entry);
    queueStatus[status.status]++;
  }

  return {
    date: date.toISOString().split('T')[0],
    reviewed: completed.length,
    withinSLA,
    breached: completed.length - withinSLA,
    complianceRate: completed.length > 0
      ? ((withinSLA / completed.length) * 100).toFixed(1)
      : 100,
    avgReviewTime: completed.length > 0
      ? (totalReviewTime / completed.length).toFixed(1)
      : 0,
    currentQueue: {
      total: pendingQueue.length,
      ...queueStatus
    }
  };
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Cron Scheduler | System | SLA monitoring |
| Notification Service | Internal | Alerts |
| Audit Logger | Internal | SLA tracking |

---

## Related Stories

- [S-RTE-01](../system/S-RTE-01.md) - Queue routing with SLA
- [V-REV-01](./V-REV-01.md) - Review submission
- [V-REJ-03](./V-REJ-03.md) - Escalate to senior
- [DA-02](../admin/DA-02.md) - Verification funnel metrics
