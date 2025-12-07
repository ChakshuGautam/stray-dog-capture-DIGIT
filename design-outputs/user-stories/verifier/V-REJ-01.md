# V-REJ-01: Reject with Reason

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Verifier
**I want to** reject invalid submissions and select a reason from a predefined list
**So that** rejection reasons are consistent and trackable.

---

## Description

When a submission fails verification, the verifier rejects it with a standardized reason. This ensures teachers receive clear feedback, enables analytics on rejection patterns, and maintains consistency across verifiers.

---

## Acceptance Criteria

### Rejection Flow

- [ ] Single-click reject button opens reason selector
- [ ] Predefined rejection reasons available
- [ ] Optional notes field for additional context
- [ ] Confirmation required before submission
- [ ] Teacher notified immediately
- [ ] Submission removed from queue
- [ ] No payout eligibility for rejected submissions

### Predefined Rejection Reasons

| Code | Label | Description |
|------|-------|-------------|
| UNCLEAR_PHOTO | Unclear Photo | Photo is blurry, dark, or dog not visible |
| NO_DOG | No Dog Visible | Cannot identify a dog in the photo |
| NOT_STRAY | Not a Stray | Dog appears to be pet (collar, groomed) |
| FAKE_PHOTO | Fake/Screenshot | Photo appears to be screenshot or fake |
| GPS_ISSUE | Location Issue | GPS coordinates appear incorrect |
| SELFIE_MISMATCH | Selfie Mismatch | Selfie doesn't match location |
| OLD_PHOTO | Photo Too Old | Photo is older than allowed |
| INAPPROPRIATE | Inappropriate Content | Photo contains inappropriate content |
| OTHER | Other | Custom reason (requires notes) |

---

## UI/UX Requirements

### Rejection Reason Selector

```
┌─────────────────────────────────────────────────────────────┐
│  ← Back                     Reject Submission               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Select rejection reason:                                   │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ○ Unclear Photo                                       │  │
│  │   Photo is blurry, dark, or dog not visible           │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ○ No Dog Visible                                      │  │
│  │   Cannot identify a dog in the photo                  │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ○ Not a Stray Dog                                     │  │
│  │   Dog appears to be a pet (collar, groomed)           │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ○ Fake/Screenshot Photo                               │  │
│  │   Photo appears to be screenshot or downloaded        │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ○ Location Issue                                      │  │
│  │   GPS coordinates appear incorrect or spoofed         │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ○ Selfie Mismatch                                     │  │
│  │   Selfie doesn't match expected location              │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ○ Photo Too Old                                       │  │
│  │   Photo timestamp exceeds allowed age                 │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ○ Inappropriate Content                               │  │
│  │   Photo contains inappropriate or offensive content   │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ○ Other                                               │  │
│  │   Custom reason (please specify below)                │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Selected Reason with Notes

```
┌─────────────────────────────────────────────────────────────┐
│  ← Back                     Reject Submission               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Selected reason:                                           │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ● Unclear Photo                                       │  │
│  │   Photo is blurry, dark, or dog not visible           │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  Additional notes (optional):                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ The photo is too dark to identify the dog. Please     │  │
│  │ take a photo in better lighting conditions.           │  │
│  │                                                       │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│  0/200 characters                                           │
│                                                             │
│  ─────────────────────────────────────────────────────      │
│                                                             │
│  ⚠️ This action cannot be undone. The teacher will be       │
│     notified of the rejection.                              │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   Cancel           │  │   ❌ Confirm Rejection     │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Rejection Confirmation

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  Confirm Rejection                                          │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  You are about to reject:                                   │
│                                                             │
│  Application: SDC-2024-001234                               │
│  Reason: Unclear Photo                                      │
│                                                             │
│  ─────────────────────────────────────────────────────      │
│                                                             │
│  This will:                                                 │
│  • Notify the teacher of the rejection                      │
│  • Remove from verification queue                           │
│  • Disqualify for payout                                    │
│                                                             │
│  The teacher can submit a new report for this dog.          │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   Go Back          │  │   ❌ Yes, Reject           │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### Rejection Reasons Configuration

