# V-NOT-01: Notify Teacher on Rejection

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** System (on behalf of Verifier)
**I want to** automatically notify teachers when their submissions are rejected
**So that** they understand the outcome and can improve future submissions.

---

## Description

When a verifier rejects a submission, the teacher must be informed with a clear explanation of why their report was rejected. The notification should be educational rather than punitive, providing specific tips for improvement when possible. Notifications are sent through multiple channels (push, in-app, SMS) to ensure delivery, and are tracked for audit purposes.

---

## Acceptance Criteria

### Notification Triggers

- [ ] Triggered immediately after rejection is saved
- [ ] Triggered for all rejection reasons
- [ ] Triggered for duplicate marking
- [ ] Triggered for auto-rejection (with different template)
- [ ] Single notification per rejection (no duplicates)

### Notification Content

- [ ] Application ID clearly shown
- [ ] Rejection reason in teacher's language
- [ ] Helpful tip for improvement (when available)
- [ ] No blame language
- [ ] Clear next steps (if applicable)
- [ ] Deep link to view submission details

### Notification Channels

| Channel | Priority | Fallback |
|---------|----------|----------|
| Push notification | Primary | In-app if offline |
| In-app notification | Always | Persisted until read |
| SMS | For critical/repeat | After 2 failed deliveries |

### Delivery Guarantees

- [ ] At-least-once delivery
- [ ] Retry failed notifications up to 3 times
- [ ] SMS fallback after push/in-app failures
- [ ] Delivery status tracked
- [ ] Failed delivery alerts to admin

### Privacy & Compliance

- [ ] No verifier identity in teacher notification
- [ ] Rejection reason appropriate for teacher
- [ ] SMS content follows regulatory guidelines
- [ ] Notification preferences respected

---

## UI/UX Requirements

### Push Notification

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  📱 Push Notification                                       │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  SDCRS                                          Just now    │
│                                                             │
│  Report Not Approved                                        │
│  Your report SDC-2024-001234 could not be approved:        │
│  Photo unclear. Tap for details and tips.                  │
│                                                             │
│                                          [View]  [Dismiss]  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### In-App Notification

```
┌─────────────────────────────────────────────────────────────┐
│  Notifications                                    Mark All  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  🔴                                                   │  │
│  │  Report Not Approved                      2 mins ago  │  │
│  │                                                       │  │
│  │  Your report SDC-2024-001234 could not be approved.   │  │
│  │                                                       │  │
│  │  Reason: Photo unclear or unidentifiable              │  │
│  │                                                       │  │
│  │  💡 Tip: Ensure the dog is clearly visible and        │  │
│  │  the photo is not blurry. Try getting closer or       │  │
│  │  using better lighting.                               │  │
│  │                                                       │  │
│  │  [ View Submission ]                                  │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  🟡                                                   │  │
│  │  Report Marked as Duplicate               1 hour ago  │  │
│  │                                                       │  │
│  │  Your report SDC-2024-001189 was identified as a      │  │
│  │  duplicate of an existing report.                     │  │
│  │                                                       │  │
│  │  The dog you reported has already been registered     │  │
│  │  by another reporter. No action needed.               │  │
│  │                                                       │  │
│  │  [ View Details ]                                     │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Submission Detail View (After Rejection)

```
┌─────────────────────────────────────────────────────────────┐
│  ← Back                           SDC-2024-001234           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                       │  │
│  │  ❌ NOT APPROVED                                      │  │
│  │                                                       │  │
│  │  This report could not be approved.                   │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  [Photo thumbnail]                                          │
│                                                             │
│  ─────────────────────────────────────────────────────      │
│                                                             │
│  Reason                                                     │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Photo unclear or unidentifiable                      │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  Helpful Tips                                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  📸 For clearer photos:                               │  │
│  │                                                       │  │
│  │  • Ensure the dog fills at least 30% of the frame    │  │
│  │  • Avoid photos taken through windows or fences       │  │
│  │  • Wait for the dog to be still before taking photo  │  │
│  │  • Use natural daylight when possible                 │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  Timeline                                                   │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  ● Submitted       Dec 7, 2024 at 9:15 AM            │  │
│  │  │                                                    │  │
│  │  ● Under Review    Dec 7, 2024 at 9:20 AM            │  │
│  │  │                                                    │  │
│  │  ● Not Approved    Dec 7, 2024 at 10:45 AM           │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌────────────────────────────────────────────────────┐    │
│  │             + Submit New Report                     │    │
│  └────────────────────────────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### SMS Notification

