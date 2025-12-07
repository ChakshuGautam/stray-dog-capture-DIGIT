# T-STAT-02: SMS Notifications

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** receive SMS notifications when my submission status changes,
**So that** I stay informed without needing to constantly check the app.

---

## Description

Teachers receive SMS notifications at key status transitions. SMS is used instead of push notifications to ensure delivery even when the app is not installed or the device is offline. Each SMS includes the Application ID and a link to check full status.

---

## Acceptance Criteria

### Functional

- [ ] SMS sent to teacher's registered phone number
- [ ] SMS sent at key status transitions (see trigger table)
- [ ] SMS includes Application ID for reference
- [ ] SMS includes short tracking URL
- [ ] Teacher can opt-out of SMS notifications in settings
- [ ] SMS delivery status tracked for audit
- [ ] Failed SMS triggers retry (max 2 attempts)
- [ ] SMS content localized to French/Arabic based on preference

### SMS Trigger Points

| Event | SMS Sent | Priority |
|-------|----------|----------|
| Submission received | ✓ | High |
| Verification approved | ✓ | High |
| Verification rejected | ✓ | High |
| MC Officer assigned | ✗ | - |
| Dog captured | ✓ | High |
| Unable to locate | ✓ | Medium |
| Payout initiated | ✓ | High |
| Payout complete | ✓ | High |

### SMS Content Templates

| Event | Template |
|-------|----------|
| Submitted | "SDCRS: Your report {ID} has been submitted. Track: {URL}" |
| Approved | "SDCRS: Your report {ID} has been verified and assigned for capture. Track: {URL}" |
| Rejected | "SDCRS: Your report {ID} could not be verified. Reason: {REASON}. Track: {URL}" |
| Captured | "SDCRS: Good news! The dog from report {ID} has been captured. Payout processing. Track: {URL}" |
| Unable to Locate | "SDCRS: Report {ID}: Our team visited but couldn't locate the dog. No further action needed. Track: {URL}" |
| Payout Initiated | "SDCRS: Payment of {AMOUNT} DJ for report {ID} is being processed. Track: {URL}" |
| Payout Complete | "SDCRS: Payment of {AMOUNT} DJ for report {ID} has been credited. Ref: {TXN_ID}" |

---

## UI/UX Requirements (PWA)

### SMS Notification Settings

```
┌─────────────────────────────────┐
│  ← Notification Settings        │
├─────────────────────────────────┤
│                                 │
│  SMS Notifications              │
│                                 │
│  📱 Phone: +253 77 XX XX XX     │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Receive SMS for:               │
│                                 │
│  [✓] Submission confirmation    │
│  [✓] Verification updates       │
│  [✓] Capture updates            │
│  [✓] Payment notifications      │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Language: [French ▼]           │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  ℹ️ Standard SMS rates may apply │
│                                 │
│  [  Save Preferences  ]         │
│                                 │
└─────────────────────────────────┘
```

### Sample SMS Appearance

```
┌─────────────────────────────────┐
│  📱 SMS from SDCRS              │
├─────────────────────────────────┤
│                                 │
│  SDCRS: Your report             │
│  SDCRS-20241207-A1B2C has been  │
│  verified and assigned for      │
│  capture.                       │
│                                 │
│  Track: sdcrs.gov.dj/s/A1B2C    │
│                                 │
│  ─────────────────────────────  │
│  Dec 7, 2024, 3:15 PM           │
│                                 │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Settings page to manage SMS preferences
- [ ] Show SMS notification history in app
- [ ] Indicate if SMS delivery failed
- [ ] Allow resend of failed SMS from app
- [ ] Deep link from SMS URL opens app if installed

---

## Technical Notes

### SMS Notification Service

```javascript
// Backend service for SMS notifications
class SMSNotificationService {
  constructor(smsGateway, shortUrlService) {
    this.gateway = smsGateway;
    this.shortUrl = shortUrlService;
  }

  async sendStatusNotification(application, event) {
    const teacher = await getTeacher(application.teacherId);

    if (!teacher.smsPreferences[event]) {
      console.log('SMS disabled for event:', event);
      return;
    }

    const template = SMS_TEMPLATES[event][teacher.language];
    const trackingUrl = await this.shortUrl.create(
      `https://sdcrs.gov.dj/status/${application.applicationId}`
    );

    const message = this.formatMessage(template, {
      ID: application.applicationId,
      URL: trackingUrl,
      REASON: application.rejectionReason,
      AMOUNT: application.payoutAmount,
      TXN_ID: application.transactionId
    });

    return this.sendSMS(teacher.phone, message);
  }

