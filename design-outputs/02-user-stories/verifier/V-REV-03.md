# V-REV-03: View Fraud Flags

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Verifier
**I want to** see fraud flags (GPS mismatch, timestamp issues, high submission rate)
**So that** I can prioritize suspicious cases for careful review.

---

## Description

The system automatically flags submissions that exhibit suspicious patterns. Verifiers need to see these flags prominently to focus their attention on potentially fraudulent submissions while still allowing legitimate edge cases through after careful review.

---

## Acceptance Criteria

### Flag Types Displayed

- [ ] GPS mismatch (selfie >500m from dog photo)
- [ ] Timestamp anomaly (future date, too old, stripped)
- [ ] High submission rate (>10 in 1 hour from user)
- [ ] Gallery upload (photo not taken in-app)
- [ ] Duplicate hash similarity detected
- [ ] Low GPS accuracy (>50m radius)
- [ ] Location near boundary edge

### Flag Display Requirements

- [ ] Flags shown as prominent badges on review screen
- [ ] Each flag has severity level (warning/critical)
- [ ] Flag tooltip explains the issue
- [ ] Flags are clickable to see details
- [ ] Critical flags require acknowledge before approval
- [ ] Flag history shown (if user has prior flags)

### Flag Severity Levels

| Severity | Color | Requires | Examples |
|----------|-------|----------|----------|
| Critical | 🔴 Red | Acknowledge | GPS mismatch >1km, Future timestamp |
| Warning | 🟠 Orange | Review | Gallery upload, High rate |
| Info | 🟡 Yellow | Note | Boundary edge, Low accuracy |

---

## UI/UX Requirements

### Flags Panel on Review Screen

```
┌─────────────────────────────────────────────────────────────┐
│  ⚠️ FLAGS DETECTED (3)                                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  🔴 CRITICAL                                                │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ 📍 GPS Mismatch                                       │  │
│  │ Selfie location is 1.2km from dog photo location      │  │
│  │ ─────────────────────────────────────────────────     │  │
│  │ Dog Photo: 11.5892°N, 43.1456°E                       │  │
│  │ Selfie:    11.5783°N, 43.1589°E                       │  │
│  │                                   [View on Map]       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  🟠 WARNING                                                 │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ 🖼️ Gallery Upload                                     │  │
│  │ Photo was selected from gallery, not taken in-app     │  │
│  │ Photo timestamp: 3 hours before submission            │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  🟡 INFO                                                    │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ 📡 Low GPS Accuracy                                   │  │
│  │ GPS accuracy is 45m (threshold: 30m)                  │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Critical Flag Acknowledgment Modal

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ⚠️ Critical Flag Requires Acknowledgment                   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  This submission has a critical flag:                       │
│                                                             │
│  📍 GPS MISMATCH                                            │
│                                                             │
│  The selfie GPS location is 1.2km away from the dog         │
│  photo GPS location. This could indicate:                   │
│                                                             │
│  • Photo taken at different location than selfie            │
│  • GPS spoofing attempt                                     │
│  • Photo from gallery taken elsewhere                       │
│                                                             │
│  ─────────────────────────────────────────────────────      │
│                                                             │
│  To approve this submission despite this flag, please       │
│  confirm you have reviewed and verified the evidence.       │
│                                                             │
│  [ ] I have reviewed this submission and believe it         │
│      is legitimate despite the GPS mismatch flag.           │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   Cancel           │  │   ✓ Acknowledge & Review   │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### User Flag History Panel

```
┌─────────────────────────────────────────────────────────────┐
│  📊 Teacher Flag History                                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Teacher ID: T-XXXX-1234 (anonymized)                       │
│  Total Submissions: 23                                      │
│  Flagged: 4 (17%)                                          │
│                                                             │
│  Recent Flags:                                              │
│  ├─ 🟠 Gallery Upload (3 times)                            │
│  ├─ 🟡 Low GPS Accuracy (2 times)                          │
│  └─ 🔴 GPS Mismatch (1 time)                               │
│                                                             │
│  Rejection Rate: 8%                                         │
│  Duplicate Rate: 4%                                         │
│                                                             │
│  ⚠️ Note: Higher than average flag rate                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### High Submission Rate Warning

