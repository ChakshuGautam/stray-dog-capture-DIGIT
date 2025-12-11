# V-REJ-02: Mark as Duplicate

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Verifier
**I want to** mark submissions as duplicates and link them to the original submission
**So that** duplicate tracking is maintained and prevents double payouts.

---

## Description

When a verifier identifies that a submission is a duplicate of an existing report (same dog, same location), they can mark it as duplicate and link it to the original. This maintains referential integrity, prevents duplicate MC visits, and ensures only one payout per unique dog sighting.

---

## Acceptance Criteria

### Duplicate Marking Flow

- [ ] "Mark as Duplicate" action available in comparison view
- [ ] Must select original submission to link
- [ ] Search for original by Application ID or from suggestions
- [ ] Confirmation required before marking
- [ ] Original submission must be valid (approved or pending)
- [ ] Teacher notified with link to original
- [ ] Duplicate removed from queue

### Linking Rules

- [ ] Only one-to-one linking (duplicate → original)
- [ ] Cannot link to another duplicate
- [ ] Cannot link to rejected submission
- [ ] Cannot link to submission from same teacher
- [ ] Audit trail maintained

### Teacher Notification

- [ ] Explain why marked as duplicate
- [ ] Reference original submission ID
- [ ] Clarify no payout for duplicate
- [ ] Allow dispute option (future)

---

## UI/UX Requirements

### Duplicate Marking Screen

```
┌─────────────────────────────────────────────────────────────┐
│  ← Back                 Mark as Duplicate                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Current Submission: SDC-2026-001234                        │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                                                      │   │
│  │  [Dog Photo from current submission]                 │   │
│  │                                                      │   │
│  │  Block: Boulaos                                      │   │
│  │  Submitted: Today 9:23 AM                            │   │
│  │                                                      │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
│  ─────────────────────────────────────────────────────      │
│                                                             │
│  Link to Original Submission:                               │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 🔍 Search by Application ID...                       │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
│  Suggested Matches:                                         │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ ○ SDC-2026-001230                   92% Match        │   │
│  │   Block: Boulaos | 3 hours ago | ✅ Approved         │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ ○ SDC-2026-001228                   88% Match        │   │
│  │   Block: Boulaos | 5 hours ago | ⏳ Pending          │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ ○ SDC-2026-001225                   Nearby           │   │
│  │   Block: Boulaos | 8 hours ago | ✅ Approved         │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   Cancel           │  │   📎 Link as Duplicate     │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Search Results

```
┌─────────────────────────────────────────────────────────────┐
│  ← Back                 Search Results                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Search: "SDC-2026-001"                                     │
│                                                             │
│  Found 5 matching submissions:                              │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  SDC-2026-001230                                     │   │
│  │  ├─ Status: ✅ Approved                              │   │
│  │  ├─ Block: Boulaos                                   │   │
│  │  ├─ Teacher: T-XXXX-5678                             │   │
│  │  └─ Date: Jan 15, 2026 6:15 AM                       │   │
│  │                                   [Select]           │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  SDC-2026-001228                                     │   │
│  │  ├─ Status: ⏳ Pending Verification                  │   │
│  │  ├─ Block: Boulaos                                   │   │
│  │  ├─ Teacher: T-XXXX-9012                             │   │
│  │  └─ Date: Jan 15, 2026 4:45 AM                       │   │
│  │                                   [Select]           │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
│  ⚠️ Cannot link to:                                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  SDC-2026-001220  ❌ (Rejected)                      │   │
│  │  SDC-2026-001234  🚫 (Same submission)               │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Confirmation Dialog

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  Confirm Duplicate Link                                     │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  You are marking:                                           │
│                                                             │
│  SDC-2026-001234 (current)                                  │
│           ↓                                                 │
│  as a duplicate of:                                         │
│           ↓                                                 │
│  SDC-2026-001230 (original)                                 │
│                                                             │
│  ─────────────────────────────────────────────────────      │
│                                                             │
│  This will:                                                 │
│  • Reject the current submission as duplicate               │
│  • Link to original for reference                           │
│  • Notify the teacher                                       │
│  • Teacher NOT eligible for payout on this submission       │
│                                                             │
│  The original submission remains unchanged.                 │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   Cancel           │  │   ✓ Confirm Link           │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### Duplicate Service