```javascript
const REJECTION_REASONS = [
  {
    code: 'UNCLEAR_PHOTO',
    label: 'Unclear Photo',
    description: 'Photo is blurry, dark, or dog not visible',
    teacherMessage: 'Your photo was not clear enough. Please take a clearer photo in good lighting.',
    tips: ['Use natural daylight', 'Hold camera steady', 'Ensure dog is in focus'],
    requiresNotes: false
  },
  {
    code: 'NO_DOG',
    label: 'No Dog Visible',
    description: 'Cannot identify a dog in the photo',
    teacherMessage: 'We could not identify a dog in your photo.',
    tips: ['Ensure the dog is clearly visible', 'Include the full dog in frame'],
    requiresNotes: false
  },
  {
    code: 'NOT_STRAY',
    label: 'Not a Stray Dog',
    description: 'Dog appears to be a pet (collar, groomed)',
    teacherMessage: 'The dog in your photo appears to be a pet, not a stray.',
    tips: ['Only report stray dogs', 'Look for collar or tags'],
    requiresNotes: false
  },
  {
    code: 'FAKE_PHOTO',
    label: 'Fake/Screenshot Photo',
    description: 'Photo appears to be screenshot or downloaded',
    teacherMessage: 'The photo appears to be a screenshot or downloaded image. Please take an original photo.',
    tips: ['Use the in-app camera', 'Do not use gallery photos from internet'],
    requiresNotes: false,
    flagForReview: true // May indicate fraud
  },
  {
    code: 'GPS_ISSUE',
    label: 'Location Issue',
    description: 'GPS coordinates appear incorrect or spoofed',
    teacherMessage: 'There was an issue with the location data. Please ensure GPS is enabled and accurate.',
    tips: ['Enable high-accuracy GPS', 'Submit from actual location'],
    requiresNotes: false,
    flagForReview: true
  },
  {
    code: 'SELFIE_MISMATCH',
    label: 'Selfie Mismatch',
    description: 'Selfie does not match expected location',
    teacherMessage: 'Your selfie location does not match the dog photo location.',
    tips: ['Take both photos at the same location', 'Ensure GPS is accurate'],
    requiresNotes: false,
    flagForReview: true
  },
  {
    code: 'OLD_PHOTO',
    label: 'Photo Too Old',
    description: 'Photo timestamp exceeds allowed age',
    teacherMessage: 'Your photo is older than 24 hours. Please submit a recent photo.',
    tips: ['Take fresh photos', 'Photos must be within 24 hours'],
    requiresNotes: false
  },
  {
    code: 'INAPPROPRIATE',
    label: 'Inappropriate Content',
    description: 'Photo contains inappropriate or offensive content',
    teacherMessage: 'Your submission contained inappropriate content and has been rejected.',
    tips: ['Submit only dog photos', 'Ensure content is appropriate'],
    requiresNotes: true,
    flagForReview: true
  },
  {
    code: 'OTHER',
    label: 'Other',
    description: 'Custom reason (please specify)',
    teacherMessage: 'Your submission was rejected.',
    tips: [],
    requiresNotes: true
  }
];
```

### Rejection Service

