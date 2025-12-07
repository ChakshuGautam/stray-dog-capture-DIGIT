# T-STAT-03: View Rejection Reason

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** see why my submission was rejected,
**So that** I can submit better reports in the future.

---

## Description

When a submission is rejected by a Verifier, the teacher should see a clear explanation of why it was rejected. This helps teachers understand the quality standards expected and improves future submissions. The rejection reason is selected from predefined options by the Verifier, optionally with additional comments.

---

## Acceptance Criteria

### Functional

- [ ] Rejected status prominently displayed with red indicator
- [ ] Rejection reason shown immediately below status
- [ ] Verifier's additional comments shown if provided
- [ ] Rejection timestamp displayed
- [ ] Original submission details still visible (photos, location, tags)
- [ ] "Submit New Report" CTA to encourage retry
- [ ] Rejection reason included in SMS notification
- [ ] Rejected submissions remain in history for 30 days
- [ ] Teacher cannot appeal rejections (v1.0)

### Predefined Rejection Reasons

| Code | Reason | Description |
|------|--------|-------------|
| PHOTO_UNCLEAR | Photo Not Clear | Dog not clearly visible in photo |
| NOT_STRAY | Not a Stray Dog | Dog appears to be a pet (collar, well-groomed) |
| DUPLICATE | Duplicate Report | Same dog already reported recently |
| OUTSIDE_AREA | Outside Service Area | Location outside program boundaries |
| TIMESTAMP_OLD | Timestamp Too Old | Photo taken more than 24 hours ago |
| SELFIE_MISMATCH | Selfie Doesn't Match | Selfie location doesn't match dog photo |
| INAPPROPRIATE | Inappropriate Content | Photo contains inappropriate content |
| OTHER | Other | Custom reason provided in comments |

---

## UI/UX Requirements (PWA)

### Rejected Application View

```
┌─────────────────────────────────┐
│  ← Application Details          │
├─────────────────────────────────┤
│                                 │
│  SDCRS-20241205-P3Q4R      [📋] │
│                                 │
│  ┌───────────────────────────┐  │
│  │      [Dog Photo]          │  │
│  │                           │  │
│  └───────────────────────────┘  │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Status                         │
│  ┌───────────────────────────┐  │
│  │  🔴 REJECTED               │  │
│  │  Dec 5, 2024, 5:00 PM     │  │
│  └───────────────────────────┘  │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  Reason for Rejection           │
│  ┌───────────────────────────┐  │
│  │  📷 Photo Not Clear        │  │
│  │                           │  │
│  │  The photo doesn't show   │  │
│  │  the dog clearly enough   │  │
│  │  for identification.      │  │
│  └───────────────────────────┘  │
│                                 │
│  Verifier's Comment:            │
│  ┌───────────────────────────┐  │
│  │  "Please take the photo   │  │
│  │  from closer and ensure   │  │
│  │  the dog is in focus."    │  │
│  └───────────────────────────┘  │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  💡 Tips for better photos:     │
│  • Get within 3-5 meters        │
│  • Ensure good lighting         │
│  • Focus on the dog             │
│  • Avoid obstructions           │
│                                 │
│  [  Submit New Report  ]        │
│                                 │
└─────────────────────────────────┘
```

### Rejection Reasons Explanation

```
┌─────────────────────────────────┐
│  ℹ️ Photo Not Clear             │
├─────────────────────────────────┤
│                                 │
│  Your photo was rejected        │
│  because the dog was not        │
│  clearly visible.               │
│                                 │
│  Common reasons:                │
│  • Photo taken from too far     │
│  • Dog is blurry or out of      │
│    focus                        │
│  • Dog is partially hidden      │
│  • Poor lighting conditions     │
│                                 │
│  For your next submission:      │
│  ✓ Get closer (3-5 meters)      │
│  ✓ Make sure dog fills at       │
│    least 1/3 of the frame       │
│  ✓ Wait for dog to be still     │
│  ✓ Use good natural light       │
│                                 │
│  [  Got it  ]                   │
│                                 │
└─────────────────────────────────┘
```

### Rejection in List View