```
┌─────────────────────────────────────────────────────────────┐
│  🚨 HIGH SUBMISSION RATE ALERT                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  This teacher has submitted 12 reports in the last hour.    │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Submission Timeline (Last 2 hours)                   │  │
│  │                                                       │  │
│  │  ●●●  ●●●●●  ●●●●                      Current →      │  │
│  │  └──────────────────────────────────────────────────  │  │
│  │  10:00    10:30    11:00    11:30    Now              │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  This could indicate:                                       │
│  • Teacher patrolling an area with many strays             │
│  • Potential fraudulent bulk submissions                   │
│  • Camera roll dump from old photos                        │
│                                                             │
│  [ Review All 12 Submissions Together ]                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### Flag Detection Rules

```javascript
const FLAG_RULES = {
  // Critical flags
  GPS_MISMATCH_CRITICAL: {
    severity: 'critical',
    check: (submission) => {
      const distance = submission.selfieToPhotoDistance;
      return distance > 1000; // >1km
    },
    message: 'Selfie location is {distance}km from dog photo location',
    code: 'GPS_MISMATCH_CRITICAL'
  },

  TIMESTAMP_FUTURE: {
    severity: 'critical',
    check: (submission) => {
      const photoTime = new Date(submission.dogPhotoTimestamp);
      const now = new Date();
      return photoTime > now;
    },
    message: 'Photo timestamp is in the future',
    code: 'TIMESTAMP_FUTURE'
  },

  // Warning flags
  GPS_MISMATCH_WARNING: {
    severity: 'warning',
    check: (submission) => {
      const distance = submission.selfieToPhotoDistance;
      return distance > 500 && distance <= 1000;
    },
    message: 'Selfie location is {distance}m from dog photo',
    code: 'GPS_MISMATCH_WARNING'
  },

  GALLERY_UPLOAD: {
    severity: 'warning',
    check: (submission) => submission.dogPhotoSource === 'gallery',
    message: 'Photo was selected from gallery, not taken in-app',
    code: 'GALLERY_UPLOAD'
  },

  HIGH_SUBMISSION_RATE: {
    severity: 'warning',
    check: async (submission) => {
      const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000);
      const recentCount = await db.submissions.countDocuments({
        teacherId: submission.teacherId,
        createdAt: { $gte: oneHourAgo }
      });
      return recentCount > 10;
    },
    message: 'Teacher submitted {count} reports in the last hour',
    code: 'HIGH_SUBMISSION_RATE'
  },

  DUPLICATE_SIMILARITY: {
    severity: 'warning',
    check: (submission) => submission.duplicateMatches?.some(d => d.similarity > 0.85),
    message: 'Similar photo found in recent submissions',
    code: 'DUPLICATE_SIMILARITY'
  },

  // Info flags
  LOW_GPS_ACCURACY: {
    severity: 'info',
    check: (submission) => submission.gpsAccuracy > 30,
    message: 'GPS accuracy is {accuracy}m (threshold: 30m)',
    code: 'LOW_GPS_ACCURACY'
  },

  BOUNDARY_EDGE: {
    severity: 'info',
    check: (submission) => submission.distanceToBoundary < 100,
    message: 'Location is {distance}m from service area boundary',
    code: 'BOUNDARY_EDGE'
  },

  TIMESTAMP_OLD: {
    severity: 'info',
    check: (submission) => {
      const photoAge = (Date.now() - new Date(submission.dogPhotoTimestamp)) / (1000 * 60 * 60);
      return photoAge > 12 && photoAge <= 24;
    },
    message: 'Photo is {age} hours old',
    code: 'TIMESTAMP_OLD'
  }
};
```

### Generate Flags for Submission

```javascript
async function generateFlags(submission) {
  const flags = [];

  for (const [ruleName, rule] of Object.entries(FLAG_RULES)) {
    try {
      const triggered = typeof rule.check === 'function'
        ? await rule.check(submission)
        : rule.check;

      if (triggered) {
        flags.push({
          code: rule.code,
          severity: rule.severity,
          message: interpolateMessage(rule.message, submission),
          details: extractFlagDetails(rule.code, submission),
          triggeredAt: new Date().toISOString()
        });
      }
    } catch (error) {
      console.error(`Flag rule ${ruleName} failed:`, error);
    }
  }

  // Sort by severity
  const severityOrder = { critical: 0, warning: 1, info: 2 };
  flags.sort((a, b) => severityOrder[a.severity] - severityOrder[b.severity]);

  return flags;
}

