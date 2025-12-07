# V-APP-02: Auto-Route to MC Queue

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Verifier
**I want** approved applications to be automatically routed to the Municipal Corporation queue
**So that** field action can begin without manual intervention.

---

## Description

Upon approval, the system automatically routes the submission to the appropriate MC Officer queue based on geographic jurisdiction. This ensures zero delay between verification and field assignment, and removes the need for manual routing decisions.

---

## Acceptance Criteria

### Auto-Routing Rules

- [ ] Routing triggered immediately upon approval
- [ ] MC Officer selected based on submission block
- [ ] Fallback to nearest available if zone officer unavailable
- [ ] Routing completes within 5 seconds of approval
- [ ] Teacher notified of MC assignment (without officer identity)
- [ ] MC Officer receives new assignment notification
- [ ] Routing logged for audit trail

### Routing Outcomes

| Scenario | Action | Notification |
|----------|--------|--------------|
| Zone MC available | Direct assignment | MC + Teacher |
| Zone MC at capacity | Assign to backup | MC + Teacher |
| No MC available | Queue for manual assignment | Admin alert |
| High priority | Bypass daily limits | MC (urgent) |

### Failure Handling

- [ ] Retry routing up to 3 times on failure
- [ ] Alert admin if routing repeatedly fails
- [ ] Never leave submission in "approved but unrouted" state
- [ ] Manual assignment fallback available

---

## UI/UX Requirements

### Routing Status in Approval Confirmation

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    ✓ Approved & Routed                      │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  SDC-2024-001234                                            │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  ✓ Approved by you                                    │  │
│  │                                                       │  │
│  │  ✓ Routed to MC queue                                │  │
│  │    └─ Assigned to: Boulaos Zone Officer              │  │
│  │                                                       │  │
│  │  ✓ Teacher notified                                  │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  The MC Officer will receive this assignment and           │
│  visit the location to capture the dog.                    │
│                                                             │
│                [  View Next Submission  ]                   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Routing Failure Alert

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ⚠️ Routing Issue                                           │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Submission approved but could not be auto-routed:         │
│                                                             │
│  • No MC Officer available for Block: Ambouli              │
│                                                             │
│  The submission has been queued for manual assignment.      │
│  An admin has been notified.                               │
│                                                             │
│  You can continue reviewing other submissions.              │
│                                                             │
│  ┌────────────────────┐  ┌────────────────────────────┐    │
│  │   View Details     │  │   Continue Reviewing       │    │
│  └────────────────────┘  └────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Technical Implementation

### Auto-Routing Flow

```javascript
async function autoRouteToMCQueue(submission, verifierId) {
  const startTime = Date.now();

  try {
    // 1. Get routing configuration
    const routingConfig = await getRoutingConfig(submission.tenantId);

    // 2. Find appropriate MC Officer
    const mcOfficer = await findMCOfficer(submission, routingConfig);

    if (!mcOfficer) {
      // No MC available - queue for manual assignment
      return await handleNoMCAvailable(submission);
    }

    // 3. Create assignment
    const assignment = await createMCAssignment(submission, mcOfficer);

    // 4. Send notifications
    await sendRoutingNotifications(submission, assignment);

    // 5. Log routing
    const routingTime = Date.now() - startTime;
    await logRouting(submission, assignment, routingTime);

    return {
      success: true,
      assignmentId: assignment.assignmentId,
      mcOfficerId: mcOfficer.id,
      mcOfficerName: mcOfficer.name,
      routingTime
    };

  } catch (error) {
    console.error('Auto-routing failed:', error);

    // Retry logic
    return await retryRouting(submission, verifierId, error);
  }
}

async function findMCOfficer(submission, config) {
  const { block, tenantId, priority } = submission;

  // Priority 1: Zone MC Officer
  const zoneOfficer = await db.users.findOne({
    tenantId,
    role: 'MC_OFFICER',
    isActive: true,
    isOnDuty: true,
    assignedBlocks: block
  });

  if (zoneOfficer) {
    const workload = await getMCWorkload(zoneOfficer.id);

    // High priority bypasses daily limits
    if (priority >= 3 || workload.todayTotal < config.dailyLimit) {
      return zoneOfficer;
    }
  }

  // Priority 2: Least loaded MC in tenant
  const allMCs = await db.users.find({
    tenantId,
    role: 'MC_OFFICER',
    isActive: true,
    isOnDuty: true
  }).toArray();

  if (allMCs.length === 0) {
    return null;
  }

  // Score by workload
  const scored = await Promise.all(
    allMCs.map(async mc => ({
      mc,
      workload: await getMCWorkload(mc.id),
      distance: calculateDistance(mc.lastKnownLocation, submission.location)
    }))
  );

  // Filter by capacity (unless high priority)
  const available = scored.filter(s =>
    priority >= 3 || s.workload.todayTotal < config.dailyLimit
  );

  if (available.length === 0) {
    return null;
  }

  // Sort by workload, then distance
  available.sort((a, b) => {
    const workloadDiff = a.workload.pending - b.workload.pending;
    if (workloadDiff !== 0) return workloadDiff;
    return a.distance - b.distance;
  });

  return available[0].mc;
}

async function createMCAssignment(submission, mcOfficer) {
  const assignment = {
    assignmentId: generateUUID(),
    applicationId: submission.applicationId,
    mcOfficerId: mcOfficer.id,
    mcOfficerName: mcOfficer.name,
    mcOfficerPhone: mcOfficer.phone,
    status: 'assigned',
    assignedAt: new Date().toISOString(),
    location: {
      latitude: submission.latitude,
      longitude: submission.longitude,
      block: submission.block,
      address: submission.reverseGeocodedAddress
    },
    dogPhotoUrl: submission.dogPhotoUrl,
    conditionTags: submission.conditionTags,
    notes: submission.notes,
    priority: submission.priority,
    slaDeadline: calculateMCSLA(submission.priority),
    source: 'AUTO_ROUTED'
  };

  await db.mcAssignments.insertOne(assignment);

  // Update submission
  await db.submissions.updateOne(
    { applicationId: submission.applicationId },
    {
      $set: {
        status: 'assigned_to_mc',
        mcAssignmentId: assignment.assignmentId,
        mcOfficerId: mcOfficer.id,
        assignedToMCAt: assignment.assignedAt
      }
    }
  );

  return assignment;
}

function calculateMCSLA(priority) {
  const slaHours = {
    0: 72, // Low priority - 3 days
    1: 48,
    2: 36,
    3: 24, // High priority - 1 day
    4: 12,
    5: 6  // Critical - 6 hours
  };

  const hours = slaHours[priority] || 48;
  return new Date(Date.now() + hours * 60 * 60 * 1000).toISOString();
}
```

