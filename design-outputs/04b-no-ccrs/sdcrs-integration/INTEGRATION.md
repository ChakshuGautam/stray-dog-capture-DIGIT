# SDCRS + UPI Payout Adapter Integration

## Overview

This document explains how the SDCRS service integrates with the UPI Payout Adapter for automated teacher payouts.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                               SDCRS Service                                       │
│                                                                                   │
│  ┌──────────────┐    ┌────────────────────┐    ┌────────────────────────────┐  │
│  │ DogReport    │───▶│ PayoutTriggerService│───▶│ Kafka: upi-payout-create  │──┼──┐
│  │ Service      │    │                    │    └────────────────────────────┘  │  │
│  │ (CAPTURED)   │    └────────────────────┘                                    │  │
│  └──────────────┘                                                              │  │
│                                                                                 │  │
│  ┌──────────────┐    ┌────────────────────┐    ┌────────────────────────────┐  │  │
│  │ Workflow     │◀───│PayoutCallbackConsumer│◀───│Kafka: sdcrs-payout-callback│◀─┼──┤
│  │ Service      │    │                    │    └────────────────────────────┘  │  │
│  │ (RESOLVED)   │    └────────────────────┘                                    │  │
│  └──────────────┘                                                              │  │
└─────────────────────────────────────────────────────────────────────────────────┘  │
                                                                                      │
                                              ┌───────────────────────────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           UPI Payout Adapter                                      │
│                                                                                   │
│  ┌────────────────────┐    ┌──────────────┐    ┌───────────────────────────┐   │
│  │ PayoutConsumer     │───▶│ PayoutService│───▶│ RazorpayPayoutClient      │   │
│  │ (upi-payout-create)│    │              │    │ - createContact()         │   │
│  └────────────────────┘    │              │    │ - createFundAccount()     │   │
│                            │              │    │ - createPayout()          │   │
│                            └──────────────┘    └───────────────────────────┘   │
│                                                           │                     │
│                                                           ▼                     │
│  ┌────────────────────┐    ┌──────────────┐    ┌───────────────────────────┐   │
│  │ WebhookController  │◀───│ Razorpay X   │◀───│ Razorpay Webhook          │   │
│  │ (/webhook/v1/razorpay)  │ (Payouts)    │    │ (payout.processed/failed) │   │
│  └────────────────────┘    └──────────────┘    └───────────────────────────┘   │
│           │                                                                     │
│           ▼                                                                     │
│  ┌────────────────────────────────────────────────────────────────────────┐    │
│  │ Kafka: sdcrs-payout-callback                                           │    │
│  └────────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Workflow States (with UPI)

```
                    ┌─────────────────┐
                    │    SUBMITTED    │
                    └────────┬────────┘
                             │ AUTO_VALIDATE
                    ┌────────▼────────┐
                    │PENDING_VALIDATION│
                    └────────┬────────┘
              ┌──────────────┼──────────────┐
    AUTO_FAIL │              │              │ AUTO_PASS
              ▼              │              ▼
     ┌─────────────┐         │     ┌────────────────────┐
     │AUTO_REJECTED│         │     │PENDING_VERIFICATION│
     └─────────────┘         │     └────────┬───────────┘
                             │   ┌──────────┼──────────┐
                             │   │REJECT    │VERIFY    │DUPLICATE
                             │   ▼          ▼          ▼
                             │ ┌────────┐ ┌────────┐ ┌─────────┐
                             │ │REJECTED│ │VERIFIED│ │DUPLICATE│
                             │ └────────┘ └───┬────┘ └─────────┘
                             │                │ ASSIGN
                             │       ┌────────▼────────┐
                             │       │    ASSIGNED     │
                             │       └────────┬────────┘
                             │                │ START_FIELD_VISIT
                             │       ┌────────▼────────┐
                             │       │   IN_PROGRESS   │
                             │       └────────┬────────┘
                             │     ┌──────────┴──────────┐
                             │     │MARK_CAPTURED        │MARK_UNABLE_TO_LOCATE
                             │     ▼                     ▼
                             │ ┌────────┐          ┌────────────────┐
                             │ │CAPTURED│          │UNABLE_TO_LOCATE│
                             │ └───┬────┘          └────────────────┘
                             │     │ INITIATE_PAYOUT (SYSTEM)
                             │     ▼
                             │ ┌──────────────┐
                             │ │PAYOUT_PENDING│◀────────────────┐
                             │ └──────┬───────┘                 │
                             │   ┌────┴─────┐                   │
                             │   │SUCCESS   │FAILED             │RETRY_PAYOUT
                             │   ▼          ▼                   │
                             │ ┌────────┐ ┌─────────────┐       │
                             │ │RESOLVED│ │PAYOUT_FAILED├───────┘
                             │ └───┬────┘ └──────┬──────┘
                             │     │RATE         │MANUAL_RESOLVE
                             │     ▼             │
                             │ ┌────────┐        │
                             │ │ CLOSED │◀───────┘
                             │ └────────┘
                             │
    Terminal states (no payout): AUTO_REJECTED, REJECTED, DUPLICATE, UNABLE_TO_LOCATE
    Terminal states (with payout): RESOLVED, CLOSED
```

