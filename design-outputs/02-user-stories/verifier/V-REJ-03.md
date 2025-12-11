# V-REJ-03: Escalate to Senior Verifier

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Verifier
**I want to** escalate edge cases to senior verifiers
**So that** I can handle ambiguous submissions appropriately.

---

## Description

Not all submissions fit neatly into "approve" or "reject" categories. When a verifier encounters an ambiguous case—unclear photos, borderline fraud indicators, complex duplicates, or unusual circumstances—they can escalate to a senior verifier for a second opinion. This ensures difficult decisions are made by experienced staff while allowing junior verifiers to continue processing clear-cut cases.

---

## Acceptance Criteria

### Escalation Triggers

- [ ] Manual escalation via "Escalate" button
- [ ] Auto-escalation for SLA-breached items
- [ ] Escalation with reason required
- [ ] Escalation notes preserved for context
- [ ] Original verifier notified of resolution
- [ ] Escalation count tracked per submission

### Escalation Reasons

| Reason Code | Label | Description |
|-------------|-------|-------------|
| `unclear_photo` | Unclear Photo Quality | Photos are ambiguous - can't determine dog identity |
| `borderline_fraud` | Suspected Fraud | Fraud indicators present but not conclusive |
| `complex_duplicate` | Complex Duplicate | Multiple potential matches, unclear original |
| `unusual_location` | Unusual Location | GPS/location anomalies need expert review |
| `teacher_history` | Teacher History Concern | Teacher has flagged history, need senior decision |
| `policy_question` | Policy Question | Case doesn't fit standard approval/rejection criteria |
| `technical_issue` | Technical Issue | System-detected anomaly needs investigation |
| `other` | Other | Free-text explanation required |

### Senior Verifier Queue

- [ ] Separate queue for escalated items
- [ ] Shows escalation reason and notes
- [ ] Original verifier's actions visible
- [ ] Can approve, reject, or return to original verifier
- [ ] Resolution reason captured

### Permissions

- [ ] Any verifier can escalate
- [ ] Only senior verifiers can resolve escalations
- [ ] Admins can view all escalations
- [ ] Escalation metrics in dashboard

---

## UI/UX Requirements

### Escalation Button

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ┌────────────────┐  ┌────────────────┐  ┌───────────────┐ │
│  │                │  │                │  │               │ │
│  │   ❌ REJECT    │  │   ⬆️ ESCALATE  │  │  ✓ APPROVE   │ │
│  │                │  │                │  │               │ │
│  └────────────────┘  └────────────────┘  └───────────────┘ │
│                                                             │
│  Keyboard: R = Reject, E = Escalate, A = Approve           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Escalation Dialog

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ⬆️ Escalate to Senior Verifier                             │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Why are you escalating this submission?                    │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  ○ Unclear Photo Quality                              │  │
│  │  ○ Suspected Fraud                                    │  │
│  │  ○ Complex Duplicate                                  │  │
│  │  ○ Unusual Location                                   │  │
│  │  ○ Teacher History Concern                            │  │
│  │  ○ Policy Question                                    │  │
│  │  ○ Technical Issue                                    │  │
│  │  ● Other                                              │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  Add notes for senior verifier: *                           │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ The dog photo shows what appears to be a domestic     │  │
│  │ pet with a collar. Teacher claims it's stray but      │  │
│  │ location is in affluent residential area. Need        │  │
│  │ senior opinion on whether to proceed.                 │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                        Min 20 characters    │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   Cancel           │  │   ⬆️ Escalate              │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Escalation Confirmation

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    ⬆️ Escalated                             │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  SDC-2026-001234 has been escalated.                        │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  ⬆️  Escalated to Senior Verifier Queue              │  │
│  │                                                       │  │
│  │  Reason: Other                                        │  │
│  │                                                       │  │
│  │  You will be notified when this submission            │  │
│  │  is resolved.                                         │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│            Loading next submission...                       │
│            ●○○                                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Senior Verifier Queue View