```
SDCRS: Your report SDC-2024-001234 was not approved.
Reason: Photo unclear. Open app for tips to improve
future reports.
```

---

## Technical Implementation

### Teacher Notification Service

```javascript
class TeacherRejectionNotificationService {
  constructor(db, pushService, smsService) {
    this.db = db;
    this.push = pushService;
    this.sms = smsService;
  }

  async notifyRejection(submission, rejectionData) {
    const { teacherId, teacherPhone, applicationId } = submission;
    const { reason, reasonCode, isDuplicate } = rejectionData;

    // Get teacher's language preference
    const teacher = await this.db.users.findOne({ id: teacherId });
    const language = teacher?.preferredLanguage || 'en';

    // Get notification content
    const content = this.buildNotificationContent(
      reasonCode,
      applicationId,
      isDuplicate,
      language
    );

    // Create notification record
    const notificationId = generateUUID();
    const notification = {
      notificationId,
      userId: teacherId,
      type: isDuplicate ? 'submission_duplicate' : 'submission_rejected',
      applicationId,
      content,
      channels: [],
      status: 'pending',
      createdAt: new Date().toISOString(),
      deliveryAttempts: []
    };

    await this.db.notifications.insertOne(notification);

    // Attempt delivery across channels
    const results = await this.deliverNotification(notification, teacherPhone);

    return {
      notificationId,
      deliveryResults: results
    };
  }

  buildNotificationContent(reasonCode, applicationId, isDuplicate, language) {
    const templates = REJECTION_NOTIFICATION_TEMPLATES[language] ||
                      REJECTION_NOTIFICATION_TEMPLATES['en'];

    if (isDuplicate) {
      return {
        title: templates.duplicate.title,
        body: templates.duplicate.body.replace('{ID}', applicationId),
        tip: templates.duplicate.tip,
        pushBody: templates.duplicate.pushBody.replace('{ID}', applicationId),
        smsBody: templates.duplicate.smsBody.replace('{ID}', applicationId)
      };
    }

    const reasonTemplate = templates.reasons[reasonCode] || templates.reasons.other;

    return {
      title: templates.rejected.title,
      body: templates.rejected.body
        .replace('{ID}', applicationId)
        .replace('{REASON}', reasonTemplate.label),
      tip: reasonTemplate.tip,
      pushBody: templates.rejected.pushBody
        .replace('{ID}', applicationId)
        .replace('{REASON}', reasonTemplate.shortLabel),
      smsBody: templates.rejected.smsBody
        .replace('{ID}', applicationId)
        .replace('{REASON}', reasonTemplate.shortLabel)
    };
  }

  async deliverNotification(notification, teacherPhone) {
    const results = {
      push: null,
      inApp: null,
      sms: null
    };

    // 1. Always create in-app notification
    try {
      await this.createInAppNotification(notification);
      results.inApp = { success: true, timestamp: new Date().toISOString() };
    } catch (error) {
      results.inApp = { success: false, error: error.message };
    }

    // 2. Attempt push notification
    try {
      const pushResult = await this.sendPushNotification(notification);
      results.push = {
        success: pushResult.delivered,
        timestamp: new Date().toISOString(),
        deviceCount: pushResult.deviceCount
      };
    } catch (error) {
      results.push = { success: false, error: error.message };
    }

    // 3. Send SMS if push failed or teacher prefers SMS
    const teacher = await this.db.users.findOne({ id: notification.userId });
    const shouldSendSMS = !results.push.success ||
                          teacher?.notificationPreferences?.preferSMS;

    if (shouldSendSMS && teacherPhone) {
      try {
        await this.sendSMSNotification(notification, teacherPhone);
        results.sms = { success: true, timestamp: new Date().toISOString() };
      } catch (error) {
        results.sms = { success: false, error: error.message };
      }
    }

    // Update notification record
    await this.db.notifications.updateOne(
      { notificationId: notification.notificationId },
      {
        $set: {
          status: this.determineOverallStatus(results),
          channels: Object.keys(results).filter(k => results[k]?.success),
          deliveryResults: results
        },
        $push: {
          deliveryAttempts: {
            timestamp: new Date().toISOString(),
            results
          }
        }
      }
    );

    return results;
  }

  async createInAppNotification(notification) {
    const inAppRecord = {
      id: generateUUID(),
      userId: notification.userId,
      type: notification.type,
      title: notification.content.title,
      body: notification.content.body,
      tip: notification.content.tip,
      data: {
        applicationId: notification.applicationId,
        action: 'VIEW_SUBMISSION'
      },
      read: false,
      createdAt: new Date().toISOString()
    };

    await this.db.inAppNotifications.insertOne(inAppRecord);
    return inAppRecord;
  }

  async sendPushNotification(notification) {
    const { userId, content, applicationId } = notification;

    // Get user's registered devices
    const devices = await this.db.pushTokens.find({
      userId,
      isActive: true
    }).toArray();

    if (devices.length === 0) {
      return { delivered: false, deviceCount: 0, reason: 'NO_DEVICES' };
    }

    const pushPayload = {
      title: content.title,
      body: content.pushBody,
      data: {
        type: notification.type,
        applicationId,
        action: 'VIEW_SUBMISSION'
      },
      badge: await this.getUnreadCount(userId),
      sound: 'default'
    };

    let deliveredCount = 0;
    const failedTokens = [];

    for (const device of devices) {
      try {
        await this.push.send(device.token, pushPayload);
        deliveredCount++;
      } catch (error) {
        if (error.code === 'INVALID_TOKEN' || error.code === 'UNREGISTERED') {
          failedTokens.push(device.token);
        }
      }
    }

    // Clean up invalid tokens
    if (failedTokens.length > 0) {
      await this.db.pushTokens.updateMany(
        { token: { $in: failedTokens } },
        { $set: { isActive: false, deactivatedAt: new Date() } }
      );
    }

    return {
      delivered: deliveredCount > 0,
      deviceCount: deliveredCount,
      totalDevices: devices.length
    };
  }

  async sendSMSNotification(notification, phone) {
    const message = notification.content.smsBody;

    // Validate message length (160 chars for single SMS)
    if (message.length > 160) {
      console.warn('SMS message truncated:', message);
    }

    await this.sms.send(phone, message.substring(0, 160));
  }

  determineOverallStatus(results) {
    if (results.inApp?.success || results.push?.success || results.sms?.success) {
      return 'delivered';
    }
    return 'failed';
  }

  async getUnreadCount(userId) {
    return await this.db.inAppNotifications.countDocuments({
      userId,
      read: false
    });
  }
}
```