```javascript
class RejectionService {
  constructor(db, notificationService) {
    this.db = db;
    this.notifications = notificationService;
  }

  async rejectSubmission(queueId, verifierId, { reasonCode, notes }) {
    const session = this.db.startSession();

    try {
      session.startTransaction();

      // Get queue entry
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

      // Validate reason
      const reason = REJECTION_REASONS.find(r => r.code === reasonCode);
      if (!reason) {
        throw new Error('Invalid rejection reason');
      }

      if (reason.requiresNotes && (!notes || notes.trim().length === 0)) {
        throw new Error('Notes required for this rejection reason');
      }

      const now = new Date().toISOString();

      // Update submission
      await this.db.submissions.updateOne(
        { applicationId: queueEntry.applicationId },
        {
          $set: {
            status: 'rejected',
            rejectedAt: now,
            rejectedBy: verifierId,
            rejection: {
              reasonCode: reason.code,
              reasonLabel: reason.label,
              description: reason.description,
              teacherMessage: reason.teacherMessage,
              tips: reason.tips,
              notes: notes || null
            }
          }
        },
        { session }
      );

      // Update queue entry
      await this.db.verifierQueue.updateOne(
        { queueId },
        {
          $set: {
            status: 'rejected',
            'timestamps.completedAt': now,
            completedBy: verifierId,
            rejectionReason: reason.code
          }
        },
        { session }
      );

      // Log rejection
      await this.logRejection(queueEntry, verifierId, reason, notes, { session });

      await session.commitTransaction();

      // Post-rejection actions (not in transaction)
      await this.postRejectionActions(queueEntry, reason, notes);

      return {
        success: true,
        applicationId: queueEntry.applicationId,
        reasonCode: reason.code
      };

    } catch (error) {
      await session.abortTransaction();
      throw error;
    } finally {
      session.endSession();
    }
  }

  async logRejection(queueEntry, verifierId, reason, notes, options = {}) {
    const queuedTime = new Date(queueEntry.timestamps.queued);
    const completedTime = new Date();
    const reviewDuration = (completedTime - queuedTime) / 1000;

    await this.db.auditLog.insertOne({
      type: 'SUBMISSION_REJECTED',
      applicationId: queueEntry.applicationId,
      actorId: verifierId,
      actorRole: 'VERIFIER',
      timestamp: completedTime,
      data: {
        queueId: queueEntry.queueId,
        reasonCode: reason.code,
        reasonLabel: reason.label,
        notes: notes,
        reviewDuration,
        flags: queueEntry.submissionSummary.flags?.map(f => f.code) || [],
        flagForReview: reason.flagForReview || false
      }
    }, options);

    // Update verifier stats
    await this.db.verifierStats.updateOne(
      { verifierId, date: getDateString(completedTime) },
      {
        $inc: {
          rejected: 1,
          totalReviewed: 1,
          totalReviewTime: reviewDuration,
          [`rejectionsByReason.${reason.code}`]: 1
        }
      },
      { upsert: true, ...options }
    );

    // Track teacher rejection rate
    await this.db.teacherStats.updateOne(
      { teacherId: queueEntry.teacherId },
      {
        $inc: { rejectionCount: 1 },
        $push: {
          recentRejections: {
            $each: [{
              applicationId: queueEntry.applicationId,
              reasonCode: reason.code,
              rejectedAt: completedTime
            }],
            $slice: -10 // Keep last 10
          }
        }
      },
      { upsert: true, ...options }
    );
  }

  async postRejectionActions(queueEntry, reason, notes) {
    const submission = await this.db.submissions.findOne({
      applicationId: queueEntry.applicationId
    });

    // 1. Notify teacher
    await this.notifications.create({
      userId: queueEntry.teacherId,
      type: 'submission_rejected',
      title: 'Report Not Accepted',
      body: reason.teacherMessage,
      data: {
        applicationId: queueEntry.applicationId,
        reasonCode: reason.code,
        reasonLabel: reason.label,
        tips: reason.tips,
        notes: notes // Only if appropriate to share
      },
      priority: 'high'
    });

    // SMS notification
    await smsService.send(submission.teacherPhone, 'submission_rejected', {
      ID: queueEntry.applicationId,
      REASON: reason.label
    });

    // 2. Add timeline event
    await addTimelineEvent(queueEntry.applicationId, {
      status: 'rejected',
      timestamp: new Date().toISOString(),
      data: {
        reason: reason.label,
        message: reason.teacherMessage
      }
    });

    // 3. Flag for admin review if needed
    if (reason.flagForReview) {
      await this.flagForAdminReview(queueEntry, reason, notes);
    }
  }

  async flagForAdminReview(queueEntry, reason, notes) {
    await this.db.adminReviewQueue.insertOne({
      applicationId: queueEntry.applicationId,
      teacherId: queueEntry.teacherId,
      flagType: 'REJECTION_REVIEW',
      rejectionReason: reason.code,
      notes: notes,
      flaggedAt: new Date().toISOString(),
      status: 'pending'
    });

    await this.notifications.sendToRole('ADMIN', {
      type: 'rejection_review_needed',
      title: 'Rejection Review',
      body: `${queueEntry.applicationId} rejected for ${reason.label} - review needed`,
      priority: 'normal'
    });
  }
}
```

### Rejection Component

