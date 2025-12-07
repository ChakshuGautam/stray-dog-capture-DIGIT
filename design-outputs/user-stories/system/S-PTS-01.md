# S-PTS-01: Initiate Payout

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## System Behavior

**The system** initiates payout to the teacher after MC confirms successful capture
**To ensure** prompt reward for verified dog reports.

---

## Description

When an MC Officer marks a case as "Captured Successfully", the system automatically initiates the payout process to the teacher who submitted the original report. The payout is a fixed amount (configurable per tenant) and is transferred to the teacher's verified bank account.

---

## Acceptance Criteria

### Payout Trigger Rules

- [ ] Payout triggered ONLY when MC marks status as "Captured"
- [ ] Teacher must have verified bank account on file
- [ ] Payout amount fixed per tenant (e.g., 500 DJF)
- [ ] No payout for: rejected, unable to locate, duplicate, already captured
- [ ] Single payout per application (no duplicates)
- [ ] Payout initiated within 1 hour of capture confirmation
- [ ] Teacher notified when payout initiated
- [ ] Teacher notified when payout complete

### Payout Status Flow

| Status | Description | Next Status |
|--------|-------------|-------------|
| eligible | MC confirmed capture | initiated |
| initiated | Sent to payment gateway | processing |
| processing | Gateway processing | completed/failed |
| completed | Funds transferred | (final) |
| failed | Transfer failed | retry/manual |

### Payout Blocking Conditions

| Condition | Action |
|-----------|--------|
| No bank account | Block, notify teacher |
| Unverified bank account | Block, notify teacher |
| Daily limit reached | Queue for next day |
| Duplicate payout attempt | Block, log |

---

## Technical Implementation

### Payout Service

```javascript
class PayoutService {
  constructor(db, paymentGateway, notificationService) {
    this.db = db;
    this.gateway = paymentGateway;
    this.notifications = notificationService;
  }

  async initiatePayout(application) {
    // Validate payout eligibility
    const validation = await this.validatePayoutEligibility(application);
    if (!validation.eligible) {
      await this.handleIneligible(application, validation);
      return { success: false, reason: validation.reason };
    }

    // Get teacher's bank account
    const bankAccount = await this.getTeacherBankAccount(application.teacherId);
    if (!bankAccount || !bankAccount.verified) {
      await this.handleNoBankAccount(application);
      return { success: false, reason: 'NO_VERIFIED_BANK_ACCOUNT' };
    }

    // Get payout amount
    const amount = await this.getPayoutAmount(application.tenantId);

    // Create payout record
    const payout = await this.createPayoutRecord(application, bankAccount, amount);

    // Submit to payment gateway
    const gatewayResult = await this.submitToGateway(payout);

    // Update payout status
    await this.updatePayoutStatus(payout.payoutId, gatewayResult);

    // Notify teacher
    await this.notifyPayoutInitiated(payout);

    return { success: true, payoutId: payout.payoutId };
  }

  async validatePayoutEligibility(application) {
    // Check application status
    if (application.captureStatus !== 'captured') {
      return { eligible: false, reason: 'NOT_CAPTURED' };
    }

    // Check for existing payout
    const existingPayout = await this.db.payouts.findOne({
      applicationId: application.applicationId
    });
    if (existingPayout) {
      return { eligible: false, reason: 'PAYOUT_EXISTS' };
    }

    // Check teacher eligibility
    const teacher = await this.db.users.findOne({ id: application.teacherId });
    if (!teacher || !teacher.isActive) {
      return { eligible: false, reason: 'TEACHER_INELIGIBLE' };
    }

    return { eligible: true };
  }

  async getPayoutAmount(tenantId) {
    // Get from MDMS/config
    const config = await this.db.tenantConfig.findOne({ tenantId });
    return config?.payoutAmount || 500; // Default 500 DJF
  }

  async createPayoutRecord(application, bankAccount, amount) {
    const payout = {
      payoutId: generateUUID(),
      applicationId: application.applicationId,
      teacherId: application.teacherId,
      tenantId: application.tenantId,
      amount: amount,
      currency: 'DJF',
      status: 'initiated',
      bankAccount: {
        accountNumber: maskAccountNumber(bankAccount.accountNumber),
        accountNumberFull: bankAccount.accountNumber, // Encrypted
        ifscCode: bankAccount.ifscCode,
        bankName: bankAccount.bankName,
        accountHolderName: bankAccount.accountHolderName
      },
      timestamps: {
        eligibleAt: application.capturedAt,
        initiatedAt: new Date().toISOString()
      },
      gatewayDetails: null,
      retryCount: 0
    };

    await this.db.payouts.insertOne(payout);

    // Update application with payout reference
    await this.db.submissions.updateOne(
      { applicationId: application.applicationId },
      { $set: { payoutId: payout.payoutId, payoutStatus: 'initiated' } }
    );

    return payout;
  }

  async submitToGateway(payout) {
    try {
      const response = await this.gateway.initiateTransfer({
        referenceId: payout.payoutId,
        amount: payout.amount,
        currency: payout.currency,
        beneficiary: {
          accountNumber: payout.bankAccount.accountNumberFull,
          ifscCode: payout.bankAccount.ifscCode,
          name: payout.bankAccount.accountHolderName
        },
        purpose: 'SDCRS_REWARD',
        remarks: `SDCRS reward for ${payout.applicationId}`
      });

      return {
        success: true,
        gatewayTransactionId: response.transactionId,
        status: response.status
      };

    } catch (error) {
      console.error('Gateway error:', error);
      return {
        success: false,
        error: error.message
      };
    }
  }

  async updatePayoutStatus(payoutId, gatewayResult) {
    const update = {
      status: gatewayResult.success ? 'processing' : 'failed',
      'gatewayDetails.transactionId': gatewayResult.gatewayTransactionId,
      'gatewayDetails.lastResponse': gatewayResult,
      'timestamps.lastUpdated': new Date().toISOString()
    };

    if (!gatewayResult.success) {
      update['gatewayDetails.error'] = gatewayResult.error;
    }

    await this.db.payouts.updateOne(
      { payoutId },
      { $set: update }
    );
  }

  async notifyPayoutInitiated(payout) {
    const teacher = await this.db.users.findOne({ id: payout.teacherId });

    // SMS notification
    await smsService.send(teacher.phone, SMS_TEMPLATES.payout_initiated, {
      ID: payout.applicationId,
      AMOUNT: payout.amount,
      URL: getTrackingUrl(payout.applicationId)
    });

    // In-app notification
    await this.notifications.create({
      userId: teacher.id,
      type: 'payout_initiated',
      title: 'Payment Processing',
      body: `Your payment of ${payout.amount} DJF is being processed`,
      data: {
        applicationId: payout.applicationId,
        payoutId: payout.payoutId,
        amount: payout.amount
      }
    });
  }

  async handleNoBankAccount(application) {
    const teacher = await this.db.users.findOne({ id: application.teacherId });

    await this.notifications.create({
      userId: teacher.id,
      type: 'bank_account_required',
      title: 'Bank Account Required',
      body: 'Please add your bank account to receive payment',
      data: { applicationId: application.applicationId }
    });
  }
}
```