### Send Routing Notifications

```javascript
async function sendRoutingNotifications(submission, assignment) {
  // 1. Notify MC Officer
  await notificationService.create({
    userId: assignment.mcOfficerId,
    type: 'new_assignment',
    title: 'New Assignment',
    body: `New dog capture assignment in ${assignment.location.block}`,
    data: {
      assignmentId: assignment.assignmentId,
      applicationId: submission.applicationId,
      priority: assignment.priority,
      conditionTags: assignment.conditionTags,
      action: 'VIEW_ASSIGNMENT'
    },
    priority: assignment.priority >= 3 ? 'high' : 'normal'
  });

  // SMS for MC Officer
  await smsService.send(assignment.mcOfficerPhone, 'new_mc_assignment', {
    ID: submission.applicationId,
    LOCATION: assignment.location.block,
    TAGS: assignment.conditionTags.join(', ')
  });

  // 2. Notify Teacher (anonymized)
  await notificationService.create({
    userId: submission.teacherId,
    type: 'submission_routed',
    title: 'Report Assigned',
    body: 'Your report has been assigned to a Municipal Corporation officer for action.',
    data: {
      applicationId: submission.applicationId,
      status: 'assigned_to_mc',
      // Note: MC Officer identity NOT shared with teacher
      expectedAction: 'Field visit within 48 hours'
    }
  });

  // SMS for Teacher
  await smsService.send(submission.teacherPhone, 'submission_assigned', {
    ID: submission.applicationId
  });
}

// SMS Templates
const SMS_TEMPLATES = {
  new_mc_assignment: {
    en: 'SDCRS: New assignment {ID} at {LOCATION}. Condition: {TAGS}. Check app for details.',
    fr: 'SDCRS: Nouvelle mission {ID} à {LOCATION}. État: {TAGS}. Vérifiez l\'app.'
  },
  submission_assigned: {
    en: 'SDCRS: Your report {ID} has been assigned for field action. Track status in app.',
    fr: 'SDCRS: Votre rapport {ID} a été assigné pour action. Suivez le statut dans l\'app.'
  }
};
```

### Handle No MC Available