```
┌─────────────────────────────────────────────────────────────┐
│  Escalated Submissions                          ⚙️ Filters  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  ⬆️  SDC-2026-001234         ⚠️ SLA: 2h remaining    │  │
│  │  ─────────────────────────────────────────────────    │  │
│  │  Escalated by: Verifier #12    2 hours ago            │  │
│  │  Reason: Other                                        │  │
│  │                                                       │  │
│  │  "The dog photo shows what appears to be a            │  │
│  │   domestic pet with a collar..."                      │  │
│  │                                                       │  │
│  │  Flags: 🟡 Gallery Upload                             │  │
│  │                                                       │  │
│  │  [ View Full Details ]                                │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  ⬆️  SDC-2026-001189         🟢 SLA: 18h remaining   │  │
│  │  ─────────────────────────────────────────────────    │  │
│  │  Escalated by: Verifier #08    5 hours ago            │  │
│  │  Reason: Complex Duplicate                            │  │
│  │                                                       │  │
│  │  "Found 3 potential matches with >75% similarity.     │  │
│  │   All submitted within same week..."                  │  │
│  │                                                       │  │
│  │  [ View Full Details ]                                │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  Showing 2 of 2 escalated items                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Senior Verifier Resolution View

```
┌─────────────────────────────────────────────────────────────┐
│  ← Back to Escalation Queue                                 │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  SDC-2026-001234                                            │
│                                                             │
│  ┌─────────────────────── Escalation Context ─────────────┐ │
│  │                                                        │ │
│  │  Escalated by: Verifier #12 (Marie D.)                 │ │
│  │  Date: Dec 7, 2026 at 10:32 AM                         │ │
│  │  Reason: Other                                         │ │
│  │                                                        │ │
│  │  Notes:                                                │ │
│  │  "The dog photo shows what appears to be a domestic    │ │
│  │   pet with a collar. Teacher claims it's stray but     │ │
│  │   location is in affluent residential area. Need       │ │
│  │   senior opinion on whether to proceed."               │ │
│  │                                                        │ │
│  │  ─────────────────────────────────────────────         │ │
│  │                                                        │ │
│  │  Original Verifier Actions:                            │ │
│  │  • Viewed photos (3x zoom used)                        │ │
│  │  • Checked location on map                             │ │
│  │  • Viewed teacher history (clean)                      │ │
│  │  • Time spent: 4m 32s                                  │ │
│  │                                                        │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                             │
│  [... Standard review interface with photos, map, etc ...] │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Resolution:                                                │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ After review, I believe this is indeed a stray dog.   │  │
│  │ The collar appears old and worn. Location proximity   │  │
│  │ to residential area doesn't disqualify submission.    │  │
│  │ Approving with note for MC to verify collar.          │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌─────────┐  ┌─────────────┐  │
│  │ ❌ REJECT │  │ ↩️ RETURN │  │ ⏭ SKIP │  │ ✓ APPROVE  │  │
│  └──────────┘  └──────────┘  └─────────┘  └─────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### Escalation Service

