# S-MC-01: Assign to MC Officer

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## System Behavior

**The system** assigns verified submissions to an MC Officer based on location
**To ensure** the nearest available MC Officer handles each case.

---

## Description

After a verifier approves a submission, the system automatically assigns it to an MC Officer. Assignment is based on geographic proximity to the submission location, MC Officer availability, and current workload. The assigned MC Officer receives notification with all details needed for field visit.

---

## Acceptance Criteria

### Assignment Rules

- [ ] Auto-assign triggered immediately upon verifier approval
- [ ] MC Officer selected based on assigned zone/block
- [ ] Fallback to nearest available if zone officer unavailable
- [ ] Consider current workload (pending assignments)
- [ ] Daily assignment limit per MC Officer (configurable)
- [ ] Priority submissions assigned to available officers first
- [ ] Officer receives SMS and in-app notification
- [ ] Assignment visible in MC Officer dashboard

### Assignment Criteria Priority

| Priority | Criteria |
|----------|----------|
| 1 | MC Officer assigned to submission block |
| 2 | MC Officer with fewest pending assignments |
| 3 | MC Officer nearest to submission GPS |
| 4 | Any available MC Officer in tenant |

### Assignment Outcomes

| Scenario | Action |
|----------|--------|
| Zone MC available | Direct assignment |
| Zone MC at capacity | Assign to backup/nearest |
| No MC available | Queue for manual assignment |
| High priority case | Bypass daily limits |

---

## Technical Implementation

### MC Assignment Service