```javascript
class DuplicateService {
  constructor(db, notificationService) {
    this.db = db;
    this.notifications = notificationService;
  }

  async markAsDuplicate(queueId, originalId, verifierId) {
    const session = this.db.startSession();

    try {
      session.startTransaction();

      // 1. Validate current submission
      const queueEntry = await this.db.verifierQueue.findOne(
        { queueId },
        { session }
      );

      if (!queueEntry) {
        throw new Error('Queue entry not found');
      }

      if (queueEntry.status !== 'pending') {
        throw new Error('Submission already processed');
      }

      // 2. Validate original submission
      const original = await this.db.submissions.findOne(
        { applicationId: originalId },
        { session }
      );

      if (!original) {
        throw new Error('Original submission not found');
      }

      // Check original is linkable
      const validationResult = await this.validateOriginal(original, queueEntry);
      if (!validationResult.valid) {
        throw new Error(validationResult.reason);
      }

      const now = new Date().toISOString();

      // 3. Update current submission as duplicate
      await this.db.submissions.updateOne(
        { applicationId: queueEntry.applicationId },
        {
          $set: {
            status: 'rejected',
            rejectedAt: now,
            rejectedBy: verifierId,
            rejection: {
              reasonCode: 'DUPLICATE',
              reasonLabel: 'Duplicate Submission',
              description: 'This appears to be a duplicate of another submission.',
              linkedOriginalId: originalId
            }
          }
        },
        { session }
      );

      // 4. Create duplicate link record
      await this.db.duplicateLinks.insertOne({
        duplicateId: queueEntry.applicationId,
        originalId: originalId,
        linkedAt: now,
        linkedBy: verifierId,
        metadata: {
          duplicateTeacherId: queueEntry.teacherId,
          originalTeacherId: original.teacherId,
          gpsDistance: calculateDistance(
            { latitude: queueEntry.submissionSummary.location.latitude, longitude: queueEntry.submissionSummary.location.longitude },
            { latitude: original.latitude, longitude: original.longitude }
          ),
          timeGap: new Date(queueEntry.timestamps.submitted) - new Date(original.createdAt)
        }
      }, { session });

      // 5. Update original with duplicate count
      await this.db.submissions.updateOne(
        { applicationId: originalId },
        {
          $inc: { duplicateCount: 1 },
          $push: {
            linkedDuplicates: {
              applicationId: queueEntry.applicationId,
              linkedAt: now
            }
          }
        },
        { session }
      );

      // 6. Update queue entry
      await this.db.verifierQueue.updateOne(
        { queueId },
        {
          $set: {
            status: 'duplicate',
            'timestamps.completedAt': now,
            completedBy: verifierId,
            linkedOriginalId: originalId
          }
        },
        { session }
      );

      // 7. Log action
      await this.logDuplicateLink(queueEntry, original, verifierId, { session });

      await session.commitTransaction();

      // 8. Post-link actions (not in transaction)
      await this.postLinkActions(queueEntry, original, verifierId);

      return {
        success: true,
        duplicateId: queueEntry.applicationId,
        originalId: originalId
      };

    } catch (error) {
      await session.abortTransaction();
      throw error;
    } finally {
      session.endSession();
    }
  }

  async validateOriginal(original, current) {
    // Cannot link to self
    if (original.applicationId === current.applicationId) {
      return { valid: false, reason: 'Cannot link submission to itself' };
    }

    // Cannot link to rejected
    if (original.status === 'rejected') {
      return { valid: false, reason: 'Cannot link to a rejected submission' };
    }

    // Cannot link to another duplicate
    if (original.rejection?.reasonCode === 'DUPLICATE') {
      return { valid: false, reason: 'Cannot link to another duplicate' };
    }

    // Cannot link to same teacher
    if (original.teacherId === current.teacherId) {
      return { valid: false, reason: 'Cannot link to submission from same teacher' };
    }

    return { valid: true };
  }

  async logDuplicateLink(duplicate, original, verifierId, options = {}) {
    await this.db.auditLog.insertOne({
      type: 'DUPLICATE_LINKED',
      applicationId: duplicate.applicationId,
      actorId: verifierId,
      actorRole: 'VERIFIER',
      timestamp: new Date(),
      data: {
        duplicateId: duplicate.applicationId,
        originalId: original.applicationId,
        duplicateTeacher: anonymizeId(duplicate.teacherId),
        originalTeacher: anonymizeId(original.teacherId)
      }
    }, options);

    // Update verifier stats
    await this.db.verifierStats.updateOne(
      { verifierId, date: getDateString(new Date()) },
      {
        $inc: {
          duplicatesMarked: 1,
          totalReviewed: 1
        }
      },
      { upsert: true, ...options }
    );
  }

  async postLinkActions(duplicate, original, verifierId) {
    const duplicateSubmission = await this.db.submissions.findOne({
      applicationId: duplicate.applicationId
    });

    // 1. Notify teacher of duplicate
    await this.notifications.create({
      userId: duplicate.teacherId,
      type: 'submission_duplicate',
      title: 'Duplicate Report',
      body: 'Your submission appears to be a duplicate of an existing report.',
      data: {
        applicationId: duplicate.applicationId,
        originalId: original.applicationId,
        reasonCode: 'DUPLICATE',
        linkedTo: original.applicationId
      },
      priority: 'high'
    });

    // SMS notification
    await smsService.send(duplicateSubmission.teacherPhone, 'submission_duplicate', {
      ID: duplicate.applicationId,
      ORIGINAL_ID: original.applicationId
    });

    // 2. Add timeline event
    await addTimelineEvent(duplicate.applicationId, {
      status: 'rejected',
      timestamp: new Date().toISOString(),
      data: {
        reason: 'Duplicate Submission',
        message: `This report is a duplicate of ${original.applicationId}`,
        linkedTo: original.applicationId
      }
    });
  }
}

// SMS Template
const SMS_TEMPLATES = {
  submission_duplicate: {
    en: 'SDCRS: Your report {ID} is a duplicate of {ORIGINAL_ID}. No payout for duplicate reports.',
    fr: 'SDCRS: Votre rapport {ID} est un doublon de {ORIGINAL_ID}. Pas de paiement pour les doublons.'
  }
};
```