```javascript
class EscalationService {
  constructor(db, notificationService) {
    this.db = db;
    this.notifications = notificationService;
  }

  async escalateSubmission(queueId, verifierId, escalationData) {
    const session = this.db.startSession();

    try {
      session.startTransaction();

      // 1. Get queue entry
      const queueEntry = await this.db.verifierQueue.findOne(
        { queueId },
        { session }
      );

      if (!queueEntry) {
        throw new Error('Queue entry not found');
      }

      if (queueEntry.status !== 'pending') {
        throw new Error('Cannot escalate - already processed');
      }

      // 2. Validate escalation data
      this.validateEscalation(escalationData);

      const now = new Date().toISOString();

      // 3. Create escalation record
      const escalation = {
        escalationId: generateUUID(),
        queueId,
        applicationId: queueEntry.applicationId,
        tenantId: queueEntry.tenantId,
        escalatedBy: verifierId,
        escalatedAt: now,
        reason: escalationData.reason,
        notes: escalationData.notes,
        verifierActions: await this.getVerifierActions(queueId, verifierId),
        status: 'pending',
        escalationCount: (queueEntry.escalationCount || 0) + 1,
        previousEscalations: queueEntry.escalationHistory || []
      };

      await this.db.escalations.insertOne(escalation, { session });

      // 4. Update queue entry
      await this.db.verifierQueue.updateOne(
        { queueId },
        {
          $set: {
            status: 'escalated',
            escalationId: escalation.escalationId,
            'timestamps.escalatedAt': now
          },
          $inc: { escalationCount: 1 },
          $push: {
            escalationHistory: {
              escalationId: escalation.escalationId,
              escalatedBy: verifierId,
              escalatedAt: now,
              reason: escalationData.reason
            }
          }
        },
        { session }
      );

      // 5. Update submission status
      await this.db.submissions.updateOne(
        { applicationId: queueEntry.applicationId },
        {
          $set: {
            status: 'escalated',
            currentEscalationId: escalation.escalationId
          }
        },
        { session }
      );

      // 6. Log escalation
      await this.logEscalation(escalation, { session });

      await session.commitTransaction();

      // 7. Notify senior verifiers (async)
      await this.notifySeniorVerifiers(escalation);

      return {
        success: true,
        escalationId: escalation.escalationId
      };

    } catch (error) {
      await session.abortTransaction();
      throw error;
    } finally {
      session.endSession();
    }
  }

  validateEscalation(data) {
    const validReasons = [
      'unclear_photo',
      'borderline_fraud',
      'complex_duplicate',
      'unusual_location',
      'teacher_history',
      'policy_question',
      'technical_issue',
      'other'
    ];

    if (!validReasons.includes(data.reason)) {
      throw new Error('Invalid escalation reason');
    }

    if (!data.notes || data.notes.trim().length < 20) {
      throw new Error('Escalation notes must be at least 20 characters');
    }

    if (data.notes.length > 1000) {
      throw new Error('Escalation notes cannot exceed 1000 characters');
    }
  }

  async getVerifierActions(queueId, verifierId) {
    // Get tracked actions for context
    const actions = await this.db.verifierActions.find({
      queueId,
      verifierId
    }).sort({ timestamp: 1 }).toArray();

    return actions.map(a => ({
      action: a.action,
      timestamp: a.timestamp,
      details: a.details
    }));
  }

  async notifySeniorVerifiers(escalation) {
    // Get all senior verifiers for tenant
    const seniorVerifiers = await this.db.users.find({
      tenantId: escalation.tenantId,
      role: 'SENIOR_VERIFIER',
      isActive: true
    }).toArray();

    // Create notifications
    await Promise.all(seniorVerifiers.map(sv =>
      this.notifications.create({
        userId: sv.id,
        type: 'new_escalation',
        title: 'New Escalation',
        body: `Submission ${escalation.applicationId} escalated: ${escalation.reason}`,
        data: {
          escalationId: escalation.escalationId,
          applicationId: escalation.applicationId,
          reason: escalation.reason
        },
        priority: 'high'
      })
    ));
  }

  async logEscalation(escalation, options = {}) {
    await this.db.auditLog.insertOne({
      type: 'SUBMISSION_ESCALATED',
      applicationId: escalation.applicationId,
      actorId: escalation.escalatedBy,
      actorRole: 'VERIFIER',
      timestamp: new Date(),
      data: {
        escalationId: escalation.escalationId,
        reason: escalation.reason,
        notesLength: escalation.notes.length,
        escalationCount: escalation.escalationCount
      }
    }, options);
  }
}
```

### Escalation Resolution Service

