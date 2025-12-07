# V-APP-01: Approve Submission

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Verifier
**I want to** approve valid submissions with a single click
**So that** I can process the queue efficiently.

---

## Description

After reviewing the evidence, the verifier approves legitimate submissions. The approval action should be quick and efficient while ensuring critical flags have been acknowledged. Upon approval, the submission moves to the MC Officer queue for field action.

---

## Acceptance Criteria

### Approval Flow

- [ ] Single-click approval button on review screen
- [ ] Critical flags must be acknowledged before approval
- [ ] Confirmation dialog for submissions with warnings
- [ ] Approval timestamp recorded
- [ ] Verifier ID logged for audit
- [ ] Queue item removed upon approval
- [ ] Next submission auto-loaded (optional setting)

### Pre-Approval Checks

- [ ] All critical flags acknowledged
- [ ] Photos verified as visible and relevant
- [ ] GPS location within service area
- [ ] No exact duplicate exists

### Post-Approval Actions

- [ ] Status updated to "approved"
- [ ] Route to MC queue (auto)
- [ ] Timeline event added
- [ ] Verifier stats updated

---

## UI/UX Requirements

### Approval Button States

```
Normal State:
┌────────────────────────────────────┐
│                                    │
│          ✓ APPROVE                 │
│                                    │
└────────────────────────────────────┘

With Warnings (requires confirmation):
┌────────────────────────────────────┐
│                                    │
│       ⚠️ APPROVE WITH FLAGS        │
│                                    │
└────────────────────────────────────┘

Blocked (critical flags not acknowledged):
┌────────────────────────────────────┐
│                                    │
│       🔒 ACKNOWLEDGE FLAGS         │
│                                    │
└────────────────────────────────────┘

Processing:
┌────────────────────────────────────┐
│                                    │
│       ⏳ Processing...             │
│                                    │
└────────────────────────────────────┘
```

### Confirmation Dialog (With Warnings)

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  Confirm Approval                                           │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  This submission has 2 warning flags:                       │
│                                                             │
│  ├─ 🟠 Gallery Upload                                      │
│  └─ 🟠 Low GPS Accuracy (42m)                              │
│                                                             │
│  ─────────────────────────────────────────────────────      │
│                                                             │
│  Are you sure you want to approve this submission?          │
│                                                             │
│  Approving will:                                            │
│  • Route to MC Officer queue                                │
│  • Make teacher eligible for payout upon capture            │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   Cancel           │  │   ✓ Yes, Approve           │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Success Feedback

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    ✓ Approved                               │
│                                                             │
│         SDC-2024-001234 routed to MC queue                  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  🎉  Submission approved successfully                 │  │
│  │                                                       │  │
│  │  • Assigned to: MC-Block-Boulaos                      │  │
│  │  • Teacher will be notified                           │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│            Loading next submission...                       │
│            ●○○                                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Action Bar

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ┌────────────────┐  ┌────────────────┐  ┌───────────────┐ │
│  │                │  │                │  │               │ │
│  │   ❌ REJECT    │  │   ⏭ SKIP      │  │  ✓ APPROVE   │ │
│  │                │  │                │  │               │ │
│  └────────────────┘  └────────────────┘  └───────────────┘ │
│                                                             │
│  Keyboard shortcuts: R = Reject, S = Skip, A = Approve      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### Approval Service

