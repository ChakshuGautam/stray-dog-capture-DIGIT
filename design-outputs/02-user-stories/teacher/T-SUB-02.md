# T-SUB-02: Capture Selfie at Location

> **Master Document:** [02-user-stories.md](../../design-outputs/02-user-stories.md)

---

## User Story

**As a** Teacher,
**I want to** capture a selfie at the same location as the dog photo,
**So that** my physical presence at the sighting location can be verified.

---

## Description

After capturing the dog photo, the teacher must take a selfie at the same location. This serves as proof that the teacher was physically present at the reported location and helps prevent fraudulent submissions where someone submits photos taken by others or from the internet.

---

## Acceptance Criteria

### Functional

- [ ] Selfie capture screen appears automatically after dog photo is accepted
- [ ] Camera opens in front-facing mode
- [ ] Clear instruction displayed: "Take a selfie at this location"
- [ ] GPS captured at selfie capture time (independent of dog photo GPS)
- [ ] Timestamp embedded in selfie EXIF data
- [ ] Face detection to ensure a face is visible in frame
- [ ] Selfie preview shown with Retake/Use options
- [ ] Selfie compressed to <1MB while maintaining face clarity
- [ ] System validates selfie GPS is within 500m of dog photo GPS
- [ ] If GPS mismatch >500m, show warning and allow proceed with flag
- [ ] Selfie stored securely with access restrictions

### Validation Rules

| Rule | Threshold | Action |
|------|-----------|--------|
| GPS present | Required | Block if missing |
| GPS within range of dog photo | ≤500m | Warn if exceeded, flag for review |
| Timestamp within range | ≤30 minutes of dog photo | Warn if exceeded |
| Face detected | Required | Block if no face |
| Photo quality | Min 480x480 | Block if below |

---

## UI/UX Requirements (PWA)

### Selfie Capture Screen

```
┌─────────────────────────────────┐
│  Step 2 of 4                    │
│                                 │
│  📸 Take a selfie here          │
│                                 │
│  ┌─────────────────────────┐    │
│  │                         │    │
│  │   Front Camera Preview  │    │
│  │                         │    │
│  │      ┌─────────┐        │    │
│  │      │  Face   │        │    │
│  │      │  Guide  │        │    │
│  │      └─────────┘        │    │
│  │                         │    │
│  └─────────────────────────┘    │
│                                 │
│  📍 Location: Matching ✓        │
│                                 │
│         [◉]                     │
│       Capture                   │
│                                 │
│  ⓘ Your selfie verifies your   │
│    presence at the location     │
└─────────────────────────────────┘
```

### GPS Mismatch Warning

```
┌─────────────────────────────────┐
│  ⚠️ Location Mismatch           │
├─────────────────────────────────┤
│                                 │
│  Your selfie location is 650m   │
│  away from the dog photo.       │
│                                 │
│  This may cause your submission │
│  to be flagged for review.      │
│                                 │
│  Options:                       │
│  • Retake selfie at the exact   │
│    location of the dog photo    │
│  • Continue anyway (may be      │
│    rejected during verification)│
│                                 │
│  [  Retake Selfie  ]            │
│  [  Continue Anyway  ]          │
│                                 │
└─────────────────────────────────┘
```

### PWA Considerations

- [ ] Use front-facing camera by default (`facingMode: 'user'`)
- [ ] Implement basic face detection using Shape Detection API or ML library
- [ ] Show face guide overlay to help framing
- [ ] Calculate distance between dog photo GPS and selfie GPS client-side
- [ ] Display real-time GPS accuracy indicator
- [ ] Ensure selfie is not stored in device gallery (privacy)
- [ ] Encrypt selfie before upload

---

## Technical Notes

### Face Detection

```javascript
// Using Shape Detection API (Chrome)
async function detectFace(imageBlob) {
  const faceDetector = new FaceDetector();
  const image = await createImageBitmap(imageBlob);
  const faces = await faceDetector.detect(image);
  return faces.length > 0;
}

// Fallback: Use face-api.js or TensorFlow.js
```

### GPS Distance Calculation

```javascript
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371e3; // Earth radius in meters
  const φ1 = lat1 * Math.PI / 180;
  const φ2 = lat2 * Math.PI / 180;
  const Δφ = (lat2 - lat1) * Math.PI / 180;
  const Δλ = (lon2 - lon1) * Math.PI / 180;

  const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ/2) * Math.sin(Δλ/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

  return R * c; // Distance in meters
}
```

### Privacy Considerations

- Selfie images stored in encrypted bucket separate from dog photos
- Access restricted to verification team only
- Selfies auto-deleted 90 days after verification
- Selfies never exposed in public permalinks or MC Officer dashboard
- Audit log for all selfie access

---

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Camera API | Browser | Front camera access |
| Face Detection API | Browser/Library | Detect face in frame |
| Geolocation API | Browser | GPS capture |
| Haversine Formula | Algorithm | GPS distance calculation |

---

## Related Stories

- [T-SUB-01](./T-SUB-01.md) - Capture dog photo
- [T-SUB-03](./T-SUB-03.md) - GPS auto-extraction
- [S-VAL-06](../system/S-VAL-06.md) - GPS mismatch detection