```javascript
class EscalationResolutionService {
  constructor(db, approvalService, rejectionService, notificationService) {
    this.db = db;
    this.approval = approvalService;
    this.rejection = rejectionService;
    this.notifications = notificationService;
  }

  async resolveEscalation(escalationId, seniorVerifierId, resolution) {
    const session = this.db.startSession();

    try {
      session.startTransaction();

      // 1. Get escalation
      const escalation = await this.db.escalations.findOne(
        { escalationId },
        { session }
      );

      if (!escalation) {
        throw new Error('Escalation not found');
      }

      if (escalation.status !== 'pending') {
        throw new Error('Escalation already resolved');
      }

      // 2. Verify senior verifier role
      const resolver = await this.db.users.findOne({ id: seniorVerifierId });
      if (resolver?.role !== 'SENIOR_VERIFIER' && resolver?.role !== 'ADMIN') {
        throw new Error('Only senior verifiers can resolve escalations');
      }

      const now = new Date().toISOString();

      // 3. Update escalation
      await this.db.escalations.updateOne(
        { escalationId },
        {
          $set: {
            status: 'resolved',
            resolvedBy: seniorVerifierId,
            resolvedAt: now,
            resolution: {
              action: resolution.action, // 'approve', 'reject', 'return'
              notes: resolution.notes,
              rejectionReason: resolution.rejectionReason || null
            }
          }
        },
        { session }
      );

      await session.commitTransaction();

      // 4. Execute resolution action
      let result;
      switch (resolution.action) {
        case 'approve':
          result = await this.approval.approveSubmission(
            escalation.queueId,
            seniorVerifierId,
            { notes: resolution.notes, fromEscalation: true }
          );
          break;

        case 'reject':
          result = await this.rejection.rejectSubmission(
            escalation.queueId,
            seniorVerifierId,
            {
              reason: resolution.rejectionReason,
              notes: resolution.notes,
              fromEscalation: true
            }
          );
          break;

        case 'return':
          result = await this.returnToVerifier(escalation, resolution.notes);
          break;
      }

      // 5. Notify original verifier
      await this.notifyOriginalVerifier(escalation, resolution);

      // 6. Log resolution
      await this.logResolution(escalation, resolution, seniorVerifierId);

      return {
        success: true,
        action: resolution.action,
        result
      };

    } catch (error) {
      await session.abortTransaction();
      throw error;
    } finally {
      session.endSession();
    }
  }

  async returnToVerifier(escalation, notes) {
    // Put back in regular queue for original verifier
    await this.db.verifierQueue.updateOne(
      { queueId: escalation.queueId },
      {
        $set: {
          status: 'pending',
          'timestamps.returnedAt': new Date().toISOString(),
          returnNotes: notes
        },
        $unset: {
          escalationId: 1
        }
      }
    );

    await this.db.submissions.updateOne(
      { applicationId: escalation.applicationId },
      {
        $set: {
          status: 'pending_verification'
        },
        $unset: {
          currentEscalationId: 1
        }
      }
    );

    return { returned: true };
  }

  async notifyOriginalVerifier(escalation, resolution) {
    const actionLabels = {
      approve: 'approved',
      reject: 'rejected',
      return: 'returned to you'
    };

    await this.notifications.create({
      userId: escalation.escalatedBy,
      type: 'escalation_resolved',
      title: 'Escalation Resolved',
      body: `Submission ${escalation.applicationId} was ${actionLabels[resolution.action]}`,
      data: {
        escalationId: escalation.escalationId,
        applicationId: escalation.applicationId,
        resolution: resolution.action,
        notes: resolution.notes
      }
    });
  }

  async logResolution(escalation, resolution, resolverId) {
    await this.db.auditLog.insertOne({
      type: 'ESCALATION_RESOLVED',
      applicationId: escalation.applicationId,
      actorId: resolverId,
      actorRole: 'SENIOR_VERIFIER',
      timestamp: new Date(),
      data: {
        escalationId: escalation.escalationId,
        originalVerifier: escalation.escalatedBy,
        action: resolution.action,
        reason: escalation.reason
      }
    });
  }
}
```

