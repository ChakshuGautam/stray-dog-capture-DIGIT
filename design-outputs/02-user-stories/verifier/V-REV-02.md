# V-REV-02: Compare Potential Duplicates

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Verifier
**I want to** see potential duplicate matches displayed alongside the current submission
**So that** I can identify resubmissions and prevent duplicate payouts.

---

## Description

When a submission has potential duplicates flagged by the system (based on perceptual hash similarity, GPS proximity, or time clustering), the verifier sees these matches in a comparison view. This helps identify intentional resubmissions of the same dog or accidental duplicates from multiple teachers.

---

## Acceptance Criteria

### Duplicate Display

- [ ] Potential duplicates shown as thumbnail cards
- [ ] Similarity percentage displayed for each match
- [ ] Match type indicated (hash/GPS/cluster)
- [ ] Side-by-side comparison mode available
- [ ] Original submission date/time visible
- [ ] Original submission status visible
- [ ] Original submitter info (anonymized) visible

### Comparison Features

- [ ] Swipe between current and duplicate photos
- [ ] Overlay comparison (fade slider)
- [ ] Split-screen comparison
- [ ] GPS distance between submissions shown
- [ ] Time gap between submissions shown
- [ ] One-click "Mark as Duplicate" action

### Match Indicators

| Match Type | Threshold | Display |
|------------|-----------|---------|
| Hash Match | >95% similar | 🔴 Very High |
| Hash Similar | 85-95% similar | 🟠 High |
| GPS Close | <100m apart | 🟡 Nearby |
| Time Cluster | <24h apart | 🔵 Recent |

---

## UI/UX Requirements

### Duplicate Alert Banner

```
┌─────────────────────────────────────────────────────────────┐
│  ⚠️ POTENTIAL DUPLICATES DETECTED                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  This submission may be a duplicate of:                     │
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │                 │  │                 │                  │
│  │  [Dog Photo 1]  │  │  [Dog Photo 2]  │                  │
│  │                 │  │                 │                  │
│  ├─────────────────┤  ├─────────────────┤                  │
│  │ 🔴 92% Match    │  │ 🟡 Nearby       │                  │
│  │ SDC-2026-001230 │  │ SDC-2026-001228 │                  │
│  │ ✅ Approved     │  │ ⏳ Pending      │                  │
│  │ 3 hours ago     │  │ 5 hours ago     │                  │
│  └─────────────────┘  └─────────────────┘                  │
│                                                             │
│  [  Compare  ]       [  View All (4)  ]                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Side-by-Side Comparison

```
┌─────────────────────────────────────────────────────────────┐
│  ← Back                 Compare Submissions                 │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│    CURRENT                        POTENTIAL DUPLICATE       │
│  ┌──────────────────┐          ┌──────────────────┐        │
│  │                  │          │                  │        │
│  │                  │          │                  │        │
│  │  [Dog Photo]     │          │  [Dog Photo]     │        │
│  │                  │          │                  │        │
│  │                  │          │                  │        │
│  └──────────────────┘          └──────────────────┘        │
│                                                             │
│  SDC-2026-001234              SDC-2026-001230               │
│  Today 9:23 AM                Today 6:15 AM                │
│  Block: Boulaos               Block: Boulaos               │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  Comparison Details:                                        │
│  ├─ Image Similarity: 92%  🔴                              │
│  ├─ GPS Distance: 45 meters                                │
│  ├─ Time Gap: 3 hours 8 minutes                            │
│  └─ Same Block: Yes                                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  [ ◀ Prev Match ]   2 of 4   [ Next Match ▶ ]             │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌────────────────┐  ┌────────────────┐  ┌───────────────┐ │
│  │ 🔙 Not Dup     │  │ 📎 Link as Dup │  │ ⏭ Skip Both  │ │
│  └────────────────┘  └────────────────┘  └───────────────┘ │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Overlay Comparison Mode

