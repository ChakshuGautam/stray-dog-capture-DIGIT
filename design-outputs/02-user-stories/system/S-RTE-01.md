# S-RTE-01: Route to Verifier Queue

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## System Behavior

**The system** routes validated submissions to the verifier queue
**To ensure** all submissions are reviewed before MC assignment.

---

## Description

After a submission passes all automated validations (timestamp, boundary, hash), it enters the verifier queue for manual review. The system determines which verifier(s) should receive the submission based on workload balancing and geographic assignment.

---

## Acceptance Criteria

### Routing Rules

- [ ] Only validated submissions enter verifier queue
- [ ] Queue entry created with all submission metadata
- [ ] Verifier assigned based on workload (round-robin or least loaded)
- [ ] Queue priority calculated from condition tags
- [ ] Submissions with flags get higher review priority
- [ ] Queue entry tracks time since submission
- [ ] SLA timer starts when entering queue (24h target)
- [ ] Notifications sent to assigned verifier

### Queue Priority Calculation

| Factor | Priority Points |
|--------|-----------------|
| Base submission | 0 |
| "Injured" tag | +2 |
| "Aggressive" tag | +2 |
| "With Puppies" tag | +1 |
| "Sick" tag | +2 |
| "Pack" tag | +1 |
| Has flags (edge cases) | +1 |
| Waiting > 12 hours | +1 |
| Waiting > 20 hours | +2 |

### Queue States

| State | Description | Next States |
|-------|-------------|-------------|
| pending | In queue, awaiting review | in_review |
| in_review | Verifier is reviewing | approved, rejected |
| approved | Passed verification | (exits queue) |
| rejected | Failed verification | (exits queue) |

---

## Technical Implementation

### Queue Entry Creation

```javascript
async function routeToVerifierQueue(submission) {
  // Calculate priority
  const priority = calculateQueuePriority(submission);

  // Assign verifier (round-robin or least loaded)
  const verifier = await assignVerifier(submission.tenantId, submission.block);

  // Create queue entry
  const queueEntry = {
    queueId: generateUUID(),
    applicationId: submission.applicationId,
    teacherId: submission.teacherId,
    tenantId: submission.tenantId,
    block: submission.block,
    priority: priority,
    status: 'pending',
    assignedVerifierId: verifier.id,
    submissionSummary: {
      dogPhotoUrl: submission.dogPhotoUrl,
      selfieUrl: submission.selfieUrl,
      location: submission.location,
      conditionTags: submission.conditionTags,
      notes: submission.notes,
      flags: submission.flags || []
    },
    timestamps: {
      submitted: submission.createdAt,
      queued: new Date().toISOString(),
      slaDeadline: addHours(new Date(), 24)
    },
    metadata: {
      gpsAccuracy: submission.gpsAccuracy,
      photoSource: submission.photoSource,
      validationResults: submission.validationResults
    }
  };

  // Insert into queue
  await db.verifierQueue.insertOne(queueEntry);

  // Update submission status
  await db.submissions.updateOne(
    { applicationId: submission.applicationId },
    { $set: { status: 'pending_verification', queueId: queueEntry.queueId } }
  );

  // Notify verifier
  await notifyVerifierNewSubmission(verifier, queueEntry);

  // Log routing
  await logQueueRouting(submission, queueEntry);

  return queueEntry;
}
```

### Priority Calculation

```javascript
function calculateQueuePriority(submission) {
  let priority = 0;

  // Condition tag priorities
  const tagPriorities = {
    'INJURED': 2,
    'AGGRESSIVE': 2,
    'SICK': 2,
    'WITH_PUPPIES': 1,
    'PACK': 1,
    'NORMAL': 0,
    'COLLARED': 0
  };

  for (const tag of submission.conditionTags) {
    priority += tagPriorities[tag] || 0;
  }

  // Flags increase priority
  if (submission.flags && submission.flags.length > 0) {
    priority += 1;
  }

  return Math.min(priority, 5); // Cap at 5
}
```

### Verifier Assignment