### Notification Templates

```javascript
const REJECTION_NOTIFICATION_TEMPLATES = {
  en: {
    rejected: {
      title: 'Report Not Approved',
      body: 'Your report {ID} could not be approved.\n\nReason: {REASON}',
      pushBody: 'Report {ID} not approved: {REASON}. Tap for details.',
      smsBody: 'SDCRS: Report {ID} not approved. Reason: {REASON}. Open app for tips.'
    },
    duplicate: {
      title: 'Report Marked as Duplicate',
      body: 'Your report {ID} was identified as a duplicate of an existing report.',
      tip: 'The dog you reported has already been registered. No action needed from you.',
      pushBody: 'Report {ID} is a duplicate of existing report. Tap for details.',
      smsBody: 'SDCRS: Report {ID} was a duplicate. This dog was already reported. No action needed.'
    },
    reasons: {
      photo_unclear: {
        label: 'Photo unclear or unidentifiable',
        shortLabel: 'Photo unclear',
        tip: 'For clearer photos:\n• Ensure the dog fills at least 30% of the frame\n• Avoid photos through windows or fences\n• Wait for the dog to be still\n• Use natural daylight when possible'
      },
      not_stray: {
        label: 'Dog appears to be a pet, not stray',
        shortLabel: 'Not a stray dog',
        tip: 'Stray dogs typically:\n• Have no collar or visible tags\n• Appear unkempt or underweight\n• Are found in public areas away from homes\n• Show no signs of recent care'
      },
      no_dog: {
        label: 'No dog visible in photo',
        shortLabel: 'No dog in photo',
        tip: 'Please ensure the dog is clearly visible in the photo before submitting. The system requires a clear image of the dog to process your report.'
      },
      location_invalid: {
        label: 'Location outside service area',
        shortLabel: 'Invalid location',
        tip: 'This service is only available within the municipal boundaries. Please ensure you are reporting dogs within the designated service area.'
      },
      photo_too_old: {
        label: 'Photo was taken more than 2 hours ago',
        shortLabel: 'Old photo',
        tip: 'For accurate reporting, photos must be taken within 2 hours of submission. This ensures the dog is still likely at the reported location.'
      },
      selfie_mismatch: {
        label: 'Selfie does not match location',
        shortLabel: 'Selfie mismatch',
        tip: 'Your selfie must be taken at the same location as the dog photo. This helps verify the report\'s authenticity.'
      },
      fraudulent: {
        label: 'Report appears to be fraudulent',
        shortLabel: 'Verification failed',
        tip: 'Your submission did not pass verification. Please ensure all photos are original and taken by you at the reported location.'
      },
      duplicate: {
        label: 'This dog has already been reported',
        shortLabel: 'Duplicate report',
        tip: 'Another reporter has already submitted a report for this dog. No action needed from you.'
      },
      other: {
        label: 'Did not meet submission criteria',
        shortLabel: 'Criteria not met',
        tip: 'Your submission did not meet the required criteria. Please review the guidelines in the app and try again.'
      }
    }
  },
  fr: {
    rejected: {
      title: 'Signalement Non Approuvé',
      body: 'Votre signalement {ID} n\'a pas pu être approuvé.\n\nRaison: {REASON}',
      pushBody: 'Signalement {ID} non approuvé: {REASON}. Appuyez pour plus de détails.',
      smsBody: 'SDCRS: Signalement {ID} non approuvé. Raison: {REASON}. Ouvrez l\'app pour des conseils.'
    },
    duplicate: {
      title: 'Signalement Marqué comme Doublon',
      body: 'Votre signalement {ID} a été identifié comme un doublon d\'un signalement existant.',
      tip: 'Le chien que vous avez signalé a déjà été enregistré. Aucune action requise de votre part.',
      pushBody: 'Signalement {ID} est un doublon. Appuyez pour plus de détails.',
      smsBody: 'SDCRS: Signalement {ID} était un doublon. Ce chien a déjà été signalé. Aucune action requise.'
    },
    reasons: {
      photo_unclear: {
        label: 'Photo floue ou non identifiable',
        shortLabel: 'Photo floue',
        tip: 'Pour des photos plus claires:\n• Assurez-vous que le chien occupe au moins 30% du cadre\n• Évitez les photos à travers fenêtres ou clôtures\n• Attendez que le chien soit immobile\n• Utilisez la lumière naturelle'
      },
      not_stray: {
        label: 'Le chien semble être un animal de compagnie',
        shortLabel: 'Pas un chien errant',
        tip: 'Les chiens errants typiquement:\n• N\'ont pas de collier ou d\'étiquette visible\n• Paraissent négligés ou maigres\n• Se trouvent dans des zones publiques\n• Ne montrent aucun signe de soins récents'
      },
      no_dog: {
        label: 'Aucun chien visible sur la photo',
        shortLabel: 'Pas de chien',
        tip: 'Veuillez vous assurer que le chien est clairement visible sur la photo avant de soumettre.'
      },
      location_invalid: {
        label: 'Emplacement hors zone de service',
        shortLabel: 'Emplacement invalide',
        tip: 'Ce service n\'est disponible que dans les limites municipales. Assurez-vous de signaler des chiens dans la zone de service désignée.'
      },
      photo_too_old: {
        label: 'Photo prise il y a plus de 2 heures',
        shortLabel: 'Photo ancienne',
        tip: 'Pour un signalement précis, les photos doivent être prises dans les 2 heures suivant la soumission.'
      },
      other: {
        label: 'Ne répond pas aux critères de soumission',
        shortLabel: 'Critères non remplis',
        tip: 'Votre soumission ne répond pas aux critères requis. Veuillez consulter les directives dans l\'application.'
      }
    }
  }
};
```