```javascript
async function handleNoMCAvailable(submission) {
  // 1. Queue for manual assignment
  const queueEntry = {
    queueId: generateUUID(),
    applicationId: submission.applicationId,
    tenantId: submission.tenantId,
    block: submission.block,
    priority: submission.priority,
    reason: 'NO_MC_AVAILABLE',
    queuedAt: new Date().toISOString(),
    status: 'pending_manual'
  };

  await db.manualAssignmentQueue.insertOne(queueEntry);

  // 2. Update submission status
  await db.submissions.updateOne(
    { applicationId: submission.applicationId },
    {
      $set: {
        status: 'pending_mc_assignment',
        routingFailed: true,
        routingFailReason: 'NO_MC_AVAILABLE'
      }
    }
  );

  // 3. Alert admin
  await notificationService.sendToRole('ADMIN', {
    tenantId: submission.tenantId,
    type: 'manual_assignment_needed',
    title: 'Manual MC Assignment Required',
    body: `No MC Officer available for ${submission.applicationId} in ${submission.block}`,
    priority: 'high',
    data: {
      applicationId: submission.applicationId,
      block: submission.block,
      queueId: queueEntry.queueId
    }
  });

  // 4. Log failure
  await auditLog.create({
    type: 'ROUTING_FAILED',
    applicationId: submission.applicationId,
    data: {
      reason: 'NO_MC_AVAILABLE',
      block: submission.block,
      queuedForManual: true
    }
  });

  return {
    success: false,
    queued: true,
    reason: 'NO_MC_AVAILABLE',
    queueId: queueEntry.queueId
  };
}
```

### Retry Routing Logic

```javascript
async function retryRouting(submission, verifierId, originalError, attempt = 1) {
  const MAX_RETRIES = 3;
  const RETRY_DELAY = 2000; // 2 seconds

  if (attempt > MAX_RETRIES) {
    // Max retries exceeded - queue for manual
    console.error(`Routing failed after ${MAX_RETRIES} attempts:`, originalError);

    await auditLog.create({
      type: 'ROUTING_RETRY_EXHAUSTED',
      applicationId: submission.applicationId,
      data: {
        attempts: MAX_RETRIES,
        error: originalError.message
      }
    });

    return await handleNoMCAvailable(submission);
  }

  // Wait before retry
  await new Promise(resolve => setTimeout(resolve, RETRY_DELAY * attempt));

  console.log(`Retrying routing for ${submission.applicationId}, attempt ${attempt + 1}`);

  try {
    const routingConfig = await getRoutingConfig(submission.tenantId);
    const mcOfficer = await findMCOfficer(submission, routingConfig);

    if (!mcOfficer) {
      throw new Error('No MC Officer available');
    }

    const assignment = await createMCAssignment(submission, mcOfficer);
    await sendRoutingNotifications(submission, assignment);

    await auditLog.create({
      type: 'ROUTING_RETRY_SUCCESS',
      applicationId: submission.applicationId,
      data: {
        attempt: attempt + 1,
        mcOfficerId: mcOfficer.id
      }
    });

    return {
      success: true,
      assignmentId: assignment.assignmentId,
      retryAttempt: attempt + 1
    };

  } catch (error) {
    return await retryRouting(submission, verifierId, error, attempt + 1);
  }
}
```

### Timeline Event

```javascript
async function addRoutingTimelineEvent(submission, assignment) {
  await addTimelineEvent(submission.applicationId, {
    status: 'assigned_to_mc',
    timestamp: assignment.assignedAt,
    data: {
      // Teacher sees this (anonymized)
      message: 'Assigned to Municipal Corporation for field action',
      expectedAction: 'Field visit and capture/resolution',
      // Internal data (not shown to teacher)
      _internal: {
        mcOfficerId: assignment.mcOfficerId,
        assignmentId: assignment.assignmentId,
        routingSource: 'AUTO'
      }
    }
  });
}
```

---

## Monitoring & Metrics

```javascript
async function getRoutingMetrics(tenantId, dateRange) {
  const stats = await db.auditLog.aggregate([
    {
      $match: {
        type: { $in: ['ROUTING_SUCCESS', 'ROUTING_FAILED', 'ROUTING_RETRY_SUCCESS'] },
        'data.tenantId': tenantId,
        timestamp: { $gte: dateRange.start, $lte: dateRange.end }
      }
    },
    {
      $group: {
        _id: '$type',
        count: { $sum: 1 },
        avgRoutingTime: { $avg: '$data.routingTime' }
      }
    }
  ]).toArray();

  return {
    totalRouted: stats.find(s => s._id === 'ROUTING_SUCCESS')?.count || 0,
    failed: stats.find(s => s._id === 'ROUTING_FAILED')?.count || 0,
    retriedSuccess: stats.find(s => s._id === 'ROUTING_RETRY_SUCCESS')?.count || 0,
    avgRoutingTime: stats.find(s => s._id === 'ROUTING_SUCCESS')?.avgRoutingTime || 0,
    successRate: calculateSuccessRate(stats)
  };
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| MC Officer Service | Internal | Officer availability |
| Notification Service | Internal | MC & Teacher alerts |
| SMS Gateway | External | SMS notifications |
| Geolocation | Utility | Distance calculation |

---

## Related Stories

- [V-APP-01](./V-APP-01.md) - Approve submission
- [S-MC-01](../system/S-MC-01.md) - MC assignment logic
- [MC-VIEW-01](../mc-officer/MC-VIEW-01.md) - MC receives assignment
- [T-STAT-02](../teacher/T-STAT-02.md) - Teacher notified