```javascript
class ApprovalService {
  constructor(db, mcAssignmentService, notificationService) {
    this.db = db;
    this.mcAssignment = mcAssignmentService;
    this.notifications = notificationService;
  }

  async approveSubmission(queueId, verifierId, options = {}) {
    const session = this.db.startSession();

    try {
      session.startTransaction();

      // Fetch queue entry
      const queueEntry = await this.db.verifierQueue.findOne(
        { queueId },
        { session }
      );

      if (!queueEntry) {
        throw new Error('Queue entry not found');
      }

      // Verify pre-approval checks
      const preCheck = await this.verifyPreApprovalChecks(queueEntry, verifierId);
      if (!preCheck.passed) {
        throw new Error(preCheck.reason);
      }

      // Fetch full submission
      const submission = await this.db.submissions.findOne(
        { applicationId: queueEntry.applicationId },
        { session }
      );

      const now = new Date().toISOString();

      // Update submission status
      await this.db.submissions.updateOne(
        { applicationId: submission.applicationId },
        {
          $set: {
            status: 'approved',
            approvedAt: now,
            approvedBy: verifierId,
            verificationNotes: options.notes || null
          }
        },
        { session }
      );

      // Remove from verifier queue
      await this.db.verifierQueue.updateOne(
        { queueId },
        {
          $set: {
            status: 'approved',
            'timestamps.completedAt': now,
            completedBy: verifierId
          }
        },
        { session }
      );

      // Log approval action
      await this.logApproval(submission, verifierId, queueEntry, { session });

      await session.commitTransaction();

      // Post-approval async actions (not in transaction)
      await this.postApprovalActions(submission, verifierId);

      return {
        success: true,
        applicationId: submission.applicationId,
        message: 'Submission approved successfully'
      };

    } catch (error) {
      await session.abortTransaction();
      throw error;
    } finally {
      session.endSession();
    }
  }

  async verifyPreApprovalChecks(queueEntry, verifierId) {
    // 1. Check critical flags are acknowledged
    const criticalFlags = queueEntry.submissionSummary.flags?.filter(
      f => f.severity === 'critical'
    ) || [];

    const acknowledgments = await this.db.flagAcknowledgments.find({
      queueId: queueEntry.queueId,
      verifierId: verifierId
    }).toArray();

    const acknowledgedCodes = acknowledgments.map(a => a.flagCode);
    const unacknowledged = criticalFlags.filter(
      f => !acknowledgedCodes.includes(f.code)
    );

    if (unacknowledged.length > 0) {
      return {
        passed: false,
        reason: 'CRITICAL_FLAGS_NOT_ACKNOWLEDGED',
        details: unacknowledged.map(f => f.code)
      };
    }

    // 2. Check submission is still pending
    if (queueEntry.status !== 'pending') {
      return {
        passed: false,
        reason: 'ALREADY_PROCESSED',
        details: { currentStatus: queueEntry.status }
      };
    }

    // 3. Check verifier has permission
    const verifier = await this.db.users.findOne({ id: verifierId });
    if (!verifier || verifier.role !== 'VERIFIER') {
      return {
        passed: false,
        reason: 'UNAUTHORIZED'
      };
    }

    return { passed: true };
  }

  async logApproval(submission, verifierId, queueEntry, options = {}) {
    const queuedTime = new Date(queueEntry.timestamps.queued);
    const completedTime = new Date();
    const reviewDuration = (completedTime - queuedTime) / 1000; // seconds

    await this.db.auditLog.insertOne({
      type: 'SUBMISSION_APPROVED',
      applicationId: submission.applicationId,
      actorId: verifierId,
      actorRole: 'VERIFIER',
      timestamp: completedTime,
      data: {
        queueId: queueEntry.queueId,
        reviewDuration,
        flags: queueEntry.submissionSummary.flags?.map(f => f.code) || [],
        priority: queueEntry.priority,
        slaRemaining: calculateSLARemaining(queueEntry.timestamps.slaDeadline)
      }
    }, options);

    // Update verifier stats
    await this.db.verifierStats.updateOne(
      { verifierId, date: getDateString(completedTime) },
      {
        $inc: {
          approved: 1,
          totalReviewed: 1,
          totalReviewTime: reviewDuration
        }
      },
      { upsert: true, ...options }
    );
  }

  async postApprovalActions(submission, verifierId) {
    // 1. Route to MC queue
    const assignment = await this.mcAssignment.assignMCOfficer(submission);

    // 2. Notify teacher
    await this.notifications.create({
      userId: submission.teacherId,
      type: 'submission_approved',
      title: 'Report Approved',
      body: 'Your report has been verified and assigned for action.',
      data: {
        applicationId: submission.applicationId,
        status: 'approved'
      }
    });

    // 3. Add timeline event
    await addTimelineEvent(submission.applicationId, {
      status: 'approved',
      timestamp: new Date().toISOString(),
      actor: 'Verification Team',
      data: {
        verifierId: anonymizeId(verifierId),
        assignedTo: 'MC Officer'
      }
    });

    return assignment;
  }
}
```

### Approval API Endpoint

```javascript
// POST /api/verifier/submissions/:queueId/approve
async function handleApprove(req, res) {
  const { queueId } = req.params;
  const { notes, confirmWarnings } = req.body;
  const verifierId = req.user.id;

  try {
    // Check for warnings requiring confirmation
    const queueEntry = await db.verifierQueue.findOne({ queueId });
    const warnings = queueEntry.submissionSummary.flags?.filter(
      f => f.severity === 'warning'
    ) || [];

    if (warnings.length > 0 && !confirmWarnings) {
      return res.status(400).json({
        success: false,
        requiresConfirmation: true,
        warnings: warnings.map(w => ({
          code: w.code,
          message: w.message
        }))
      });
    }

    const approvalService = new ApprovalService(db, mcAssignmentService, notificationService);
    const result = await approvalService.approveSubmission(queueId, verifierId, { notes });

    // Get next item in queue if auto-advance enabled
    let nextItem = null;
    if (req.user.preferences?.autoAdvance) {
      nextItem = await getNextQueueItem(verifierId);
    }

    return res.json({
      success: true,
      ...result,
      nextItem
    });

  } catch (error) {
    console.error('Approval failed:', error);

    if (error.message === 'CRITICAL_FLAGS_NOT_ACKNOWLEDGED') {
      return res.status(400).json({
        success: false,
        error: 'CRITICAL_FLAGS_NOT_ACKNOWLEDGED',
        message: 'Please acknowledge all critical flags before approving'
      });
    }

    return res.status(500).json({
      success: false,
      error: 'APPROVAL_FAILED',
      message: error.message
    });
  }
}
```