```
┌─────────────────────────────────────────────────────────────┐
│  ← Back                 Overlay Compare                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │                                                       │  │
│  │              [Overlaid Dog Photos]                    │  │
│  │                                                       │  │
│  │                                                       │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Current  ○─────────────────●───────────────○ Original │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  Drag slider to compare                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### Fetch Duplicates for Submission

```javascript
async function fetchPotentialDuplicates(submission) {
  const duplicates = [];

  // 1. Hash-based duplicates (pHash similarity)
  const hashMatches = await db.submissions.find({
    applicationId: { $ne: submission.applicationId },
    tenantId: submission.tenantId,
    createdAt: { $gte: daysAgo(7) },
    status: { $nin: ['auto_rejected'] }
  }).toArray();

  for (const match of hashMatches) {
    const similarity = calculateHashSimilarity(
      submission.dogPhotoHash,
      match.dogPhotoHash
    );

    if (similarity >= 0.85) {
      duplicates.push({
        type: 'hash',
        applicationId: match.applicationId,
        similarity: similarity,
        photoUrl: await getSignedUrl(match.dogPhotoPath, 3600),
        submittedAt: match.createdAt,
        status: match.status,
        teacherId: anonymizeId(match.teacherId),
        location: match.location,
        gpsDistance: calculateDistance(submission.location, match.location)
      });
    }
  }

  // 2. GPS-based proximity matches
  const nearbyMatches = await db.submissions.find({
    applicationId: { $ne: submission.applicationId },
    tenantId: submission.tenantId,
    createdAt: { $gte: daysAgo(7) },
    status: { $nin: ['auto_rejected'] },
    location: {
      $near: {
        $geometry: {
          type: 'Point',
          coordinates: [submission.longitude, submission.latitude]
        },
        $maxDistance: 500 // meters
      }
    }
  }).toArray();

  for (const match of nearbyMatches) {
    // Don't duplicate hash matches
    if (!duplicates.find(d => d.applicationId === match.applicationId)) {
      const distance = calculateDistance(submission.location, match.location);
      if (distance <= 100) {
        duplicates.push({
          type: 'gps',
          applicationId: match.applicationId,
          similarity: null,
          photoUrl: await getSignedUrl(match.dogPhotoPath, 3600),
          submittedAt: match.createdAt,
          status: match.status,
          teacherId: anonymizeId(match.teacherId),
          location: match.location,
          gpsDistance: distance
        });
      }
    }
  }

  // Sort by relevance (hash similarity > GPS proximity > time)
  duplicates.sort((a, b) => {
    if (a.type === 'hash' && b.type !== 'hash') return -1;
    if (b.type === 'hash' && a.type !== 'hash') return 1;
    if (a.similarity && b.similarity) return b.similarity - a.similarity;
    return a.gpsDistance - b.gpsDistance;
  });

  return duplicates;
}
```

### Hash Similarity Calculation

```javascript
function calculateHashSimilarity(hash1, hash2) {
  // Hamming distance for perceptual hashes
  const binary1 = hexToBinary(hash1);
  const binary2 = hexToBinary(hash2);

  let distance = 0;
  for (let i = 0; i < binary1.length; i++) {
    if (binary1[i] !== binary2[i]) {
      distance++;
    }
  }

  // Convert hamming distance to similarity percentage
  const maxDistance = binary1.length;
  return 1 - (distance / maxDistance);
}

function hexToBinary(hex) {
  return hex.split('').map(h =>
    parseInt(h, 16).toString(2).padStart(4, '0')
  ).join('');
}
```

### Comparison Component

```javascript
class DuplicateComparison extends Component {
  constructor(props) {
    super(props);
    this.state = {
      currentIndex: 0,
      viewMode: 'side-by-side', // 'side-by-side' | 'overlay' | 'swipe'
      overlayOpacity: 0.5
    };
  }

  navigateDuplicate(direction) {
    const { duplicates } = this.props;
    const newIndex = direction === 'next'
      ? Math.min(this.state.currentIndex + 1, duplicates.length - 1)
      : Math.max(this.state.currentIndex - 1, 0);
    this.setState({ currentIndex: newIndex });
  }

  async markAsDuplicate() {
    const { submission, duplicates, onAction } = this.props;
    const duplicate = duplicates[this.state.currentIndex];

    const result = await markSubmissionAsDuplicate(
      submission.applicationId,
      duplicate.applicationId
    );

    onAction('duplicate', result);
  }

  confirmNotDuplicate() {
    const { submission, duplicates, onAction } = this.props;
    const duplicate = duplicates[this.state.currentIndex];

    // Log that verifier reviewed and determined not duplicate
    logDuplicateReview(submission.applicationId, duplicate.applicationId, 'not_duplicate');

    // Move to next duplicate or back to review
    if (this.state.currentIndex < duplicates.length - 1) {
      this.navigateDuplicate('next');
    } else {
      onAction('review_complete');
    }
  }

  renderSideBySide() {
    const { submission, duplicates } = this.props;
    const duplicate = duplicates[this.state.currentIndex];

    return (
      <div className="side-by-side">
        <div className="current-submission">
          <h3>Current Submission</h3>
          <img src={submission.dogPhoto.url} alt="Current" />
          <div className="details">
            <p>{submission.applicationId}</p>
            <p>{formatDateTime(submission.submittedAt)}</p>
          </div>
        </div>

        <div className="potential-duplicate">
          <h3>Potential Duplicate</h3>
          <img src={duplicate.photoUrl} alt="Duplicate" />
          <div className="details">
            <p>{duplicate.applicationId}</p>
            <p>{formatDateTime(duplicate.submittedAt)}</p>
            <StatusBadge status={duplicate.status} />
          </div>
        </div>

        <div className="comparison-stats">
          {duplicate.similarity && (
            <div className="stat">
              <span className="label">Image Similarity</span>
              <span className={`value ${this.getSimilarityClass(duplicate.similarity)}`}>
                {Math.round(duplicate.similarity * 100)}%
              </span>
            </div>
          )}
          <div className="stat">
            <span className="label">GPS Distance</span>
            <span className="value">{duplicate.gpsDistance.toFixed(0)}m</span>
          </div>
          <div className="stat">
            <span className="label">Time Gap</span>
            <span className="value">{formatTimeGap(submission.submittedAt, duplicate.submittedAt)}</span>
          </div>
        </div>
      </div>
    );
  }

