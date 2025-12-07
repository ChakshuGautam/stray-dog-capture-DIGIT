# T-PAY-01: Payout Notification

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** be notified when my payout is processed,
**So that** I know to expect funds in my account.

---

## Description

After a stray dog is successfully captured by the MC Officer, the teacher becomes eligible for payout. The system initiates payment processing and notifies the teacher at two key points: when payout is initiated (processing) and when payout is complete (credited). Both notifications include the Application ID, amount, and reference number.

---

## Acceptance Criteria

### Functional

- [ ] Payout triggered ONLY after MC confirms successful capture
- [ ] SMS sent when payout processing begins
- [ ] SMS sent when payout credited to account
- [ ] In-app notification with payout details
- [ ] Payout amount displayed in app (fixed amount per capture)
- [ ] Bank reference/transaction ID shown after completion
- [ ] Payout history viewable in app
- [ ] Failed payout triggers alert to admin (not teacher)
- [ ] Teacher cannot request early payout

### Payout Trigger Conditions

| Condition | Payout Triggered | Notes |
|-----------|------------------|-------|
| Dog captured successfully | ✓ Yes | Standard payout |
| Dog unable to locate | ✗ No | No payout |
| Submission rejected | ✗ No | No payout |
| Duplicate submission | ✗ No | No payout |
| MC marks "Already Captured" | ✗ No | No payout |

### Payout Timeline

| Step | Timing | Notification |
|------|--------|--------------|
| MC confirms capture | T+0 | - |
| Payout initiated | T+1 day | SMS + In-app |
| Payout credited | T+2-3 days | SMS + In-app |

---

## UI/UX Requirements (PWA)

### In-App Payout Notification

```
┌─────────────────────────────────┐
│  🔔 Notifications               │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐  │
│  │ 💰 Payout Complete        │  │
│  │                           │  │
│  │ Your payout of 500 DJF    │  │
│  │ for SDCRS-20241207-A1B2C  │  │
│  │ has been credited.        │  │
│  │                           │  │
│  │ Ref: TXN-2024120812345    │  │
│  │                           │  │
│  │ 🕐 Just now           →   │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 🔄 Payout Processing      │  │
│  │                           │  │
│  │ Payment of 500 DJF for    │  │
│  │ SDCRS-20241207-A1B2C is   │  │
│  │ being processed.          │  │
│  │                           │  │
│  │ 🕐 Yesterday          →   │  │
│  └───────────────────────────┘  │
│                                 │
└─────────────────────────────────┘
```

### Application Detail - Payout Section

```
┌─────────────────────────────────┐
│  ← Application Details          │
├─────────────────────────────────┤
│                                 │
│  SDCRS-20241207-A1B2C           │
│                                 │
│  Status: 🟢 Captured            │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  💰 Payout                      │
│  ┌───────────────────────────┐  │
│  │                           │  │
│  │  Amount: 500 DJF          │  │
│  │  Status: ✓ Credited       │  │
│  │  Date: Dec 10, 2024       │  │
│  │  Ref: TXN-2024120812345   │  │
│  │                           │  │
│  │  To: Bank Account ****456 │  │
│  │                           │  │
│  └───────────────────────────┘  │
│                                 │
└─────────────────────────────────┘
```

### Payout Processing State

```
┌─────────────────────────────────┐
│  💰 Payout                      │
│  ┌───────────────────────────┐  │
│  │                           │  │
│  │  Amount: 500 DJF          │  │
│  │  Status: 🔄 Processing    │  │
│  │  Initiated: Dec 9, 2024   │  │
│  │                           │  │
│  │  Expected: 2-3 business   │  │
│  │            days           │  │
│  │                           │  │
│  │  To: Bank Account ****456 │  │
│  │                           │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Show payout status in application detail view
- [ ] Notification badge on app icon for new payouts
- [ ] Cache payout status for offline viewing
- [ ] Deep link from SMS to payout details
- [ ] Show masked bank account number for verification
- [ ] Display transaction reference prominently

---

## Technical Notes

### Payout Data Model

```typescript
interface Payout {
  payoutId: string;
  applicationId: string;
  teacherId: string;
  amount: number;
  currency: 'DJF';
  status: 'pending' | 'processing' | 'completed' | 'failed';
  bankAccount: {
    accountNumber: string; // Masked: ****456
    ifscCode: string;
    bankName: string;
    accountHolderName: string;
  };
  initiatedAt?: string;
  completedAt?: string;
  transactionId?: string;
  failureReason?: string;
}
```

### Payout Status API

```javascript
// Fetch payout details for an application
async function getPayoutStatus(applicationId) {
  const token = await getAuthToken();

  const response = await fetch(`/api/payouts/application/${applicationId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  if (response.status === 404) {
    return null; // No payout yet
  }

  return response.json();
}
```

### Payout Notification Trigger (Backend)

```javascript
// Called when MC confirms capture
async function onDogCaptured(application) {
  // Update application status
  await updateApplicationStatus(application.id, 'captured');

  // Initiate payout
  const payout = await createPayout({
    applicationId: application.id,
    teacherId: application.teacherId,
    amount: PAYOUT_AMOUNT, // Fixed amount from config
    bankAccount: await getTeacherBankAccount(application.teacherId)
  });

  // Queue for processing
  await queuePayoutForProcessing(payout.payoutId);

  // Notify teacher
  await sendPayoutInitiatedNotification(payout);
}

async function sendPayoutInitiatedNotification(payout) {
  const teacher = await getTeacher(payout.teacherId);

  // SMS notification
  await smsService.send(teacher.phone, SMS_TEMPLATES.payout_initiated, {
    ID: payout.applicationId,
    AMOUNT: payout.amount,
    URL: getTrackingUrl(payout.applicationId)
  });

  // In-app notification
  await createInAppNotification({
    userId: teacher.id,
    type: 'payout_initiated',
    title: 'Payout Processing',
    body: `Payment of ${payout.amount} DJF for ${payout.applicationId} is being processed.`,
    data: { applicationId: payout.applicationId, payoutId: payout.payoutId }
  });
}
```

### SMS Templates for Payout

```javascript
const PAYOUT_SMS_TEMPLATES = {
  initiated: {
    en: 'SDCRS: Payment of {AMOUNT} DJF for report {ID} is being processed. Track: {URL}',
    fr: 'SDCRS: Le paiement de {AMOUNT} DJF pour le rapport {ID} est en cours. Suivre: {URL}'
  },
  completed: {
    en: 'SDCRS: Payment of {AMOUNT} DJF for report {ID} has been credited to your account. Ref: {TXN_ID}',
    fr: 'SDCRS: Le paiement de {AMOUNT} DJF pour le rapport {ID} a été crédité. Réf: {TXN_ID}'
  },
  failed: {
    en: 'SDCRS: Payment for report {ID} could not be processed. Please contact support.',
    fr: 'SDCRS: Le paiement pour le rapport {ID} n\'a pas pu être traité. Contactez le support.'
  }
};
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Payment Gateway | External | Process bank transfers |
| SMS Gateway | External | Send notifications |
| Notification Service | Internal | In-app notifications |

---

## Related Stories

- [T-PAY-02](./T-PAY-02.md) - View payout history
- [T-PAY-03](./T-PAY-03.md) - Cumulative earnings
- [MC-CAP-02](../mc-officer/MC-CAP-02.md) - MC confirms capture
- [T-AUTH-03](./T-AUTH-03.md) - Bank account verification