### Escalation API Endpoints

```javascript
// POST /api/verifier/submissions/:queueId/escalate
async function handleEscalate(req, res) {
  const { queueId } = req.params;
  const { reason, notes } = req.body;
  const verifierId = req.user.id;

  try {
    const escalationService = new EscalationService(db, notificationService);
    const result = await escalationService.escalateSubmission(queueId, verifierId, {
      reason,
      notes
    });

    // Get next queue item
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
    console.error('Escalation failed:', error);
    return res.status(400).json({
      success: false,
      error: error.message
    });
  }
}

// GET /api/senior-verifier/escalations
async function getEscalatedQueue(req, res) {
  const { tenantId } = req.user;
  const { page = 1, limit = 20 } = req.query;

  const escalations = await db.escalations.aggregate([
    {
      $match: {
        tenantId,
        status: 'pending'
      }
    },
    {
      $lookup: {
        from: 'verifierQueue',
        localField: 'queueId',
        foreignField: 'queueId',
        as: 'queueEntry'
      }
    },
    {
      $unwind: '$queueEntry'
    },
    {
      $lookup: {
        from: 'users',
        localField: 'escalatedBy',
        foreignField: 'id',
        as: 'escalator'
      }
    },
    {
      $unwind: '$escalator'
    },
    {
      $project: {
        escalationId: 1,
        applicationId: 1,
        reason: 1,
        notes: 1,
        escalatedAt: 1,
        escalatorName: '$escalator.name',
        submissionSummary: '$queueEntry.submissionSummary',
        slaDeadline: '$queueEntry.timestamps.slaDeadline',
        flags: '$queueEntry.submissionSummary.flags'
      }
    },
    { $sort: { escalatedAt: 1 } },
    { $skip: (page - 1) * limit },
    { $limit: parseInt(limit) }
  ]).toArray();

  const total = await db.escalations.countDocuments({
    tenantId,
    status: 'pending'
  });

  return res.json({
    escalations,
    pagination: {
      page: parseInt(page),
      limit: parseInt(limit),
      total,
      pages: Math.ceil(total / limit)
    }
  });
}

// POST /api/senior-verifier/escalations/:escalationId/resolve
async function handleResolve(req, res) {
  const { escalationId } = req.params;
  const { action, notes, rejectionReason } = req.body;
  const seniorVerifierId = req.user.id;

  try {
    const resolutionService = new EscalationResolutionService(
      db, approvalService, rejectionService, notificationService
    );

    const result = await resolutionService.resolveEscalation(
      escalationId,
      seniorVerifierId,
      { action, notes, rejectionReason }
    );

    return res.json(result);

  } catch (error) {
    console.error('Resolution failed:', error);
    return res.status(400).json({
      success: false,
      error: error.message
    });
  }
}
```

### Escalation Panel Component