```javascript
class MCAssignmentService {
  constructor(db, notificationService) {
    this.db = db;
    this.notifications = notificationService;
    this.dailyLimit = 10; // Configurable
  }

  async assignMCOfficer(submission) {
    // Get eligible MC Officers
    const candidates = await this.getEligibleOfficers(submission);

    if (candidates.length === 0) {
      return this.queueForManualAssignment(submission);
    }

    // Score and sort candidates
    const scored = await this.scoreCandidates(candidates, submission);
    const bestMatch = scored[0];

    // Create assignment
    const assignment = await this.createAssignment(submission, bestMatch.officer);

    // Notify MC Officer
    await this.notifyMCOfficer(assignment);

    return assignment;
  }

  async getEligibleOfficers(submission) {
    return this.db.users.find({
      tenantId: submission.tenantId,
      role: 'MC_OFFICER',
      isActive: true,
      isOnDuty: true,
      $or: [
        { assignedBlocks: submission.block },
        { assignedBlocks: 'ALL' }
      ]
    }).toArray();
  }

  async scoreCandidates(candidates, submission) {
    const scored = await Promise.all(
      candidates.map(async officer => {
        const workload = await this.getOfficerWorkload(officer.id);
        const distance = this.calculateDistance(
          officer.lastKnownLocation,
          submission.location
        );

        // Score: lower is better
        let score = 0;

        // Zone match bonus (lower score)
        if (officer.assignedBlocks.includes(submission.block)) {
          score -= 100;
        }

        // Workload penalty
        score += workload.pending * 10;

        // Distance penalty (1 point per km)
        score += distance;

        // At capacity penalty
        if (workload.todayTotal >= this.dailyLimit) {
          score += 1000; // Heavily penalize
        }

        return { officer, score, workload, distance };
      })
    );

    return scored.sort((a, b) => a.score - b.score);
  }

  async getOfficerWorkload(officerId) {
    const todayStart = new Date();
    todayStart.setHours(0, 0, 0, 0);

    const pending = await this.db.assignments.countDocuments({
      mcOfficerId: officerId,
      status: { $in: ['assigned', 'in_progress'] }
    });

    const todayTotal = await this.db.assignments.countDocuments({
      mcOfficerId: officerId,
      assignedAt: { $gte: todayStart }
    });

    return { pending, todayTotal };
  }

  calculateDistance(from, to) {
    if (!from || !to) return 999; // Unknown location penalty

    const R = 6371; // Earth's radius in km
    const dLat = this.toRad(to.latitude - from.latitude);
    const dLon = this.toRad(to.longitude - from.longitude);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.toRad(from.latitude)) *
        Math.cos(this.toRad(to.latitude)) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  toRad(deg) {
    return deg * (Math.PI / 180);
  }

  async createAssignment(submission, officer) {
    const assignment = {
      assignmentId: generateUUID(),
      applicationId: submission.applicationId,
      mcOfficerId: officer.id,
      mcOfficerName: officer.name,
      mcOfficerPhone: officer.phone,
      status: 'assigned',
      assignedAt: new Date().toISOString(),
      location: submission.location,
      conditionTags: submission.conditionTags,
      notes: submission.notes,
      dogPhotoUrl: submission.dogPhotoUrl,
      teacherInfo: {
        name: submission.teacherName,
        phone: submission.teacherPhone
      },
      priority: submission.priority,
      slaDeadline: this.calculateSLA(submission.priority)
    };

    await this.db.assignments.insertOne(assignment);

    // Update submission status
    await this.db.submissions.updateOne(
      { applicationId: submission.applicationId },
      {
        $set: {
          status: 'assigned_to_mc',
          mcOfficerId: officer.id,
          assignedAt: assignment.assignedAt
        }
      }
    );

    // Log assignment
    await this.logAssignment(assignment);

    return assignment;
  }

  calculateSLA(priority) {
    // Higher priority = shorter SLA
    const slaHours = {
      0: 48,
      1: 36,
      2: 24,
      3: 12,
      4: 8,
      5: 4
    };
    const hours = slaHours[priority] || 48;
    return new Date(Date.now() + hours * 60 * 60 * 1000).toISOString();
  }

  async queueForManualAssignment(submission) {
    await this.db.manualAssignmentQueue.insertOne({
      applicationId: submission.applicationId,
      reason: 'NO_AVAILABLE_MC',
      queuedAt: new Date().toISOString(),
      priority: submission.priority
    });

    // Alert admin
    await this.notifications.sendToRole('ADMIN', {
      type: 'manual_assignment_needed',
      title: 'MC Assignment Required',
      body: `No MC Officer available for ${submission.applicationId}`,
      data: { applicationId: submission.applicationId }
    });

    return { queued: true, reason: 'NO_AVAILABLE_MC' };
  }

  async notifyMCOfficer(assignment) {
    // In-app notification
    await this.notifications.create({
      userId: assignment.mcOfficerId,
      type: 'new_assignment',
      title: 'New Assignment',
      body: `New dog capture assignment in ${assignment.location.block}`,
      data: {
        assignmentId: assignment.assignmentId,
        applicationId: assignment.applicationId,
        priority: assignment.priority,
        conditionTags: assignment.conditionTags
      },
      priority: assignment.priority >= 3 ? 'high' : 'normal'
    });

    // SMS notification
    await smsService.send(assignment.mcOfficerPhone, SMS_TEMPLATES.new_assignment, {
      ID: assignment.applicationId,
      LOCATION: assignment.location.address || assignment.location.block,
      TAGS: assignment.conditionTags.join(', ')
    });
  }

  async logAssignment(assignment) {
    await auditLog.create({
      type: 'MC_ASSIGNMENT',
      applicationId: assignment.applicationId,
      data: {
        assignmentId: assignment.assignmentId,
        mcOfficerId: assignment.mcOfficerId,
        priority: assignment.priority,
        slaDeadline: assignment.slaDeadline
      },
      actor: 'SYSTEM',
      timestamp: new Date()
    });
  }
}
```

### SMS Template

```javascript
const SMS_TEMPLATES = {
  new_assignment: {
    en: 'SDCRS: New assignment {ID} at {LOCATION}. Condition: {TAGS}. Open app for details.',
    fr: 'SDCRS: Nouvelle mission {ID} à {LOCATION}. État: {TAGS}. Ouvrez l\'app pour les détails.'
  }
};
```

### Assignment API

```javascript
// Called after verifier approval
async function onVerifierApproval(submission) {
  const service = new MCAssignmentService(db, notificationService);
  const assignment = await service.assignMCOfficer(submission);

  // Update submission timeline
  await addTimelineEvent(submission.applicationId, {
    status: 'assigned_to_mc',
    timestamp: new Date().toISOString(),
    data: {
      mcOfficerName: assignment.mcOfficerName,
      assignmentId: assignment.assignmentId
    }
  });

  return assignment;
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| User Service | DIGIT | MC Officer data |
| Notification Service | Internal | Push/SMS alerts |
| Geolocation | Utility | Distance calculation |

---

## Related Stories

- [V-APP-01](../verifier/V-APP-01.md) - Approve submission
- [MC-VIEW-01](../mc-officer/MC-VIEW-01.md) - View assignments
- [MC-FLD-01](../mc-officer/MC-FLD-01.md) - Navigate to location