  async sendSMS(phone, message) {
    try {
      const result = await this.gateway.send({
        to: phone,
        message: message,
        sender: 'SDCRS'
      });

      await this.logDelivery(phone, message, 'sent', result.messageId);
      return result;

    } catch (error) {
      await this.logDelivery(phone, message, 'failed', null, error);
      throw error;
    }
  }

  formatMessage(template, variables) {
    return template.replace(/\{(\w+)\}/g, (match, key) => {
      return variables[key] || match;
    });
  }
}
```

### SMS Templates Configuration

```javascript
const SMS_TEMPLATES = {
  submitted: {
    en: 'SDCRS: Your report {ID} has been submitted. Track: {URL}',
    fr: 'SDCRS: Votre rapport {ID} a été soumis. Suivre: {URL}',
    ar: 'SDCRS: تم تقديم تقريرك {ID}. تتبع: {URL}'
  },
  verified: {
    en: 'SDCRS: Your report {ID} has been verified and assigned for capture. Track: {URL}',
    fr: 'SDCRS: Votre rapport {ID} a été vérifié et assigné pour capture. Suivre: {URL}',
    ar: 'SDCRS: تم التحقق من تقريرك {ID} وتعيينه للقبض. تتبع: {URL}'
  },
  rejected: {
    en: 'SDCRS: Your report {ID} could not be verified. Reason: {REASON}. Track: {URL}',
    fr: 'SDCRS: Votre rapport {ID} n\'a pas pu être vérifié. Raison: {REASON}. Suivre: {URL}',
    ar: 'SDCRS: لم يتم التحقق من تقريرك {ID}. السبب: {REASON}. تتبع: {URL}'
  },
  captured: {
    en: 'SDCRS: Good news! The dog from report {ID} has been captured. Payout processing. Track: {URL}',
    fr: 'SDCRS: Bonne nouvelle! Le chien du rapport {ID} a été capturé. Paiement en cours. Suivre: {URL}',
    ar: 'SDCRS: أخبار جيدة! تم القبض على الكلب من التقرير {ID}. معالجة الدفع. تتبع: {URL}'
  },
  unable_to_locate: {
    en: 'SDCRS: Report {ID}: Our team visited but couldn\'t locate the dog. No further action needed. Track: {URL}',
    fr: 'SDCRS: Rapport {ID}: Notre équipe a visité mais n\'a pas pu localiser le chien. Aucune action requise. Suivre: {URL}',
    ar: 'SDCRS: التقرير {ID}: زار فريقنا لكن لم يتمكن من تحديد موقع الكلب. لا يلزم اتخاذ أي إجراء. تتبع: {URL}'
  },
  payout_initiated: {
    en: 'SDCRS: Payment of {AMOUNT} DJF for report {ID} is being processed. Track: {URL}',
    fr: 'SDCRS: Le paiement de {AMOUNT} DJF pour le rapport {ID} est en cours. Suivre: {URL}',
    ar: 'SDCRS: يتم معالجة دفع {AMOUNT} فرنك جيبوتي للتقرير {ID}. تتبع: {URL}'
  },
  payout_complete: {
    en: 'SDCRS: Payment of {AMOUNT} DJF for report {ID} has been credited. Ref: {TXN_ID}',
    fr: 'SDCRS: Le paiement de {AMOUNT} DJF pour le rapport {ID} a été crédité. Réf: {TXN_ID}',
    ar: 'SDCRS: تم إيداع دفع {AMOUNT} فرنك جيبوتي للتقرير {ID}. المرجع: {TXN_ID}'
  }
};
```

### SMS Preferences Schema

```javascript
const smsPreferencesSchema = {
  teacherId: 'string',
  phone: 'string',
  language: 'string', // 'en', 'fr', 'ar'
  preferences: {
    submitted: true,
    verified: true,
    rejected: true,
    captured: true,
    unable_to_locate: true,
    payout_initiated: true,
    payout_complete: true
  },
  lastUpdated: 'timestamp'
};
```

### Update SMS Preferences (Client)

```javascript
async function updateSMSPreferences(preferences) {
  const token = await getAuthToken();

  const response = await fetch('/api/user/sms-preferences', {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(preferences)
  });

  if (!response.ok) throw new Error('Failed to update preferences');

  showToast('Preferences saved');
  return response.json();
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| SMS Gateway | External | Deliver SMS messages |
| URL Shortener | Service | Create tracking URLs |
| User Service | DIGIT | Store preferences |

---

## Related Stories

- [T-AUTH-01](./T-AUTH-01.md) - OTP Login (phone verification)
- [T-STAT-01](./T-STAT-01.md) - View submission status
- [T-SUB-06](./T-SUB-06.md) - Receive Application ID