### Notification Retry Service

```javascript
class NotificationRetryService {
  constructor(db, notificationService) {
    this.db = db;
    this.notificationService = notificationService;
  }

  // Run every 5 minutes
  async processFailedNotifications() {
    const MAX_RETRIES = 3;
    const RETRY_DELAY_MINUTES = [5, 15, 60]; // Exponential backoff

    const failedNotifications = await this.db.notifications.find({
      status: 'failed',
      retryCount: { $lt: MAX_RETRIES },
      nextRetryAt: { $lte: new Date() }
    }).toArray();

    for (const notification of failedNotifications) {
      try {
        // Re-attempt delivery
        const teacherPhone = await this.getTeacherPhone(notification.userId);
        const results = await this.notificationService.deliverNotification(
          notification,
          teacherPhone
        );

        const newRetryCount = notification.retryCount + 1;
        const newStatus = this.determineStatus(results);

        // Update notification
        await this.db.notifications.updateOne(
          { notificationId: notification.notificationId },
          {
            $set: {
              status: newStatus,
              retryCount: newRetryCount,
              lastRetryAt: new Date().toISOString(),
              nextRetryAt: newStatus === 'failed' && newRetryCount < MAX_RETRIES
                ? this.calculateNextRetry(newRetryCount, RETRY_DELAY_MINUTES)
                : null
            },
            $push: {
              deliveryAttempts: {
                timestamp: new Date().toISOString(),
                attempt: newRetryCount,
                results
              }
            }
          }
        );

        // Alert admin if max retries reached and still failed
        if (newRetryCount >= MAX_RETRIES && newStatus === 'failed') {
          await this.alertAdminOfPersistentFailure(notification);
        }

      } catch (error) {
        console.error(`Retry failed for ${notification.notificationId}:`, error);
      }
    }
  }

  async getTeacherPhone(teacherId) {
    const teacher = await this.db.users.findOne({ id: teacherId });
    return teacher?.phone;
  }

  determineStatus(results) {
    if (results.inApp?.success || results.push?.success || results.sms?.success) {
      return 'delivered';
    }
    return 'failed';
  }

  calculateNextRetry(retryCount, delays) {
    const delayMinutes = delays[retryCount - 1] || delays[delays.length - 1];
    return new Date(Date.now() + delayMinutes * 60 * 1000);
  }

  async alertAdminOfPersistentFailure(notification) {
    await this.db.adminAlerts.insertOne({
      type: 'NOTIFICATION_DELIVERY_FAILED',
      severity: 'medium',
      data: {
        notificationId: notification.notificationId,
        userId: notification.userId,
        applicationId: notification.applicationId,
        attempts: notification.retryCount + 1
      },
      createdAt: new Date()
    });
  }
}
```