```
┌─────────────────────────────────┐
│  ← My Applications              │
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐  │
│  │ SDCRS-20241205-P3Q4R      │  │
│  │ 🔴 Rejected                │  │
│  │ Reason: Photo Not Clear    │  │
│  │ 📍 East Block              │  │
│  │ 🕐 Dec 5, 2024             │  │
│  │                      →    │  │
│  └───────────────────────────┘  │
│                                 │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Rejection reason translated to user's language
- [ ] Contextual tips based on rejection reason
- [ ] Pre-fill new submission with corrected guidance
- [ ] Track rejection patterns for quality improvement
- [ ] Cache rejection details for offline viewing
- [ ] Show rejection reason in notification tray

---

## Technical Notes

### Rejection Reason Data Model

```typescript
interface RejectionReason {
  code: string;
  label: string;
  description: string;
  tips: string[];
  severity: 'soft' | 'hard'; // soft = can resubmit, hard = blocked
}

const REJECTION_REASONS: RejectionReason[] = [
  {
    code: 'PHOTO_UNCLEAR',
    label: 'Photo Not Clear',
    description: 'The photo doesn\'t show the dog clearly enough for identification.',
    tips: [
      'Get within 3-5 meters of the dog',
      'Ensure good lighting',
      'Focus on the dog',
      'Avoid obstructions'
    ],
    severity: 'soft'
  },
  {
    code: 'NOT_STRAY',
    label: 'Not a Stray Dog',
    description: 'The dog appears to be a pet based on visible collar, grooming, or behavior.',
    tips: [
      'Only report dogs without collars',
      'Look for signs of neglect',
      'Pet dogs are not eligible'
    ],
    severity: 'soft'
  },
  {
    code: 'DUPLICATE',
    label: 'Duplicate Report',
    description: 'This dog has already been reported by you or another teacher.',
    tips: [
      'Check "My Applications" before submitting',
      'Report only new sightings'
    ],
    severity: 'soft'
  },
  // ... more reasons
];
```

### Rejected Application Response

```typescript
interface RejectedApplication {
  applicationId: string;
  status: 'rejected';
  rejectedAt: string;
  rejection: {
    reasonCode: string;
    reasonLabel: string;
    description: string;
    verifierComment?: string;
    verifierId: string;
  };
  originalSubmission: {
    dogPhotoUrl: string;
    selfieUrl: string;
    location: Location;
    conditionTags: string[];
    notes?: string;
    submittedAt: string;
  };
}
```

### Display Rejection Reason

```javascript
function getRejectionDetails(reasonCode) {
  const reason = REJECTION_REASONS.find(r => r.code === reasonCode);

  return {
    label: translateLabel(reason.label),
    description: translateDescription(reason.description),
    tips: reason.tips.map(tip => translateTip(tip)),
    canResubmit: reason.severity === 'soft'
  };
}

// React component
function RejectionReasonCard({ rejection }) {
  const details = getRejectionDetails(rejection.reasonCode);

  return (
    <div className="rejection-card">
      <div className="rejection-header">
        <span className="rejection-icon">🔴</span>
        <span className="rejection-label">{details.label}</span>
      </div>

      <p className="rejection-description">{details.description}</p>

      {rejection.verifierComment && (
        <div className="verifier-comment">
          <strong>Verifier's Comment:</strong>
          <p>"{rejection.verifierComment}"</p>
        </div>
      )}

      {details.canResubmit && (
        <div className="tips-section">
          <h4>Tips for your next submission:</h4>
          <ul>
            {details.tips.map((tip, i) => (
              <li key={i}>{tip}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
```

### Track Rejection Patterns

```javascript
// Analytics for quality improvement
async function trackRejection(application) {
  await analytics.track('submission_rejected', {
    applicationId: application.applicationId,
    teacherId: application.teacherId,
    reasonCode: application.rejection.reasonCode,
    block: application.originalSubmission.location.block,
    conditionTags: application.originalSubmission.conditionTags,
    submissionHour: new Date(application.originalSubmission.submittedAt).getHours()
  });
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| MDMS | DIGIT | Rejection reasons configuration |
| Localization | Service | Multi-language support |
| Analytics | Service | Track rejection patterns |

---

## Related Stories

- [T-STAT-01](./T-STAT-01.md) - View submission status
- [V-REJ-01](../verifier/V-REJ-01.md) - Verifier rejection workflow
- [T-SUB-01](./T-SUB-01.md) - Capture dog photo (quality guidelines)
