# T-SUB-05: Optional Notes

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** add optional notes about the sighting,
**So that** I can provide additional context to help MC Officers.

---

## Description

Teachers can add free-text notes to provide additional context not covered by condition tags. This could include specific location descriptions ("near the blue water tank"), behavioral observations ("dog seems friendly but scared"), or time-based patterns ("seen here every morning").

---

## Acceptance Criteria

### Functional

- [ ] Notes field appears after condition tag selection
- [ ] Field is optional - submission allowed without notes
- [ ] Maximum 200 characters allowed
- [ ] Character counter shows remaining characters
- [ ] Notes support basic text only (no emojis, no formatting)
- [ ] Notes visible to Verifier and MC Officer
- [ ] Notes NOT displayed in public permalink
- [ ] Profanity filter applied before submission
- [ ] Notes stored with submission metadata

### Content Guidelines

Display hint text with examples:
- "Near the school gate"
- "Dog appears friendly"
- "Seen here daily around 8 AM"
- "Specific landmark details"

---

## UI/UX Requirements (PWA)

### Notes Input Screen

```
┌─────────────────────────────────┐
│  Step 4 of 4                    │
├─────────────────────────────────┤
│                                 │
│  📝 Add notes (optional)        │
│                                 │
│  ┌───────────────────────────┐  │
│  │                           │  │
│  │ Any additional details    │  │
│  │ about the dog or location │  │
│  │                           │  │
│  │                           │  │
│  └───────────────────────────┘  │
│                     0/200       │
│                                 │
│  💡 Examples:                   │
│  • "Near the blue gate"         │
│  • "Dog limping on left leg"    │
│  • "Part of a larger pack"      │
│                                 │
│  [    Skip    ]  [  Submit  ]   │
│                                 │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Text input with auto-resize up to 4 lines
- [ ] Show soft keyboard when field focused
- [ ] Character limit enforced in real-time
- [ ] Disable submit if over character limit
- [ ] Store notes with offline submission queue
- [ ] Sanitize input before display (XSS prevention)

---

## Technical Notes

### Input Sanitization

```javascript
function sanitizeNotes(text) {
  // Remove HTML tags
  let sanitized = text.replace(/<[^>]*>/g, '');
  // Remove special characters except basic punctuation
  sanitized = sanitized.replace(/[^\w\s.,!?-]/g, '');
  // Trim and limit length
  return sanitized.trim().substring(0, 200);
}
```

### Profanity Filter

```javascript
import Filter from 'bad-words';
const filter = new Filter();

function checkProfanity(text) {
  return {
    clean: filter.clean(text),
    hasProfanity: filter.isProfane(text)
  };
}
```

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| bad-words | Library | Profanity filtering |

---

## Related Stories

- [T-SUB-04](./T-SUB-04.md) - Dog condition tagging
- [T-SUB-06](./T-SUB-06.md) - Receive Application ID