function interpolateMessage(template, submission) {
  return template
    .replace('{distance}', submission.selfieToPhotoDistance?.toFixed(0))
    .replace('{accuracy}', submission.gpsAccuracy?.toFixed(0))
    .replace('{age}', calculatePhotoAge(submission.dogPhotoTimestamp).toFixed(1))
    .replace('{count}', submission.recentSubmissionCount);
}

function extractFlagDetails(code, submission) {
  switch (code) {
    case 'GPS_MISMATCH_CRITICAL':
    case 'GPS_MISMATCH_WARNING':
      return {
        dogPhotoLocation: {
          latitude: submission.latitude,
          longitude: submission.longitude
        },
        selfieLocation: {
          latitude: submission.selfieLatitude,
          longitude: submission.selfieLongitude
        },
        distance: submission.selfieToPhotoDistance
      };

    case 'HIGH_SUBMISSION_RATE':
      return {
        count: submission.recentSubmissionCount,
        timeWindow: '1 hour',
        submissions: submission.recentSubmissions?.map(s => ({
          applicationId: s.applicationId,
          submittedAt: s.createdAt
        }))
      };

    default:
      return null;
  }
}
```

### Flags Display Component

```javascript
class FlagsPanel extends Component {
  constructor(props) {
    super(props);
    this.state = {
      expandedFlag: null,
      acknowledged: {}
    };
  }

  toggleFlagExpand(code) {
    this.setState({
      expandedFlag: this.state.expandedFlag === code ? null : code
    });
  }

  acknowledgeFlag(code) {
    this.setState({
      acknowledged: { ...this.state.acknowledged, [code]: true }
    });

    // Log acknowledgment for audit
    logFlagAcknowledgment(this.props.applicationId, code, getCurrentVerifierId());
  }

  getFlagIcon(severity) {
    switch (severity) {
      case 'critical': return '🔴';
      case 'warning': return '🟠';
      case 'info': return '🟡';
      default: return '⚪';
    }
  }

  hasCriticalUnacknowledged() {
    const { flags } = this.props;
    return flags.some(f =>
      f.severity === 'critical' && !this.state.acknowledged[f.code]
    );
  }

  render() {
    const { flags, onApprovalBlocked } = this.props;

    if (!flags || flags.length === 0) {
      return (
        <div className="flags-panel no-flags">
          ✓ No flags detected
        </div>
      );
    }

    // Group by severity
    const grouped = {
      critical: flags.filter(f => f.severity === 'critical'),
      warning: flags.filter(f => f.severity === 'warning'),
      info: flags.filter(f => f.severity === 'info')
    };

    return (
      <div className="flags-panel">
        <h3>⚠️ FLAGS DETECTED ({flags.length})</h3>

        {grouped.critical.length > 0 && (
          <div className="flag-group critical">
            <h4>🔴 CRITICAL</h4>
            {grouped.critical.map(flag => (
              <FlagCard
                key={flag.code}
                flag={flag}
                expanded={this.state.expandedFlag === flag.code}
                acknowledged={this.state.acknowledged[flag.code]}
                onToggle={() => this.toggleFlagExpand(flag.code)}
                onAcknowledge={() => this.acknowledgeFlag(flag.code)}
              />
            ))}
          </div>
        )}

        {grouped.warning.length > 0 && (
          <div className="flag-group warning">
            <h4>🟠 WARNING</h4>
            {grouped.warning.map(flag => (
              <FlagCard
                key={flag.code}
                flag={flag}
                expanded={this.state.expandedFlag === flag.code}
                onToggle={() => this.toggleFlagExpand(flag.code)}
              />
            ))}
          </div>
        )}

        {grouped.info.length > 0 && (
          <div className="flag-group info">
            <h4>🟡 INFO</h4>
            {grouped.info.map(flag => (
              <FlagCard
                key={flag.code}
                flag={flag}
                expanded={this.state.expandedFlag === flag.code}
                onToggle={() => this.toggleFlagExpand(flag.code)}
              />
            ))}
          </div>
        )}

        {this.hasCriticalUnacknowledged() && (
          <div className="approval-blocked-notice">
            ⚠️ You must acknowledge all critical flags before approving
          </div>
        )}
      </div>
    );
  }
}

