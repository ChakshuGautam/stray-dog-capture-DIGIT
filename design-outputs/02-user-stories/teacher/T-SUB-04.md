# T-SUB-04: Dog Condition Tagging

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** tag the dog's condition (normal, injured, aggressive, with puppies, collared),
**So that** MC Officers know what to expect during field visits.

---

## Description

Teachers should categorize the dog's condition using predefined tags. This information helps MC Officers prioritize incidents (injured dogs need faster response), prepare appropriate equipment, and anticipate the dog's behavior. Multiple tags can be selected for a single submission.

---

## Acceptance Criteria

### Functional

- [ ] Condition tagging screen appears after photos are captured
- [ ] At least one condition tag must be selected (mandatory)
- [ ] Multiple tags can be selected simultaneously
- [ ] Tags are clearly labeled with icons for quick recognition
- [ ] Selected tags highlighted visually
- [ ] "Normal" tag auto-deselected if severity tag chosen
- [ ] Confirmation of selected tags before proceeding
- [ ] Tags stored with submission and visible to Verifier and MC Officer

### Available Tags

| Tag | Icon | Description | Priority Boost |
|-----|------|-------------|----------------|
| Normal | 🐕 | Healthy, non-threatening | None |
| Injured | 🩹 | Visible injury or limping | +1 |
| Aggressive | ⚠️ | Showing aggressive behavior | +1 |
| With Puppies | 🐕‍🦺 | Mother dog with puppies nearby | +1 |
| Collared | 🔖 | Wearing collar (possible pet) | None |
| Sick | 🤒 | Appears ill (lethargy, discharge) | +1 |
| Pack | 🐕🐕 | Part of a group (3+ dogs) | +1 |

### Tag Combinations

| Combination | Allowed | Notes |
|-------------|---------|-------|
| Normal + Collared | ✓ | Pet that seems lost |
| Injured + Aggressive | ✓ | High priority |
| Normal + Injured | ✗ | Mutually exclusive |
| With Puppies + Pack | ✓ | Multiple priority indicators |

---

## UI/UX Requirements (PWA)

### Tag Selection Screen

```
┌─────────────────────────────────┐
│  Step 3 of 4                    │
├─────────────────────────────────┤
│                                 │
│  🐕 Describe the dog's          │
│     condition                   │
│                                 │
│  Select all that apply:         │
│                                 │
│  ┌─────────┐  ┌─────────┐       │
│  │   🐕    │  │   🩹    │       │
│  │ Normal  │  │ Injured │       │
│  │   [ ]   │  │   [✓]   │       │
│  └─────────┘  └─────────┘       │
│                                 │
│  ┌─────────┐  ┌─────────┐       │
│  │   ⚠️    │  │  🐕‍🦺   │       │
│  │Aggressive│ │ Puppies │       │
│  │   [ ]   │  │   [ ]   │       │
│  └─────────┘  └─────────┘       │
│                                 │
│  ┌─────────┐  ┌─────────┐       │
│  │   🔖    │  │   🤒    │       │
│  │Collared │  │  Sick   │       │
│  │   [ ]   │  │   [ ]   │       │
│  └─────────┘  └─────────┘       │
│                                 │
│  ┌─────────┐                    │
│  │  🐕🐕   │                    │
│  │  Pack   │                    │
│  │   [ ]   │                    │
│  └─────────┘                    │
│                                 │
│  [      Continue      ]         │
│                                 │
└─────────────────────────────────┘
```

### Tag Description Tooltip

```
┌─────────────────────────────────┐
│  ℹ️ Injured                     │
├─────────────────────────────────┤
│                                 │
│  Select if the dog:             │
│  • Is limping or has difficulty │
│    walking                      │
│  • Has visible wounds or blood  │
│  • Appears to be in pain        │
│                                 │
│  This helps prioritize urgent   │
│  cases for faster response.     │
│                                 │
│  [  Got it  ]                   │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Large touch targets for tag buttons (min 48x48px)
- [ ] Haptic feedback on selection (if supported)
- [ ] Visual highlight for selected tags (border + background)
- [ ] Smooth animation for selection/deselection
- [ ] Long-press on tag shows description tooltip
- [ ] Tags stored locally if submission queued offline
- [ ] Prevent accidental double-tap

---

## Technical Notes

### Tag Data Model

```typescript
interface ConditionTag {
  code: string;
  label: string;
  icon: string;
  priorityBoost: number;
  mutuallyExclusive?: string[];
}

const CONDITION_TAGS: ConditionTag[] = [
  { code: 'NORMAL', label: 'Normal', icon: '🐕', priorityBoost: 0, mutuallyExclusive: ['INJURED', 'SICK'] },
  { code: 'INJURED', label: 'Injured', icon: '🩹', priorityBoost: 1, mutuallyExclusive: ['NORMAL'] },
  { code: 'AGGRESSIVE', label: 'Aggressive', icon: '⚠️', priorityBoost: 1 },
  { code: 'WITH_PUPPIES', label: 'With Puppies', icon: '🐕‍🦺', priorityBoost: 1 },
  { code: 'COLLARED', label: 'Collared', icon: '🔖', priorityBoost: 0 },
  { code: 'SICK', label: 'Sick', icon: '🤒', priorityBoost: 1, mutuallyExclusive: ['NORMAL'] },
  { code: 'PACK', label: 'Pack', icon: '🐕🐕', priorityBoost: 1 }
];
```

### Submission Payload

```json
{
  "dogPhoto": "base64...",
  "selfiePhoto": "base64...",
  "location": {
    "latitude": 11.5886,
    "longitude": 43.1456
  },
  "conditionTags": ["INJURED", "AGGRESSIVE"],
  "notes": "Dog appears to have injured left leg",
  "timestamp": "2026-12-07T14:30:00Z"
}
```

### Priority Calculation

```javascript
function calculatePriority(tags) {
  const basePriority = 0;
  const boost = tags.reduce((sum, tag) => {
    const tagDef = CONDITION_TAGS.find(t => t.code === tag);
    return sum + (tagDef?.priorityBoost || 0);
  }, 0);
  return Math.min(basePriority + boost, 3); // Max priority 3
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| MDMS | DIGIT | Tag definitions (configurable) |
| LocalStorage | Browser | Temporary tag selection storage |

---

## Related Stories

- [T-SUB-01](./T-SUB-01.md) - Capture dog photo
- [T-SUB-05](./T-SUB-05.md) - Optional notes
- [T-SUB-06](./T-SUB-06.md) - Receive Application ID
- [MC-VIEW-04](../mc-officer/MC-VIEW-04.md) - MC Officer sees condition tags