```javascript
class RejectionPanel extends Component {
  constructor(props) {
    super(props);
    this.state = {
      selectedReason: null,
      notes: '',
      step: 'select', // 'select' | 'confirm' | 'processing' | 'success'
      error: null
    };
  }

  selectReason(reasonCode) {
    this.setState({ selectedReason: reasonCode, error: null });
  }

  handleNotesChange(value) {
    if (value.length <= 200) {
      this.setState({ notes: value });
    }
  }

  canProceed() {
    const { selectedReason, notes } = this.state;
    const reason = REJECTION_REASONS.find(r => r.code === selectedReason);

    if (!reason) return false;
    if (reason.requiresNotes && notes.trim().length === 0) return false;

    return true;
  }

  proceedToConfirm() {
    if (this.canProceed()) {
      this.setState({ step: 'confirm' });
    }
  }

  async confirmRejection() {
    const { queueId, onSuccess, onNext } = this.props;
    const { selectedReason, notes } = this.state;

    this.setState({ step: 'processing', error: null });

    try {
      const response = await fetch(`/api/verifier/submissions/${queueId}/reject`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          reasonCode: selectedReason,
          notes: notes.trim() || null
        })
      });

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

  goBack() {
    if (this.state.step === 'confirm') {
      this.setState({ step: 'select' });
    } else {
      this.props.onCancel?.();
    }
  }

  render() {
    const { step, selectedReason, notes, error } = this.state;

    if (step === 'success') {
      return (
        <div className="rejection-success">
          <div className="icon">❌</div>
          <p>Rejected</p>
          <p className="loading-next">Loading next submission...</p>
        </div>
      );
    }

    if (step === 'processing') {
      return (
        <div className="rejection-processing">
          <div className="spinner" />
          <p>Processing rejection...</p>
        </div>
      );
    }

    if (step === 'confirm') {
      const reason = REJECTION_REASONS.find(r => r.code === selectedReason);

      return (
        <div className="rejection-confirm">
          <h3>Confirm Rejection</h3>

          {error && <div className="error">{error}</div>}

          <div className="confirm-details">
            <p><strong>Reason:</strong> {reason.label}</p>
            {notes && <p><strong>Notes:</strong> {notes}</p>}
          </div>

          <div className="warning">
            ⚠️ This action cannot be undone. The teacher will be notified.
          </div>

          <div className="actions">
            <button onClick={() => this.goBack()}>Go Back</button>
            <button
              className="reject-confirm"
              onClick={() => this.confirmRejection()}
            >
              ❌ Yes, Reject
            </button>
          </div>
        </div>
      );
    }

    // Select reason step
    return (
      <div className="rejection-select">
        <h3>Select Rejection Reason</h3>

        <div className="reasons-list">
          {REJECTION_REASONS.map(reason => (
            <div
              key={reason.code}
              className={`reason-item ${selectedReason === reason.code ? 'selected' : ''}`}
              onClick={() => this.selectReason(reason.code)}
            >
              <div className="radio">
                {selectedReason === reason.code ? '●' : '○'}
              </div>
              <div className="content">
                <div className="label">{reason.label}</div>
                <div className="description">{reason.description}</div>
              </div>
            </div>
          ))}
        </div>

        {selectedReason && (
          <div className="notes-section">
            <label>
              Additional notes
              {REJECTION_REASONS.find(r => r.code === selectedReason)?.requiresNotes
                ? ' (required)'
                : ' (optional)'
              }:
            </label>
            <textarea
              value={notes}
              onChange={(e) => this.handleNotesChange(e.target.value)}
              placeholder="Add context for the rejection..."
              maxLength={200}
            />
            <div className="char-count">{notes.length}/200</div>
          </div>
        )}

        <div className="actions">
          <button onClick={() => this.props.onCancel?.()}>Cancel</button>
          <button
            className="proceed"
            onClick={() => this.proceedToConfirm()}
            disabled={!this.canProceed()}
          >
            Continue
          </button>
        </div>
      </div>
    );
  }
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Notification Service | Internal | Teacher notification |
| SMS Gateway | External | Rejection SMS |
| Audit Logger | Internal | Rejection tracking |

---

## Related Stories

- [V-REV-01](./V-REV-01.md) - Review submission
- [V-REJ-02](./V-REJ-02.md) - Mark as duplicate
- [V-NOT-01](./V-NOT-01.md) - Notify teacher
- [T-STAT-03](../teacher/T-STAT-03.md) - View rejection reason