```javascript
async function assignVerifier(tenantId, block) {
  // Get active verifiers for this tenant
  const verifiers = await db.users.find({
    tenantId,
    role: 'VERIFIER',
    isActive: true,
    assignedBlocks: { $in: [block, 'ALL'] }
  }).toArray();

  if (verifiers.length === 0) {
    throw new Error(`No verifiers available for ${tenantId}/${block}`);
  }

  // Calculate current load for each verifier
  const verifierLoads = await Promise.all(
    verifiers.map(async v => ({
      verifier: v,
      pendingCount: await db.verifierQueue.countDocuments({
        assignedVerifierId: v.id,
        status: 'pending'
      })
    }))
  );

  // Sort by load (least loaded first)
  verifierLoads.sort((a, b) => a.pendingCount - b.pendingCount);

  return verifierLoads[0].verifier;
}
```

### SLA Monitoring

```javascript
// Cron job to update priorities based on age
async function updateQueuePriorities() {
  const now = new Date();

  // Bump priority for items waiting > 12 hours
  await db.verifierQueue.updateMany(
    {
      status: 'pending',
      'timestamps.queued': { $lt: hoursAgo(12) },
      'metadata.agePriorityBump12h': { $ne: true }
    },
    {
      $inc: { priority: 1 },
      $set: { 'metadata.agePriorityBump12h': true }
    }
  );

  // Bump priority for items waiting > 20 hours
  await db.verifierQueue.updateMany(
    {
      status: 'pending',
      'timestamps.queued': { $lt: hoursAgo(20) },
      'metadata.agePriorityBump20h': { $ne: true }
    },
    {
      $inc: { priority: 2 },
      $set: { 'metadata.agePriorityBump20h': true }
    }
  );

  // Alert for items approaching SLA breach
  const slaAlerts = await db.verifierQueue.find({
    status: 'pending',
    'timestamps.slaDeadline': { $lt: hoursFromNow(2) },
    'metadata.slaAlertSent': { $ne: true }
  }).toArray();

  for (const entry of slaAlerts) {
    await sendSLAWarning(entry);
  }
}

function hoursAgo(hours) {
  return new Date(Date.now() - hours * 60 * 60 * 1000);
}

function hoursFromNow(hours) {
  return new Date(Date.now() + hours * 60 * 60 * 1000);
}
```

### Verifier Notification

```javascript
async function notifyVerifierNewSubmission(verifier, queueEntry) {
  // In-app notification
  await createInAppNotification({
    userId: verifier.id,
    type: 'new_submission_to_verify',
    title: 'New Submission',
    body: `New submission from ${queueEntry.block} awaiting review`,
    data: {
      queueId: queueEntry.queueId,
      applicationId: queueEntry.applicationId,
      priority: queueEntry.priority,
      tags: queueEntry.submissionSummary.conditionTags
    },
    priority: queueEntry.priority >= 3 ? 'high' : 'normal'
  });

  // SMS for high priority
  if (queueEntry.priority >= 3) {
    await smsService.send(verifier.phone, SMS_TEMPLATES.urgent_submission, {
      ID: queueEntry.applicationId,
      TAGS: queueEntry.submissionSummary.conditionTags.join(', ')
    });
  }
}
```

---

## Queue Dashboard Data

```javascript
async function getVerifierQueueStats(verifierId) {
  const stats = await db.verifierQueue.aggregate([
    {
      $match: {
        assignedVerifierId: verifierId,
        status: 'pending'
      }
    },
    {
      $group: {
        _id: null,
        total: { $sum: 1 },
        highPriority: {
          $sum: { $cond: [{ $gte: ['$priority', 3] }, 1, 0] }
        },
        approachingSLA: {
          $sum: {
            $cond: [
              { $lt: ['$timestamps.slaDeadline', hoursFromNow(4)] },
              1,
              0
            ]
          }
        }
      }
    }
  ]).toArray();

  return stats[0] || { total: 0, highPriority: 0, approachingSLA: 0 };
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| MongoDB | Database | Queue storage |
| Notification Service | Internal | Verifier alerts |
| SMS Gateway | External | Urgent notifications |
| Cron Scheduler | Internal | SLA monitoring |

---

## Related Stories

- [S-VAL-01](./S-VAL-01.md) - Timestamp validation
- [S-VAL-02](./S-VAL-02.md) - Boundary validation
- [V-REV-01](../verifier/V-REV-01.md) - View pending submissions
- [V-APP-01](../verifier/V-APP-01.md) - Approve submission
