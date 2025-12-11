# T-SUB-06: Receive Application ID

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** receive a unique Application ID upon submission,
**So that** I can track my report and reference it if needed.

---

## Description

After successful submission, the system generates a unique Application ID that serves as a reference for the teacher to track their submission. This ID is displayed prominently on the confirmation screen and can be used to check status or share with others via the public permalink.

---

## Acceptance Criteria

### Functional

- [ ] Unique Application ID generated server-side upon successful upload
- [ ] ID displayed on confirmation screen immediately after submission
- [ ] ID format: `SDCRS-YYYYMMDD-XXXXX` (e.g., SDCRS-20261207-A1B2C)
- [ ] ID is copyable to clipboard with one tap
- [ ] ID included in SMS confirmation sent to teacher
- [ ] ID visible in submission history (My Applications)
- [ ] ID used in shareable public permalink
- [ ] ID searchable in admin dashboards

### ID Format Specification

| Component | Description | Example |
|-----------|-------------|---------|
| Prefix | System identifier | SDCRS |
| Date | Submission date (YYYYMMDD) | 20261207 |
| Unique | 5-char alphanumeric | A1B2C |

### Confirmation Information

Display on confirmation screen:
- Application ID (prominent)
- Submission timestamp
- Location summary (district/block)
- Selected condition tags
- Next steps / expected timeline
- Share button for public permalink

---

## UI/UX Requirements (PWA)

### Submission Confirmation Screen

```
┌─────────────────────────────────┐
│                                 │
│         ✓                       │
│   Submitted Successfully!       │
│                                 │
├─────────────────────────────────┤
│                                 │
│  Application ID                 │
│  ┌───────────────────────────┐  │
│  │  SDCRS-20261207-A1B2C     │  │
│  │                    [📋]   │  │
│  └───────────────────────────┘  │
│                                 │
│  📅 Dec 7, 2026, 2:45 PM        │
│  📍 Central Block, Djibouti     │
│  🏷️ Injured                     │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  What happens next?             │
│  1. Our team will verify your   │
│     submission within 24 hours  │
│  2. You'll receive an SMS when  │
│     status changes              │
│  3. Payout after MC captures    │
│     the dog                     │
│                                 │
│  [  Share Status Link  ]        │
│                                 │
│  [  Submit Another  ]           │
│  [  View My Applications  ]     │
│                                 │
└─────────────────────────────────┘
```

### Copy Confirmation Toast

```
┌─────────────────────────────────┐
│  ✓ Application ID copied        │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Use Clipboard API for copy functionality
- [ ] Show success animation on submission
- [ ] Confetti or subtle celebration animation (optional)
- [ ] Auto-scroll to confirmation after submission
- [ ] Store Application ID locally for offline access
- [ ] Deep link to application details screen

---

## Technical Notes

### ID Generation (Server-side)

```javascript
function generateApplicationId() {
  const prefix = 'SDCRS';
  const date = new Date().toISOString().slice(0, 10).replace(/-/g, '');
  const unique = generateAlphanumeric(5);
  return `${prefix}-${date}-${unique}`;
}

function generateAlphanumeric(length) {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'; // Avoid confusing chars (0,O,1,I)
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}
```

### Clipboard API

```javascript
async function copyToClipboard(text) {
  try {
    await navigator.clipboard.writeText(text);
    showToast('Application ID copied');
  } catch (err) {
    // Fallback for older browsers
    const textarea = document.createElement('textarea');
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
    showToast('Application ID copied');
  }
}
```

### SMS Notification

```
Your SDCRS application has been submitted.

Application ID: SDCRS-20261207-A1B2C
Status: Pending Verification

Track status: https://sdcrs.gov.dj/status/SDCRS-20261207-A1B2C
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Clipboard API | Browser | Copy to clipboard |
| SMS Gateway | External | Send confirmation SMS |
| Short URL Service | Optional | Generate short tracking URLs |

---

## Related Stories

- [T-SUB-05](./T-SUB-05.md) - Optional notes
- [T-STAT-01](./T-STAT-01.md) - View submission status
- [T-STAT-04](./T-STAT-04.md) - Shareable public link