```javascript
function EscalationPanel({ queueId, onEscalated, onCancel }) {
  const [reason, setReason] = useState(null);
  const [notes, setNotes] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const REASONS = [
    { code: 'unclear_photo', label: 'Unclear Photo Quality' },
    { code: 'borderline_fraud', label: 'Suspected Fraud' },
    { code: 'complex_duplicate', label: 'Complex Duplicate' },
    { code: 'unusual_location', label: 'Unusual Location' },
    { code: 'teacher_history', label: 'Teacher History Concern' },
    { code: 'policy_question', label: 'Policy Question' },
    { code: 'technical_issue', label: 'Technical Issue' },
    { code: 'other', label: 'Other' }
  ];

  const canSubmit = reason && notes.trim().length >= 20;

  async function handleSubmit() {
    if (!canSubmit) return;

    setIsSubmitting(true);
    setError(null);

    try {
      const response = await fetch(`/api/verifier/submissions/${queueId}/escalate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reason, notes })
      });

      const result = await response.json();

      if (result.success) {
        onEscalated?.(result);
      } else {
        throw new Error(result.error);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="escalation-panel">
      <div className="panel-header">
        <span className="icon">⬆️</span>
        <h3>Escalate to Senior Verifier</h3>
      </div>

      <div className="panel-body">
        <p>Why are you escalating this submission?</p>

        <div className="reason-list">
          {REASONS.map(r => (
            <label key={r.code} className="reason-option">
              <input
                type="radio"
                name="reason"
                value={r.code}
                checked={reason === r.code}
                onChange={() => setReason(r.code)}
              />
              <span>{r.label}</span>
            </label>
          ))}
        </div>

        <div className="notes-section">
          <label>
            Add notes for senior verifier: <span className="required">*</span>
          </label>
          <textarea
            value={notes}
            onChange={e => setNotes(e.target.value)}
            placeholder="Explain why this submission needs senior review..."
            rows={4}
            maxLength={1000}
          />
          <div className="char-count">
            {notes.length}/1000 (min 20)
          </div>
        </div>

        {error && <div className="error-message">{error}</div>}
      </div>

      <div className="panel-actions">
        <button
          className="btn-secondary"
          onClick={onCancel}
          disabled={isSubmitting}
        >
          Cancel
        </button>
        <button
          className="btn-primary"
          onClick={handleSubmit}
          disabled={!canSubmit || isSubmitting}
        >
          {isSubmitting ? 'Escalating...' : '⬆️ Escalate'}
        </button>
      </div>
    </div>
  );
}
```

---

## Metrics & Monitoring

```javascript
async function getEscalationMetrics(tenantId, dateRange) {
  const stats = await db.escalations.aggregate([
    {
      $match: {
        tenantId,
        escalatedAt: { $gte: dateRange.start, $lte: dateRange.end }
      }
    },
    {
      $group: {
        _id: {
          reason: '$reason',
          resolution: '$resolution.action'
        },
        count: { $sum: 1 },
        avgResolutionTime: {
          $avg: {
            $subtract: [
              { $toDate: '$resolvedAt' },
              { $toDate: '$escalatedAt' }
            ]
          }
        }
      }
    }
  ]).toArray();

  // Verifier escalation rates
  const verifierStats = await db.escalations.aggregate([
    {
      $match: {
        tenantId,
        escalatedAt: { $gte: dateRange.start, $lte: dateRange.end }
      }
    },
    {
      $group: {
        _id: '$escalatedBy',
        escalationCount: { $sum: 1 }
      }
    },
    {
      $lookup: {
        from: 'verifierStats',
        let: { verifierId: '$_id' },
        pipeline: [
          {
            $match: {
              $expr: { $eq: ['$verifierId', '$$verifierId'] },
              date: { $gte: dateRange.start, $lte: dateRange.end }
            }
          },
          {
            $group: {
              _id: null,
              totalReviewed: { $sum: '$totalReviewed' }
            }
          }
        ],
        as: 'reviewStats'
      }
    }
  ]).toArray();

  return {
    byReason: stats.filter(s => !s._id.resolution),
    byResolution: stats.filter(s => s._id.resolution),
    verifierRates: verifierStats.map(v => ({
      verifierId: v._id,
      escalations: v.escalationCount,
      totalReviewed: v.reviewStats[0]?.totalReviewed || 0,
      rate: (v.escalationCount / (v.reviewStats[0]?.totalReviewed || 1)) * 100
    }))
  };
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Approval Service | Internal | For approved resolutions |
| Rejection Service | Internal | For rejected resolutions |
| Notification Service | Internal | Alert senior verifiers |
| Audit Logger | Internal | Track escalation flow |

---

## Related Stories

- [V-REV-01](./V-REV-01.md) - Review submission
- [V-APP-01](./V-APP-01.md) - Approve submission
- [V-REJ-01](./V-REJ-01.md) - Reject submission
- [V-REV-04](./V-REV-04.md) - SLA monitoring