  renderOverlay() {
    const { submission, duplicates } = this.props;
    const duplicate = duplicates[this.state.currentIndex];

    return (
      <div className="overlay-comparison">
        <div className="photo-stack">
          <img
            src={submission.dogPhoto.url}
            className="base-photo"
            alt="Current"
          />
          <img
            src={duplicate.photoUrl}
            className="overlay-photo"
            style={{ opacity: this.state.overlayOpacity }}
            alt="Duplicate"
          />
        </div>

        <input
          type="range"
          min="0"
          max="1"
          step="0.01"
          value={this.state.overlayOpacity}
          onChange={(e) => this.setState({ overlayOpacity: parseFloat(e.target.value) })}
          className="opacity-slider"
        />
        <div className="slider-labels">
          <span>Current</span>
          <span>Original</span>
        </div>
      </div>
    );
  }

  getSimilarityClass(similarity) {
    if (similarity >= 0.95) return 'very-high';
    if (similarity >= 0.90) return 'high';
    if (similarity >= 0.85) return 'medium';
    return 'low';
  }

  render() {
    const { duplicates } = this.props;

    if (!duplicates || duplicates.length === 0) {
      return <div className="no-duplicates">No potential duplicates found</div>;
    }

    return (
      <div className="duplicate-comparison">
        <div className="view-mode-tabs">
          <button
            className={this.state.viewMode === 'side-by-side' ? 'active' : ''}
            onClick={() => this.setState({ viewMode: 'side-by-side' })}
          >
            Side by Side
          </button>
          <button
            className={this.state.viewMode === 'overlay' ? 'active' : ''}
            onClick={() => this.setState({ viewMode: 'overlay' })}
          >
            Overlay
          </button>
        </div>

        {this.state.viewMode === 'side-by-side'
          ? this.renderSideBySide()
          : this.renderOverlay()
        }

        <div className="navigation">
          <button
            onClick={() => this.navigateDuplicate('prev')}
            disabled={this.state.currentIndex === 0}
          >
            ◀ Previous
          </button>
          <span>{this.state.currentIndex + 1} of {duplicates.length}</span>
          <button
            onClick={() => this.navigateDuplicate('next')}
            disabled={this.state.currentIndex === duplicates.length - 1}
          >
            Next ▶
          </button>
        </div>

        <div className="actions">
          <button className="not-duplicate" onClick={this.confirmNotDuplicate.bind(this)}>
            🔙 Not Duplicate
          </button>
          <button className="mark-duplicate" onClick={this.markAsDuplicate.bind(this)}>
            📎 Link as Duplicate
          </button>
        </div>
      </div>
    );
  }
}
```

### Mark as Duplicate API

```javascript
async function markSubmissionAsDuplicate(currentId, originalId) {
  const session = db.startSession();

  try {
    session.startTransaction();

    // Update current submission as duplicate
    await db.submissions.updateOne(
      { applicationId: currentId },
      {
        $set: {
          status: 'rejected',
          rejectedAt: new Date().toISOString(),
          rejection: {
            reasonCode: 'DUPLICATE',
            reasonLabel: 'Duplicate Submission',
            linkedTo: originalId
          }
        }
      },
      { session }
    );

    // Link in duplicates collection
    await db.duplicates.insertOne({
      duplicateId: currentId,
      originalId: originalId,
      markedAt: new Date().toISOString(),
      markedBy: getCurrentVerifierId()
    }, { session });

    // Remove from verifier queue
    await db.verifierQueue.deleteOne(
      { applicationId: currentId },
      { session }
    );

    await session.commitTransaction();

    // Notify teacher
    await notifyDuplicateRejection(currentId, originalId);

    return { success: true };

  } catch (error) {
    await session.abortTransaction();
    throw error;
  } finally {
    session.endSession();
  }
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| phash-image | Library | Perceptual hashing |
| MongoDB GeoNear | Database | Proximity queries |
| Image comparison | Library | Visual diff |

---

## Related Stories

- [S-VAL-03](../system/S-VAL-03.md) - Verify photo hash
- [V-REV-01](./V-REV-01.md) - Review submission
- [V-REJ-02](./V-REJ-02.md) - Mark as duplicate
- [V-REJ-04](./V-REJ-04.md) - Bulk duplicate actions