## Integration Steps

### Step 1: SDCRS Triggers Payout (on CAPTURED)

When MC Officer marks report as CAPTURED via workflow:

```java
// In DogReportService.java - after workflow transition
if ("CAPTURED".equals(newState)) {
    payoutTriggerService.triggerPayoutForCapturedReport(dogReport, requestInfo);
}
```

### Step 2: Workflow Transitions to PAYOUT_PENDING

The payout trigger also needs to transition the workflow:

```java
// Auto-transition by system after pushing to Kafka
workflowService.transitionWorkflow(
    dogReport.getTenantId(),
    dogReport.getId(),
    "SDCRS",
    "INITIATE_PAYOUT",
    systemRequestInfo,
    "Payout initiated"
);
```

### Step 3: UPI Adapter Processes Payment

1. Consumes from `upi-payout-create` topic
2. Creates Contact in Razorpay (if new beneficiary)
3. Creates Fund Account (VPA)
4. Creates Payout

### Step 4: Razorpay Webhook Callback

Razorpay sends webhook to `/webhook/v1/razorpay` with events:
- `payout.processed` → Payment successful
- `payout.failed` → Payment failed
- `payout.reversed` → Payment reversed (rare)

### Step 5: UPI Adapter Pushes to SDCRS Callback Topic

After webhook processing:

```java
// In PayoutService.java
PayoutCallback callback = PayoutCallback.builder()
    .payoutId(payout.getId())
    .referenceId(payout.getReferenceId())
    .status(mapStatus(webhookPayload.getStatus()))
    .utr(webhookPayload.getUtr())
    .build();

kafkaTemplate.send(config.getSdcrsCallbackTopic(), callback);
```

### Step 6: SDCRS Handles Callback

`PayoutCallbackConsumer` receives message and transitions workflow:
- `COMPLETED` → Transition to RESOLVED
- `FAILED` → Transition to PAYOUT_FAILED
- `REVERSED` → Transition to PAYOUT_FAILED (for manual review)

## Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `upi-payout-create` | SDCRS | UPI Adapter | Trigger new payout |
| `upi-payout-status` | Webhook | UPI Adapter | Internal status processing |
| `upi-payout-persist` | UPI Adapter | Persister | Database persistence |
| `sdcrs-payout-callback` | UPI Adapter | SDCRS | Notify payout result |

## Configuration Required

### SDCRS application.properties

```properties
# UPI Payout Integration
sdcrs.kafka.payout.create.topic=upi-payout-create
sdcrs.kafka.payout.callback.topic=sdcrs-payout-callback

# Payout Configuration
sdcrs.payout.amount=500
sdcrs.payout.currency=INR
```

### UPI Adapter application.properties

```properties
# Kafka Topics
upi.payout.kafka.topics.create=upi-payout-create
upi.payout.kafka.topics.callback=sdcrs-payout-callback
```

## Error Handling

### Payout Failures

1. **First failure**: UPI Adapter retries automatically (configurable retries)
2. **Max retries exceeded**: Pushes FAILED callback to SDCRS
3. **SDCRS receives FAILED**: Workflow transitions to PAYOUT_FAILED
4. **Admin action**: Can trigger `RETRY_PAYOUT` or `MANUAL_RESOLVE`

### Duplicate Prevention

- UPI Adapter uses idempotency keys based on `referenceId` (Dog Report ID)
- If same report triggers payout twice, second request is ignored
- Response from first payout is returned

## Files to Implement

### In SDCRS Service

| File | Purpose |
|------|---------|
| `PayoutTriggerService.java` | Triggers payout on CAPTURED state |
| `PayoutCallbackConsumer.java` | Handles callbacks from UPI Adapter |
| `PayoutCallback.java` | Callback DTO model |

### In UPI Payout Adapter

| File | Purpose |
|------|---------|
| `PayoutConsumer.java` | Consumes payout create requests |
| `PayoutProducer.java` | Pushes to callback topic |
| `WebhookController.java` | Handles Razorpay webhooks |

## Testing the Integration

1. Submit a dog report as teacher
2. Auto-validate passes → PENDING_VERIFICATION
3. Verifier approves → VERIFIED
4. Supervisor assigns → ASSIGNED
5. MC starts field visit → IN_PROGRESS
6. MC marks captured → CAPTURED
7. System auto-transitions → PAYOUT_PENDING
8. UPI Adapter creates payout in Razorpay
9. Razorpay processes (sandbox: immediate)
10. Webhook received → PAYOUT_SUCCESS
11. Callback to SDCRS → RESOLVED
12. Teacher receives ₹500 via UPI