### Search Original Submissions

```javascript
async function searchOriginalSubmissions(query, tenantId, currentSubmissionId) {
  const results = [];

  // Search by application ID
  if (query.startsWith('SDC-')) {
    const byId = await db.submissions.find({
      applicationId: { $regex: query, $options: 'i' },
      tenantId,
      applicationId: { $ne: currentSubmissionId },
      status: { $in: ['pending_verification', 'approved', 'assigned_to_mc', 'captured'] },
      'rejection.reasonCode': { $ne: 'DUPLICATE' }
    }).limit(10).toArray();

    results.push(...byId.map(s => ({
      ...s,
      searchType: 'id_match',
      canLink: true
    })));
  }

  // Add rejection status for non-linkable
  const rejected = await db.submissions.find({
    applicationId: { $regex: query, $options: 'i' },
    tenantId,
    status: 'rejected'
  }).limit(5).toArray();

  results.push(...rejected.map(s => ({
    ...s,
    searchType: 'rejected',
    canLink: false,
    cannotLinkReason: 'Rejected submission'
  })));

  return results;
}

async function getSuggestedDuplicates(queueEntry) {
  const { applicationId, submissionSummary } = queueEntry;
  const { location, dogPhotoHash } = submissionSummary;

  // Get pre-computed duplicate matches
  const matches = await db.duplicateMatches.find({
    applicationId,
    similarity: { $gte: 0.8 }
  }).sort({ similarity: -1 }).toArray();

  // Also get GPS-nearby
  const nearby = await db.submissions.find({
    applicationId: { $ne: applicationId },
    tenantId: queueEntry.tenantId,
    createdAt: { $gte: daysAgo(7) },
    status: { $nin: ['auto_rejected', 'rejected'] },
    location: {
      $near: {
        $geometry: {
          type: 'Point',
          coordinates: [location.longitude, location.latitude]
        },
        $maxDistance: 200
      }
    }
  }).limit(5).toArray();

  const suggestions = [];

  // Add hash matches
  for (const match of matches) {
    const submission = await db.submissions.findOne({
      applicationId: match.matchedApplicationId
    });

    if (submission && canLinkAsOriginal(submission, queueEntry)) {
      suggestions.push({
        ...submission,
        matchType: 'hash',
        similarity: match.similarity,
        canLink: true
      });
    }
  }

  // Add nearby matches
  for (const sub of nearby) {
    if (!suggestions.find(s => s.applicationId === sub.applicationId)) {
      const canLink = canLinkAsOriginal(sub, queueEntry);
      suggestions.push({
        ...sub,
        matchType: 'gps',
        distance: calculateDistance(location, sub.location),
        canLink: canLink.valid,
        cannotLinkReason: canLink.valid ? null : canLink.reason
      });
    }
  }

  return suggestions;
}

function canLinkAsOriginal(original, duplicate) {
  if (original.applicationId === duplicate.applicationId) {
    return { valid: false, reason: 'Same submission' };
  }
  if (original.status === 'rejected') {
    return { valid: false, reason: 'Rejected' };
  }
  if (original.rejection?.reasonCode === 'DUPLICATE') {
    return { valid: false, reason: 'Already a duplicate' };
  }
  if (original.teacherId === duplicate.teacherId) {
    return { valid: false, reason: 'Same teacher' };
  }
  return { valid: true };
}
```