function FlagCard({ flag, expanded, acknowledged, onToggle, onAcknowledge }) {
  return (
    <div className={`flag-card ${flag.severity} ${acknowledged ? 'acknowledged' : ''}`}>
      <div className="flag-header" onClick={onToggle}>
        <span className="flag-icon">{getFlagIcon(flag.code)}</span>
        <span className="flag-title">{getFlagTitle(flag.code)}</span>
        {acknowledged && <span className="ack-badge">✓ Acknowledged</span>}
        <span className="expand-icon">{expanded ? '▼' : '▶'}</span>
      </div>

      {expanded && (
        <div className="flag-details">
          <p>{flag.message}</p>

          {flag.details && (
            <div className="flag-data">
              <FlagDetailsView code={flag.code} details={flag.details} />
            </div>
          )}

          {flag.severity === 'critical' && !acknowledged && onAcknowledge && (
            <button className="acknowledge-btn" onClick={onAcknowledge}>
              ✓ I have reviewed this flag
            </button>
          )}
        </div>
      )}
    </div>
  );
}

function getFlagIcon(code) {
  const icons = {
    GPS_MISMATCH_CRITICAL: '📍',
    GPS_MISMATCH_WARNING: '📍',
    TIMESTAMP_FUTURE: '⏰',
    GALLERY_UPLOAD: '🖼️',
    HIGH_SUBMISSION_RATE: '🚨',
    DUPLICATE_SIMILARITY: '📋',
    LOW_GPS_ACCURACY: '📡',
    BOUNDARY_EDGE: '🗺️',
    TIMESTAMP_OLD: '⏳'
  };
  return icons[code] || '⚠️';
}

function getFlagTitle(code) {
  const titles = {
    GPS_MISMATCH_CRITICAL: 'Critical GPS Mismatch',
    GPS_MISMATCH_WARNING: 'GPS Mismatch',
    TIMESTAMP_FUTURE: 'Future Timestamp',
    GALLERY_UPLOAD: 'Gallery Upload',
    HIGH_SUBMISSION_RATE: 'High Submission Rate',
    DUPLICATE_SIMILARITY: 'Potential Duplicate',
    LOW_GPS_ACCURACY: 'Low GPS Accuracy',
    BOUNDARY_EDGE: 'Near Boundary',
    TIMESTAMP_OLD: 'Older Photo'
  };
  return titles[code] || 'Unknown Flag';
}
```

### Fetch Teacher Flag History

```javascript
async function fetchTeacherFlagHistory(teacherId) {
  const submissions = await db.submissions.find({
    teacherId,
    createdAt: { $gte: daysAgo(30) }
  }).toArray();

  const flagCounts = {};
  let totalFlagged = 0;

  for (const sub of submissions) {
    if (sub.flags && sub.flags.length > 0) {
      totalFlagged++;
      for (const flag of sub.flags) {
        flagCounts[flag.code] = (flagCounts[flag.code] || 0) + 1;
      }
    }
  }

  const rejections = await db.submissions.countDocuments({
    teacherId,
    status: 'rejected',
    createdAt: { $gte: daysAgo(30) }
  });

  const duplicates = await db.submissions.countDocuments({
    teacherId,
    'rejection.reasonCode': 'DUPLICATE',
    createdAt: { $gte: daysAgo(30) }
  });

  return {
    teacherId: anonymizeId(teacherId),
    totalSubmissions: submissions.length,
    flaggedCount: totalFlagged,
    flagRate: ((totalFlagged / submissions.length) * 100).toFixed(1),
    flagBreakdown: Object.entries(flagCounts).map(([code, count]) => ({
      code,
      count,
      severity: FLAG_RULES[code]?.severity || 'unknown'
    })),
    rejectionRate: ((rejections / submissions.length) * 100).toFixed(1),
    duplicateRate: ((duplicates / submissions.length) * 100).toFixed(1),
    isHighFlagRate: (totalFlagged / submissions.length) > 0.15
  };
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Flag Rules Engine | Internal | Evaluate flag conditions |
| Audit Logger | Internal | Track flag acknowledgments |
| User Analytics | Internal | Teacher history stats |

---

## Related Stories

- [S-VAL-01](../system/S-VAL-01.md) - GPS validation
- [S-VAL-02](../system/S-VAL-02.md) - Boundary validation
- [V-REV-01](./V-REV-01.md) - Review submission
- [V-REJ-03](./V-REJ-03.md) - Escalate to senior