### Approval Button Component

```javascript
class ApprovalButton extends Component {
  constructor(props) {
    super(props);
    this.state = {
      status: 'idle', // 'idle' | 'confirming' | 'processing' | 'success'
      error: null
    };
  }

  canApprove() {
    const { flags, acknowledgedFlags } = this.props;
    const criticalFlags = flags?.filter(f => f.severity === 'critical') || [];
    const allAcknowledged = criticalFlags.every(
      f => acknowledgedFlags?.includes(f.code)
    );
    return allAcknowledged;
  }

  hasWarnings() {
    const { flags } = this.props;
    return flags?.some(f => f.severity === 'warning');
  }

  async handleApprove() {
    if (!this.canApprove()) {
      this.setState({ error: 'Please acknowledge all critical flags first' });
      return;
    }

    if (this.hasWarnings() && this.state.status !== 'confirming') {
      this.setState({ status: 'confirming' });
      return;
    }

    this.setState({ status: 'processing', error: null });

    try {
      const { queueId, onSuccess, onNext } = this.props;

      const response = await fetch(`/api/verifier/submissions/${queueId}/approve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          confirmWarnings: this.state.status === 'confirming'
        })
      });

      const result = await response.json();

      if (result.success) {
        this.setState({ status: 'success' });

        // Show success briefly, then advance
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
        status: 'idle',
        error: error.message
      });
    }
  }

  handleCancel() {
    this.setState({ status: 'idle' });
  }

  render() {
    const { status, error } = this.state;

    if (status === 'confirming') {
      return (
        <ConfirmationDialog
          title="Confirm Approval"
          warnings={this.props.flags?.filter(f => f.severity === 'warning')}
          onConfirm={() => this.handleApprove()}
          onCancel={() => this.handleCancel()}
        />
      );
    }

    if (status === 'success') {
      return (
        <div className="approval-success">
          <div className="success-icon">✓</div>
          <p>Approved</p>
          <p className="loading-next">Loading next submission...</p>
        </div>
      );
    }

    const buttonClass = !this.canApprove()
      ? 'blocked'
      : this.hasWarnings()
        ? 'with-warnings'
        : 'normal';

    return (
      <div className="approval-button-container">
        {error && <div className="error-message">{error}</div>}

        <button
          className={`approval-button ${buttonClass}`}
          onClick={() => this.handleApprove()}
          disabled={status === 'processing' || !this.canApprove()}
        >
          {status === 'processing' && '⏳ Processing...'}
          {status === 'idle' && !this.canApprove() && '🔒 Acknowledge Flags'}
          {status === 'idle' && this.canApprove() && this.hasWarnings() && '⚠️ Approve with Flags'}
          {status === 'idle' && this.canApprove() && !this.hasWarnings() && '✓ Approve'}
        </button>

        <div className="keyboard-hint">
          Press <kbd>A</kbd> to approve
        </div>
      </div>
    );
  }
}
```

### Keyboard Shortcuts

```javascript
function useApprovalShortcuts({ onApprove, onReject, onSkip }) {
  useEffect(() => {
    function handleKeyPress(event) {
      // Don't trigger if typing in input
      if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA') {
        return;
      }

      switch (event.key.toLowerCase()) {
        case 'a':
          onApprove?.();
          break;
        case 'r':
          onReject?.();
          break;
        case 's':
          onSkip?.();
          break;
      }
    }

    window.addEventListener('keypress', handleKeyPress);
    return () => window.removeEventListener('keypress', handleKeyPress);
  }, [onApprove, onReject, onSkip]);
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| MC Assignment Service | Internal | Route to MC queue |
| Notification Service | Internal | Teacher notification |
| Audit Logger | Internal | Action logging |

---

## Related Stories

- [V-REV-01](./V-REV-01.md) - Review submission
- [V-REV-03](./V-REV-03.md) - View fraud flags
- [V-APP-02](./V-APP-02.md) - Auto-route to MC queue
- [S-MC-01](../system/S-MC-01.md) - MC assignment