### Duplicate Marking Component

```javascript
class DuplicateMarker extends Component {
  constructor(props) {
    super(props);
    this.state = {
      step: 'select', // 'select' | 'confirm' | 'processing' | 'success'
      selectedOriginal: null,
      searchQuery: '',
      searchResults: [],
      suggestions: [],
      loading: false,
      error: null
    };
  }

  async componentDidMount() {
    // Load suggestions
    const suggestions = await this.loadSuggestions();
    this.setState({ suggestions });
  }

  async loadSuggestions() {
    const { queueEntry } = this.props;
    const response = await fetch(
      `/api/verifier/submissions/${queueEntry.queueId}/duplicate-suggestions`
    );
    return response.json();
  }

  async handleSearch(query) {
    this.setState({ searchQuery: query });

    if (query.length < 3) {
      this.setState({ searchResults: [] });
      return;
    }

    this.setState({ loading: true });

    try {
      const response = await fetch(
        `/api/verifier/submissions/search?q=${encodeURIComponent(query)}&tenantId=${this.props.queueEntry.tenantId}`
      );
      const results = await response.json();
      this.setState({ searchResults: results, loading: false });
    } catch (error) {
      this.setState({ loading: false, error: 'Search failed' });
    }
  }

  selectOriginal(submission) {
    if (submission.canLink) {
      this.setState({ selectedOriginal: submission });
    }
  }

  proceedToConfirm() {
    if (this.state.selectedOriginal) {
      this.setState({ step: 'confirm' });
    }
  }

  async confirmDuplicate() {
    const { queueId, onSuccess, onNext } = this.props;
    const { selectedOriginal } = this.state;

    this.setState({ step: 'processing', error: null });

    try {
      const response = await fetch(
        `/api/verifier/submissions/${queueId}/mark-duplicate`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            originalId: selectedOriginal.applicationId
          })
        }
      );

      const result = await response.json();

      if (result.success) {
        this.setState({ step: 'success' });
        setTimeout(() => {
          onSuccess?.(result);
          if (result.nextItem) {
            onNext?.(result.nextItem);
          }
        }, 1500);
      } else {
        throw new Error(result.message);
      }

    } catch (error) {
      this.setState({
        step: 'confirm',
        error: error.message
      });
    }
  }

  render() {
    const { step, selectedOriginal, searchQuery, searchResults, suggestions, loading, error } = this.state;
    const { queueEntry } = this.props;

    if (step === 'success') {
      return (
        <div className="duplicate-success">
          <div className="icon">📎</div>
          <p>Marked as Duplicate</p>
          <p className="loading-next">Loading next submission...</p>
        </div>
      );
    }

    if (step === 'processing') {
      return (
        <div className="duplicate-processing">
          <div className="spinner" />
          <p>Linking duplicate...</p>
        </div>
      );
    }

    if (step === 'confirm') {
      return (
        <div className="duplicate-confirm">
          <h3>Confirm Duplicate Link</h3>

          {error && <div className="error">{error}</div>}

          <div className="link-visualization">
            <div className="submission current">
              <p className="id">{queueEntry.applicationId}</p>
              <p className="label">(current)</p>
            </div>
            <div className="arrow">↓</div>
            <p className="link-label">as a duplicate of</p>
            <div className="arrow">↓</div>
            <div className="submission original">
              <p className="id">{selectedOriginal.applicationId}</p>
              <p className="label">(original)</p>
            </div>
          </div>

          <div className="consequences">
            <h4>This will:</h4>
            <ul>
              <li>Reject the current submission as duplicate</li>
              <li>Link to original for reference</li>
              <li>Notify the teacher</li>
              <li>Teacher NOT eligible for payout</li>
            </ul>
          </div>

          <div className="actions">
            <button onClick={() => this.setState({ step: 'select' })}>
              Cancel
            </button>
            <button
              className="confirm"
              onClick={() => this.confirmDuplicate()}
            >
              ✓ Confirm Link
            </button>
          </div>
        </div>
      );
    }

    // Select step
    return (
      <div className="duplicate-select">
        <h3>Mark as Duplicate</h3>

        <div className="current-submission">
          <p>Current: {queueEntry.applicationId}</p>
        </div>

        <div className="search-section">
          <input
            type="text"
            placeholder="🔍 Search by Application ID..."
            value={searchQuery}
            onChange={(e) => this.handleSearch(e.target.value)}
          />
          {loading && <div className="search-loading">Searching...</div>}
        </div>

        {searchResults.length > 0 && (
          <div className="search-results">
            <h4>Search Results</h4>
            {searchResults.map(sub => (
              <SubmissionCard
                key={sub.applicationId}
                submission={sub}
                selected={selectedOriginal?.applicationId === sub.applicationId}
                onSelect={() => this.selectOriginal(sub)}
              />
            ))}
          </div>
        )}

        {searchResults.length === 0 && suggestions.length > 0 && (
          <div className="suggestions">
            <h4>Suggested Matches</h4>
            {suggestions.map(sub => (
              <SubmissionCard
                key={sub.applicationId}
                submission={sub}
                selected={selectedOriginal?.applicationId === sub.applicationId}
                onSelect={() => this.selectOriginal(sub)}
                showMatchType
              />
            ))}
          </div>
        )}

        <div className="actions">
          <button onClick={() => this.props.onCancel?.()}>Cancel</button>
          <button
            className="proceed"
            onClick={() => this.proceedToConfirm()}
            disabled={!selectedOriginal}
          >
            📎 Link as Duplicate
          </button>
        </div>
      </div>
    );
  }
}

function SubmissionCard({ submission, selected, onSelect, showMatchType }) {
  return (
    <div
      className={`submission-card ${selected ? 'selected' : ''} ${submission.canLink ? '' : 'disabled'}`}
      onClick={() => submission.canLink && onSelect()}
    >
      <div className="radio">{selected ? '●' : '○'}</div>
      <div className="content">
        <div className="header">
          <span className="id">{submission.applicationId}</span>
          {showMatchType && submission.matchType === 'hash' && (
            <span className="match-badge hash">{Math.round(submission.similarity * 100)}% Match</span>
          )}
          {showMatchType && submission.matchType === 'gps' && (
            <span className="match-badge gps">Nearby</span>
          )}
        </div>
        <div className="details">
          <span>Block: {submission.block}</span>
          <span>{formatTimeAgo(submission.createdAt)}</span>
          <StatusBadge status={submission.status} />
        </div>
        {!submission.canLink && (
          <div className="cannot-link">
            ⚠️ {submission.cannotLinkReason}
          </div>
        )}
      </div>
    </div>
  );
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Duplicate Detection | System | Pre-computed matches |
| Notification Service | Internal | Teacher notification |
| Audit Logger | Internal | Link tracking |

---

## Related Stories

- [V-REV-02](./V-REV-02.md) - Compare duplicates
- [V-REJ-04](./V-REJ-04.md) - Bulk duplicate actions
- [S-VAL-03](../system/S-VAL-03.md) - Photo hash verification
- [T-STAT-03](../teacher/T-STAT-03.md) - View rejection reason