### Webhook Handler for Gateway Callbacks

```javascript
async function handlePayoutWebhook(webhookData) {
  const { referenceId, status, transactionId, failureReason } = webhookData;

  const payout = await db.payouts.findOne({ payoutId: referenceId });
  if (!payout) {
    console.error('Unknown payout reference:', referenceId);
    return;
  }

  const update = {
    status: mapGatewayStatus(status),
    'gatewayDetails.transactionId': transactionId,
    'gatewayDetails.lastStatus': status,
    'timestamps.lastUpdated': new Date().toISOString()
  };

  if (status === 'SUCCESS') {
    update['timestamps.completedAt'] = new Date().toISOString();
    update['gatewayDetails.finalTransactionId'] = transactionId;
  } else if (status === 'FAILED') {
    update['gatewayDetails.failureReason'] = failureReason;
    update['retryCount'] = payout.retryCount + 1;
  }

  await db.payouts.updateOne({ payoutId: referenceId }, { $set: update });

  // Update application status
  await db.submissions.updateOne(
    { applicationId: payout.applicationId },
    { $set: { payoutStatus: update.status } }
  );

  // Send notifications
  if (status === 'SUCCESS') {
    await sendPayoutCompleteNotification(payout, transactionId);
  } else if (status === 'FAILED') {
    await handlePayoutFailure(payout, failureReason);
  }
}

function mapGatewayStatus(gatewayStatus) {
  const statusMap = {
    'INITIATED': 'processing',
    'PROCESSING': 'processing',
    'SUCCESS': 'completed',
    'FAILED': 'failed',
    'REVERSED': 'failed'
  };
  return statusMap[gatewayStatus] || 'processing';
}
```

### Payout Complete Notification

```javascript
async function sendPayoutCompleteNotification(payout, transactionId) {
  const teacher = await db.users.findOne({ id: payout.teacherId });

  // SMS
  await smsService.send(teacher.phone, SMS_TEMPLATES.payout_complete, {
    ID: payout.applicationId,
    AMOUNT: payout.amount,
    TXN_ID: transactionId
  });

  // In-app
  await notificationService.create({
    userId: teacher.id,
    type: 'payout_completed',
    title: 'Payment Complete',
    body: `${payout.amount} DJF has been credited to your account`,
    data: {
      applicationId: payout.applicationId,
      payoutId: payout.payoutId,
      amount: payout.amount,
      transactionId: transactionId
    }
  });

  // Add to timeline
  await addTimelineEvent(payout.applicationId, {
    status: 'payout_complete',
    timestamp: new Date().toISOString(),
    data: { amount: payout.amount, transactionId }
  });
}
```

---

## SMS Templates

```javascript
const SMS_TEMPLATES = {
  payout_initiated: {
    en: 'SDCRS: Payment of {AMOUNT} DJF for report {ID} is being processed. Track: {URL}',
    fr: 'SDCRS: Le paiement de {AMOUNT} DJF pour le rapport {ID} est en cours. Suivre: {URL}'
  },
  payout_complete: {
    en: 'SDCRS: Payment of {AMOUNT} DJF for report {ID} has been credited. Ref: {TXN_ID}',
    fr: 'SDCRS: Le paiement de {AMOUNT} DJF pour le rapport {ID} a été crédité. Réf: {TXN_ID}'
  }
};
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Payment Gateway | External | Process bank transfers |
| SMS Gateway | External | Notifications |
| MDMS | DIGIT | Payout configuration |

---

## Related Stories

- [MC-CAP-02](../mc-officer/MC-CAP-02.md) - Confirm capture
- [T-PAY-01](../teacher/T-PAY-01.md) - Payout notification
- [T-AUTH-03](../teacher/T-AUTH-03.md) - Bank account verification
