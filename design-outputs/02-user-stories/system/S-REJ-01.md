# S-REJ-01: Auto-Reject Old Photos

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## System Behavior

**The system** automatically rejects submissions with photos older than 24 hours
**To ensure** only current sightings are processed and resources aren't wasted on stale reports.

---

## Description

When timestamp validation (S-VAL-01) determines a photo is too old, the system automatically rejects the submission without manual review. The teacher receives an immediate rejection notification with clear explanation and guidance to submit a fresh photo.

---

## Acceptance Criteria

### Rejection Rules

- [ ] Auto-reject triggered when photo age > 25 hours (24h + 1h grace)
- [ ] Rejection is immediate - no verifier queue entry created
- [ ] Teacher notified via SMS and in-app notification
- [ ] Rejection reason clearly states "Photo too old"
- [ ] Submission logged but not stored for processing
- [ ] No MC assignment or payout eligibility
- [ ] Teacher can immediately submit new report

### Auto-Reject Outcomes

| Scenario | Action | Notification |
|----------|--------|--------------|
| Photo > 25 hours old | Auto-reject | SMS + In-app |
| Future timestamp | Auto-reject | In-app only |
| No valid timestamp | Flag for review | None (goes to verifier) |
| Screenshot of photo | Auto-reject | SMS + In-app |

---

## Technical Implementation

### Auto-Rejection Service

```javascript
async function autoRejectForTimestamp(submission, validationResult) {
  const rejection = {
    applicationId: submission.applicationId,
    status: 'auto_rejected',
    rejectedAt: new Date().toISOString(),
    rejectionType: 'AUTOMATIC',
    rejection: {
      reasonCode: 'TIMESTAMP_OLD',
      reasonLabel: 'Photo Too Old',
      description: 'The photo was taken more than 24 hours ago.',
      systemReason: validationResult.errors[0],
      photoAge: validationResult.photoAge
    }
  };

  // Update submission status
  await db.submissions.updateOne(
    { applicationId: submission.applicationId },
    {
      $set: {
        status: 'auto_rejected',
        rejectedAt: rejection.rejectedAt,
        rejection: rejection.rejection,
        processedAt: new Date().toISOString()
      }
    }
  );

  // Send notifications
  await sendAutoRejectionNotifications(submission, rejection);

  // Log for audit
  await logAutoRejection(submission, rejection);

  return rejection;
}
```

### Notification Dispatch

```javascript
async function sendAutoRejectionNotifications(submission, rejection) {
  const teacher = await getTeacher(submission.teacherId);

  // SMS notification
  await smsService.send(teacher.phone, SMS_TEMPLATES.auto_rejected_timestamp, {
    ID: submission.applicationId
  });

  // In-app notification
  await createInAppNotification({
    userId: teacher.id,
    type: 'submission_auto_rejected',
    title: 'Submission Not Accepted',
    body: 'Your submission was not accepted because the photo is too old. Please take a new photo.',
    data: {
      applicationId: submission.applicationId,
      reasonCode: rejection.rejection.reasonCode
    },
    priority: 'high'
  });
}

const SMS_TEMPLATES = {
  auto_rejected_timestamp: {
    en: 'SDCRS: Your report {ID} could not be accepted. The photo is more than 24 hours old. Please submit a new photo.',
    fr: 'SDCRS: Votre rapport {ID} n\'a pas pu être accepté. La photo date de plus de 24 heures. Veuillez soumettre une nouvelle photo.'
  }
};
```

### Audit Logging

```javascript
async function logAutoRejection(submission, rejection) {
  await auditLog.create({
    type: 'AUTO_REJECTION',
    applicationId: submission.applicationId,
    teacherId: submission.teacherId,
    data: {
      rejectionReason: rejection.rejection.reasonCode,
      photoAge: rejection.rejection.photoAge,
      systemReason: rejection.rejection.systemReason,
      timestamp: rejection.rejectedAt
    },
    actor: 'SYSTEM',
    timestamp: new Date()
  });
}
```

### Client Response

```javascript
// API response for auto-rejected submission
function buildAutoRejectResponse(rejection) {
  return {
    success: false,
    status: 'auto_rejected',
    applicationId: rejection.applicationId,
    error: {
      code: 'PHOTO_TOO_OLD',
      message: 'This photo was taken more than 24 hours ago.',
      userMessage: 'Please take a new photo of the dog and submit again.',
      details: {
        photoAge: `${Math.round(rejection.rejection.photoAge)} hours`,
        maxAllowed: '24 hours'
      }
    },
    actions: [
      {
        label: 'Take New Photo',
        action: 'OPEN_CAMERA'
      },
      {
        label: 'View Guidelines',
        action: 'SHOW_GUIDELINES'
      }
    ]
  };
}
```

---

## User Interface Impact

### Immediate Rejection Screen (PWA)

```
┌─────────────────────────────────┐
│                                 │
│            ❌                   │
│   Submission Not Accepted       │
│                                 │
├─────────────────────────────────┤
│                                 │
│  Your photo is too old          │
│                                 │
│  ┌───────────────────────────┐  │
│  │                           │  │
│  │  Photos must be taken     │  │
│  │  within 24 hours of       │  │
│  │  submission.              │  │
│  │                           │  │
│  │  Your photo was taken     │  │
│  │  approximately 36 hours   │  │
│  │  ago.                     │  │
│  │                           │  │
│  └───────────────────────────┘  │
│                                 │
│  💡 Tip: Use the app camera     │
│     to take fresh photos for    │
│     best results.               │
│                                 │
│  [  Take New Photo  ]           │
│                                 │
│  [  Back to Home  ]             │
│                                 │
└─────────────────────────────────┘
```

---

## Error Codes

| Code | Description | Recovery Action |
|------|-------------|-----------------|
| TIMESTAMP_OLD | Photo EXIF timestamp > 25 hours | Take new photo |
| TIMESTAMP_FUTURE | Photo timestamp in future | Check device date/time |
| TIMESTAMP_INVALID | Cannot parse timestamp | Use app camera |

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| S-VAL-01 | System Story | Timestamp validation |
| SMS Gateway | External | Send rejection SMS |
| Notification Service | Internal | In-app notifications |

---

## Related Stories

- [S-VAL-01](./S-VAL-01.md) - Timestamp validation
- [S-REJ-02](./S-REJ-02.md) - Auto-reject outside boundary
- [T-STAT-03](../teacher/T-STAT-03.md) - View rejection reason