### API for Notification Preferences

```javascript
// GET /api/teacher/notification-preferences
async function getNotificationPreferences(req, res) {
  const { id: teacherId } = req.user;

  const preferences = await db.users.findOne(
    { id: teacherId },
    { projection: { notificationPreferences: 1 } }
  );

  return res.json({
    preferences: preferences?.notificationPreferences || {
      push: true,
      sms: false,
      email: false,
      preferSMS: false,
      quietHoursStart: null,
      quietHoursEnd: null
    }
  });
}

// PUT /api/teacher/notification-preferences
async function updateNotificationPreferences(req, res) {
  const { id: teacherId } = req.user;
  const { push, sms, preferSMS, quietHoursStart, quietHoursEnd } = req.body;

  await db.users.updateOne(
    { id: teacherId },
    {
      $set: {
        notificationPreferences: {
          push: push !== false,
          sms: sms === true,
          preferSMS: preferSMS === true,
          quietHoursStart,
          quietHoursEnd,
          updatedAt: new Date().toISOString()
        }
      }
    }
  );

  return res.json({ success: true });
}
```

---

## Monitoring & Metrics

```javascript
async function getNotificationDeliveryMetrics(tenantId, dateRange) {
  const stats = await db.notifications.aggregate([
    {
      $match: {
        type: { $in: ['submission_rejected', 'submission_duplicate'] },
        createdAt: { $gte: dateRange.start, $lte: dateRange.end }
      }
    },
    {
      $group: {
        _id: {
          status: '$status',
          channel: { $arrayElemAt: ['$channels', 0] }
        },
        count: { $sum: 1 }
      }
    }
  ]).toArray();

  const retryStats = await db.notifications.aggregate([
    {
      $match: {
        type: { $in: ['submission_rejected', 'submission_duplicate'] },
        createdAt: { $gte: dateRange.start, $lte: dateRange.end }
      }
    },
    {
      $group: {
        _id: '$retryCount',
        count: { $sum: 1 }
      }
    }
  ]).toArray();

  return {
    deliveryStats: stats,
    retryDistribution: retryStats,
    deliveryRate: calculateDeliveryRate(stats),
    avgRetries: calculateAvgRetries(retryStats)
  };
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Push Service (FCM/APNs) | External | Push notifications |
| SMS Gateway | External | SMS notifications |
| Notification DB | Internal | Notification storage |

---

## Related Stories

- [V-REJ-01](./V-REJ-01.md) - Reject with reason
- [V-REJ-02](./V-REJ-02.md) - Mark as duplicate
- [T-STAT-02](../teacher/T-STAT-02.md) - Teacher notification preferences
- [S-NOT-01](../system/S-NOT-01.md) - Notification service
